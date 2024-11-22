package com.sphereon.crypto.kms.azure

external interface AzureKeyvaultKey {
    val key: AzureKeyvaultKeyDetails
    val id: String
    val name: String
    val keyOperations: Array<String>
    val keyType: String
    val properties: KeyProperties
}

external interface AzureKeyvaultKeyDetails {
    val kid: String
    val kty: String
    val keyOps: Array<String>
    val n: ByteArray // Use ByteArray for buffers
    val e: ByteArray
    val crv: String
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

external interface AzureKeyvaultSignDataResult {
    val result: ByteArray
    val algorithm: String
    val keyID: String
}

external interface AzureKeyvaultVerifyDataResult {
    val result: Boolean
    val keyID: String
}
