package com.sphereon.crypto.kms.azure

import com.sphereon.crypto.kms.IKeyManagementSystem
import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService

expect class AzureKeyVaultCryptoProvider(
    id: String = "azure-key-vault",
    config: AzureKeyvaultClientConfig
) : IKeyManagementSystem,
    IRawSignatureService,
    ISimpleSignatureService,
    BaseAzureKeyVaultCryptoProvider
