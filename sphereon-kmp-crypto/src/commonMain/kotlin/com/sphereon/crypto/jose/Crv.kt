@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.crypto.jose

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
@Serializable(with = JwaCurveSerializer::class)
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

internal object JwaCurveSerializer : KSerializer<JwaCurve> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JwaCurve", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JwaCurve) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): JwaCurve {
        val value = decoder.decodeString()
        return JwaCurve.fromValue(value) ?: throw IllegalArgumentException("Invalid jwa curve")
    }
}
