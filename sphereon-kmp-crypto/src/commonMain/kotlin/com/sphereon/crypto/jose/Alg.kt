@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.crypto.jose

import com.sphereon.cbor.CDDL
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
interface JwaAlgorithm {
    val value: String
}

/**
 * Represents JSON Web Algorithm (JWA) signature algorithms.
 *
 * @constructor Creates a JwaSignatureAlgorithm with the specified value.
 * @property value The value representing the algorithm.
 */
@JsExport
@Serializable(with = JwaAlgorithmSerializer::class)
sealed class JwaSignatureAlgorithm(override val value: String) : JwaAlgorithm {
    object HS256 : JwaSignatureAlgorithm("HS256")
    object HS384 : JwaSignatureAlgorithm("HS384")
    object HS512 : JwaSignatureAlgorithm("HS512")
    object RS256 : JwaSignatureAlgorithm("RS256")
    object RS384 : JwaSignatureAlgorithm("RS384")
    object RS512 : JwaSignatureAlgorithm("RS512")
    object ES256 : JwaSignatureAlgorithm("ES256")
    object ES384 : JwaSignatureAlgorithm("ES384")
    object ES512 : JwaSignatureAlgorithm("ES512")
    object ES256K : JwaSignatureAlgorithm("ES256K")
    object PS256 : JwaSignatureAlgorithm("PS256")
    object PS384 : JwaSignatureAlgorithm("PS384")
    object PS512 : JwaSignatureAlgorithm("PS512")
    object EdDSA : JwaSignatureAlgorithm("EdDSA")

    object Static {
        val asList = listOf(HS256, HS384, HS512, RS256, RS384, RS512, ES256, ES384, ES512, ES256K, PS256, PS384, PS512, EdDSA)
        fun fromValue(value: String?): JwaSignatureAlgorithm? {
            if (value == null) {
                return null
            }
            return asList.firstOrNull { it.value === value }
        }
    }
}

/**
 * Represents a JSON Web Algorithm (JWA) encryption algorithm.
 *
 * @property value The string value representing the algorithm.
 */
@JsExport
@Serializable(with = JwaAlgorithmSerializer::class)
sealed class JwaEncryptionAlgorithm(override val value: String) : JwaAlgorithm {
    object RSA1_5 : JwaEncryptionAlgorithm("RSA1_5")
    object RSA_OAEP : JwaEncryptionAlgorithm("RSA-OAEP")
    object RSA_OAEP_256 : JwaEncryptionAlgorithm("RSA-OAEP-256")
    object A128KW : JwaEncryptionAlgorithm("A128KW")
    object A192KW : JwaEncryptionAlgorithm("A192KW")
    object A256KW : JwaEncryptionAlgorithm("A256KW")
    object ECDH_ES : JwaEncryptionAlgorithm("ECDH-ES")
    object ECDH_ES_A128KW : JwaEncryptionAlgorithm("ECDH-ES+A128KW")
    object ECDH_ES_A192KW : JwaEncryptionAlgorithm("ECDH-ES+A192KW")
    object ECDH_ES_A256KW : JwaEncryptionAlgorithm("ECDH-ES+A256KW")
    object A128GCMKW : JwaEncryptionAlgorithm("A128GCMKW")
    object A192GCMKW : JwaEncryptionAlgorithm("A192GCMKW")
    object A256GCMKW : JwaEncryptionAlgorithm("A256GCMKW")
    object PBES2_HS256_A128KW : JwaEncryptionAlgorithm("PBES2-HS256+A128KW")
    object PBES2_HS384_A192KW : JwaEncryptionAlgorithm("PBES2-HS384+A192KW")
    object PBES2_HS512_A256KW : JwaEncryptionAlgorithm("PBES2-HS512+A256KW")
    object Static {
        val asList = listOf(
            RSA1_5,
            RSA_OAEP,
            RSA_OAEP_256,
            A128KW,
            A192KW,
            A256KW,
            ECDH_ES,
            ECDH_ES_A128KW,
            ECDH_ES_A192KW,
            ECDH_ES_A256KW,
            A128GCMKW,
            A192GCMKW,
            A256GCMKW,
            PBES2_HS256_A128KW,
            PBES2_HS384_A192KW,
            PBES2_HS512_A256KW
        )
        fun fromValue(value: String?): JwaEncryptionAlgorithm? {
            if (value == null) {
                return null
            }
            return asList.firstOrNull { it.value === value }
        }

    }

}

internal object JwaAlgorithmSerializer : KSerializer<JwaAlgorithm> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JWAAlgorithm", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JwaAlgorithm) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): JwaAlgorithm {
        val value = decoder.decodeString()
        return JwaSignatureAlgorithm.Static.fromValue(value) ?: JwaEncryptionAlgorithm.Static.fromValue(value) ?: throw IllegalArgumentException("Invalid signature algorithm")
    }
}
