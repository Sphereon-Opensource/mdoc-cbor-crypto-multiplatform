package com.sphereon.crypto

actual interface IKey {
    actual val kty: Any
    actual val kid: Any?
    actual val alg: Any?
    actual val key_ops: Any?
    actual val crv: Any?
    actual val x: Any?
    actual val y: Any?
    actual val d: Any?
    actual val additional: Any?


    // Mappings to help implementers easily get values in their poison of choice (COSE/JWA) no matter the key type
    actual fun getAlgMapping(): AlgorithmMapping?
    actual fun getKtyMapping(): KeyTypeMapping
    actual fun getKeyOperationsMapping(): Array<KeyOperationsMapping>?
    actual fun getX5cArray(): Array<String>?

}
