package com.sphereon.crypto.kms.aws

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

private const val SECOND = 1000L
private const val ONE = 1
private const val FIFTEEN = 15

@Serializable
@JsExport
data class AwsKmsClientConfig(
    val applicationId: String = "aws-kms",
    val region: String,
    val credentialOpts: CredentialOpts,
    val exponentialBackoffRetryOpts: ExponentialBackoffRetryOpts? = null,
)

@Serializable
@JsExport
enum class CredentialMode {
    ACCESS_KEY,
    PROFILE,
    CONTAINER,
    INSTANCE
}

@Serializable
@JsExport
data class CredentialOpts(
    val credentialMode: CredentialMode,
    val accessKeyCredentialOpts: AccessKeyCredentialOpts? = null,
    val profileCredentialOpts: ProfileCredentialOpts? = null,
)

@Serializable
@JsExport
data class ExponentialBackoffRetryOpts(
    val maxRetries: Int? = 10,
    val baseDelayInMS: Long? = ONE * SECOND,
    val maxDelayInMS: Long? = FIFTEEN * SECOND
)

/**
 *  Authenticate with access key ID and secret key.
 */
@Serializable
@JsExport
data class AccessKeyCredentialOpts(
    val accessKeyId: String,
    val secretAccessKey: String,
    val sessionToken: String? = null
)

/**
 *  Authenticate with a named profile from AWS config.
 */
@Serializable
@JsExport
data class ProfileCredentialOpts(
    val profileName: String,
)
