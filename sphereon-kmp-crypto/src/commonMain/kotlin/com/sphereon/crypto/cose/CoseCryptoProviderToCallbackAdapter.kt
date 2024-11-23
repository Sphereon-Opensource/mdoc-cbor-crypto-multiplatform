package com.sphereon.crypto.cose

import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.ICoseCryptoCallbackService
import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.defaultCreateMac0
import com.sphereon.crypto.generic.IVerifySignatureResult
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.VerifySignatureResult
import com.sphereon.crypto.kms.IKeyManagerService
import com.sphereon.crypto.kms.IKeyResolverService
import com.sphereon.crypto.sign.IRawSignatureService
import kotlin.js.JsExport

/**
 * Adapter class to bridge between `ICoseCryptoCallbackService` and an array of `ICryptoProvider`.
 * Responsible for handling COSE cryptographic operations including signing and verifying signatures.
 *
 * Either a Key Manager needs to be supplied, or both a RAW Signature service and public key resolver service need to be supplied
 *
 * @param providers Array of cryptographic providers implementing `ICryptoProvider`.
 */
@JsExport.Ignore
class CoseCryptoProviderToCallbackAdapter(
    private val keyManagerService: IKeyManagerService? = null,
    private val rawSignatureService: IRawSignatureService? = null,
    private val publicKeyResolverService: IKeyResolverService? = null
) : ICoseCryptoCallbackService {
    init {
        require(keyManagerService !== null || rawSignatureService !== null) { "Either a keyManager or rawSignature service needs to be provided" }
        require(
            keyManagerService?.getResolverIds()?.isNotEmpty() ?: false || publicKeyResolverService !== null
        ) { "Either a keyManager or public key resolver service needs to be provided" }
    }

    private fun assertedSignatureProvider(alg: SignatureAlgorithm? = null, kms: String? = null): IRawSignatureService {
        return (keyManagerService?.getKms(kms = kms, alg = alg) ?: rawSignatureService)!! // already asserted during construction
    }

    private fun assertedPublicKeyProvider(keyInfo: IKeyInfo<*>): IKeyResolverService {
        return keyManagerService?.getResolverByKeyTypeOrIdentifier(
            keyType = keyInfo.keyType,
            resolverId = keyInfo.kms ?: keyManagerService.defaultResolverId()
        ) ?: publicKeyResolverService ?: throw PKIException("Could not deduce key resolver from key info, default resolver, or provided resolver")
    }

    /**
     * Signs the provided input data using the specified key and algorithm.
     *
     * @param input The data to be signed, along with the key and algorithm information.
     * @return The generated signature as a ByteArray.
     */
    override suspend fun sign(input: ToBeSignedCbor, requireX5Chain: Boolean): ByteArray {
        val keyInfo = input.keyInfo
        val alg = keyInfo.signatureAlgorithm ?: input.alg
        return assertedSignatureProvider(alg = alg, kms = keyInfo.kms).createRawSignatureAsync(keyInfo, input.value, requireX5Chain)

    }

    /**
     * Verifies a COSE_Sign1 message using the provided key information.
     *
     * @param input The COSE_Sign1 message to verify.
     * @param keyInfo The key information used for verification.
     * @return The result of the signature verification.
     */
    override suspend fun verify1(input: CoseSign1Cbor<*>, keyInfo: IKeyInfo<*>, requireX5Chain: Boolean): IVerifySignatureResult<ICoseKeyCbor> {
        val resolvedKeyInfo = this.resolvePublicKeyAsync(keyInfo)
        val key = resolvedKeyInfo.key
        val alg = resolvedKeyInfo.signatureAlgorithm ?: key.getSignatureAlgorithm() ?: throw IllegalArgumentException("No alg was supplied for key")
        if (input.payload?.value === null) {
            throw IllegalArgumentException("Null payload supplied to verify signature")
        }

        val validSig = assertedSignatureProvider(alg = alg, kms = resolvedKeyInfo.kms).isValidRawSignatureAsync(
            resolvedKeyInfo,
            input = input.payload.value,
            signature = input.signature.value
        )
        return VerifySignatureResult(
            keyInfo = CoseJoseKeyMappingService.toCoseKeyInfo(resolvedKeyInfo),
            error = !validSig,
            critical = !validSig,
            message = if (validSig) "Signature valid" else "Signature invalid",
            name = "Cose verify1"
        )
    }

    override suspend fun mac0(input: CoseMac0InputCbor, sharedSecret: ByteArray, alg: SignatureAlgorithm) =
        defaultCreateMac0(input, sharedSecret, alg)


    /**
     * Resolves a public key based on the provided key information.
     *
     * @param keyInfo Contains information about the key to be resolved, possibly including the key identifier (kid), key type, and other optional parameters.
     * @return The resolved public key as an instance of IKey.
     */
    override suspend fun <KeyType : IKey> resolvePublicKeyAsync(keyInfo: IKeyInfo<KeyType>): IResolvedKeyInfo<KeyType> {
        // fixme: How do we determine which provider if only a kid was supplied? (that is a valid use case)
        return assertedPublicKeyProvider(keyInfo = keyInfo).resolvePublicKeyAsync(
            keyInfo = keyInfo,
            verifyX509CertificateChain = false,
        ) // We call this method from the verification, so let's not verify ourselves as well!
    }

}
