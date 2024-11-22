package com.sphereon.crypto.kms.azure

import com.azure.core.http.policy.RetryOptions
import com.azure.core.http.policy.RetryPolicy
import com.azure.security.keyvault.keys.KeyAsyncClient
import com.azure.security.keyvault.keys.KeyClientBuilder
import com.azure.security.keyvault.keys.KeyServiceVersion
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions
import com.azure.security.keyvault.keys.models.KeyCurveName
import com.azure.security.keyvault.keys.models.KeyOperation
import com.azure.security.keyvault.keys.models.KeyVaultKey
import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.SignClientException
import com.sphereon.crypto.generic.CoseKeyPair
import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.JoseKeyPair
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.jose.generateJwkThumbprint
import com.sphereon.crypto.kms.IKeyManagementSystem
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
import com.sphereon.kmp.Logger
import com.sphereon.kmp.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

private val logger = Logger("sphereon:kmp:kms:azure-keyvault")

actual class AzureKeyvaultCryptoProvider actual constructor(
    config: AzureKeyvaultClientConfig
) : IKeyManagementSystem,
    IRawSignatureService,
    ISimpleSignatureService,
    BaseAzureKeyvaultCryptoProvider(config.applicationId) {
    private val keyClient: KeyAsyncClient

    init {
        keyClient = KeyClientBuilder()
            .serviceVersion(KeyServiceVersion.V7_3)
            .vaultUrl(config.keyvaultUrl)
            .clientOptions(config.toClientOptions())
            .retryPolicy(
                if (config.exponentialBackoffRetryOpts == null) null
                else RetryPolicy(RetryOptions(config.exponentialBackoffRetryOpts.toExponentialBackoffOptions()))
            )
            .credential(config.credentialOpts.toTokenCredential(config.tenantId))
            .buildAsyncClient()
    }

    /**
     * Generates a new key pair in Azure Key Vault.
     *
     * @param kmsKeyRef The key reference to use for the key pair. If null, a new key reference will be generated.
     * @param keyOperations The key operations that the key pair should support.
     * @param alg The signature algorithm to use for the key pair.
     * @return A ManagedKeyPair object containing the generated key pair.
     * @throws IllegalArgumentException if the curve is not supported by Azure Key Vault.
     * @throws SignClientException if the key pair could not be generated.
     */
    override suspend fun generateKeyAsync(
        kmsKeyRef: String?,
        use: JwkUse?,
        keyOperations: Array<out KeyOperations>?,
        alg: SignatureAlgorithm?
    ): ManagedKeyPair {
        val signatureAlgorithm = alg ?: SignatureAlgorithm.ECDSA_SHA256

        if (!isSupportedSignatureAlgorithm(signatureAlgorithm)) {
            throw IllegalArgumentException("Signature algorithm ${signatureAlgorithm.cryptoAlgorithm.name} is not supported by Azure Key Vault")
        }

        val keyName = kmsKeyRef ?: "key-${Uuid.v4String()}"
        val operations: Array<KeyOperation> = (keyOperations ?: arrayOf(KeyOperations.SIGN, KeyOperations.VERIFY))
            .map {
                it.toAzureKeyOperation()
            }
            .toTypedArray()

        val curve = signatureAlgorithm.curve ?: Curve.P_256

        val createEcKeyOptions = CreateEcKeyOptions(keyName)
            .setCurveName(curve.toAzureKeyCurveName())
            .setKeyOperations(*operations)

        val keyVaultKey = withContext(Dispatchers.IO) {
            keyClient.createEcKey(createEcKeyOptions).block()
        }

        if (keyVaultKey == null) {
            logger.debug("Failed to create key in Azure Key Vault for reference $keyName in vault ${keyClient.vaultUrl}")
            throw SignClientException("Failed to create key in Azure Key Vault")
        }

        val keyVaultJwk = keyVaultKey.toJwk()
        val kid = keyVaultJwk.kid ?: generateJwkThumbprint(keyVaultJwk)
        val jwk = keyVaultJwk.copy(kid = kid)
        val publicCoseKey = CoseJoseKeyMappingService.toCoseKey(jwk)

        return ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = keyVaultKey.name,
            kid = kid,
            jose = JoseKeyPair(null, jwk),
            cose = CoseKeyPair(null, publicCoseKey)
        )
    }

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        val keyVaultKey = withContext(Dispatchers.IO) {
            keyClient.getKey(keyInfo.kmsKeyRef).block()
        } ?: throw SignClientException("Key not found in Azure Key Vault for reference: ${keyInfo.kmsKeyRef}")

        val cryptoClient = keyClient.getCryptographyAsyncClient(keyInfo.kmsKeyRef)

        val hashedInput = MessageDigest.getInstance(keyInfo.signatureAlgorithm?.digestAlgorithm?.name).digest(input)

        val signResult = withContext(Dispatchers.IO) {
            cryptoClient.sign(keyVaultKey.toSignatureAlgorithm(), hashedInput).block()
        } ?: throw SignClientException("Failed to create raw signature for key: ${keyInfo.kmsKeyRef}")

        return signResult.signature
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        val keyVaultKey = withContext(Dispatchers.IO) {
            keyClient.getKey(keyInfo.kmsKeyRef).block()
        } ?: throw SignClientException("Key not found in Azure Key Vault for reference: ${keyInfo.kmsKeyRef}")

        val cryptoClient = keyClient.getCryptographyAsyncClient(keyInfo.kmsKeyRef)

        val algorithm = keyVaultKey.toSignatureAlgorithm()
        val hashedInput = MessageDigest.getInstance(keyInfo.signatureAlgorithm?.digestAlgorithm?.name).digest(input)

        val verifyResult = withContext(Dispatchers.IO) {
            cryptoClient.verify(algorithm, hashedInput, signature).block()
        } ?: throw SignClientException("Failed to verify signature for key: ${keyInfo.kmsKeyRef}")

        return verifyResult.isValid
    }

    override suspend fun createSignature(
        signInput: SignInput,
        keyInfo: IKeyInfo<*>?,
        signatureAlgorithm: SignatureAlgorithm?
    ): SignOutput {
        TODO("Not yet implemented")
    }

    override suspend fun isValidSignature(signInput: SignInput, signature: Signature): Boolean {
        TODO("Not yet implemented")
    }
}
