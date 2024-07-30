package com.sphereon.crypto

import kotlin.jvm.JvmStatic

/**
 * The main interface used for the platform specific callback. Has to be implemented by external developers.
 *
 * Not exported to JS as it has a similar interface exported using Promises instead of coroutines
 */
interface X509Service {
    suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedPEM: Array<String>
    ): VerifyResult
}

/**
 * The main entry point for X509 Certificate validation, delegating to a platform specific callback implemented by external developers
 */
interface X509CallbackService : CallbackService<X509Service>, X509Service

// The JSExport is on the actual JS impl which has an adaptor to Promises
expect fun x509Service(): X509CallbackService


/**
 * The X509 Service object that can be used to register the actual callback. It is not available for JS,
 * which has its own adapted version supporting Promises. Actual implementations can use this object or provide their own
 */
object X509ServiceObject : X509CallbackService {
    @JvmStatic
    private lateinit var platformCallback: X509Service

    private var disabled = false


    override fun isEnabled(): Boolean {
        return !this.disabled
    }

    override fun disable(): X509Service {
        this.disabled = true
        return this
    }

    override fun enable(): X509Service {
        this.disabled = false
        return this
    }

    override fun register(platformCallback: X509Service): X509CallbackService {
        this.platformCallback = platformCallback
        return this
    }

    override suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): VerifyResult {
        if (!this.isEnabled()) {
            return VerifyResult(
                name = "x509",
                message = "X509 verification has been disabled",
                error = false,
                critical = false
            )

        }
        if (!X509ServiceObject::platformCallback.isInitialized) {
            throw IllegalStateException("X509Callbacks have not been initialized. Please register your X509Service implementation, or register a default implementation")
        }
        return platformCallback.verifyCertificateChain(chainDER, chainPEM, trustedPEM)
    }
}
