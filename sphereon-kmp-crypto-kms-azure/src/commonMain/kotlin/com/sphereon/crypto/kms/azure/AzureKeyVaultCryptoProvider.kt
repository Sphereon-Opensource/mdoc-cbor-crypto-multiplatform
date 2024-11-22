package com.sphereon.crypto.kms.azure

expect class AzureKeyvaultCryptoProvider(
    config: AzureKeyvaultClientConfig
) : BaseAzureKeyvaultCryptoProvider
