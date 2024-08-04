package com.sphereon.crypto

import kotlin.js.JsExport

/**
 * The main object used by code to be calling into the platform specific callbacks for X509 Certificates and signature creation/verification
 *
 * Non Kotlin code could still extend their implementations. This object is available directly as well except for JS, which has to use its actual implementations this service depends on
 */

object CryptoService {
    val X509 = x509Service()
    val COSE = coseService()
    // TODO: JOSE
}


/**
 * The main entry point for platform validation, delegating to a platform specific callback implemented by external developers
 */
@JsExport
interface CallbackService<PlatformCallbackType> {
    /**
     * Disable callback verification (be careful!)
     */
    fun disable(): PlatformCallbackType

    /**
     * Enable the callback verification (default)
     */
    fun enable(): PlatformCallbackType


    /**
     * Is the service enabled or not
     */
    fun isEnabled(): Boolean

    /**
     * Register the platform specific callback that implements the verification
     *
     * External developers use this as an entry point for their platform code
     */
    fun register(platformCallback: PlatformCallbackType): CallbackService<PlatformCallbackType>
}
