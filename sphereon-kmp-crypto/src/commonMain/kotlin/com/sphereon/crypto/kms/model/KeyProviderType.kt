package com.sphereon.crypto.kms.model

import kotlin.js.JsExport

/**
 * The Key Provider Types supported
 */
@JsExport
enum class KeyProviderType {
    PKCS11, PKCS12, REST, JKS, AZURE_KEYVAULT, AWS_KMS, DIGIDENTITY, MEMORY
}
