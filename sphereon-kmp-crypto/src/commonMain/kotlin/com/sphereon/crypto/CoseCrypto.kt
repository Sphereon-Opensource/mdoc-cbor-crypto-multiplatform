package com.sphereon.crypto

import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.crypto.cose.CoseHeaderCbor
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ToBeSignedCbor
import com.sphereon.crypto.jose.Jwk
import com.sphereon.kmp.Encoding
import kotlin.js.JsExport


expect interface ICoseCryptoCallbackMarkerType
interface ICoseCryptoMarkerType

/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
@JsExport.Ignore
interface ICoseCryptoCallbackService: ICoseCryptoCallbackMarkerType {
    suspend fun sign(
        input: ToBeSignedCbor
    ): ByteArray

    suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<ICoseKeyCbor>
    ): IVerifySignatureResult<ICoseKeyCbor>

    suspend fun resolvePublicKey(keyInfo: IKeyInfo<*>): IKey
}


/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
@JsExport.Ignore
interface ICoseCryptoService: ICoseCryptoMarkerType {
    suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>? = null,
        requireX5Chain: Boolean
    ): CoseSign1Result<CborType>

    suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>? = null,
        requireX5Chain: Boolean
    ): IVerifySignatureResult<ICoseKeyCbor>

    suspend fun resolvePublicKey(keyInfo: IKeyInfo<*>): IKey
}

/**
 * The main entry point for COSE signature creation/validation, delegating to a platform specific callback implemented by external developers
 */
//interface ICoseCryptoCallbackService : ICallbackService<ICoseCryptoCallbacks>, ICoseCryptoCallbacks

expect fun coseCryptoService(platformCallback: ICoseCryptoCallbackMarkerType = DefaultCallbacks.coseCrypto()): ICoseCryptoService
//expect fun coseService(platformCallback: ICoseCryptoCallbackMarkerType): ICoseCryptoCallbackService

abstract class AbstractCoseCryptoService<CallbackServiceType>(open val platformCallback: CallbackServiceType?) :
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

    protected suspend fun preSign1(input: CoseSign1InputCbor, keyInfo: IKeyInfo<*>?, requireX5Chain: Boolean): Triple<CoseSign1InputCbor, ToBeSignedCbor, IKeyInfo<ICoseKeyCbor>> {
        assertEnabled()
        val (protectedHeader, cborKeyInfo) = verifyAndAmendKeyInfo(
            protectedHeader = input.protectedHeader,
            unprotectedHeader = input.unprotectedHeader,
            keyInfo = keyInfo,
            requireX5Chain = requireX5Chain
        )
        val key = cborKeyInfo.key ?: throw IllegalStateException("No key supplied")
        val coseSign1 = input.copy(protectedHeader = protectedHeader)
        val toSign = coseSign1.toBeSignedCbor(key = key, alg = key.getAlgMapping()?.cose ?: throw IllegalStateException("No alg supplied. ${key}"))
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

    protected suspend fun verifyAndAmendKeyInfo(
        protectedHeader: CoseHeaderCbor? = null,
        unprotectedHeader: CoseHeaderCbor? = null,
        keyInfo: IKeyInfo<*>? = null,
        requireX5Chain: Boolean = true
    ): Pair<CoseHeaderCbor, IKeyInfo<ICoseKeyCbor>> {
        var x5chain = protectedHeader?.x5chain
        val sigAlg = protectedHeader?.alg ?: unprotectedHeader?.alg
        val kid =
            keyInfo?.kid ?: protectedHeader?.kid?.encodeTo(Encoding.BASE64URL) ?: unprotectedHeader?.kid?.encodeTo(Encoding.BASE64URL)

        var keyInfoWithKey = keyInfo
        if (keyInfo === null && x5chain !== null) {
            if (sigAlg?.keyType !== null) {
                // Let's create a key info for platform specific code from the x5chain // TODO: We could also get the leaf cert and fill the rest
                keyInfoWithKey = KeyInfo(
                    key = CoseKeyCbor(x5chain = x5chain, kty = sigAlg.keyType.toCbor(), kid = kid?.toCborByteString(Encoding.BASE64URL)),
                    kid = kid
                )
            }
        }
        if (keyInfoWithKey === null) {
            throw IllegalStateException("No protected header or key info passed in")
        }
        val key = CoseJoseKeyMappingService.toCoseKey(keyInfoWithKey.key ?: this.resolvePublicCborKey(keyInfoWithKey))
        if (x5chain === null) {
            x5chain = key.x5chain
        }
        if (requireX5Chain && x5chain === null) {
            throw IllegalArgumentException("No x5c or x5chain could be found in header or resolved key")
        }
        sigAlg
        CoseAlgorithm.ES256

        keyInfoWithKey = CoseJoseKeyMappingService.toCoseKeyInfo(keyInfoWithKey)


        val protectedHeaderWithX5chain = protectedHeader?.copy(x5chain = x5chain) ?: CoseHeaderCbor(x5chain = x5chain)
        val keyType = sigAlg?.keyType ?: key.getKtyMapping().cose
        if (keyType == null) {
            throw IllegalStateException("No Key type found or provided")
        }
        return Pair(protectedHeaderWithX5chain, CoseJoseKeyMappingService.toCoseKeyInfo(keyInfoWithKey))
    }

    protected abstract suspend fun resolvePublicCborKey(keyInfo: IKeyInfo<*>): ICoseKeyCbor
}

class CoseCryptoService(override val platformCallback: ICoseCryptoCallbackService = DefaultCallbacks.coseCrypto()) :
    AbstractCoseCryptoService<ICoseCryptoCallbackService>(platformCallback),
    ICoseCryptoService {


    override suspend fun resolvePublicCborKey(keyInfo: IKeyInfo<*>): ICoseKeyCbor {
        var key = keyInfo.key
        if (key === null) {
            key = resolvePublicKey(keyInfo)
        }
        return CoseJoseKeyMappingService.toCoseKey(key)
    }

    override fun platform(): ICoseCryptoCallbackService {
        return this.platformCallback
    }


    override suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): CoseSign1Result<CborType> {
        val (preSignInputResult, toSign, preSignKeyInfoResult) = this.preSign1(input, keyInfo, requireX5Chain)
        val signature = this.platformCallback.sign(toSign)
        return this.postSign1(preSignInputResult, preSignKeyInfoResult, signature)
    }


    override suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): IVerifySignatureResult<ICoseKeyCbor> {
        val (_, info) = verifyAndAmendKeyInfo(
            protectedHeader = input.protectedHeader,
            unprotectedHeader = input.unprotectedHeader,
            keyInfo = keyInfo,
            requireX5Chain = requireX5Chain
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
        val keyType = sigAlg?.keyType ?: info.key?.getKtyMapping()?.cose
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

    override suspend fun resolvePublicKey(keyInfo: IKeyInfo<*>) = this.platformCallback.resolvePublicKey(keyInfo)

}

@JsExport
data class CoseSign1Result<CborType>(val coseSign1: CoseSign1Cbor<CborType>, val keyInfo: IKeyInfo<ICoseKeyCbor>, val input: CoseSign1InputCbor)
