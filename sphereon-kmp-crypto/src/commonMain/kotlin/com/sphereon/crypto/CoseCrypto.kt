package com.sphereon.crypto

import com.sphereon.cbor.encodeToBase64Array
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseMac0Cbor
import com.sphereon.crypto.cose.CoseMac0InputCbor
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ToBeSignedCbor
import com.sphereon.crypto.generic.Certificate
import com.sphereon.crypto.generic.IVerifySignatureResult
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.VerifySignatureResult
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import dev.whyoleg.cryptography.BinarySize.Companion.bytes
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.HMAC
import dev.whyoleg.cryptography.algorithms.SHA256
import toRawEcdhPrivateKey
import toRawEcdhPublicKey
import kotlin.js.JsExport


expect interface ICoseCryptoCallbackMarkerType
interface ICoseCryptoMarkerType

/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
@JsExport.Ignore
interface ICoseCryptoCallbackService : ICoseCryptoCallbackMarkerType {
    suspend fun sign(
        input: ToBeSignedCbor,
        requireX5Chain: Boolean? = true
    ): ByteArray

    suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>,
        requireX5Chain: Boolean? = true
    ): IVerifySignatureResult<ICoseKeyCbor>

    suspend fun mac0(
        input: CoseMac0InputCbor,
        sharedSecret: ByteArray,
        alg: SignatureAlgorithm
    ): CoseMac0Result

    suspend fun <KeyType : IKey> resolvePublicKeyAsync(keyInfo: IKeyInfo<KeyType>): IResolvedKeyInfo<KeyType>
}


/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
@JsExport.Ignore
interface ICoseCryptoService : ICoseCryptoMarkerType {
    suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>? = null,
        requireX5Chain: Boolean? = true
    ): CoseSign1Result<CborType>

    suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>? = null,
        requireX5Chain: Boolean? = true
    ): IVerifySignatureResult<ICoseKeyCbor>


    suspend fun mac0(
        input: CoseMac0InputCbor,
        sharedSecret: ByteArray,
        alg: SignatureAlgorithm
    ): CoseMac0Result

    suspend fun <KeyType : IKey> resolvePublicKey(keyInfo: IKeyInfo<KeyType>): IResolvedKeyInfo<KeyType>
}

/**
 * The main entry point for COSE signature creation/validation, delegating to a platform specific callback implemented by external developers
 */
//interface ICoseCryptoCallbackService : ICallbackService<ICoseCryptoCallbacks>, ICoseCryptoCallbacks

expect fun coseCryptoService(platformCallback: ICoseCryptoCallbackMarkerType = DefaultCallbacks.coseCrypto()): ICoseCryptoService
//expect fun coseService(platformCallback: ICoseCryptoCallbackMarkerType): ICoseCryptoCallbackService

@JsExport
abstract class AbstractCoseCryptoService<CallbackServiceType>(
    open val platformCallback: CallbackServiceType?,
    val provider: CryptographyProvider? = CryptographyProvider.Default,
) :
    ICallbackService<CallbackServiceType> {
    private var disabled = false

    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun disable() = apply {
        this.disabled = true
    }

    override fun enable() = apply {
        this.disabled = false
    }

    protected fun assertEnabled() {
        if (!isEnabled()) {
            CryptoConst.LOG.info("COSE sign1 has been disabled")
            throw IllegalStateException("COSE service is disabled; cannot sign")
        } else if (this.platformCallback === null) {
            // TODO: Probably good to provide an option to the logger whether it should do log-throws
            CryptoConst.LOG.error(
                "COSE callback is not registered"
            ) // Yes this is logs-exception anti pattern, but we are a lib with no knowledge about platform integration
            throw IllegalStateException("COSE have not been initialized. Please register your CoseCallback implementation, or register a default implementation")
        }
    }

    @JsExport.Ignore
    protected suspend fun preSign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): Triple<CoseSign1InputCbor, ToBeSignedCbor, IKeyInfo<ICoseKeyCbor>> {
        assertEnabled()
        var (protectedHeader, cborKeyInfo) = verifyAndAmendKeyInfo(
            protectedHeader = input.protectedHeader,
            unprotectedHeader = input.unprotectedHeader,
            keyInfo = keyInfo,
            requireX5Chain = requireX5Chain
        )
        val key = cborKeyInfo.key
        var alg = protectedHeader.alg ?: cborKeyInfo.signatureAlgorithm?.cose ?: key.getSignatureAlgorithm()?.cose
        if (protectedHeader.alg === null) {
            protectedHeader = protectedHeader.copy(alg = alg)
        }
        val coseSign1 = input.copy(protectedHeader = protectedHeader)
        val toSign = coseSign1.toBeSignedCbor(
            keyInfo = cborKeyInfo,
            alg = input.protectedHeader?.alg?.let { SignatureAlgorithm.Static.fromCose(it) } ?: cborKeyInfo.signatureAlgorithm
            ?: key.getSignatureAlgorithm() ?: throw IllegalStateException("No alg supplied. ${key}")
        )
        return Triple(coseSign1, toSign, cborKeyInfo)
    }


    protected fun <CborType> postSign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<ICoseKeyCbor>,
        signature: ByteArray
    ): CoseSign1Result<CborType> {
        val coseSign1 = CoseSign1Cbor<CborType>(
            protectedHeader = input.protectedHeader ?: throw IllegalStateException("No protected header present"),
            unprotectedHeader = input.unprotectedHeader,
            signature = signature.toCborByteString(),
            payload = input.payload
        )
        return CoseSign1Result(coseSign1 = coseSign1, keyInfo = keyInfo, input = input)
    }

    @JsExport.Ignore
    protected suspend fun verifyAndAmendKeyInfo(
        protectedHeader: CoseHeaderCbor? = null,
        unprotectedHeader: CoseHeaderCbor? = null,
        keyInfo: IKeyInfo<*>? = null,
        managedKey: Boolean = false,
        requireX5Chain: Boolean = true
    ): Pair<CoseHeaderCbor, IResolvedKeyInfo<ICoseKeyCbor>> {
        var x5chain = protectedHeader?.x5chain ?: unprotectedHeader?.x5chain
        val sigAlg = protectedHeader?.alg ?: unprotectedHeader?.alg ?: keyInfo?.signatureAlgorithm?.cose
        val kid =
            keyInfo?.kid ?: protectedHeader?.kid?.encodeTo(Encoding.BASE64URL) ?: unprotectedHeader?.kid?.encodeTo(Encoding.BASE64URL)

        var keyInfoWithKey = keyInfo
        if (keyInfo === null && x5chain !== null) {
            if (sigAlg?.keyType !== null) {
                // Let's create a key info for platform specific code from the x5chain
                // TODO: We should also get the leaf cert and fill the rest
                println("TODO: Key derived from x5chain, but we do not convert all properties to a Cborkey yet!")

                keyInfoWithKey = KeyInfo(
                    key = CoseKeyCbor(x5chain = x5chain, kty = sigAlg.keyType.toCbor(), kid = kid?.toCborByteString(Encoding.BASE64URL)),
                    x5c = x5chain.encodeToBase64Array(false),
                    signatureAlgorithm = SignatureAlgorithm.Static.fromCose(sigAlg),
                    kid = kid
                )
            }
        }
        if (keyInfoWithKey === null) {
            throw IllegalStateException("No protected header or key info passed in. Could not construct key info with key")
        }
        if (managedKey) {

        }
        val key = CoseJoseKeyMappingService.toCoseKey(keyInfoWithKey.key ?: this.resolvePublicCborKey(keyInfoWithKey).key)
        if (x5chain === null) {
            x5chain = key.x5chain
        }
        if (requireX5Chain && x5chain === null) {
            throw IllegalArgumentException("No x5c or x5chain could be found in header or resolved key. keyinfo x5c: ${keyInfoWithKey.x5c}, header: ${protectedHeader}")
        }


        val protectedHeaderWithX5chain = protectedHeader?.copy(x5chain = x5chain) ?: CoseHeaderCbor(x5chain = x5chain)
        return Pair(
            protectedHeaderWithX5chain,
            CoseJoseKeyMappingService.toResolvedCoseKeyInfo(CoseJoseKeyMappingService.toResolvedKeyInfo(keyInfoWithKey, key))
        )
    }


    @JsExport.Ignore
    protected abstract suspend fun resolvePublicCborKey(keyInfo: IKeyInfo<*>): IResolvedKeyInfo<ICoseKeyCbor>
}

class CoseCryptoService(override val platformCallback: ICoseCryptoCallbackService = DefaultCallbacks.coseCrypto()) :
    AbstractCoseCryptoService<ICoseCryptoCallbackService>(platformCallback),
    ICoseCryptoService {

    override suspend fun resolvePublicCborKey(keyInfo: IKeyInfo<*>): IResolvedKeyInfo<ICoseKeyCbor> {
        val info = resolvePublicKey(keyInfo)
        return CoseJoseKeyMappingService.toResolvedCoseKeyInfo(info)
    }

    override fun platform(): ICoseCryptoCallbackService {
        return this.platformCallback
    }


    override suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean?
    ): CoseSign1Result<CborType> {
        val (preSignInputResult, toSign, preSignKeyInfoResult) = this.preSign1(input, keyInfo, requireX5Chain == true)
        val signature = this.platformCallback.sign(toSign, requireX5Chain)
        return this.postSign1(preSignInputResult, preSignKeyInfoResult, signature)
    }


    override suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean?
    ): IVerifySignatureResult<ICoseKeyCbor> {
        val (_, info) = verifyAndAmendKeyInfo(
            protectedHeader = input.protectedHeader,
            unprotectedHeader = input.unprotectedHeader,
            keyInfo = keyInfo,
            requireX5Chain = requireX5Chain == true
        )
        try {
            this.assertEnabled()
        } catch (e: IllegalStateException) {
            return VerifySignatureResult(
                keyInfo = info,
                name = CryptoConst.COSE_LITERAL,
                message = "COSE signing/verification has been disabled or not callback has been regenstered! ${e.message}",
                error = this.isEnabled(),
                critical = this.isEnabled()
            )
        }

        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyType = sigAlg?.keyType ?: info.key.getKty().cose
        if (keyType == null) {
            return VerifySignatureResult(
                keyInfo = info,
                name = CryptoConst.COSE_LITERAL,
                error = true,
                message = "No signature algorithm or key type found or provided",
                critical = true
            )
        }
        return platformCallback.verify1(input = input, keyInfo = info)
    }

    override suspend fun mac0(input: CoseMac0InputCbor,
                              sharedSecret: ByteArray,
                              alg: SignatureAlgorithm): CoseMac0Result = this.platformCallback.mac0(input = input, sharedSecret = sharedSecret, alg = alg)

    override suspend fun <KeyType : IKey> resolvePublicKey(keyInfo: IKeyInfo<KeyType>) = this.platformCallback.resolvePublicKeyAsync(keyInfo)

}

@JsExport
data class CoseSign1Result<CborType>(val coseSign1: CoseSign1Cbor<CborType>, val keyInfo: IKeyInfo<ICoseKeyCbor>, val input: CoseSign1InputCbor)


@JsExport
data class CoseMac0Result(val coseMac0: CoseMac0Cbor, val input: CoseMac0InputCbor)


@JsExport.Ignore
suspend fun defaultCreateMac0(
    input: CoseMac0InputCbor,
    sharedSecret: ByteArray,
    alg: SignatureAlgorithm = input.protectedHeader.alg?.let { SignatureAlgorithm.Static.fromCose(it) } ?: SignatureAlgorithm.HMAC_SHA256,
    provider: CryptographyProvider? = CryptographyProvider.Default
): CoseMac0Result {
    // Since this lib will mostly be used in the context of Mdl/Mdocs where the mac0 is ECKA-DH (Diffie-Hellman) with SHA-256, we provide a default
    // We expose the provider as optional, as it can be set later on services. But in reallity it is required for this call!
    requireNotNull(provider) { "Crypto provider needs to be set for the default Mac0 implementation" }
    val digest = alg.digestAlgorithm?.toCryptoGraphicAlgorithm()
    checkNotNull(digest) {
        """Signature (Digest) algorithm $alg not supported or provided. Supported algorithms: ${
            arrayOf(SignatureAlgorithm.HMAC_SHA256, SignatureAlgorithm.HMAC_SHA384, SignatureAlgorithm.HMAC_SHA512).joinToString(", ")
        }"""
    }
    val protectedHeader = input.protectedHeader.copy(alg = alg.cose)
    val inputWithHeader = input.copy(protectedHeader = protectedHeader)
    val toBeMaced = inputWithHeader.toMac0Structure().toBeMaced()
    val tag = provider.get(HMAC).keyDecoder(digest).decodeFromByteArray(HMAC.Key.Format.RAW, sharedSecret).signatureGenerator()
        .generateSignature(toBeMaced.value)
    val coseMac0 = CoseMac0Cbor(
        tag = tag.toCborByteString(),
        protectedHeader = inputWithHeader.protectedHeader ?: CoseHeaderCbor(alg = CoseAlgorithm.HMAC256_256),
        unprotectedHeader = inputWithHeader.unprotectedHeader,
        payload = inputWithHeader.payload?.toCborByteString()
    )
    return CoseMac0Result(input = inputWithHeader, coseMac0 = coseMac0)
}

@JsExport.Ignore
suspend fun defaultCreateMac0UsingKeys(
    provider: CryptographyProvider? = null,
    input: CoseMac0InputCbor,
    selfPrivateKey: IResolvedKeyInfo<*>,
    otherPublicKey: IResolvedKeyInfo<*>,
    alg: SignatureAlgorithm = SignatureAlgorithm.HMAC_SHA256,
    info: String = "EMacKey",
    salt: ByteArray = byteArrayOf(),
    macCallback: (provider: CryptographyProvider?, input: CoseMac0InputCbor,
                  sharedSecret: ByteArray,
                  alg: SignatureAlgorithm) -> CoseMac0Result
): CoseMac0Result {
    // Since this lib will mostly be used in the context of Mdl/Mdocs where the mac0 is ECKA-DH (Diffie-Hellman) with SHA-256, we provide a default
    requireNotNull(provider) { "Crypto provider needs to be set for the default Mac0 implementation" }
    val selfRawPrivateKey = toRawEcdhPrivateKey(provider = provider, selfPrivateKey)
    val otherRawPublicKey = toRawEcdhPublicKey(provider = provider, otherPublicKey)
    val sharedSecret = selfRawPrivateKey.sharedSecretGenerator().generateSharedSecretToByteArray(otherRawPublicKey)
    val emacKey =
        provider.get(HKDF).secretDerivation(digest = SHA256, outputSize = 32.bytes, salt = salt, info = info.decodeFrom(Encoding.UTF8))
            .deriveSecretToByteArray(sharedSecret)
//fixme
    return macCallback(provider, input, emacKey, alg)
}
