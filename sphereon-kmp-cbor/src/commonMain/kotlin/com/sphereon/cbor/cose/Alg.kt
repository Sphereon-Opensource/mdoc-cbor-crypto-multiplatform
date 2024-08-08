package com.sphereon.cbor.cose

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * This parameter is used to restrict the algorithm that is used with the key.
 *
 *       If this parameter is present in the key structure,
 *       the application MUST verify that this algorithm matches the
 *       algorithm for which the key is being used.  If the algorithms do
 *       not match, then this key object MUST NOT be used to perform the
 *       cryptographic operation.
 */
@JsExport
interface CoseAlgorithm {
    val name: String
    val value: Int
    val keyType: CoseKeyType?
    val hash: HashAlgorithm?
    val tagLength: Int?
    val curve: CoseCurve?
    val description: String
}

/**
 * @see CoseAlgorithm
 */


//todo: Add curve
@Serializable
@JsExport
sealed class CoseSignatureAlgorithm(
    override val name: String,
    override val value: Int,
    override val keyType: CoseKeyType?,
    override val hash: HashAlgorithm?,
    override val tagLength: Int?,
    override val curve: CoseCurve?,
    override val description: String
) : CoseAlgorithm {
    object ES256 : CoseSignatureAlgorithm(
        "ES256",
        -7,
        CoseKeyType.EC2,
        HashAlgorithm.SHA256,
        null,
        CoseCurve.P_256,
        "ECDSA w/ SHA-256"
    )

    object ES256K : CoseSignatureAlgorithm(
        "ES256K",
        -47,
        CoseKeyType.EC2,
        HashAlgorithm.SHA256,
        null,
        CoseCurve.secp256k1,
        "ECDSA secp256k1 curve w/ SHA-256"
    )

    object ES384 : CoseSignatureAlgorithm(
        "ES384",
        -35,
        CoseKeyType.EC2,
        HashAlgorithm.SHA384,
        null,
        CoseCurve.P_384,
        "ECDSA w/ SHA-384"
    )

    object ES512 : CoseSignatureAlgorithm(
        "ES512",
        -36,
        CoseKeyType.EC2,
        HashAlgorithm.SHA512,
        null,
        CoseCurve.P_521,
        "ECDSA w/ SHA-512"
    )

    object EdDSA : CoseSignatureAlgorithm("EdDSA", -8, CoseKeyType.OKP, null, null, CoseCurve.P_521, "EdDSA")

    object HS256_64 :
        CoseSignatureAlgorithm(
            "HS256/64",
            4,
            null,
            HashAlgorithm.SHA256,
            64,
            null,
            "HMAC w/ SHA-256 truncated to 64 bits"
        )

    object HS256 :
        CoseSignatureAlgorithm("HS256", 5, CoseKeyType.Symmetric, HashAlgorithm.SHA256, 256, null, "HMAC w/ SHA-256")

    object HS384 :
        CoseSignatureAlgorithm("HS384", 6, CoseKeyType.Symmetric, HashAlgorithm.SHA384, 384, null, "HMAC w/ SHA-384")

    object HS512 :
        CoseSignatureAlgorithm("HS512", 7, CoseKeyType.Symmetric, HashAlgorithm.SHA512, 512, null, "HMAC w/ SHA-512")

    object PS256 :
        CoseSignatureAlgorithm(
            "PS256",
            -37,
            CoseKeyType.RSA,
            HashAlgorithm.SHA256,
            256,
            CoseCurve.P_256,
            "RSASSA-PSS w/ SHA-256"
        )

    object PS384 :
        CoseSignatureAlgorithm(
            "PS384",
            -38,
            CoseKeyType.RSA,
            HashAlgorithm.SHA384,
            384,
            CoseCurve.P_384,
            "RSASSA-PSS w/ SHA-384"
        )

    object PS512 :
        CoseSignatureAlgorithm(
            "PS512",
            -39,
            CoseKeyType.RSA,
            HashAlgorithm.SHA512,
            512,
            CoseCurve.P_521,
            "RSASSA-PSS w/ SHA-512"
        )


    companion object {
        val asList = listOf(ES256, ES384, ES512, EdDSA, HS256_64, HS256, HS384, HS512)
        fun fromValue(value: Int?): CoseSignatureAlgorithm? {
            if (value == null) {
                return null
            }
            return asList.firstOrNull { it.value == value.toInt() }
        }
    }

}
/*
*/
/**
 * Represents a JSON Web Algorithm (JWA) encryption algorithm.
 *
 * @property algValue The string value representing the algorithm.
 *//*
sealed class CBOREncryptionAlgorithm(override val algValue: String) : JwaAlgorithm {
    object RSA1_5 : CoseSignatureAlgorithm("RSA1_5")
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
}*/
