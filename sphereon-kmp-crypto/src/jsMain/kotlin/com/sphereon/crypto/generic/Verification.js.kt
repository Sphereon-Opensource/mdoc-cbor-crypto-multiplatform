package com.sphereon.crypto.generic

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo

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
actual external interface IVerifyResults<out KeyType : IKey> {
    @JsName("error")
    actual val error: Boolean
    @JsName("verifications")
    actual val verifications: Array<out IVerifyResult>
    @JsName("keyInfo")
    actual val keyInfo: IKeyInfo<KeyType>?
}
