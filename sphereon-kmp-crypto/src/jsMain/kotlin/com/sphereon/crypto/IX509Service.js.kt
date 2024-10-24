package com.sphereon.crypto

import com.sphereon.crypto.generic.IVerifyResult

@JsExport
actual external interface IX509VerificationResult<out KeyType : IKey> : IVerifyResult {
    actual val publicKey: KeyType?
    actual val publicKeyAlgorithm: String?
    actual val publicKeyParams: Any?
}
