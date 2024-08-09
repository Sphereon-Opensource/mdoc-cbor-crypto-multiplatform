package com.sphereon.jose.jwk

import com.sphereon.jose.jwa.JwaAlgorithm
import com.sphereon.jose.jwa.JwaCurve
import com.sphereon.jose.jwa.JwaKeyType
import kotlinx.serialization.SerialName

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
actual interface IJwk {
    actual val alg: JwaAlgorithm?
    actual val crv: JwaCurve?
    actual val d: String?
    actual val e: String?
    actual val k: String?
    actual val key_ops: Set<JoseKeyOperations>?
    actual val kid: String?
    actual val kty: JwaKeyType
    actual val n: String?
    actual val use: String?
    actual val x: String?
    actual val x5c: Array<String>?
    actual val x5t: String?
    actual val x5u: String?

    @SerialName("x5t#S256")
    actual val x5t_S256: String?
    actual val y: String?

}

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
actual interface IJwkJson {
    actual val alg: String?
    actual val crv: String?
    actual val d: String?
    actual val e: String?
    actual val k: String?
    actual val key_ops: Array<String>?
    actual val kid: String?
    actual val kty: String
    actual val n: String?
    actual val use: String?
    actual val x: String?
    actual val x5c: Array<String>?
    actual val x5t: String?
    actual val x5u: String?

    @SerialName("x5t#S256")
    actual val x5t_S256: String?
    actual val y: String?

}
