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
 * Represents the possible key types defined by the JSON Web Algorithms (JWA) specification.
 * These key types are used for cryptographic operations.
 *
 * @property value The identifier of the key type.
 */
@JsExport
@Serializable(with = JwaKeyTypeSerializer::class)
enum class JwaKeyType(val value: String) {
    EC("EC"),
    RSA("RSA"),
    oct("oct"),
    OKP("OKP");

    companion object {
        fun fromValue(value: String): JwaKeyType {
            return JwaKeyType.entries.find { entry -> entry.value == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}


internal object JwaKeyTypeSerializer : KSerializer<JwaKeyType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JwaKeyType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JwaKeyType) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): JwaKeyType {
        val value = decoder.decodeString()
        return JwaKeyType.fromValue(value)
    }
}

