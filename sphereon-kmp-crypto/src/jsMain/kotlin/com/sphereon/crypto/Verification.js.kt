package com.sphereon.crypto

@JsExport
actual external interface IVerifyResult {
    @JsName("name")
    actual val name: String
    @JsName("error")
    actual val error: Boolean
    @JsName("message")
    actual val message: String?
    @JsName("critical")
    actual val critical: Boolean
}

@JsExport
actual external interface IVerifySignatureResult<out KeyType : IKey> : IVerifyResult {
    @JsName("keyInfo")
    actual val keyInfo: IKeyInfo<KeyType>?
}

@JsExport
actual external interface IKeyInfo<out KeyType : IKey> {
    @JsName("kid")
    actual val kid: String?
    @JsName("key")
    actual val key: KeyType?
    @JsName("opts")
    actual val opts: Map<*, *>?

}

@JsExport
actual external interface IVerifyResults<out KeyType : IKey> {
    @JsName("error")
    actual val error: Boolean
    @JsName("verifications")
    actual val verifications: Array<out IVerifyResult>
    @JsName("keyInfo")
    actual val keyInfo: IKeyInfo<KeyType>?
}
