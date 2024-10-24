package com.sphereon.crypto.generic

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo


actual interface IVerifyResult {
    actual val name: String
    actual val error: Boolean
    actual val message: String?
    actual val critical: Boolean
}

actual interface IVerifySignatureResult<out KeyType : IKey> : IVerifyResult {
    actual val keyInfo: IKeyInfo<KeyType>?
}

actual interface IVerifyResults<out KeyType : IKey> {
    actual val error: Boolean
    actual val verifications: Array<out IVerifyResult>
    actual val keyInfo: IKeyInfo<KeyType>?
}
