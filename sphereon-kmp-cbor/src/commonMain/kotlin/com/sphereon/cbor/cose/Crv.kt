@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

import com.sphereon.cbor.cose.NumberLabel
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * JwaCurve is an enumeration class that represents the available elliptic curves for JWA cryptography algorithms.
 *
 * @property curveName The name of the curve.
 *
 * @since 0.1.0
 */
enum class CoseCurve(val curveName: String, val label: NumberLabel) {
    P_256("P-256", NumberLabel(1)),
    P_384("P-384", NumberLabel(2)),
    P_521("P-521", NumberLabel(3)),
    /*Ed25519("Ed25519"),
    X25519("X25519"),
    Secp256k1("secp256k1");*/
}
