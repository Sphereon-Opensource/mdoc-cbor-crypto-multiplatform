package com.sphereon.crypto

@JsExport
object CryptoServiceJS {
    val X509 = X509ServiceObjectJS
    val COSE = CoseCryptoServiceJS
    // TODO: JOSE
}


/**
 * The main entry point for platform validation, delegating to a platform specific callback implemented by external developers
 */
@JsExport
external interface ICallbackServiceJS<PlatformCallbackType> {
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
    @JsName("register")
    fun register(platformCallback: PlatformCallbackType): ICallbackServiceJS<PlatformCallbackType>
}
