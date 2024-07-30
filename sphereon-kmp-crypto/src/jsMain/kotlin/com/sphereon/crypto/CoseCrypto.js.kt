package com.sphereon.crypto

import com.sphereon.cbor.cose.CoseKeyCbor
import com.sphereon.cbor.cose.CoseKeyType
import com.sphereon.cbor.cose.CoseSign1Cbor
import com.sphereon.cbor.cose.CoseSign1InputCbor
import kotlinx.coroutines.await
import kotlin.js.Promise

/**
 * A version that resembles the internal CoseCrypto interface, but then using promises instead of coroutines to make it fit the JS world
 */
@JsExport
interface CoseCryptoCallbackJS {
    fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>? = null
    ): Promise<CoseSign1Cbor<CborType, JsonType>>

    fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>? = null
    ): Promise<VerifyResults<CoseKeyCbor>>
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
object CoseCryptoServiceJS : CallbackService<CoseCryptoCallbackJS>, CoseCryptoCallbackJS {
    private lateinit var platformCallback: CoseCryptoCallbackJS
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

    override fun register(platformCallback: CoseCryptoCallbackJS): CoseCryptoServiceJS {
        this.platformCallback = platformCallback
        return this
    }

    override fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>?
    ): Promise<CoseSign1Cbor<CborType, JsonType>> {
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
            ?: if (keyInfo?.key?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.key.alg?.value?.toInt() } else null
        if (keyAlg == null) {
            throw IllegalStateException("No Key algorithm found or provided")
        }
        return this.platformCallback.sign1(input, keyInfo)
    }

    override fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>?
    ): Promise<VerifyResults<CoseKeyCbor>> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("COSE service (JS) has been disabled")
            return Promise.resolve(
                VerifyResults(
                    error = false, keyInfo = keyInfo, verifications = arrayOf(
                        VerifyResult(
                            name = CryptoConst.COSE_LITERAL,
                            message = "COSE signature creation/verification has been disabled",
                            error = false,
                            critical = false
                        )
                    )
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
internal object CoseCryptoServiceJSAdapter : CoseCryptoCallbackService {
    private val x509CallbackJS = CoseCryptoServiceJS

    override fun disable(): CoseCryptoService {
        this.x509CallbackJS.disable()
        return this
    }

    override fun enable(): CoseCryptoService {
        this.x509CallbackJS.enable()
        return this
    }

    override fun isEnabled(): Boolean {
        return this.x509CallbackJS.isEnabled()
    }

    override fun register(platformCallback: CoseCryptoService): CoseCryptoCallbackService {
        throw Error("Register function should not be used on the adapter. It depends on the Javascript coseCryptoService object")
    }

    override suspend fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>?
    ): CoseSign1Cbor<CborType, JsonType> {
        CryptoConst.LOG.debug("Creating COSE_Sign1 signature...")

        return try {
            x509CallbackJS.sign1(input = input, keyInfo = keyInfo).await()
        } catch (e: Exception) {
            throw e
        }.also {
            CryptoConst.LOG.info("Create COSE_Sign1 signature result: $it")
        }
    }


    override suspend fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: KeyInfo<CoseKeyCbor>?
    ): VerifyResults<CoseKeyCbor> {
        CryptoConst.LOG.debug("Verifying COSE_Sign1 signature...")
        return try {
            x509CallbackJS.verify1(input = input, keyInfo = keyInfo).await()
        } catch (e: Exception) {
            CryptoConst.LOG.error(e.message ?: "COSE_Sign1 signature verification failed", e)
            VerifyResults(
                keyInfo = keyInfo, error = true, verifications = arrayOf(
                    VerifyResult(
                        name = CryptoConst.COSE_LITERAL,
                        error = true,
                        message = "COSE_Sign1 signature verification failed ${e.message}",
                        critical = true
                    )
                )
            )
        }.also {
            CryptoConst.LOG.info("COSE_Sign1 signature result: $it")
        }
    }
}

/**
 * The actual implementation is using the internal object above, which is hidden from external developers
 */
actual fun coseService(): CoseCryptoCallbackService = CoseCryptoServiceJSAdapter
