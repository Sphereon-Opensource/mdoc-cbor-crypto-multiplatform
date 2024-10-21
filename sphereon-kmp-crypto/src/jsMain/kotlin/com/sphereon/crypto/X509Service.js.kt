package com.sphereon.crypto

import kotlinx.coroutines.await
import kotlin.js.Promise


/**
 * A version that resembles the internal X509Callbacks interface, but then using promises instead of coroutines to make it fit the JS world
 */
@JsExport
external interface IX509ServiceJS: IX509ServiceMarkerType {
    @JsName("verifyCertificateChainJS")
    fun <KeyType : IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile?,
    ): Promise<IX509VerificationResult<KeyType>>

    /**
     * A function returning trusted Certificates in PEM format. Most functions use this as a default in case trusted certificates are not passed in
     */
    fun getTrustedCerts(): Array<String>?
}

interface IX509ServiceWithCallbacksJS : ICallbackServiceJS<IX509ServiceJS>, IX509ServiceJS

/**
 * You can register your own X.509 JS implementation with this class
 */
@JsExport
class X509ServiceJS(val platformCallback: IX509ServiceJS  = DefaultCallbacks.x509(), private var trustedCerts: Set<String>? = null) : IX509ServiceWithCallbacksJS {
    init {
        if (platformCallback === this) {
            throw IllegalArgumentException("Platform callback cannot be myself. Platform callbacks share the same interface is the main x509Service class, but really should implement their own logic and be passed to the X509Service class")
        }
    }

    fun setTrustedCerts(trustedCerts: Array<String>?) {
        this.trustedCerts = trustedCerts?.toSet()
    }

    private var disabled = false

    override fun disable() = apply {
        this.disabled = true
    }

    override fun enable() = apply {
        this.disabled = false
    }

    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun platform(): IX509ServiceJS {
        return this.platformCallback
    }


    override fun <KeyType : IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile?
    ): Promise<IX509VerificationResult<KeyType>> {
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
        }

        return this.platformCallback.verifyCertificateChain(
            chainDER,
            chainPEM,
            trustedCerts = trustedCerts ?: this.getTrustedCerts(),
            verificationProfile
        )
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
internal class X509ServiceJSAdapter(private val x509ServiceJS: X509ServiceJS = X509ServiceJS(), trustedCerts: Array<String>? = null) : IX509ServiceUsingCallbacks<IX509ServiceJS> {

    init {
        if (trustedCerts != null) {
            x509ServiceJS.setTrustedCerts(trustedCerts)
        }
    }


    fun setTrustedCerts(trustedCerts: Array<String>?) {
        x509ServiceJS.setTrustedCerts(trustedCerts)
    }

    override fun disable() = apply {
        x509ServiceJS.disable()
    }

    override fun enable() = apply { x509ServiceJS.enable() }

    override fun isEnabled() = x509ServiceJS.isEnabled()

    override fun platform() = x509ServiceJS.platformCallback

    override fun getTrustedCerts(): Array<String>? {
        return x509ServiceJS.getTrustedCerts()
    }

    override suspend fun <KeyType : IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile,
    ): IX509VerificationResult<KeyType> {
        CryptoConst.LOG.debug("Verifying certificate chain...")
        if (chainDER == null && chainPEM == null) {
            return X509VerificationResult(
                name = CryptoConst.X509_LITERAL,
                error = true,
                message = "Please provide either a chain in DER format or PEM format",
                critical = true
            )
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
            x509ServiceJS.verifyCertificateChain<KeyType>(chainDER, chainPEM, assertedCerts, verificationProfile)
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

actual fun <PlatformCallback: IX509ServiceMarkerType> x509Service(platformCallback: PlatformCallback, trustedCerts: Set<String>?): IX509ServiceUsingCallbacks<PlatformCallback> {
    val jsPlatformCallback = platformCallback.unsafeCast<IX509ServiceJS>()
    if (jsPlatformCallback === undefined) {
        throw IllegalArgumentException("Invalid platform callback supplied: Needs to be of type IX509ServiceJS, but is of type ${platformCallback.toString()} instead")
    }
    return X509ServiceJSAdapter(X509ServiceJS(jsPlatformCallback)).unsafeCast<IX509ServiceUsingCallbacks<PlatformCallback>>()
}

@JsExport
actual external interface IX509ServiceMarkerType
