@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.cbor.cose

import com.sphereon.cbor.CborUInt
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * JwaCurve is an enumeration class that represents the available elliptic curves for JWA cryptography algorithms.
 *
 * @property curveName The name of the curve.
 *
 * @since 0.1.0
 */
@JsExport
enum class CoseCurve(val curveName: String, val value: Int, keyType: CoseKeyType) {
    P_256("P-256", 1, CoseKeyType.EC2),
    P_384("P-384", 2, CoseKeyType.EC2),
    P_521("P-521", 3, CoseKeyType.EC2),
    X25519("X25519", 4, CoseKeyType.OKP),
    X448("X448", 5, CoseKeyType.OKP),
    Ed25519("Ed25519", 6, CoseKeyType.OKP),
    Ed448("Ed448", 7, CoseKeyType.OKP),
    secp256k1("secp256k1", -1, CoseKeyType.EC2),
    ;

    fun toCbor(): CborUInt {
        return CborUInt(this.value)
    }

    companion object {
        fun fromValue(value: Int): CoseCurve {
            return CoseCurve.entries.find { entry -> entry.value == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}
