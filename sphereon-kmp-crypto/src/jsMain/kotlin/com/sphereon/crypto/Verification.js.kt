package com.sphereon.crypto

import com.sphereon.cbor.cose.IKey

@JsExport
actual external interface IVerifyResult {
    actual val name: String
    actual val error: Boolean
    actual val message: String?
    actual val critical: Boolean
}

@JsExport
actual external interface IVerifySignatureResult<out KeyType : IKey> : IVerifyResult {
    actual val keyInfo: IKeyInfo<KeyType>?
}

@JsExport
actual external interface IKeyInfo<out KeyType : IKey> {
    actual val kid: String?
    actual val key: KeyType?
    actual val opts: Map<*, *>?

}

@JsExport
actual external interface IVerifyResults<out KeyType : IKey> {
    actual val error: Boolean
    actual val verifications: Array<IVerifyResult>
    actual val keyInfo: IKeyInfo<KeyType>?
}
