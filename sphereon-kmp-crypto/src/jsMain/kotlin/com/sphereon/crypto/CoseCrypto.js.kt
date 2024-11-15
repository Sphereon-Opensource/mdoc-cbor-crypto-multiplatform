package com.sphereon.crypto

import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ToBeSignedCbor
import com.sphereon.crypto.generic.IVerifySignatureResult
import com.sphereon.crypto.generic.VerifySignatureResult
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asPromise
import kotlinx.coroutines.async
import kotlinx.coroutines.await
import kotlin.js.Promise



@JsExport
external interface ICoseCryptoCallbackJS : ICoseCryptoCallbackMarkerType {

    @JsName("signAsync")
    fun signAsync(
        input: ToBeSignedCbor,
        requireX5Chain: Boolean
    ): Promise<ByteArray>

    @JsName("verify1Async")
    fun verify1Async(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<ICoseKeyCbor>
    ): Promise<IVerifySignatureResult<ICoseKeyCbor>>

    @JsName("resolvePublicKeyAsync")
    fun <KT : IKey> resolvePublicKeyAsync(keyInfo: IKeyInfo<KT>): Promise<IResolvedKeyInfo<KT>>
}


/**
 * A version that resembles the internal CoseCrypto interface, but then using promises instead of coroutines to make it fit the JS world
 */
@JsExport
external interface ICoseCryptoServiceJS {
    @JsName("sign1")
    fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): Promise<CoseSign1Result<CborType>>

    @JsName("verify1")
    fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): Promise<IVerifySignatureResult<ICoseKeyCbor>>

    @JsName("resolvePublicKeyAsync")
    fun <KT : IKey> resolvePublicKeyAsync(keyInfo: IKeyInfo<KT>): Promise<IResolvedKeyInfo<KT>>
}

private const val COSE_CRYPTO_SERVICE_JS_SCOPE = "CoseCryptoServiceJS"

/**
 * You can register your own COSE JS implementation with this class via the constructor
 *
 * This is the main integration point in JS to perform signature creation and verification for CBOR/COSE
 *
 * We do not expose crypto functions in this library on purpose. First of all there are not many good KMP crypto
 * implementations available at present. Second and more importantly:
 * We don't want to assume or dictate what crypto library you are using
 *
 * We do provide some defaults and examples
 */
@JsExport
class CoseCryptoServiceJS(override val platformCallback: ICoseCryptoCallbackJS = DefaultCallbacks.coseCrypto()) :
    AbstractCoseCryptoService<ICoseCryptoCallbackJS>(platformCallback),
    ICoseCryptoServiceJS {


    @JsExport.Ignore
    override suspend fun resolvePublicCborKey(keyInfo: IKeyInfo<*>): IResolvedKeyInfo<ICoseKeyCbor> {
        val resolvedKeyInfo = resolvePublicKeyAsync(keyInfo).await()
        return CoseJoseKeyMappingService.toResolvedCoseKeyInfo(resolvedKeyInfo)
     }

    override fun platform(): ICoseCryptoCallbackJS {
        return this.platformCallback
    }

    override fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): Promise<CoseSign1Result<CborType>> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_SERVICE_JS_SCOPE)).async {
            println("pre preSign1")
            val (preSignInputResult, toSign, preSignKeyInfoResult) = preSign1(input, keyInfo, requireX5Chain)
            println("post presign1, about to call platform callback.sign")
            val platform = platform()
            requireNotNull(platform)
            println("platform: ${platform!!::class.simpleName} ${platform.toString()}")
            val signature = platform.signAsync(input = toSign, requireX5Chain = requireX5Chain).await()
            println("post platformcallback.sign, about to call postsign")
            return@async postSign1<CborType>(preSignInputResult, preSignKeyInfoResult, signature)
        }.asPromise()

    }


    override fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): Promise<IVerifySignatureResult<ICoseKeyCbor>> {
        return CoroutineScope(CoroutineName(COSE_CRYPTO_SERVICE_JS_SCOPE)).async {
            val (protectedHeader, info) = verifyAndAmendKeyInfo(
                protectedHeader = input.protectedHeader,
                unprotectedHeader = input.unprotectedHeader,
                keyInfo = keyInfo,
                requireX5Chain = requireX5Chain
            )
            try {
                assertEnabled()
            } catch (e: IllegalStateException) {
                return@async VerifySignatureResult(
                    keyInfo = info,
                    name = CryptoConst.COSE_LITERAL,
                    message = "COSE signing/verification has been disabled or not callback has been regenstered! ${e.message}",
                    error = isEnabled(),
                    critical = isEnabled()
                )
            }

            val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
            val keyType = sigAlg?.keyType ?: info.key.getKty().cose
            if (keyType == null) {
                return@async VerifySignatureResult(
                    keyInfo = info,
                    name = CryptoConst.COSE_LITERAL,
                    error = true,
                    message = "No signature algorithm or key type found or provided",
                    critical = true
                )
            }
            return@async platformCallback.verify1Async(input = input, keyInfo = info).await()
        }.asPromise()
    }

    override fun <KT : IKey> resolvePublicKeyAsync(keyInfo: IKeyInfo<KT>): Promise<IResolvedKeyInfo<KT>> =
        this.platformCallback.resolvePublicKeyAsync(keyInfo)
}


/**
 * Internal class that has the JS exposed service as its callback.
 *
 * The main responsibility it to convert the JS Promises into the Coroutines used in the X509Service.
 *
 * The crypto code will use this object as the actual implementation.
 * We do not want to expose its API to JS, as it is not meant to be called by external developers and
 * also the coroutines would not export nicely anyway.
 *
 */
class CoseCryptoServiceJSAdapter(val coseCallbackJS: CoseCryptoServiceJS = CoseCryptoServiceJS()) :
    AbstractCoseCryptoService<ICoseCryptoCallbackJS>(coseCallbackJS.platformCallback),
    ICoseCryptoService {


    override suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): CoseSign1Result<CborType> = coseCallbackJS.sign1<CborType>(input = input, keyInfo = keyInfo, requireX5Chain = requireX5Chain).await()


    override suspend fun verify1(
        input: CoseSign1Cbor<*>,
        keyInfo: IKeyInfo<*>?,
        requireX5Chain: Boolean
    ): IVerifySignatureResult<ICoseKeyCbor> = coseCallbackJS.verify1(input = input, keyInfo = keyInfo, requireX5Chain = true).await()

    override suspend fun <KeyType : IKey> resolvePublicKey(keyInfo: IKeyInfo<KeyType>): IResolvedKeyInfo<KeyType> = coseCallbackJS.resolvePublicKeyAsync(keyInfo).await()


    override suspend fun resolvePublicCborKey(keyInfo: IKeyInfo<*>): IResolvedKeyInfo<ICoseKeyCbor> {
        val resolvedKeyInfo = resolvePublicKey(keyInfo)
        return CoseJoseKeyMappingService.toResolvedCoseKeyInfo(resolvedKeyInfo)
    }

    override fun platform(): ICoseCryptoCallbackJS {
        return coseCallbackJS.platformCallback
    }

}

/**
 * Class used by Kotlin code. Wraps a supplied platform callback in the Adapter, which in turn delegates to the JS implementation
 */
@JsExport.Ignore
actual fun coseCryptoService(platformCallback: ICoseCryptoCallbackMarkerType): ICoseCryptoService {

    val jsPlatformCallback = platformCallback.unsafeCast<ICoseCryptoCallbackJS>()
    if (jsPlatformCallback === undefined) {
        throw IllegalArgumentException("Invalid platform callback supplied: Needs to be of type ICoseCryptoCallbackJS, but is of type ${platformCallback::class.simpleName} instead")
    }
    DefaultCallbacks.setCoseCryptoDefault(jsPlatformCallback)

    val adapter = CoseCryptoServiceJSAdapter(CoseCryptoServiceJS(jsPlatformCallback))
    return adapter
}

@JsExport
actual external interface ICoseCryptoCallbackMarkerType
