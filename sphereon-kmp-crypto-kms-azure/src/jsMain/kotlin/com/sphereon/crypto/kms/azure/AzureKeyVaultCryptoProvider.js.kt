package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.CoseJoseKeyMappingService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.SignClientException
import com.sphereon.crypto.generic.CoseKeyPair
import com.sphereon.crypto.generic.JoseKeyPair
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
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
import com.sphereon.kmp.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.withContext
import kotlin.js.Promise


external interface KeyResponse {
    val key: KeyDetails
    val id: String
    val name: String
    val keyOperations: Array<String>
    val keyType: String
    val properties: KeyProperties
}

external interface KeyDetails {
    val kid: String
    val kty: String
    val keyOps: Array<String>
    val n: ByteArray // Use ByteArray for buffers
    val e: ByteArray
}

external interface KeyProperties {
    val tags: dynamic // Can be null/undefined
    val enabled: Boolean
    val notBefore: dynamic // Can be null/undefined
    val expiresOn: dynamic // Can be null/undefined
    val createdOn: String // ISO date string
    val updatedOn: String // ISO date string
    val recoverableDays: Int
    val recoveryLevel: String
    val exportable: Boolean
    val releasePolicy: dynamic // Can be null/undefined
    val hsmPlatform: String
    val vaultUrl: String
    val version: String
    val name: String
    val managed: dynamic // Can be null/undefined
    val id: String
}

@JsModule("@azure/identity")
@JsNonModule
external object AzureIdentity {
    class ClientSecretCredential(tenantId: String, clientId: String, clientSecret: String)
}
@JsModule("@azure/keyvault-keys")
@JsNonModule
external class AzureKeyvaultKeys(url: String, credential: AzureIdentity.ClientSecretCredential) {
    class KeyClient(keyvaultUrl: String, credential: AzureIdentity.ClientSecretCredential) {
        fun createKey(keyName: String, keyType: String): Promise<KeyResponse>
    }
    class KeyType
}

@JsModule("@azure/keyvault-secrets")
@JsNonModule
external class AzureKeyvaultSecrets(url: String, credential: AzureIdentity.ClientSecretCredential) {
    class SecretClient(keyvaultUrl: String, credential: AzureIdentity.ClientSecretCredential)
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

        hasCerts = config.hsmType !== HSMType.MANAGED_HSM
        isManagedHsm = config.hsmType === HSMType.MANAGED_HSM

        console.log(keyClient)


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

    private fun String.toJwaKeyType(): JwaKeyType {
        return when (this) {
            "RSA" -> JwaKeyType.RSA
            "EC" -> JwaKeyType.EC
            else -> throw IllegalArgumentException("Unsupported key type: $this")
        }
    }

    private fun String.toJoseKeyOperationsArray(): JoseKeyOperations {
            return when (this) {
                "sign" -> JoseKeyOperations.SIGN
                "verify" -> JoseKeyOperations.VERIFY
                "encrypt" -> JoseKeyOperations.ENCRYPT
                "decrypt" -> JoseKeyOperations.DECRYPT
                "wrapKey" -> JoseKeyOperations.WRAP_KEY
                "unwrapKey" -> JoseKeyOperations.UNWRAP_KEY
                else -> throw IllegalArgumentException("Unsupported key operation: $this")
            }
    }

    private fun KeyResponse.toJwk(): Jwk {
        return Jwk(
            kid = key.kid,
            kty = key.kty.toJwaKeyType(),
            key_ops = key.keyOps.map { it.toJoseKeyOperationsArray() }.toTypedArray(),
            n = key.n.toString(),
            e = key.e.toString()
        )
    }

    private fun SignatureAlgorithm.toKeyTypeString(): String {
        return when (this) {
            SignatureAlgorithm.RSA_SHA256 -> "RSA"
            SignatureAlgorithm.RSA_SHA384 -> "RSA"
            SignatureAlgorithm.RSA_SHA512 -> "RSA"
            SignatureAlgorithm.ECDSA_SHA256 -> "EC"
            SignatureAlgorithm.ECDSA_SHA384 -> "EC"
            SignatureAlgorithm.ECDSA_SHA512 -> "EC"
            else -> throw IllegalArgumentException("Unsupported signature algorithm: $this")
        }
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


        //        val keyType = alg?.toKeyType() ?: KeyType.EC
//
//        val operations: Array<KeyOperation> = (keyOperations ?: arrayOf(KeyOperations.SIGN, KeyOperations.VERIFY))
//            .map {
//                it.toAzureKeyOperation()
//            }
//            .toTypedArray()
//
//        val createKeyOptions = CreateKeyOptions(keyName, keyType)
//            .setKeyOperations(*operations)
//

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

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        TODO("Not yet implemented")
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        TODO("Not yet implemented")
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
