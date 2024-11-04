@file:OptIn(ExperimentalStdlibApi::class)

package com.sphereon.crypto.generic

import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import kotlin.js.JsExport


/**
 * Represents a key pair used by a crypto provider, encapsulating both JOSE and COSE key pairs.
 *
 * @property cose The COSE key pair which contains the private and public keys used for COSE operations.
 * @property jose The JOSE key pair which contains the private and public keys used for JOSE operations.
 */
@JsExport
data class ManagedKeyPair(
    val cose: CoseKeyPair,
    val jose: JoseKeyPair
)

/**
 * Data class representing a cryptographic key pair used with JOSE (JSON Object Signing and Encryption).
 *
 * @property privateJwk The private key in JWK (JSON Web Key) format. This may be null.
 * @property publicJwk The public key in JWK (JSON Web Key) format.
 */
@JsExport
data class JoseKeyPair(
    val privateJwk: Jwk?,
    val publicJwk: Jwk
)

/**
 * Represents a cryptographic key pair for COSE (CBOR Object Signing and Encryption) operations.
 *
 * @property privateCoseKey The private COSE key in CBOR (Concise Binary Object Representation) format.
 *                          This can be null if only the public key is available.
 * @property publicCoseKey The public COSE key in CBOR format. This is mandatory.
 */
@JsExport
data class CoseKeyPair(
    val privateCoseKey: CoseKeyCbor?,
    val publicCoseKey: CoseKeyCbor
)

@JsExport
interface GenerateKeyParams {
    val use: JwkUse?
    val keyOperations: Array<out KeyOperations>?
    val curve: Curve?
    val alg: SignatureAlgorithm?
}


