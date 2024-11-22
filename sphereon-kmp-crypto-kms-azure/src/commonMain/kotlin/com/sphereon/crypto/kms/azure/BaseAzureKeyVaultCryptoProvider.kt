package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.kms.IKeyManagementSystem

abstract class BaseAzureKeyvaultCryptoProvider(private val id: String) : IKeyManagementSystem {
    override fun getId(): String = id

    override fun supportedCurves(): Array<Curve> = arrayOf(Curve.P_256, Curve.Secp256k1, Curve.P_384, Curve.P_521)

    override fun isSupportedCurve(curve: Curve): Boolean {
        return supportedCurves().contains(curve)
    }

    override fun supportedDigests(): Array<DigestAlg> {
        return supportedSignatureAlgorithms()
            .filter { it.digestAlgorithm !== null }
            .map { it.digestAlgorithm!! }
            .toSet()
            .toTypedArray()
    }

    override fun supportedKeyTypes(): Array<KeyType> = arrayOf(KeyType.EC, KeyType.RSA)

    override fun supportedSignatureAlgorithms(): Array<SignatureAlgorithm> =
        arrayOf(
            SignatureAlgorithm.ECDSA_SHA256,
            SignatureAlgorithm.ECDSA_SHA384,
            SignatureAlgorithm.ECDSA_SHA512
        )

    fun isSupportedSignatureAlgorithm(signatureAlgorithm: SignatureAlgorithm): Boolean {
        return supportedSignatureAlgorithms().contains(signatureAlgorithm)
    }
}
