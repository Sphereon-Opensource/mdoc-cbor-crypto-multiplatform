package com.sphereon.crypto

import kotlinx.coroutines.await
import kotlin.js.Promise

@JsModule("pkijs")
@JsNonModule
external object pkijs {
    class CertificateChainValidationEngine(certs: Array<Certificate>, trustedCerts: Array<Certificate>)
    class Certificate {
        companion object {
            fun fromBER(externalArgument: ByteArray = definedExternally): Certificate
        }
    }
}

@JsModule("@sphereon-internal/mdoc-js-api")
@JsNonModule
external object MdocJSApi {
    fun verifyCertificateChain(
        chainDER: Array<ByteArray>? = definedExternally,
        chainPEM: Array<String>? = definedExternally,
        trustedPEM: Array<String>
    ): Promise<VerifyResult>
}


/**
 * A version that resembles the internal X509Callbacks interface, but then using promises instead of coroutines to make it fit the JS world
 */
@JsExport
interface X509CallbacksJS {
    fun verifyCertificateChain(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedPEM: Array<String>
    ): Promise<VerifyResult>
}

/**
 * You can register your own X.509 JS implementation with this object using the register function
 */
@JsExport
object X509ServiceJS : CallbackService<X509CallbacksJS>, X509CallbacksJS {
    private lateinit var platformCallback: X509CallbacksJS
    private var disabled = false

    override fun disable(): X509CallbacksJS {
        this.disabled = true
        return this
    }

    override fun enable(): X509CallbacksJS {
        this.disabled = false
        return this
    }

    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun register(platformCallback: X509CallbacksJS): X509ServiceJS {
        this.platformCallback = platformCallback
        return this
    }

    override fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): Promise<VerifyResult> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("Verify Certificate Chain (JS) has been disabled. Returning success result")
            return Promise.resolve(
                VerifyResult(
                    name = CryptoConst.X509_LITERAL,
                    message = "X509 verification has been disabled",
                    error = false,
                    critical = false
                )
            )
        } else if (!this::platformCallback.isInitialized) {
            // TODO: Probably good to provide an option to the logger whether it should do log-throws
            CryptoConst.LOG.error(
                "X509 callback (JS) is not registered"
            ) // Yes this is logs-exception anti pattern, but we are a lib with no knowledge about platform integration
            throw IllegalStateException("X509Callbacks have not been initialized. Please register your X509CallbacksJS implementation, or register a default implementaion")
        }
        return this.platformCallback.verifyCertificateChain(chainDER, chainPEM, trustedPEM)
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
internal object X509ServiceJSAdapter : X509CallbackService {
    private val x509CallbackJS = X509ServiceJS

    override fun disable(): X509Service {
        this.x509CallbackJS.disable()
        return this
    }

    override fun enable(): X509Service {
        this.x509CallbackJS.enable()
        return this
    }

    override fun isEnabled(): Boolean {
        return this.x509CallbackJS.isEnabled()
    }

    override fun register(platformCallback: X509Service): X509CallbackService {
        throw Error("Register function should not be used on the adapter. It depends on the Javascript x509Service object")
    }

    override suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): VerifyResult {
        CryptoConst.LOG.debug("Verifying certificate chain...")
        if (chainDER == null && chainPEM == null) {
            return VerifyResult(
                name = CryptoConst.X509_LITERAL,
                error = true,
                message = "Please provide either a chain in DER format or PEM format",
                critical = true
            )
        }
        if (chainDER != null) {
            val certificate = pkijs.Certificate.fromBER(chainDER[0])
            println(certificate)
        }
        return try {
            x509CallbackJS.verifyCertificateChain(chainDER, chainPEM, trustedPEM).await()
        } catch (e: Exception) {
            CryptoConst.LOG.error(e.message ?: "X509 validation failed", e)
            VerifyResult(
                name = CryptoConst.X509_LITERAL,
                error = true,
                message = "Certificate chain verification failed ${e.message}",
                critical = true
            )
        }.also {
            CryptoConst.LOG.info("Verifying certificate chain result: $it")
        }

    }

}

/**
 * The actual implementation is using the internal object above, which is hidden from external developers
 */
actual fun x509Service(): X509CallbackService = X509ServiceJSAdapter
