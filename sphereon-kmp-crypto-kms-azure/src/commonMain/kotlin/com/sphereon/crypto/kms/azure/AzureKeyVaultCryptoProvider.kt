package com.sphereon.crypto.kms.azure

expect class AzureKeyVaultCryptoProvider(
    config: AzureKeyVaultClientConfig
) : BaseAzureKeyvaultCryptoProvider
