package com.sphereon.crypto

import kotlinx.serialization.json.JsonObject

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

}
