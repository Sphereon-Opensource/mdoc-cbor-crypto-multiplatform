package com.sphereon.crypto

import com.sphereon.crypto.generic.IVerifyResult

actual fun <PlatformCallback: IX509ServiceMarkerType> x509Service(
    platformCallback: PlatformCallback,
    trustedCerts: Set<String>?
): IX509ServiceUsingCallbacks<PlatformCallback> {
    TODO("Not implemented yet")
}

actual interface IX509ServiceMarkerType

actual interface IX509VerificationResult<out KeyType : IKey> : IVerifyResult {
    actual val publicKey: KeyType?
    actual val publicKeyAlgorithm: String?
    actual val publicKeyParams: Any?
}
