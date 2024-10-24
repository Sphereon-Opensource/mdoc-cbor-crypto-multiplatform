package com.sphereon.crypto

import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.KeyOperationsMapping
import com.sphereon.crypto.generic.KeyTypeMapping

@JsExport
actual external interface IKey {
    @JsName("kty")
    actual val kty: Any

    @JsName("kid")
    actual val kid: Any?

    @JsName("alg")
    actual val alg: Any?

    @JsName("key_ops")
    actual val key_ops: Any?

    /*@JsName("baseIV")
    actual val baseIV: Any?
*/
    @JsName("crv")
    actual val crv: Any?

    @JsName("x")
    actual val x: Any?

    @JsName("y")
    actual val y: Any?

    /*  @JsName("x5chain") //x5c in JWK
      actual val x5chain: Any?
  */
    @JsName("additional")
    actual val additional: Any?

    @JsName("d")
    actual val d: Any?

    // Mappings to help implementers easily get values in their poison of choice (COSE/JWA) no matter the key type
    actual fun getAlgMapping(): SignatureAlgorithm?
    actual fun getKtyMapping(): KeyTypeMapping
    actual fun getKeyOperationsMapping(): Array<KeyOperationsMapping>?
    actual fun getX5cArray(): Array<String>?
}


@JsExport
actual external interface IKeyInfo<out KeyType : IKey> {
    @JsName("kid")
    actual val kid: String?

    @JsName("key")
    actual val key: KeyType?

    @JsName("opts")
    actual val opts: Map<*, *>?

    @JsName("signatureAlgorithm")
    actual val signatureAlgorithm: SignatureAlgorithm?
}

@JsExport
actual external interface IResolvedKeyInfo<out KeyType : IKey> : IKeyInfo<KeyType> {
    actual override val key: KeyType
}
