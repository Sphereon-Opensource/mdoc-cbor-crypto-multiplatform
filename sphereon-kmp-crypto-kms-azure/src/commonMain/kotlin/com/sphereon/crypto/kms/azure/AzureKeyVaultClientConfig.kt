package com.sphereon.crypto.kms.azure

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

private const val SECOND = 1000L
private const val ONE = 1
private const val FIFTEEN = 15

@Serializable
@JsExport
data class AzureKeyvaultClientConfig(
    val applicationId: String = "azure-keyvault",
    val keyvaultUrl: String,
    val tenantId: String,
    val credentialOpts: CredentialOpts,
    val headers: List<Header>? = null,
    val exponentialBackoffRetryOpts: ExponentialBackoffRetryOpts? = null,
)

@Serializable
@JsExport
enum class CredentialMode(val credentialType: CredentialType) {
    SERVICE_CLIENT_SECRET(CredentialType.SERVICE),
    SERVICE_CLIENT_CERTIFICATE(CredentialType.SERVICE),
    USER_INTERACTIVE_BROWSER(CredentialType.USER),
    USER_USERNAME_PASSWORD(CredentialType.USER)
}

@Serializable
@JsExport
data class CredentialOpts(
    val credentialMode: CredentialMode,
    val secretCredentialOpts: SecretCredentialOpts? = null,
    val certificateCredentialOpts: CertificateCredentialOpts? = null,
    val interactiveBrowserCredentialOpts: InteractiveBrowserCredentialOpts? = null,
    val usernamePasswordCredentialOpts: UsernamePasswordCredentialOpts? = null
)

@Serializable
@JsExport
enum class CredentialType {
    SERVICE, USER
}

@Serializable
@JsExport
data class Header(
    val name: String,
    val values: List<String>? = mutableListOf()
)

@Serializable
@JsExport
data class ExponentialBackoffRetryOpts(
    val maxRetries: Int? = 10,
    val baseDelayInMS: Long? = ONE * SECOND,
    val maxDelayInMS: Long? = FIFTEEN * SECOND
)

/**
 *  Authenticate with client secret.
 */
@Serializable
@JsExport
data class SecretCredentialOpts(
    val clientId: String,
    val clientSecret: String,
)

/**
 *  Authenticate with a client certificate.
 */
@Serializable
@JsExport
data class CertificateCredentialOpts(
    val clientId: String,
    val pemCertificatePath: String,
)

/**
 * Authenticate interactively in the browser.
 */
@Serializable
@JsExport
data class InteractiveBrowserCredentialOpts(
    val clientId: String,
    val redirectUrl: String
)

/**
 * Authenticate with username, password.
 */
@Serializable
@JsExport
data class UsernamePasswordCredentialOpts(
    val clientId: String,
    val userName: String,
    val password: String
)
