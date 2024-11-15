@file:OptIn(ExperimentalStdlibApi::class)

package com.sphereon.crypto.generic

import com.sphereon.crypto.IKey
import com.sphereon.crypto.KeyEncoding
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.ManagedKeyInfo
import com.sphereon.crypto.PKIException
import com.sphereon.crypto.ResolvedKeyInfo
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.jose.IJwk
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
    val kms: String,
    val kmsKeyRef: String,
    val cose: CoseKeyPair,
    val jose: JoseKeyPair
) {
    fun cborToManagedKeyInfo(visibility: KeyVisibility = KeyVisibility.PUBLIC) = toManagedKeyInfo<ICoseKeyCbor>(visibility, KeyEncoding.COSE)

    fun joseToManagedKeyInfo(visibility: KeyVisibility = KeyVisibility.PUBLIC) = toManagedKeyInfo<IJwk>(visibility, KeyEncoding.JOSE)

    fun <KT : IKey> toManagedKeyInfo(visibility: KeyVisibility = KeyVisibility.PUBLIC, keyEncoding: KeyEncoding): ManagedKeyInfo<KT> {
        val resolvedKeyInfo: ResolvedKeyInfo<KT>
        val key = if (keyEncoding === KeyEncoding.COSE && visibility === KeyVisibility.PRIVATE) {
            cose.privateCoseKey ?: throw PKIException("No private keys exists for the managed key pair")
        } else if (keyEncoding === KeyEncoding.COSE && visibility === KeyVisibility.PUBLIC) {
            cose.publicCoseKey
        } else if (keyEncoding === KeyEncoding.JOSE && visibility === KeyVisibility.PRIVATE) {
            jose.privateJwk ?: throw PKIException("No private keys exists for the managed key pair")
        } else if (keyEncoding === KeyEncoding.JOSE && visibility === KeyVisibility.PUBLIC) {
            jose.publicJwk
        } else throw IllegalArgumentException("Invalid class or visibility combination")

        resolvedKeyInfo = ResolvedKeyInfo(
            key = key as KT,
            kmsKeyRef = kmsKeyRef,
            kms = kms,
            keyVisibility = visibility,
            keyType = key.getKty(),
            x5c = key.getX509CertificateChain(),
            kid = key.getKidAsString(true),
            signatureAlgorithm = key.getSignatureAlgorithm(),
        )
        return ManagedKeyInfo(
            kms = kms,
            kmsKeyRef = kmsKeyRef,
            resolvedKeyInfo = resolvedKeyInfo,
        )
    }
}

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


