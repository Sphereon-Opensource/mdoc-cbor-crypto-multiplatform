package com.sphereon.crypto.jose

import com.sphereon.crypto.IKey
import kotlinx.serialization.SerialName

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
@JsExport
actual external interface IJwk: IKey {
    actual override val alg: JwaAlgorithm?
    actual override val crv: JwaCurve?
    actual override val d: String?
    @JsName("e")
    actual val e: String?
    @JsName("k")
    actual val k: String?
    actual override val key_ops: Array<JoseKeyOperations>?
    actual override val kid: String?
    actual override val kty: JwaKeyType
    @JsName("n")
    actual val n: String?
    @JsName("use")
    actual val use: String?
    actual override val x: String?
    @JsName("x5c")
    actual val x5c: Array<String>?
    @JsName("x5t")
    actual val x5t: String?
    @JsName("x5u")
    actual val x5u: String?

    @JsName("x5t_S256")
    @SerialName("x5t#S256")
    actual val x5t_S256: String?
    actual override val y: String?

}

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
actual external interface IJwkJson: IKey {
    actual override val alg: String?
    actual override val crv: String?
    actual override val d: String?
    @JsName("e")
    actual val e: String?
    @JsName("k")
    actual val k: String?
    actual override val key_ops: Array<String>?
    actual override val kid: String?
    actual override val kty: String
    @JsName("n")
    actual val n: String?
    @JsName("use")
    actual val use: String?
    actual override val x: String?
    @JsName("x5c")
    actual val x5c: Array<String>?
    @JsName("x5t")
    actual val x5t: String?
    @JsName("x5u")
    actual val x5u: String?

    @JsName("x5t_S256")
    @SerialName("x5t#S256")
    actual val x5t_S256: String?
    actual override val y: String?

}
