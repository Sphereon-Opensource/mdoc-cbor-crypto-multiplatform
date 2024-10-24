package com.sphereon.crypto

import com.sphereon.crypto.generic.IVerifyResult
import com.sphereon.crypto.generic.VerifyResult
import kotlin.js.JsExport

@JsExport
enum class X509VerificationProfile {
    ISO_18013_5,
    RFC_5280
}

expect interface IX509ServiceMarkerType
/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
interface IX509Service: IX509ServiceMarkerType {
    fun getTrustedCerts(): Array<String>?
    suspend fun <KeyType : IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedCerts: Array<String>? = getTrustedCerts(),
        verificationProfile: X509VerificationProfile = X509VerificationProfile.RFC_5280,
    ): IX509VerificationResult<KeyType>
}

expect interface IX509VerificationResult<out KeyType : IKey> : IVerifyResult {
    val publicKey: KeyType?
    val publicKeyAlgorithm: String?
    val publicKeyParams: Any?
}

@JsExport
class X509VerificationResult<KeyType : IKey>(
    override val publicKey: KeyType? = null,
    override val publicKeyAlgorithm: String? = null,
    override val publicKeyParams: Any? = null,
    name: String = CryptoConst.X509_LITERAL,
    critical: Boolean,
    message: String?,
    error: Boolean
) : VerifyResult(name = name, critical = critical, message = message, error = error), IX509VerificationResult<KeyType> {

}

/**
 * The main entry point for X509 Certificate validation, delegating to a platform specific callback implemented by external developers
 */
interface IX509ServiceUsingCallbacks<CallbackServiceType> : ICallbackService<CallbackServiceType>, IX509Service

// The JSExport is on the actual JS impl which has an adaptor to Promises
expect fun <PlatformCallback: IX509ServiceMarkerType> x509Service(platformCallback: PlatformCallback = DefaultCallbacks.x509(), trustedCerts: Set<String>? = null): IX509ServiceUsingCallbacks<PlatformCallback>


/**
 * The X509 Service object that can be used to register the actual callback. It is not available for JS,
 * which has its own adapted version supporting Promises. Actual implementations can use this object or provide their own
 */
class X509Service(val platformCallback: IX509Service  = DefaultCallbacks.x509(), private var trustedCerts: Set<String>? = null) : IX509ServiceUsingCallbacks<IX509Service> {
    init {
        if (platformCallback === this) {
            throw IllegalArgumentException("Platform callback cannot be myself. Platform callbacks share the same interface is the main x509Service class, but really should implement their own logic and be passed to the X509Service class")
        }
    }

    private var disabled = false


    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun platform(): IX509Service {
        return this.platformCallback
    }

    override fun disable() = apply {
        this.disabled = true
    }

    override fun enable() = apply {
        this.disabled = false
    }


    override fun getTrustedCerts(): Array<String>? {
        return this.trustedCerts?.toTypedArray()
    }

    override suspend fun <KeyType : IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile
    ): IX509VerificationResult<KeyType> {
        if (!this.isEnabled()) {
            return X509VerificationResult<KeyType>(
                name = "x509",
                message = "X509 verification has been disabled",
                error = false,
                critical = false
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
        return platformCallback.verifyCertificateChain(chainDER, chainPEM, trustedCerts = assertedCerts, verificationProfile)
    }
}
