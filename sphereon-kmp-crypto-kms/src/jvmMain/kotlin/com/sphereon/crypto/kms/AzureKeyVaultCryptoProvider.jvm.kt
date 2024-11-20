package com.sphereon.crypto.kms

import com.azure.core.credential.TokenCredential
import com.azure.core.http.policy.ExponentialBackoffOptions
import com.azure.core.http.policy.RetryOptions
import com.azure.core.http.policy.RetryPolicy
import com.azure.core.util.ClientOptions
import com.azure.identity.ClientCertificateCredential
import com.azure.identity.ClientCertificateCredentialBuilder
import com.azure.identity.ClientSecretCredential
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.identity.InteractiveBrowserCredential
import com.azure.identity.InteractiveBrowserCredentialBuilder
import com.azure.identity.UsernamePasswordCredential
import com.azure.identity.UsernamePasswordCredentialBuilder
import com.azure.security.keyvault.certificates.CertificateAsyncClient
import com.azure.security.keyvault.certificates.CertificateClientBuilder
import com.azure.security.keyvault.certificates.CertificateServiceVersion
import com.azure.security.keyvault.keys.KeyAsyncClient
import com.azure.security.keyvault.keys.KeyClientBuilder
import com.azure.security.keyvault.keys.KeyServiceVersion
import com.azure.security.keyvault.keys.models.CreateKeyOptions
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
import com.sphereon.crypto.jose.JoseKeyOperations
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
import com.sphereon.kmp.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.time.Duration

private val logger = Logger("sphereon:kmp:kms:azure-keyvault")

actual class AzureKeyVaultCryptoProvider actual constructor(
    id: String,
    config: AzureKeyvaultClientConfig
) : IKeyManagementSystem,
    IRawSignatureService, ISimpleSignatureService {
    private val id: String = "azure-keyvault"
    private val keyClient: KeyAsyncClient
    private val hasCerts: Boolean
    private val isManagedHsm: Boolean
    private val certClient: CertificateAsyncClient?

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

        hasCerts = config.hsmType !== HSMType.MANAGED_HSM
        isManagedHsm = config.hsmType === HSMType.MANAGED_HSM

        // Azure Managed HSM has no Certs API
        certClient = when (config.hsmType) {
            HSMType.MANAGED_HSM -> {
                logger.warn("Azure keyvault key provider ${config.applicationId} in mode: Managed HSM. This mode as opposed to 'keyvault' mode is untested currently!")
                null
            }

            HSMType.KEYVAULT -> {
                logger.debug("Azure keyvault key provider ${config.applicationId} in mode: Keyvault. Creating a certificate client.")
                CertificateClientBuilder()
                    .serviceVersion(CertificateServiceVersion.V7_3)
                    .vaultUrl(config.keyvaultUrl)
                    .clientOptions(config.toClientOptions())
                    .retryPolicy(
                        if (config.exponentialBackoffRetryOpts == null) null
                        else RetryPolicy(RetryOptions(config.exponentialBackoffRetryOpts.toExponentialBackoffOptions()))
                    )
                    .credential(config.credentialOpts.toTokenCredential(config.tenantId))
                    .buildAsyncClient()
            }
        }
    }

    override fun getId(): String {
        return id
    }

    override fun supportedCurves(): Array<Curve> = arrayOf(Curve.P_256, Curve.Secp256k1, Curve.P_384, Curve.P_521)

    override fun isSupportedCurve(curve: Curve): Boolean {
        return supportedCurves().contains(curve)
    }

    override fun supportedDigests(): Array<DigestAlg> {
        return supportedSignatureAlgorithms().filter { it.digestAlgorithm !== null }.map { it.digestAlgorithm!! }
            .toSet().toTypedArray()
    }

    override fun supportedKeyTypes(): Array<KeyType> = arrayOf(KeyType.EC, KeyType.RSA)

    override fun supportedSignatureAlgorithms(): Array<SignatureAlgorithm> =
        arrayOf(
            SignatureAlgorithm.ECDSA_SHA256,
            SignatureAlgorithm.ECDSA_SHA384,
            SignatureAlgorithm.ECDSA_SHA512,
            SignatureAlgorithm.RSA_SHA256,
            SignatureAlgorithm.RSA_SHA384,
            SignatureAlgorithm.RSA_SHA512,
            SignatureAlgorithm.RSA_SSA_PSS_SHA256_MGF1,
            SignatureAlgorithm.RSA_SSA_PSS_SHA384_MGF1,
            SignatureAlgorithm.RSA_SSA_PSS_SHA512_MGF1
        )

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
        if(alg?.curve?.let { isSupportedCurve(it) } == false) {
            throw IllegalArgumentException("Curve is not supported by Azure Key Vault")
        }

        val keyName = kmsKeyRef ?: "key-${java.util.UUID.randomUUID()}"
        val keyType = alg?.toKeyType() ?: com.azure.security.keyvault.keys.models.KeyType.EC

        val operations: Array<KeyOperation> = (keyOperations ?: arrayOf(KeyOperations.SIGN, KeyOperations.VERIFY))
            .map {
                it.toAzureKeyOperation()
            }
            .toTypedArray()

        val createKeyOptions = CreateKeyOptions(keyName, keyType)
            .setKeyOperations(*operations)

        val keyVaultKey = withContext(Dispatchers.IO) {
            keyClient.createKey(createKeyOptions).block()
        }

        if (keyVaultKey == null) {
            logger.debug("Failed to create key in Azure Key Vault for reference: $keyName")
            throw SignClientException("Failed to create key in Azure Key Vault")
        }

        val jwk = keyVaultKey.toJwk()
        val publicCoseKey = CoseJoseKeyMappingService.toCoseKey(jwk)

        return ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = keyVaultKey.name,
            jose = JoseKeyPair(null, jwk),
            cose = CoseKeyPair(null, publicCoseKey)
        )
    }

    private fun determineSignatureAlgorithm(keyVaultKey: KeyVaultKey): com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm {
        return when (keyVaultKey.key.keyType) {
            com.azure.security.keyvault.keys.models.KeyType.RSA -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.RS256
            com.azure.security.keyvault.keys.models.KeyType.EC -> {
                when (keyVaultKey.key.curveName) {
                    KeyCurveName.P_256 -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.ES256
                    KeyCurveName.P_384 -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.ES384
                    KeyCurveName.P_521 -> com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.ES512
                    else -> throw SignClientException("Unsupported curve: ${keyVaultKey.key.curveName}")
                }
            }
            else -> throw SignClientException("Unsupported key type: ${keyVaultKey.key.keyType}")
        }
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

        val signResult = withContext(Dispatchers.IO) {
            cryptoClient.sign(determineSignatureAlgorithm(keyVaultKey), input.sha256()).block()
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

        val algorithm = determineSignatureAlgorithm(keyVaultKey)

        val hashedInput = input.sha256()

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

    private fun AzureKeyvaultClientConfig.toClientOptions(): ClientOptions? {
        if (headers.isNullOrEmpty() && applicationId == null) {
            return null
        }
        return ClientOptions().setApplicationId(applicationId)
            .setHeaders(headers?.map { com.azure.core.util.Header(it.name, it.values) })
    }

    private fun ExponentialBackoffRetryOpts.toExponentialBackoffOptions(): ExponentialBackoffOptions {
        return ExponentialBackoffOptions()
            .setMaxRetries(maxRetries)
            .setBaseDelay(if (baseDelayInMS == null) null else Duration.ofMillis(baseDelayInMS))
            .setMaxDelay(if (maxDelayInMS == null) null else Duration.ofMillis(maxDelayInMS))
    }

    private fun CredentialOpts.toTokenCredential(tenantId: String): TokenCredential {
        return when (credentialMode) {
            CredentialMode.SERVICE_CLIENT_SECRET -> secretCredentialOpts?.toClientSecretCredential(tenantId)
                ?: throw SignClientException("No client secret options provided")

            CredentialMode.SERVICE_CLIENT_CERTIFICATE -> certificateCredentialOpts?.toClientCertificateCredential(
                tenantId
            )
                ?: throw SignClientException("No client certificate options provided")

            CredentialMode.USER_INTERACTIVE_BROWSER -> interactiveBrowserCredentialOpts?.toInteractiveBrowserCredential(
                tenantId
            )
                ?: throw SignClientException("No interactive browser options provided")

            CredentialMode.USER_USERNAME_PASSWORD -> usernamePasswordCredentialOpts?.toUsernamePasswordCredential(
                tenantId
            )
                ?: throw SignClientException("No username password options provided")
        }
    }

    private fun SecretCredentialOpts.toClientSecretCredential(tenantId: String): ClientSecretCredential {
        return ClientSecretCredentialBuilder()
            .clientId(clientId)
            .clientSecret(clientSecret)
            .tenantId(tenantId)
            .build()
    }

    private fun CertificateCredentialOpts.toClientCertificateCredential(tenantId: String): ClientCertificateCredential {
        return ClientCertificateCredentialBuilder()
            .clientId(clientId)
            .pemCertificate(pemCertificatePath)
            .tenantId(tenantId)
            .build()
    }

    private fun UsernamePasswordCredentialOpts.toUsernamePasswordCredential(tenantId: String): UsernamePasswordCredential {
        return UsernamePasswordCredentialBuilder()
            .clientId(clientId)
            .username(userName)
            .password(password)
            .tenantId(tenantId)
            .build()
    }

    private fun InteractiveBrowserCredentialOpts.toInteractiveBrowserCredential(tenantId: String): InteractiveBrowserCredential {
        return InteractiveBrowserCredentialBuilder()
            .clientId(clientId)
            .redirectUrl(redirectUrl)
            .tenantId(tenantId)
            .build()
    }

    private fun ByteArray.toBase64UrlString(): String =
        java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(this)

    private fun KeyOperation.toKeyOperations(): KeyOperations {
        return when (this) {
            KeyOperation.ENCRYPT -> KeyOperations.ENCRYPT
            KeyOperation.DECRYPT -> KeyOperations.DECRYPT
            KeyOperation.SIGN -> KeyOperations.SIGN
            KeyOperation.VERIFY -> KeyOperations.VERIFY
            KeyOperation.WRAP_KEY -> KeyOperations.WRAP_KEY
            KeyOperation.UNWRAP_KEY -> KeyOperations.UNWRAP_KEY
            else -> {
                throw SignClientException("Unsupported key operation for Azure Key Vault")
            }
        }
    }

    private fun KeyVaultKey.toJwk(): Jwk {
        val jsonWebKey = this.key
        return Jwk.Builder()
            .withKid(jsonWebKey.id) // Key ID
            .withKty(jsonWebKey.keyType?.toString()?.let { JwaKeyType.Static.fromValue(it) }) // Key type
            .withAlg(
                jsonWebKey.keyOps?.firstOrNull()?.let { JwaAlgorithm.Static.fromValue(it.toString()) }) // Algorithm
            .withN(jsonWebKey.n?.toBase64UrlString())
            .withE(jsonWebKey.e?.toBase64UrlString())
            .withX(jsonWebKey.x?.toBase64UrlString())
            .withY(jsonWebKey.y?.toBase64UrlString())
            .withKeyOps(jsonWebKey.keyOps?.map { JoseKeyOperations.Static.fromValue(it.toKeyOperations().jose.value) }
                ?.toTypedArray())
            .build()
    }

    private fun KeyOperations.toAzureKeyOperation(): KeyOperation {
        return when (this) {
            KeyOperations.ENCRYPT -> KeyOperation.ENCRYPT
            KeyOperations.DECRYPT -> KeyOperation.DECRYPT
            KeyOperations.SIGN -> KeyOperation.SIGN
            KeyOperations.UNWRAP_KEY -> KeyOperation.UNWRAP_KEY
            KeyOperations.VERIFY -> KeyOperation.VERIFY
            KeyOperations.WRAP_KEY -> KeyOperation.WRAP_KEY
            KeyOperations.DERIVE_BITS -> throw SignClientException("Azure Key Vault does not support DERIVE_BITS operation")
            KeyOperations.DERIVE_KEY -> throw SignClientException("Azure Key Vault does not support DERIVE_KEY operation")
            KeyOperations.MAC_CREATE -> throw SignClientException("Azure Key Vault does not support MAC_CREATE operation")
            KeyOperations.MAC_VERIFY -> throw SignClientException("Azure Key Vault does not support MAC_VERIFY operation")
        }
    }

    private fun SignatureAlgorithm.toKeyType(): com.azure.security.keyvault.keys.models.KeyType {
        return when (this) {
            SignatureAlgorithm.ECDSA_SHA256,
            SignatureAlgorithm.ECDSA_SHA384,
            SignatureAlgorithm.ECDSA_SHA512 -> com.azure.security.keyvault.keys.models.KeyType.EC

            SignatureAlgorithm.RSA_SHA256,
            SignatureAlgorithm.RSA_SHA384,
            SignatureAlgorithm.RSA_SHA512,
            SignatureAlgorithm.RSA_SSA_PSS_SHA256_MGF1,
            SignatureAlgorithm.RSA_SSA_PSS_SHA384_MGF1,
            SignatureAlgorithm.RSA_SSA_PSS_SHA512_MGF1 -> com.azure.security.keyvault.keys.models.KeyType.RSA

            SignatureAlgorithm.ED25519,
            SignatureAlgorithm.ES256K,
            SignatureAlgorithm.HMAC_SHA256,
            SignatureAlgorithm.HMAC_SHA384,
            SignatureAlgorithm.HMAC_SHA512,
            SignatureAlgorithm.RSA_RAW,
            SignatureAlgorithm.RSA_SSA_PSS_RAW_MGF1 -> throw IllegalArgumentException("Key type not supported for Azure Key Vault")
        }
    }

    private fun ByteArray.sha256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }
}
