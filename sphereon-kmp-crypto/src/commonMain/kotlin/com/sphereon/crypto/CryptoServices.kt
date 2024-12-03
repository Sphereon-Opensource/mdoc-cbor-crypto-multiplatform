package com.sphereon.crypto

import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * The main object used by code to be calling into the platform specific callbacks for X509 Certificates and signature creation/verification
 *
 * Non Kotlin code could still extend their implementations.
 * This object is available directly as well except for JS, which has to use its actual implementations this service depends on.
 * Any internal code calling into the JS implementation will automatically wrap that implementation
 */
@JsExport.Ignore
object CryptoServices {
    fun x509(platformCallback: IX509ServiceMarkerType = DefaultCallbacks.x509(), trustedCerts: Set<String>? = null) =
        x509Service(platformCallback, trustedCerts)

    fun cose(platformCallback: ICoseCryptoCallbackService = DefaultCallbacks.coseCrypto()) = coseCryptoService(platformCallback)
    fun mappings() = CoseJoseKeyMappingService


    // TODO: JOSE
}

@JsExport
object DefaultCallbacks {
    private var x509Callback: IX509ServiceMarkerType? = null
    private var coseCryptoCallback: ICoseCryptoCallbackMarkerType? = null

    fun <X509CallbackType : IX509ServiceMarkerType> x509(): X509CallbackType {
        checkNotNull(x509Callback) { "No default X509 Platform Callback implementation was registered" }
        return x509Callback as X509CallbackType
    }

    fun setX509Default(x509Callback: IX509ServiceMarkerType?) {
        this.x509Callback = x509Callback
    }

    fun hasX509Default(): Boolean {
        return this.x509Callback != null
    }

    fun <CoseCryptoCallbackType : ICoseCryptoCallbackMarkerType> coseCrypto(): CoseCryptoCallbackType {
        checkNotNull(coseCryptoCallback) { "No default Cose Crypto Platform Callback implementation was registered" }
        return coseCryptoCallback as CoseCryptoCallbackType
    }

    fun hasCoseCryptoDefault(): Boolean {
        return this.coseCryptoCallback != null
    }

    fun setCoseCryptoDefault(coseCryptoCallback: ICoseCryptoCallbackMarkerType?) {
        this.coseCryptoCallback = coseCryptoCallback
    }
}

/**
 * The main entry point for platform validation, delegating to a platform specific callback implemented by external developers
 */
@JsExport
interface ICallbackService<PlatformCallbackType> {
    /**
     * Disable callback verification (be careful!)
     */
    @JsName("disable")
    fun disable(): ICallbackService<PlatformCallbackType>

    /**
     * Enable the callback verification (default)
     */
    @JsName("enable")
    fun enable(): ICallbackService<PlatformCallbackType>


    /**
     * Is the service enabled or not
     */
    @JsName("isEnabled")
    fun isEnabled(): Boolean


    @JsName("platform")
    fun platform(): PlatformCallbackType
}
