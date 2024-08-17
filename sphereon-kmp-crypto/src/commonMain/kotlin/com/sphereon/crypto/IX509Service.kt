package com.sphereon.crypto

import com.sphereon.crypto.cose.IKey
import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

@JsExport
enum class X509VerificationProfile {
    ISO_18013_5,
    RFC_5280
}


/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
interface IX509Service {
    fun getTrustedCerts(): Array<String>?
    suspend fun <KeyType: IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedCerts: Array<String>? = getTrustedCerts(),
        verificationProfile: X509VerificationProfile = X509VerificationProfile.RFC_5280
    ): IX509VerificationResult<KeyType>
}

expect interface IX509VerificationResult<out KeyType: IKey>: IVerifyResult {
    val publicKey: KeyType?
    val publicKeyAlgorithm: String?
    val publicKeyParams: Any?
}

@JsExport
class X509VerificationResult<KeyType: IKey>(
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
interface X509CallbackService : ICallbackService<IX509Service>, IX509Service

// The JSExport is on the actual JS impl which has an adaptor to Promises
expect fun x509Service(): X509CallbackService


/**
 * The X509 Service object that can be used to register the actual callback. It is not available for JS,
 * which has its own adapted version supporting Promises. Actual implementations can use this object or provide their own
 */
object X509ServiceObject : X509CallbackService {
    @JvmStatic
    private lateinit var platformCallback: IX509Service

    private var disabled = false
    private var trustedCerts: Set<String>? = null


    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun disable(): IX509Service {
        this.disabled = true
        return this
    }

    override fun enable(): IX509Service {
        this.disabled = false
        return this
    }

    override fun register(platformCallback: IX509Service): X509CallbackService {
        this.platformCallback = platformCallback
        return this
    }

    override fun getTrustedCerts(): Array<String>? {
        return this.trustedCerts?.toTypedArray()
    }

    override suspend fun <KeyType: IKey> verifyCertificateChain(
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
        if (!X509ServiceObject::platformCallback.isInitialized) {
            throw IllegalStateException("X509Callbacks have not been initialized. Please register your X509Service implementation, or register a default implementation")
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
