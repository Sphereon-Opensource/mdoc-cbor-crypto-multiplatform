package com.sphereon.crypto



/**
 * CryptoServicesJS provides cryptographic services including X.509, COSE, and key mappings
 * with JavaScript callbacks to fit the JS ecosystem.
 *
 * This is the central entry point for external code to perform cose, X.509 actions
 */
@JsExport
@JsName("CryptoServices")
object CryptoServicesJS {
    // The Javascript version exposes it with JS callbacks compared to the default CryptoServices
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
