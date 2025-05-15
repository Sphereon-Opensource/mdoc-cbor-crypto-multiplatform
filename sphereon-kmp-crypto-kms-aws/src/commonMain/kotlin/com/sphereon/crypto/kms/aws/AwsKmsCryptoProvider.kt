package com.sphereon.crypto.kms.aws

import com.sphereon.crypto.kms.model.KeyProviderSettings

expect class AwsKmsCryptoProvider(
    settings: KeyProviderSettings
) : BaseAwsKmsCryptoProvider
