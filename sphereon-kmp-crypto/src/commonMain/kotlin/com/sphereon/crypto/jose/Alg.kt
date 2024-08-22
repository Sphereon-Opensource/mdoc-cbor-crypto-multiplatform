@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")

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

@JsExport
enum class JwaAlgorithmType {
    SIGNATURE,
    ENCRYPTION
}

/**
 * Represents JSON Web Algorithm (JWA) signature and encryption algorithms.
 *
 * @constructor Creates a JwaSignatureAlgorithm with the specified value.
 * @property value The value representing the algorithm.
 */
@JsExport
@Serializable(with = JwaAlgSerializer::class )
enum class JwaAlgorithm(val value: String, val type: JwaAlgorithmType) {
    // Signature algos
    HS256("HS256", JwaAlgorithmType.SIGNATURE),
    HS384("HS384", JwaAlgorithmType.SIGNATURE),
    HS512("HS512", JwaAlgorithmType.SIGNATURE),
    RS256("RS256", JwaAlgorithmType.SIGNATURE),
    RS384("RS384", JwaAlgorithmType.SIGNATURE),
    RS512("RS512", JwaAlgorithmType.SIGNATURE),
    ES256("ES256", JwaAlgorithmType.SIGNATURE),
    ES384("ES384", JwaAlgorithmType.SIGNATURE),
    ES512("ES512", JwaAlgorithmType.SIGNATURE),
    ES256K("ES256K", JwaAlgorithmType.SIGNATURE),
    PS256("PS256", JwaAlgorithmType.SIGNATURE),
    PS384("PS384", JwaAlgorithmType.SIGNATURE),
    PS512("PS512", JwaAlgorithmType.SIGNATURE),
    EdDSA("EdDSA", JwaAlgorithmType.SIGNATURE),

    // encryption
    RSA1_5("RSA1_5", JwaAlgorithmType.ENCRYPTION),
    RSA_OAEP("RSA-OAEP", JwaAlgorithmType.ENCRYPTION),
    RSA_OAEP_256("RSA-OAEP-256", JwaAlgorithmType.ENCRYPTION),
    A128KW("A128KW", JwaAlgorithmType.ENCRYPTION),
    A192KW("A192KW", JwaAlgorithmType.ENCRYPTION),
    A256KW("A256KW", JwaAlgorithmType.ENCRYPTION),
    ECDH_ES("ECDH-ES", JwaAlgorithmType.ENCRYPTION),
    ECDH_ES_A128KW("ECDH-ES+A128KW", JwaAlgorithmType.ENCRYPTION),
    ECDH_ES_A192KW("ECDH-ES+A192KW", JwaAlgorithmType.ENCRYPTION),
    ECDH_ES_A256KW("ECDH-ES+A256KW", JwaAlgorithmType.ENCRYPTION),
    A128GCMKW("A128GCMKW", JwaAlgorithmType.ENCRYPTION),
    A192GCMKW("A192GCMKW", JwaAlgorithmType.ENCRYPTION),
    A256GCMKW("A256GCMKW", JwaAlgorithmType.ENCRYPTION),
    PBES2_HS256_A128KW("PBES2-HS256+A128KW", JwaAlgorithmType.ENCRYPTION),
    PBES2_HS384_A192KW("PBES2-HS384+A192KW", JwaAlgorithmType.ENCRYPTION),
    PBES2_HS512_A256KW("PBES2-HS512+A256KW", JwaAlgorithmType.ENCRYPTION);


    object Static {
        fun fromValue(value: String?): JwaAlgorithm? {
            return JwaAlgorithm.entries.find { entry -> entry.value == value }
        }
    }
}


internal object JwaAlgSerializer : KSerializer<JwaAlgorithm> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JwaAlgorithm", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: JwaAlgorithm) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): JwaAlgorithm {
        val value = decoder.decodeString()
        return JwaAlgorithm.Static.fromValue(value) ?: throw IllegalArgumentException("Invalid jwa algorithm")
    }
}
