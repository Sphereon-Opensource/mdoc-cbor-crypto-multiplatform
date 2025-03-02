package com.sphereon.crypto.kms.aws

import kotlinx.serialization.Serializable

private const val SECOND = 1000L
private const val ONE = 1
private const val FIFTEEN = 15

@Serializable
data class AwsKmsClientConfig(
    val applicationId: String = "aws-kms",
    val region: String,
    val credentialOpts: CredentialOpts,
    val exponentialBackoffRetryOpts: ExponentialBackoffRetryOpts? = null,
)

@Serializable
enum class CredentialMode {
    ACCESS_KEY,
    PROFILE,
    CONTAINER,
    INSTANCE
}

@Serializable
data class CredentialOpts(
    val credentialMode: CredentialMode,
    val accessKeyCredentialOpts: AccessKeyCredentialOpts? = null,
    val profileCredentialOpts: ProfileCredentialOpts? = null,
)

@Serializable
data class ExponentialBackoffRetryOpts(
    val maxRetries: Int? = 10,
    val baseDelayInMS: Long? = ONE * SECOND,
    val maxDelayInMS: Long? = FIFTEEN * SECOND
)

/**
 *  Authenticate with access key ID and secret key.
 */
@Serializable
data class AccessKeyCredentialOpts(
    val accessKeyId: String,
    val secretAccessKey: String,
    val sessionToken: String? = null
)

/**
 *  Authenticate with a named profile from AWS config.
 */
@Serializable
data class ProfileCredentialOpts(
    val profileName: String,
)
