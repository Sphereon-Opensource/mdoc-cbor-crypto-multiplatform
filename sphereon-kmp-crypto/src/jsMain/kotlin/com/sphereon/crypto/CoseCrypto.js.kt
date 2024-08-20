package com.sphereon.crypto

import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1InputCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * A version that resembles the internal CoseCrypto interface, but then using promises instead of coroutines to make it fit the JS world
 */
@JsExport
external interface ICoseCryptoCallbackJS {
    @JsName("sign1")
    fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): Promise<CoseSign1Cbor<CborType>>

    @JsName("verify1")
    fun <CborType> verify1(
        input: CoseSign1Cbor<CborType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): Promise<IVerifySignatureResult<ICoseKeyCbor>>
}

/**
 * You can register your own COSE JS implementation with this object using the register function
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
object CoseCryptoServiceJS : ICallbackServiceJS<ICoseCryptoCallbackJS>, ICoseCryptoCallbackJS {
    private lateinit var platformCallback: ICoseCryptoCallbackJS
    private var disabled = false

    override fun disable(): CoseCryptoServiceJS {
        this.disabled = true
        return this
    }

    override fun enable(): CoseCryptoServiceJS {
        this.disabled = false
        return this
    }

    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun register(platformCallback: ICoseCryptoCallbackJS): CoseCryptoServiceJS {
        this.platformCallback = platformCallback
        return this
    }

    override fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): Promise<CoseSign1Cbor<CborType>> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("COSE sign1 (JS) has been disabled")
            throw IllegalStateException("COSE service is disabled; cannot sign")
        } else if (!this::platformCallback.isInitialized) {
            // TODO: Probably good to provide an option to the logger whether it should do log-throws
            CryptoConst.LOG.error(
                "COSE callback (JS) is not registered"
            ) // Yes this is logs-exception antipattern, but we are a lib with no knowledge about platform integration
            throw IllegalStateException("COSE have not been initialized. Please register your CoseCallbacksJS implementation, or register a default implementation")
        }
        // Let's do some validations, so the platform callback can be sure there is a signature and key alg available
        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyAlg = sigAlg?.keyType
            ?: if (keyInfo?.key?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.key?.alg?.value?.toInt() } else null
        if (keyAlg == null) {
            throw IllegalStateException("No Key algorithm found or provided")
        }
        return this.platformCallback.sign1(input, keyInfo)
    }

    override fun <CborType> verify1(
        input: CoseSign1Cbor<CborType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): Promise<IVerifySignatureResult<ICoseKeyCbor>> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("COSE service (JS) has been disabled")
            return Promise.resolve(
                VerifySignatureResult(
                    keyInfo = keyInfo,
                    name = CryptoConst.COSE_LITERAL,
                    message = "COSE signature creation/verification has been disabled",
                    error = false,
                    critical = false
                )
            )
        } else if (!this::platformCallback.isInitialized) {
            // TODO: Probably good to provide an option to the logger whether it should do log-throws
            CryptoConst.LOG.error(
                "COSE callback (JS) is not registered"
            ) // Yes this is logs-exception antipattern, but we are a lib with no knowledge about platform integration
            throw IllegalStateException("COSE have not been initialized. Please register your CoseCallbacksJS implementation, or register a default implementation")
        }
        return this.platformCallback.verify1(input, keyInfo)
    }
}


/**
 * Internal object that has the JS exposed service as its callback.
 *
 * The main responsibility it to convert the JS Promises into the Coroutines used in the X509Service.
 *
 * The crypto code will use this object as the actual implementation.
 * We do not want to expose its API to JS, as it is not meant to be called by external developers and
 * also the coroutines would not export nicely anyway.
 *
 */
open class CoseCryptoServiceJSAdapter(val coseCallbackJS: CoseCryptoServiceJS = CoseCryptoServiceJS) :
    CoseCryptoCallbackService {

    override fun disable(): ICoseCryptoService {
        this.coseCallbackJS.disable()
        return this
    }

    override fun enable(): ICoseCryptoService {
        this.coseCallbackJS.enable()
        return this
    }

    override fun isEnabled(): Boolean {
        return this.coseCallbackJS.isEnabled()
    }

    override fun register(platformCallback: ICoseCryptoService): CoseCryptoCallbackService {
        throw Error("Register function should not be used on the adapter. It depends on the Javascript coseCryptoService object")
    }

    override suspend fun <CborType> sign1(
        input: CoseSign1InputCbor,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): CoseSign1Cbor<CborType> {
        CryptoConst.LOG.debug("Creating COSE_Sign1 signature...")

        return try {
            coseCallbackJS.sign1<CborType>(input = input, keyInfo = keyInfo).await()
        } catch (e: Exception) {
            throw e
        }.also {
            CryptoConst.LOG.info("Create COSE_Sign1 signature result: $it")
        }
    }


    override suspend fun <CborType> verify1(
        input: CoseSign1Cbor<CborType>,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): IVerifySignatureResult<ICoseKeyCbor> {
        CryptoConst.LOG.debug("Verifying COSE_Sign1 signature...")
        return try {
            coseCallbackJS.verify1(input = input, keyInfo = keyInfo).await()
        } catch (e: Exception) {
            CryptoConst.LOG.error(e.message ?: "COSE_Sign1 signature verification failed", e)
            VerifySignatureResult(
                keyInfo = keyInfo,
                name = CryptoConst.COSE_LITERAL,
                error = true,
                message = "COSE_Sign1 signature verification failed ${e.message}",
                critical = true
            )
        }.also {
            CryptoConst.LOG.info("COSE_Sign1 signature result: $it")
        }
    }
}

object CoseCryptoServiceJSAdapterObject : CoseCryptoServiceJSAdapter(CoseCryptoServiceJS)

/**
 * The actual implementation is using the internal class above, which is hidden from external developers
 */

actual fun coseService(): CoseCryptoCallbackService = CoseCryptoServiceJSAdapterObject
