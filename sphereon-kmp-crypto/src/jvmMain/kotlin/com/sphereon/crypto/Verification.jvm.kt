package com.sphereon.crypto


actual interface IVerifyResult {
    actual val name: String
    actual val error: Boolean
    actual val message: String?
    actual val critical: Boolean
}

actual interface IVerifySignatureResult<out KeyType : IKey> : IVerifyResult {
    actual val keyInfo: IKeyInfo<KeyType>?
}

actual interface IKeyInfo<out KeyType : IKey> {
    actual val kid: String?
    actual val key: KeyType?
    actual val opts: Map<*, *>?

}

actual interface IVerifyResults<out KeyType : IKey> {
    actual val error: Boolean
    actual val verifications: Array<IVerifyResult>
    actual val keyInfo: IKeyInfo<KeyType>?
}
