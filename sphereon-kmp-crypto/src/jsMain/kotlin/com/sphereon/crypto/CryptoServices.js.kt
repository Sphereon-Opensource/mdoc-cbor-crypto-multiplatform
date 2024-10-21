package com.sphereon.crypto


@JsExport
object CryptoServicesJS {
    fun x509(platformCallback: IX509ServiceJS  = DefaultCallbacks.x509(), trustedCerts: Set<String>? = null) = X509ServiceJS(platformCallback, trustedCerts)
    fun cose(platformCallback: ICoseCryptoCallbackJS = DefaultCallbacks.coseCrypto()) = CoseCryptoServiceJS(platformCallback)
    fun mappings() = CoseJoseKeyMappingService
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
    fun disable(): ICallbackServiceJS<PlatformCallbackType>

    /**
     * Enable the callback verification (default)
     */
    fun enable(): ICallbackServiceJS<PlatformCallbackType>


    /**
     * Is the service enabled or not
     */
    fun isEnabled(): Boolean

    fun platform(): PlatformCallbackType
}
