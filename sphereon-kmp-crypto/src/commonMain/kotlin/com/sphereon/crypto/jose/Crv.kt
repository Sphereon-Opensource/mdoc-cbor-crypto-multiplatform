@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.crypto.jose

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * JwaCurve is an enumeration class that represents the available elliptic curves for JWA cryptography algorithms.
 *
 * @property value The name of the curve.
 *
 * @since 0.1.0
 */
@JsExport
enum class JwaCurve(val value: String) {
    P_256("P-256"),
    P_384("P-384"),
    P_521("P-521"),
    Ed25519("Ed25519"),
    X25519("X25519"),
    Secp256k1("secp256k1");

    companion object {
        fun fromValue(value: String?): JwaCurve? {
            return JwaCurve.entries.find { entry -> entry.value == value }
        }
    }
}
