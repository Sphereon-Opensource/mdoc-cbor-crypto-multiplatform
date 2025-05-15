package com.sphereon.crypto

actual fun coseCryptoService(platformCallback: ICoseCryptoCallbackMarkerType): ICoseCryptoService {
    if (platformCallback !is ICoseCryptoCallbackService) {
        throw IllegalArgumentException("Platform callback is not of type ICoseCryptoCallbackService, but ${platformCallback.javaClass.canonicalName}")
    }
    return CoseCryptoService(platformCallback)
}

actual interface ICoseCryptoCallbackMarkerType
