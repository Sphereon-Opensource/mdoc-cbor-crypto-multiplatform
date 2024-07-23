@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * JwaCurve is an enumeration class that represents the available elliptic curves for JWA cryptography algorithms.
 *
 * @property curveName The name of the curve.
 *
 * @since 0.1.0
 */
enum class Curve(val curveName: String) {
    P_256("P-256"),
    P_384("P-384"),
    P_521("P-521"),
    Ed25519("Ed25519"),
    X25519("X25519"),
    Secp256k1("secp256k1");
}

typealias JwaCurve = Curve
typealias CBORCurve = Curve
