package com.sphereon.crypto.jose

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport

@JsExport
@Serializable(with = JoseKeyOperationsSerializer::class)
enum class JoseKeyOperations(val value: String, val description: String) {

    SIGN("sign","The key is used to create signatures.  Requires private key fields"),
    VERIFY("verify", "The key is used for verification of signatures"),
    ENCRYPT("encrypt", "The key is used for key transport encryption."),
    DECRYPT("decrypt", "The key is used for key transport decryption"),
    WRAP_KEY("wrap key", "The key is used for key wrap encryption."),
    UNWRAP_KEY("unwrap key", "The key is used for key wrap decryption. Requires private key fields"),
    DERIVE_KEY("derive key", "The key is used for deriving keys.  Requires private key fields"),
    DERIVE_BITS(
        "derive bits",
        "The key is used for deriving bits not to be used as a key.  Requires private key fields."
    ),
    MAC_CREATE("MAC create", "The key is used for creating MACs. "),
    MAC_VERIFY("MAC verify",  "The key is used for validating MACs.");

    companion object {
        fun fromValue(value: String): JoseKeyOperations {
            return entries.find { entry -> entry.value == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}

internal object JoseKeyOperationsSerializer : KSerializer<JoseKeyOperations> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JoseKeyOperations", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JoseKeyOperations) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): JoseKeyOperations {
        val value = decoder.decodeString()
        return JoseKeyOperations.fromValue(value)
    }
}
