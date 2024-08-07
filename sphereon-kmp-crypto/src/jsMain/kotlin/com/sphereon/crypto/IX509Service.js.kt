package com.sphereon.crypto

import com.sphereon.cbor.cose.IKey

@JsExport
actual external interface IX509VerificationResult<out KeyType : IKey> : IVerifyResult {
    actual val publicKey: KeyType?
    actual val publicKeyAlgorithm: String?
    actual val publicKeyParams: Any?
}
