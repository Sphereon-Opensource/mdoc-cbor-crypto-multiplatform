package com.sphereon.crypto

import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.KeyOperationsMapping
import com.sphereon.crypto.generic.KeyTypeMapping

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
    actual fun getAlgMapping(): SignatureAlgorithm?
    actual fun getKtyMapping(): KeyTypeMapping
    actual fun getKeyOperationsMapping(): Array<KeyOperationsMapping>?
    actual fun getX5cArray(): Array<String>?

}



actual interface IKeyInfo<out KeyType : IKey> {
    actual val kid: String?
    actual val key: KeyType?
    actual val opts: Map<*, *>?
    actual val signatureAlgorithm: SignatureAlgorithm?
}

actual interface IResolvedKeyInfo<out KeyType : IKey>: IKeyInfo<KeyType> {
    actual override val key: KeyType
}
