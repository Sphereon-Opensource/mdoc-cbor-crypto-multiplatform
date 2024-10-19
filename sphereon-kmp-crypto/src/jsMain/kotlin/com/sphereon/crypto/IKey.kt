package com.sphereon.crypto

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
    actual fun getAlgMapping(): AlgorithmMapping?
    actual fun getKtyMapping(): KeyTypeMapping
    actual fun getKeyOperationsMapping(): Array<KeyOperationsMapping>?
    actual fun getX5cArray(): Array<String>?
}
