package com.sphereon.crypto

actual interface IX509VerificationResult<out KeyType : IKey> : IVerifyResult {
    actual val publicKey: KeyType?
    actual val publicKeyAlgorithm: String?
    actual val publicKeyParams: Any?
}
