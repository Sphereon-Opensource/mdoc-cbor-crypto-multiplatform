package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.generic.CoseKeyPair
import com.sphereon.crypto.generic.JoseKeyPair
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
import com.sphereon.kmp.Uuid
import kotlinx.coroutines.await
import kotlin.js.Promise

@JsModule("@azure/identity")
@JsNonModule
external object AzureIdentity {
    class ClientSecretCredential(tenantId: String, clientId: String, clientSecret: String)
}

@JsModule("@azure/keyvault-keys")
@JsNonModule
external class AzureKeyvaultKeys(url: String, credential: AzureIdentity.ClientSecretCredential) {
    interface CreateEcKeyOptions {
        var curve: String?
    }

    class KeyClient(keyvaultUrl: String, credential: AzureIdentity.ClientSecretCredential) {
        fun createEcKey(keyName: String, options: CreateEcKeyOptions): Promise<AzureKeyvaultKey>
        fun getKey(keyName: String): Promise<AzureKeyvaultKey>
    }

    class CryptographyClient(key: AzureKeyvaultKey, credential: AzureIdentity.ClientSecretCredential) {
        fun signData(
            algorithm: String,
            data: ByteArray
        ): Promise<AzureKeyvaultSignDataResult>

        fun verifyData(
            algorithm: String,
            data: ByteArray,
            signature: ByteArray
        ): Promise<AzureKeyvaultVerifyDataResult>
    }
}

actual class AzureKeyvaultCryptoProvider actual constructor(
    config: AzureKeyvaultClientConfig
) : BaseAzureKeyvaultCryptoProvider(config.applicationId) {
    private val keyClient: AzureKeyvaultKeys.KeyClient // Representing the Azure Key Vault client
    private val clientSecretCredential: AzureIdentity.ClientSecretCredential

    init {
        if (config.credentialOpts.secretCredentialOpts == null) {
            throw IllegalArgumentException("Azure Key Vault requires a secret credential")
        }
        val credential =
            AzureIdentity.ClientSecretCredential(
                config.tenantId,
                config.credentialOpts.secretCredentialOpts.clientId,
                config.credentialOpts.secretCredentialOpts.clientSecret
            )
        keyClient = AzureKeyvaultKeys.KeyClient(config.keyvaultUrl, credential)
        clientSecretCredential = AzureIdentity.ClientSecretCredential(
            config.tenantId,
            config.credentialOpts.secretCredentialOpts.clientId,
            config.credentialOpts.secretCredentialOpts.clientSecret
        )
    }

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
        val options: AzureKeyvaultKeys.CreateEcKeyOptions = js("{}").unsafeCast<AzureKeyvaultKeys.CreateEcKeyOptions>().apply {
            curve = signatureAlgorithm.curve?.jose?.value
        }
        val keyVaultKey = keyClient.createEcKey(keyName, options).await()
        val keyVaultJwk = keyVaultKey.toJwk()
        val publicCoseKey = CoseJoseKeyMappingService.toCoseKey(keyVaultJwk)
        val kid = keyVaultKey.key.kid
        return ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = keyVaultKey.name,
            kid = kid,
            jose = JoseKeyPair(null, keyVaultJwk),
            cose = CoseKeyPair(null, publicCoseKey)
        )
    }

    private fun String.toSignatureAlgorithm(): String {
        val algorithmMap = mapOf(
            "P-256" to "ES256",
            "P-384" to "ES384",
            "P-521" to "ES512"
        )
        return algorithmMap[this] ?: throw IllegalArgumentException("Unsupported algorithm or curve: $this")
    }

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        if (keyInfo.kmsKeyRef === null) {
            throw IllegalArgumentException("Key reference is required")
        }
        val azureKey = keyClient.getKey(keyInfo.kmsKeyRef.toString()).await()
        val cryptographyClient = AzureKeyvaultKeys.CryptographyClient(azureKey, clientSecretCredential)
        val signature = cryptographyClient.signData(azureKey.key.crv.toSignatureAlgorithm(), input).await()
        return signature.result
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        val azureKey = keyClient.getKey(keyInfo.kmsKeyRef.toString()).await()
        val cryptographyClient = AzureKeyvaultKeys.CryptographyClient(azureKey, clientSecretCredential)
        val verifyResult =
            cryptographyClient.verifyData(azureKey.key.crv.toSignatureAlgorithm(), input, signature).await()
        return verifyResult.result
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
