package com.sphereon.crypto.kms

import com.sphereon.crypto.sign.IRawSignatureService
import com.sphereon.crypto.sign.ISimpleSignatureService

expect class AzureKeyVaultCryptoProvider(
    id: String,
    config: AzureKeyvaultClientConfig
) : IKeyManagementSystem,
    IRawSignatureService, ISimpleSignatureService
