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
interface X509ServiceJS {
    fun <KeyType> verifyCertificateChainJS(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile = X509VerificationProfile.RFC_5280
    ): Promise<X509VerificationResult<KeyType>>

    /**
     * A function returning trusted Certificates in PEM format. Most functions use this as a default in case trusted certificates are not passed in
     */
    fun getTrustedCerts(): Array<String>?
}

/**
 * You can register your own X.509 JS implementation with this object using the register function
 */
@JsExport
object X509ServiceObjectJS : CallbackService<X509ServiceJS>, X509ServiceJS {
    private lateinit var platformCallback: X509ServiceJS
    private var trustedCerts: Set<String>? = null
    private var disabled = false


    override fun disable(): X509ServiceJS {
        this.disabled = true
        return this
    }

    override fun enable(): X509ServiceJS {
        this.disabled = false
        return this
    }

    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun register(platformCallback: X509ServiceJS): X509ServiceObjectJS {
        this.platformCallback = platformCallback
        return this
    }

    override fun <KeyType> verifyCertificateChainJS(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile
    ): Promise<X509VerificationResult<KeyType>> {
        if (!isEnabled()) {
            CryptoConst.LOG.info("Verify Certificate Chain (JS) has been disabled. Returning success result")
            return Promise.resolve(
                X509VerificationResult(
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
        return this.platformCallback.verifyCertificateChainJS(
            chainDER,
            chainPEM,
            trustedCerts = trustedCerts ?: this.getTrustedCerts(),
            verificationProfile
        )
    }

    fun setTrustedCerts(trustedCerts: Array<String>? = null) {
        this.trustedCerts = trustedCerts?.toSet()
    }

    override fun getTrustedCerts(): Array<String>? {
        return this.trustedCerts?.toTypedArray()
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
    private val x509CallbackJS = X509ServiceObjectJS
    private var trustedCerts: Set<String>? = null

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

    fun setTrustedCerts(trustedCerts: Array<String>?) {
        this.trustedCerts = trustedCerts?.toSet()
    }

    override fun getTrustedCerts(): Array<String>? {
        return this.trustedCerts?.toTypedArray()
    }

    override suspend fun <KeyType> verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile
    ): X509VerificationResult<KeyType> {
        CryptoConst.LOG.debug("Verifying certificate chain...")
        if (chainDER == null && chainPEM == null) {
            return X509VerificationResult(
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
        val assertedCerts = trustedCerts ?: this.getTrustedCerts()
        if (assertedCerts.isNullOrEmpty()) {
            return X509VerificationResult(
                error = true,
                message = "No trusted certificates have been provided.",
                critical = true,
                name = CryptoConst.X509_LITERAL
            )
        }

        return try {
            x509CallbackJS.verifyCertificateChainJS<KeyType>(chainDER, chainPEM, assertedCerts, verificationProfile)
                .await()
        } catch (e: Exception) {
            CryptoConst.LOG.error(e.message ?: "X509 validation failed", e)
            X509VerificationResult(
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
