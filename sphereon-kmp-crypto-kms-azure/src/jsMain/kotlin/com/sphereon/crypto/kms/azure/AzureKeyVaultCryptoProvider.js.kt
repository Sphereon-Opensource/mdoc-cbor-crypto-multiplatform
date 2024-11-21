package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.SignClientException
import com.sphereon.crypto.generic.CoseKeyPair
import com.sphereon.crypto.generic.JoseKeyPair
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JoseKeyOperations
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.kms.IKeyManagementSystem
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService
import com.sphereon.crypto.sign.model.SignInput
import com.sphereon.crypto.sign.model.SignOutput
import com.sphereon.crypto.sign.model.Signature
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
    class KeyClient(keyvaultUrl: String, credential: AzureIdentity.ClientSecretCredential) {
        fun createKey(keyName: String, keyType: String): Promise<AzureKeyvaultKey>
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

@JsModule("crypto")
@JsNonModule
external object Crypto {
        fun randomUUID(): String
}

actual class AzureKeyvaultCryptoProvider actual constructor(
    id: String,
    config: AzureKeyvaultClientConfig
) : IKeyManagementSystem,
    IRawSignatureService,
    ISimpleSignatureService,
    BaseAzureKeyvaultCryptoProvider(id)
{
    private val keyClient: AzureKeyvaultKeys.KeyClient // Representing the Azure Key Vault client
    private val clientSecretCredential: AzureIdentity.ClientSecretCredential
    private val hasCerts: Boolean
    private val isManagedHsm: Boolean
//    private val certClient: dynamic? // Representing the Certificate client, if any

    init {
        // Create the key client using Azure JavaScript SDK
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

        hasCerts = config.hsmType !== HSMType.MANAGED_HSM
        isManagedHsm = config.hsmType === HSMType.MANAGED_HSM

//        certClient = when (config.hsmType) {
//            HSMType.MANAGED_HSM -> {
//                console.warn("Azure keyvault key provider ${config.applicationId} in mode: Managed HSM. This mode as opposed to 'keyvault' mode is untested currently!")
//                null
//            }
//            HSMType.KEYVAULT -> {
////                logger.debug("Azure keyvault key provider ${config.applicationId} in mode: Keyvault. Creating a certificate client.")
//                js("require('@azure/keyvault-certificates')").CertificateClient(
//                    config.keyvaultUrl,
//                    config.credentialOpts.toTokenCredential(config.tenantId)
//                )
//            }
//        }
    }

    override suspend fun generateKeyAsync(
        kmsKeyRef: String?,
        use: JwkUse?,
        keyOperations: Array<out KeyOperations>?,
        alg: SignatureAlgorithm?
    ): ManagedKeyPair {
        if (alg?.curve?.let { isSupportedCurve(it) } == false) {
            throw IllegalArgumentException("Curve is not supported by Azure Key Vault")
        }

        val keyName = kmsKeyRef ?: "key-${Crypto.randomUUID()}"

        val keyVaultKey = keyClient.createKey(keyName, alg?.toKeyTypeString() ?: SignatureAlgorithm.ECDSA_SHA256.toKeyTypeString()).await()

        if (keyVaultKey === null) {
//            logger.debug("Failed to create key in Azure Key Vault for reference: $keyName")
            throw SignClientException("Failed to create key in Azure Key Vault")
        }

        val jwk = keyVaultKey.toJwk()
        val publicCoseKey = CoseJoseKeyMappingService.toCoseKey(jwk)
//
        return ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = keyVaultKey.name as String,
            jose = JoseKeyPair(null, jwk),
            cose = CoseKeyPair(null, publicCoseKey)
        )
    }

    private fun String.toSignatureAlgorithm(): String {
        val algorithmMap = mapOf(
            "P-256" to "ES256",
            "secp256k1" to "ES256K",
            "P-384" to "ES384",
            "P-521" to "ES512",
            "RS256" to "RS256",
            "RS384" to "RS384",
            "RS512" to "RS512",
            "PS256" to "PS256",
            "PS384" to "PS384",
            "PS512" to "PS512"
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
        val verifyResult = cryptographyClient.verifyData(azureKey.key.crv.toSignatureAlgorithm(), input, signature).await()
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
