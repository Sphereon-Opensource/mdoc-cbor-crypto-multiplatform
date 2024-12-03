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
import kotlin.js.JsName

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
@Serializable(with = JwaAlgSerializer::class)
enum class JwaAlgorithm(
    val value: String, val type: AlgorithmType,
    @JsName("keyType")
    val keyType: JwaKeyType?
) {
    // Signature algos
    HS256("HS256", AlgorithmType.SIGNATURE, JwaKeyType.oct),
    HS384("HS384", AlgorithmType.SIGNATURE, JwaKeyType.oct),
    HS512("HS512", AlgorithmType.SIGNATURE, JwaKeyType.oct),
    RS256("RS256", AlgorithmType.SIGNATURE, JwaKeyType.RSA),
    RS384("RS384", AlgorithmType.SIGNATURE, JwaKeyType.RSA),
    RS512("RS512", AlgorithmType.SIGNATURE, JwaKeyType.RSA),
    ES256("ES256", AlgorithmType.SIGNATURE, JwaKeyType.EC),
    ES384("ES384", AlgorithmType.SIGNATURE, JwaKeyType.EC),
    ES512("ES512", AlgorithmType.SIGNATURE, JwaKeyType.EC),
    ES256K("ES256K", AlgorithmType.SIGNATURE, JwaKeyType.EC),
    PS256("PS256", AlgorithmType.SIGNATURE, JwaKeyType.RSA),
    PS384("PS384", AlgorithmType.SIGNATURE, JwaKeyType.RSA),
    PS512("PS512", AlgorithmType.SIGNATURE, JwaKeyType.RSA),
    EdDSA("EdDSA", AlgorithmType.SIGNATURE, JwaKeyType.OKP),

    // encryption
    RSA1_5("RSA1_5", AlgorithmType.ENCRYPTION, JwaKeyType.RSA),
    RSA_OAEP("RSA-OAEP", AlgorithmType.ENCRYPTION, JwaKeyType.RSA),
    RSA_OAEP_256("RSA-OAEP-256", AlgorithmType.ENCRYPTION, JwaKeyType.RSA),
    A128KW("A128KW", AlgorithmType.ENCRYPTION, JwaKeyType.oct),
    A192KW("A192KW", AlgorithmType.ENCRYPTION, JwaKeyType.oct),
    A256KW("A256KW", AlgorithmType.ENCRYPTION, JwaKeyType.oct),
    ECDH_ES("ECDH-ES", AlgorithmType.ENCRYPTION, JwaKeyType.EC),
    ECDH_ES_A128KW("ECDH-ES+A128KW", AlgorithmType.ENCRYPTION, JwaKeyType.EC),
    ECDH_ES_A192KW("ECDH-ES+A192KW", AlgorithmType.ENCRYPTION, JwaKeyType.EC),
    ECDH_ES_A256KW("ECDH-ES+A256KW", AlgorithmType.ENCRYPTION, JwaKeyType.EC),
    A128GCMKW("A128GCMKW", AlgorithmType.ENCRYPTION, JwaKeyType.oct),
    A192GCMKW("A192GCMKW", AlgorithmType.ENCRYPTION, JwaKeyType.oct),
    A256GCMKW("A256GCMKW", AlgorithmType.ENCRYPTION, JwaKeyType.oct),
    PBES2_HS256_A128KW("PBES2-HS256+A128KW", AlgorithmType.ENCRYPTION, null),
    PBES2_HS384_A192KW("PBES2-HS384+A192KW", AlgorithmType.ENCRYPTION, null),
    PBES2_HS512_A256KW("PBES2-HS512+A256KW", AlgorithmType.ENCRYPTION, null);


    object Static {
        fun fromValue(value: String?): JwaAlgorithm? {
            return JwaAlgorithm.entries.find { entry -> entry.value == value }
        }
    }

    override fun toString() = value
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
