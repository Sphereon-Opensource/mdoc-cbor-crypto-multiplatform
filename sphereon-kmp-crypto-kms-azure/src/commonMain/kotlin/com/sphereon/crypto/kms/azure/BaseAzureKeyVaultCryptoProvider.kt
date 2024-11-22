package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.kms.IKeyManagementSystem
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature

abstract class BaseAzureKeyvaultCryptoProvider(private val id: String) : IKeyManagementSystem,
    IRawSignatureService,
    ISimpleSignatureService {
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

    override suspend fun generateKeyAsync(
        kmsKeyRef: String?,
        use: JwkUse?,
        keyOperations: Array<out KeyOperations>?,
        alg: SignatureAlgorithm?
    ): ManagedKeyPair {
        TODO()
    }

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        TODO()
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        TODO()
    }

    override suspend fun createSignature(
        signInput: SignInput,
        keyInfo: IKeyInfo<*>?,
        signatureAlgorithm: SignatureAlgorithm?
    ): SignOutput {
        TODO()
    }

    override suspend fun isValidSignature(signInput: SignInput, signature: Signature): Boolean {
        TODO()
    }
}
