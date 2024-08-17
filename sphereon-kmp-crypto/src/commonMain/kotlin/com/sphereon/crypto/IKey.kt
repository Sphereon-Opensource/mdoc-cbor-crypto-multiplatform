package com.sphereon.crypto

expect interface IKey {
    val kty: Any
    val kid: Any?
    val alg: Any?

    val key_ops: Any?

    val crv: Any?
    val x: Any?
    val y: Any?
    val d: Any?
    val additional: Any?
}
