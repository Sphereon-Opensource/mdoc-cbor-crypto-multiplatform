@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
package com.sphereon.jose.jwa

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

interface JwaAlgorithm {
    val algValue: String
}

/**
 * Represents JSON Web Algorithm (JWA) signature algorithms.
 *
 * @constructor Creates a JwaSignatureAlgorithm with the specified value.
 * @property algValue The value representing the algorithm.
 */

sealed class JwaSignatureAlgorithm(override val algValue: String): JwaAlgorithm {
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

}

/**
 * Represents a JSON Web Algorithm (JWA) encryption algorithm.
 *
 * @property algValue The string value representing the algorithm.
 */
sealed class JwaEncryptionAlgorithm(override val algValue: String): JwaAlgorithm {
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
}
