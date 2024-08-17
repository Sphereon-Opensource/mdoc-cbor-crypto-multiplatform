package com.sphereon.crypto.jose

import com.sphereon.crypto.IKey
import kotlinx.serialization.SerialName

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
actual interface IJwk: IKey {
    actual override val alg: JwaAlgorithm?
    actual override val crv: JwaCurve?
    actual override val d: String?
    actual val e: String?
    actual val k: String?
    actual override val key_ops: Set<JoseKeyOperations>?
    actual override val kid: String?
    actual override val kty: JwaKeyType
    actual val n: String?
    actual val use: String?
    actual override val x: String?
    actual val x5c: Array<String>?
    actual val x5t: String?
    actual val x5u: String?

    @SerialName("x5t#S256")
    actual val x5t_S256: String?
    actual override val y: String?

}

/**
 * JWK [RFC 7517](https://datatracker.ietf.org/doc/html/rfc7517#section-4).
 *
 * ordered alphabetically [RFC7638 s3](https://www.rfc-editor.org/rfc/rfc7638.html#section-3)
 */
actual interface IJwkJson: IKey {
    actual override val alg: String?
    actual override val crv: String?
    actual override val d: String?
    actual val e: String?
    actual val k: String?
    actual override val key_ops: Array<String>?
    actual override val kid: String?
    actual override val kty: String
    actual val n: String?
    actual val use: String?
    actual override val x: String?
    actual val x5c: Array<String>?
    actual val x5t: String?
    actual val x5u: String?

    @SerialName("x5t#S256")
    actual val x5t_S256: String?
    actual override val y: String?

}
