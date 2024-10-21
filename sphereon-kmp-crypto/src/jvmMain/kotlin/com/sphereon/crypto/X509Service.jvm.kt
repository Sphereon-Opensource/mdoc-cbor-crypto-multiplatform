package com.sphereon.crypto

actual fun <PlatformCallback: IX509ServiceMarkerType> x509Service(
    platformCallback: PlatformCallback,
    trustedCerts: Set<String>?
): IX509ServiceUsingCallbacks<PlatformCallback> {
    TODO("Not implemented yet")
}

actual interface IX509ServiceMarkerType
