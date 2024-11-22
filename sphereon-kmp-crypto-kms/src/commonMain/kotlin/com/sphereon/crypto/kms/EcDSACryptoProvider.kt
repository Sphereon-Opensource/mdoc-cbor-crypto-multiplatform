package com.sphereon.crypto.kms

import EcdsaRawKmpKeyInfoContext
import checkSupportedEcdsaCurve
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.generic.CoseKeyPair
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.JoseKeyPair
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.jose.generateJwkThumbprint
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
import com.sphereon.kmp.Encoding
import convertRawKeyBytesToJwk
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import keyInfoToEcdsaRawKmpContext
import resolveEcdsaKmpCurve
import kotlin.js.JsExport

/**
 * EcDSACryptoProvider provides Elliptic Curve Digital Signature Algorithm (ECDSA) cryptographic operations
 * using underlying cryptographic implementations.
 *
 * @param provider An instance of CryptographyProvider to use for cryptographic operations. Default is CryptographyProvider.Default.
 */
@JsExport
class EcDSACryptoProvider(
    private val id: String = "ecdsa",
    provider: CryptographyProvider = CryptographyProvider.Default,
    private val privateKeyStore: IKeyStoreService? = MemoryKeyStoreService(keyVisibility = KeyVisibility.PRIVATE),
    private val exposePrivateKeysDuringGeneration: Boolean = false
) : IKeyManagementSystem,
    IRawSignatureService, ISimpleSignatureService {
    /**
     * Provides ECDSA (Elliptic Curve Digital Signature Algorithm) cryptographic functions.
     * This variable holds an instance of the provider which is used to perform various cryptographic operations such as
     * key generation, signature generation, and signature verification.
     */
    private val ecdsa = provider.get(ECDSA)

    /**
     * Returns an array of supported elliptic curves for cryptographic operations.
     *
     * @return An array of CurveMapping objects representing the supported elliptic curves.
     */
    override fun supportedCurves(): Array<Curve> = arrayOf(Curve.P_256, Curve.P_384, Curve.P_521)

    /**
     * Checks if the provided elliptic curve is supported by the EcDSACryptoProvider.
     *
     * @param curve The elliptic curve to be checked.
     * @return True if the curve is supported, false otherwise.
     */
    override fun isSupportedCurve(curve: Curve): Boolean = supportedCurves().contains(curve)

    /**
     * Returns an array of supported hash algorithms.
     *
     * @return An array containing the supported HashAlgorithm values: SHA256, SHA384, and SHA512.
     */
    override fun supportedDigests(): Array<DigestAlg> =
        supportedSignatureAlgorithms().filter { it.digestAlgorithm !== null }.map { it.digestAlgorithm!! }.toSet().toTypedArray()

    /**
     * Generates a cryptographic key pair based on the provided elliptic curve.
     *
     * @param curve The elliptic curve mapping used to generate the key pair.
     * @return A `CryptoProviderKeyPair` object containing the generated key pair
     *         with their respective JWK and COSE representations.
     */

    @JsExport.Ignore
    override suspend fun generateKeyAsync(
        kmsKeyRef: String?,
        use: JwkUse?,
        keyOperations: Array<out KeyOperations>?,
        alg: SignatureAlgorithm?,

        ): ManagedKeyPair {
        val keyUse = use ?: JwkUse.sig
        val algMapping = alg ?: SignatureAlgorithm.ECDSA_SHA256
        val curve = alg?.curve ?: Curve.P_256

        val keyOpsMapping = keyOperations ?: arrayOf(KeyOperations.SIGN)
        checkSupportedEcdsaCurve(curve)

        val curveImpl = resolveEcdsaKmpCurve(curve)
        val keyPairGenerator = ecdsa.keyPairGenerator(curveImpl)
        val keyPair = keyPairGenerator.generateKey()


        val privateKeyRaw = keyPair.privateKey.encodeToByteArray(EC.PrivateKey.Format.RAW)
        val publicKeyRaw = keyPair.publicKey.encodeToByteArray(EC.PublicKey.Format.RAW)
        val privateJwk = convertRawKeyBytesToJwk(
            privateKeyBytes = privateKeyRaw,
            publicKeyBytes = publicKeyRaw,
            use = keyUse,
            alg = algMapping,
            curve = curve,
            keyOperations = keyOpsMapping
        )
        val publicJwk = privateJwk.copy(d = null) // let's make sure we do not leak the private key component
        val kid = privateJwk.kid ?: generateJwkThumbprint(publicJwk)
        val privateCoseKey = CoseJoseKeyMappingService.toCoseKey(privateJwk)
        val publicCoseKey = CoseJoseKeyMappingService.toCoseKey(publicJwk)

        val managedKeyPair = ManagedKeyPair(
            kms = getId(),
            kid = kid,
            kmsKeyRef = kmsKeyRef ?: kid,
            jose = JoseKeyPair(if (exposePrivateKeysDuringGeneration) privateJwk.copy(kid = kid) else null, publicJwk.copy(kid = kid)),
            cose = CoseKeyPair(
                if (exposePrivateKeysDuringGeneration) privateCoseKey.copy(kid = kid.toCborByteString(Encoding.UTF8)) else null,
                publicCoseKey.copy(kid = kid.toCborByteString(Encoding.UTF8))
            )
        )
        val keyInfo: IKeyInfo<Jwk> = KeyInfo(
            key = privateJwk,
            keyVisibility = KeyVisibility.PRIVATE,
            keyType = KeyType.EC,
            kmsKeyRef = managedKeyPair.kmsKeyRef,
            kms = managedKeyPair.kms,
            kid = kid,
            x5c = privateJwk.x5c,
            signatureAlgorithm = privateJwk.getSignatureAlgorithm() ?: alg
        )
        privateKeyStore?.storeKey(
            ResolvedKeyInfo.Static.fromKeyInfo(keyInfo, privateJwk),
            kmsKeyRef = managedKeyPair.kmsKeyRef,
            kms = managedKeyPair.kms
        )

        return managedKeyPair
    }

    private fun keyInfoToBytesWithKeystoreLookup(keyInfo: IKeyInfo<*>): EcdsaRawKmpKeyInfoContext {
        return keyInfoToEcdsaRawKmpContext(keyInfo, resolver = { privateKeyStore?.getKey(keyInfo) })
    }

    /**
     * Generates a signature for the given input data using the provided key information.
     *
     * @param keyInfo Information about the signing key.
     * @param input The data to be signed.
     * @return The generated signature as a byte array.
     * @throws IllegalArgumentException If the private key is not provided or not supported.
     */
    @JsExport.Ignore
    override suspend fun createRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray, requireX5Chain: Boolean): ByteArray {
        val (key, _, privateKeyBytes, curveImpl, algImpl) = keyInfoToBytesWithKeystoreLookup(keyInfo)

        if (key.d != null && privateKeyBytes !== null) {
            val privateKey = ecdsa.privateKeyDecoder(curveImpl).decodeFromByteArray(EC.PrivateKey.Format.RAW, privateKeyBytes)
            return privateKey.signatureGenerator(digest = algImpl, format = ECDSA.SignatureFormat.RAW).generateSignature(input)
        }
        throw IllegalArgumentException("Private key resolution or HSMs not supported yet. Please provide a private key")
    }


    /**
     * Verifies the signature of the input data using the provided key information.
     *
     * @param keyInfo Key information that includes the public key and other details.
     * @param input The original data which the signature is supposed to represent.
     * @param signature The signature that needs to be verified.
     * @return true if the signature is valid, false otherwise.
     * @throws IllegalArgumentException if a private key is used to verify the signature.
     */
    @JsExport.Ignore
    override suspend fun isValidRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray, signature: ByteArray): Boolean {
        val (_, publicKeyBytes, _, curveImpl, algImpl) = keyInfoToBytesWithKeystoreLookup(keyInfo)
        val publicKey = ecdsa.publicKeyDecoder(curveImpl).decodeFromByteArray(EC.PublicKey.Format.RAW, publicKeyBytes)
        return publicKey.signatureVerifier(digest = algImpl, format = ECDSA.SignatureFormat.RAW).tryVerifySignature(input, signature)
    }

    @JsExport.Ignore
    override suspend fun createSignature(signInput: SignInput, keyInfo: IKeyInfo<*>?, signatureAlgorithm: SignatureAlgorithm?): SignOutput {
        TODO("Not yet implemented")
    }

    @JsExport.Ignore
    override suspend fun isValidSignature(signInput: SignInput, signature: Signature): Boolean {
        TODO("Not yet implemented")
    }

    override fun getId() = id

    override fun supportedKeyTypes(): Array<KeyType> = arrayOf(KeyType.EC)

    /**
     * Returns an array of supported ECDSA algorithm mappings.
     *
     * @return An array containing AlgorithmMapping.ES256, AlgorithmMapping.ES384, AlgorithmMapping.ES512
     */
    override fun supportedSignatureAlgorithms(): Array<SignatureAlgorithm> =
        arrayOf(SignatureAlgorithm.ECDSA_SHA256, SignatureAlgorithm.ECDSA_SHA384, SignatureAlgorithm.ECDSA_SHA512)

}
