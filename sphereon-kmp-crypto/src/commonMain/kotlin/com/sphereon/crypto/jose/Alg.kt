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
enum class AlgorithmType {
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
enum class JwaAlgorithm(val value: String, val type: AlgorithmType) {
    // Signature algos
    HS256("HS256", AlgorithmType.SIGNATURE),
    HS384("HS384", AlgorithmType.SIGNATURE),
    HS512("HS512", AlgorithmType.SIGNATURE),
    RS256("RS256", AlgorithmType.SIGNATURE),
    RS384("RS384", AlgorithmType.SIGNATURE),
    RS512("RS512", AlgorithmType.SIGNATURE),
    ES256("ES256", AlgorithmType.SIGNATURE),
    ES384("ES384", AlgorithmType.SIGNATURE),
    ES512("ES512", AlgorithmType.SIGNATURE),
    ES256K("ES256K", AlgorithmType.SIGNATURE),
    PS256("PS256", AlgorithmType.SIGNATURE),
    PS384("PS384", AlgorithmType.SIGNATURE),
    PS512("PS512", AlgorithmType.SIGNATURE),
    EdDSA("EdDSA", AlgorithmType.SIGNATURE),

    // encryption
    RSA1_5("RSA1_5", AlgorithmType.ENCRYPTION),
    RSA_OAEP("RSA-OAEP", AlgorithmType.ENCRYPTION),
    RSA_OAEP_256("RSA-OAEP-256", AlgorithmType.ENCRYPTION),
    A128KW("A128KW", AlgorithmType.ENCRYPTION),
    A192KW("A192KW", AlgorithmType.ENCRYPTION),
    A256KW("A256KW", AlgorithmType.ENCRYPTION),
    ECDH_ES("ECDH-ES", AlgorithmType.ENCRYPTION),
    ECDH_ES_A128KW("ECDH-ES+A128KW", AlgorithmType.ENCRYPTION),
    ECDH_ES_A192KW("ECDH-ES+A192KW", AlgorithmType.ENCRYPTION),
    ECDH_ES_A256KW("ECDH-ES+A256KW", AlgorithmType.ENCRYPTION),
    A128GCMKW("A128GCMKW", AlgorithmType.ENCRYPTION),
    A192GCMKW("A192GCMKW", AlgorithmType.ENCRYPTION),
    A256GCMKW("A256GCMKW", AlgorithmType.ENCRYPTION),
    PBES2_HS256_A128KW("PBES2-HS256+A128KW", AlgorithmType.ENCRYPTION),
    PBES2_HS384_A192KW("PBES2-HS384+A192KW", AlgorithmType.ENCRYPTION),
    PBES2_HS512_A256KW("PBES2-HS512+A256KW", AlgorithmType.ENCRYPTION);


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
