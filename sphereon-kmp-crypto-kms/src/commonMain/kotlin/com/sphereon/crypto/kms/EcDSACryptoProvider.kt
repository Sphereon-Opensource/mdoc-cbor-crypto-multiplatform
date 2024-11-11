package com.sphereon.crypto.kms

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
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaCurve
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.Uuid
import com.sphereon.kmp.decodeFromBase64Url
import com.sphereon.kmp.encodeTo
import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.Digest
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDSA
import dev.whyoleg.cryptography.algorithms.SHA256
import dev.whyoleg.cryptography.algorithms.SHA384
import dev.whyoleg.cryptography.algorithms.SHA512
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
    private val privateKeyStore: IKeyStoreService? = MemoryKeyStoreService(keyVisibility = KeyVisibility.PRIVATE)
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
        checkSupportedCurve(curve)

        val curveImpl = resolveCurve(curve)
        val keyPairGenerator = ecdsa.keyPairGenerator(curveImpl)
        val keyPair = keyPairGenerator.generateKey()


        val privateKeyRaw = keyPair.privateKey.encodeToByteArray(EC.PrivateKey.Format.RAW)
        val publicKeyRaw = keyPair.publicKey.encodeToByteArray(EC.PublicKey.Format.RAW)
        val privateJwk = convertRawKeyToJwk(
            privateKeyBytes = privateKeyRaw,
            publicKeyBytes = publicKeyRaw,
            use = keyUse,
            alg = algMapping,
            curve = curve,
            keyOperations = keyOpsMapping
        )
        val publicJwk = privateJwk.copy(d = null) // let's make sure we do not leak the private key component
        val privateCoseKey = CoseJoseKeyMappingService.toCoseKey(privateJwk)
        val publicCoseKey = CoseJoseKeyMappingService.toCoseKey(publicJwk)


        val managedKeyPair = ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = kmsKeyRef ?: Uuid.v4String(), // TODO: Generate kid and use that as fallback instead of uuid
            jose = JoseKeyPair(privateJwk, publicJwk),
            cose = CoseKeyPair(privateCoseKey, publicCoseKey)
        )
        val keyInfo: IKeyInfo<Jwk> = KeyInfo(
            key = privateJwk,
            keyVisibility = KeyVisibility.PRIVATE,
            keyType = KeyType.EC,
            kmsKeyRef = managedKeyPair.kmsKeyRef,
            kms = managedKeyPair.kms,
            kid = privateJwk.kid,
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

    /**
     * Generates a signature for the given input data using the provided key information.
     *
     * @param keyInfo Information about the signing key.
     * @param input The data to be signed.
     * @return The generated signature as a byte array.
     * @throws IllegalArgumentException If the private key is not provided or not supported.
     */
    @JsExport.Ignore
    override suspend fun createRawSignatureAsync(keyInfo: IKeyInfo<*>, input: ByteArray): ByteArray {
        val (key, _, privateKeyBytes, curveImpl, algImpl) = prepareKeyInfo(keyInfo)

        if (key.d != null && privateKeyBytes !== null) {
            val privateKey = ecdsa.privateKeyDecoder(curveImpl).decodeFromByteArrayBlocking(EC.PrivateKey.Format.RAW, privateKeyBytes)
            return privateKey.signatureGenerator(digest = algImpl, format = ECDSA.SignatureFormat.RAW).generateSignatureBlocking(input)
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
        val (_, publicKeyBytes, _, curveImpl, algImpl) = prepareKeyInfo(keyInfo)
        val publicKey = ecdsa.publicKeyDecoder(curveImpl).decodeFromByteArrayBlocking(EC.PublicKey.Format.RAW, publicKeyBytes)
        return publicKey.signatureVerifier(digest = algImpl, format = ECDSA.SignatureFormat.RAW).tryVerifySignatureBlocking(input, signature)
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


    /**
     * Prepares key information context by converting the provided `IKeyInfo` to a JWK format
     * and serializing its key into bytes, then resolving curve and algorithm implementations.
     *
     * @param keyInfo The key information to be prepared, implementing the `IKeyInfo` interface.
     * @return A `KeyInfoContext` containing the key, its byte representation, the curve, and the algorithm.
     */
    @OptIn(ExperimentalStdlibApi::class)
    private fun prepareKeyInfo(keyInfo: IKeyInfo<*>): KeyInfoContext {
        val keyInfoJwk = toKeyInfoJwk(
            if (keyInfo.keyVisibility === KeyVisibility.PRIVATE && keyInfo.key !== null) keyInfo else privateKeyStore?.getKey(keyInfo) ?: keyInfo
        )

        val key = keyInfoJwk.key
            ?: throw IllegalArgumentException("Either we need to get the private key from a private key store or it needs to be passed in for this provider")
        if (key.x == null || key.y == null) {
            throw IllegalArgumentException("EC JWK needs an x and y coordinate as a public key")
        }
        val publicKeyBytes = "04${key.x!!.decodeFromBase64Url().encodeTo(Encoding.HEX)}${
            key.y!!.decodeFromBase64Url().encodeTo(Encoding.HEX)
        }".hexToByteArray()
        val privateKeyBytes = key.d?.decodeFromBase64Url()
        val curveImpl = resolveCurve(Curve.Static.fromJose(key.crv))
        val algImpl = resolveDigest(SignatureAlgorithm.Static.fromJose(key.alg))

        return KeyInfoContext(key, publicKeyBytes, privateKeyBytes, curveImpl, algImpl)
    }

    /**
     * Converts the provided `IKeyInfo` instance to a `KeyInfo` instance containing a `Jwk`.
     *
     * @param keyInfo The original `IKeyInfo` instance to be converted to `KeyInfo<Jwk>`.
     * @return A `KeyInfo` instance containing a `Jwk` generated from the provided `IKeyInfo`.
     * @throws IllegalArgumentException If the key is not present in the resulting `KeyInfo<Jwk>`.
     */
    private fun toKeyInfoJwk(keyInfo: IKeyInfo<*>): KeyInfo<Jwk> {
        val keyInfoJwk = CoseJoseKeyMappingService.toJwkKeyInfo(keyInfo)
        val key =
            keyInfoJwk.key ?: throw IllegalArgumentException("Looking up keys by kid is not supported yet. Please provide a public or private key")
        validateKey(key)
        return keyInfoJwk
    }

    /**
     * Validates the given JWK (JSON Web Key) to ensure it meets specific criteria for
     * Elliptic Curve Digital Signature Algorithm (ECDSA) keys.
     *
     * @param key the JWK to validate. It should have a key type of EC, a supported
     *            algorithm (ES256, ES384, or ES512), and a supported curve (P-256, P-384, or P-521).
     * @throws IllegalArgumentException if any of the validation checks fail.
     */
    private fun validateKey(key: Jwk) {
        require(key.kty == JwaKeyType.EC) { "Key type (converted to JWA) ${key.kty} is not of type ${JwaKeyType.EC}" }
        require(
            arrayOf(
                JwaAlgorithm.ES256,
                JwaAlgorithm.ES384,
                JwaAlgorithm.ES512
            ).contains(key.alg)
        ) { "Key alg (converted to JWA) ${key.alg} is not supported for key type ${JwaKeyType.EC}" }
        require(
            arrayOf(
                JwaCurve.P_256,
                JwaCurve.P_384,
                JwaCurve.P_521
            ).contains(key.crv)
        ) { "Key crv (converted to JWA) ${key.crv} is not supported for key type ${JwaKeyType.EC}" }
    }

    /**
     * Converts the provided key bytes to a JSON Web Key (JWK).
     *
     * @param publicKeyBytes The bytes representing the key to be converted.
     * @return The equivalent JWK representation of the provided key bytes.
     */
    private fun convertRawKeyToJwk(
        publicKeyBytes: ByteArray,
        privateKeyBytes: ByteArray?,
        use: JwkUse = JwkUse.sig,
        keyOperations: Array<out KeyOperations> = arrayOf(KeyOperations.SIGN),
        curve: Curve = Curve.P_256,
        alg: SignatureAlgorithm = SignatureAlgorithm.ECDSA_SHA256
    ): Jwk {

        val x = publicKeyBytes.copyOfRange(1, 33).encodeTo(Encoding.BASE64URL)
        val y = publicKeyBytes.copyOfRange(33, 65).encodeTo(Encoding.BASE64URL)
        val d = privateKeyBytes?.encodeTo(Encoding.BASE64URL)
        return Jwk(
            kty = JwaKeyType.EC,
            alg = alg.jose,
            crv = curve.jose,
            x = x,
            y = y,
            d = d,
            use = use.value,
            key_ops = keyOperations.map { it.jose }.toTypedArray()
        )
    }

    /**
     * Checks whether the provided curve is supported for EcDSA and throws an
     * IllegalArgumentException if it is not supported.
     *
     * @param curve The curve to be checked for support.
     */
    private fun checkSupportedCurve(curve: Curve) {
        if (!isSupportedCurve(curve)) {
            throw IllegalArgumentException("Curve $curve not supported for EcDSA")
        }
    }

    /**
     * Resolves the given curve mapping to an elliptic curve.
     *
     * @param curve The curve mapping to be resolved.
     * @return The corresponding elliptic curve.
     * @throws IllegalArgumentException If the provided curve is not supported.
     */
    private fun resolveCurve(curve: Curve): EC.Curve {
        return when (curve) {
            is Curve.P_256 -> EC.Curve.P256
            is Curve.P_384 -> EC.Curve.P384
            is Curve.P_521 -> EC.Curve.P521
            else -> throw IllegalArgumentException("Curve $curve not supported")
        }
    }

    /**
     * Resolves the given algorithm mapping to its corresponding digest identifier.
     *
     * @param alg The algorithm mapping to resolve.
     * @return The corresponding CryptographyAlgorithmId for the given digest.
     */
    private fun resolveDigest(alg: SignatureAlgorithm): CryptographyAlgorithmId<Digest> {
        return when (alg) {
            is SignatureAlgorithm.ECDSA_SHA256 -> SHA256
            is SignatureAlgorithm.ECDSA_SHA384 -> SHA384
            is SignatureAlgorithm.ECDSA_SHA512 -> SHA512
            else -> throw IllegalArgumentException("Algorithm $alg not supported")
        }
    }

    /**
     * Represents the context information needed for key operations in ECDSA cryptography.
     *
     * @property key The JSON Web Key (JWK) representation of the cryptographic key.
     * @property publicKeyBytes The byte-array representation of the cryptographic key.
     * @property curveImpl The elliptic curve implementation used for cryptographic operations.
     * @property algImpl The cryptography algorithm identifier tied to a specific digest.
     */
    @Suppress("NON_EXPORTABLE_TYPE")
    private data class KeyInfoContext(
        val key: Jwk,
        val publicKeyBytes: ByteArray,
        val privateKeyBytes: ByteArray?,
        val curveImpl: EC.Curve,
        val algImpl: CryptographyAlgorithmId<Digest>
    )
}
