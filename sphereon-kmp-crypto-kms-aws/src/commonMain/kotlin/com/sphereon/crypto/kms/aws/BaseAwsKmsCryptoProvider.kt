package com.sphereon.crypto.kms.aws

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.generic.*
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.kms.IKeyManagementSystem
import com.sphereon.crypto.kms.IKeyStoreService
import com.sphereon.crypto.kms.model.AwsKmsClientConfig
import com.sphereon.crypto.kms.model.KeyProviderSettings
import com.sphereon.crypto.kms.model.KeyProviderType
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature

abstract class BaseAwsKmsCryptoProvider(override val settings: KeyProviderSettings) : IKeyManagementSystem,
    IRawSignatureService,
    ISimpleSignatureService,
    IKeyStoreService {

    init {
        check(settings.id.isNotBlank()) { "Missing ID in settings.id" }
        requireNotNull(settings.config.aws) { "Missing AWS KMS configuration in settings.config.aws" }
        check(settings.config.type == KeyProviderType.AWS_KMS) { "Invalid key provider type: ${settings.config.type}. Expected AWS_KMS" }
    }


    protected val awsConfig: AwsKmsClientConfig = settings.config.aws!!

    override fun getId(): String = settings.id

    override fun supportedCurves(): Array<Curve> = arrayOf(Curve.P_256, Curve.P_384, Curve.P_521)

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

    override fun supportedKeyTypes(): Array<KeyType> = arrayOf(KeyType.EC)

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
        TODO("Implement in platform-specific code")
    }

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        TODO("Implement in platform-specific code")
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        TODO("Implement in platform-specific code")
    }

    override suspend fun createSignature(
        signInput: SignInput,
        keyInfo: IKeyInfo<*>?,
        signatureAlgorithm: SignatureAlgorithm?
    ): SignOutput {
        TODO("Implement in platform-specific code")
    }

    override suspend fun isValidSignature(signInput: SignInput, signature: Signature): Boolean {
        TODO("Implement in platform-specific code")
    }


}
