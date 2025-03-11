package com.sphereon.crypto.kms.model

import kotlinx.serialization.Serializable

/**
 * Represents the number of milliseconds in one second.
 * Used as a time unit constant for delay configurations, such as in retry mechanisms.
 */
private const val SECOND = 1000L
/**
 * A constant value representing the integer 1.
 *
 * This constant is used for calculations or initializations where the value 1 is required,
 * such as defining base values or scaling factors in various configuration settings.
 */
private const val ONE = 1
/**
 * Constant representing the numeric value 15.
 * It is used primarily in duration or delay calculations, such as in retry mechanisms
 * with exponential backoff strategies.
 */
private const val FIFTEEN = 15

/**
 * Configuration class for the AWS KMS client used in cryptographic operations.
 *
 * @param applicationId Identifier for the AWS KMS client application. Defaults to "aws-kms".
 * @param region The AWS region where the KMS operations are performed.
 * @param credentialOpts Options for configuring authentication credentials required for accessing AWS KMS.
 * @param exponentialBackoffRetryOpts Configuration options for exponential backoff retries in case of transient failures.
 */
@Serializable
data class AwsKmsClientConfig(
    val applicationId: String = "aws-kms",
    val region: String,
    val credentialOpts: CredentialOpts,
    val exponentialBackoffRetryOpts: ExponentialBackoffRetryOpts? = null,
)

/**
 * Represents the different modes of authentication and configuration used for accessing
 * AWS KMS (Key Management Service). This enumeration defines the source or mechanism
 * through which credentials are provided to interact with AWS services.
 */
@Serializable
enum class CredentialMode {
    /**
     * Represents the credential mode option "ACCESS_KEY".
     * This mode is used when AWS credentials are provided explicitly via an access key ID and a secret access key.
     * Commonly used in scenarios requiring direct authentication for AWS resources through static credentials.
     */
    ACCESS_KEY,
    /**
     * Represents the PROFILE mode of credential authentication.
     * This mode utilizes a named profile from the shared AWS configuration file
     * to authenticate with AWS services.
     */
    PROFILE,
    /**
     * Represents the `CONTAINER` mode in the `CredentialMode` enumeration.
     * This mode is used to signify that credentials are expected to be sourced
     * from within a containerized environment.
     */
    CONTAINER,
    /**
     * Represents the use of instance metadata-based credentials in the `AwsKmsClientConfig` configuration.
     * This mode leverages the credentials provided by the AWS instance profile, typically used
     * when running within an AWS-hosted environment like EC2 or ECS.
     */
    INSTANCE
}

/**
 * Represents options for configuring credentials when interacting with AWS KMS.
 *
 * This class allows selecting a specific credential mode and optionally providing
 * the corresponding credential parameters, such as access key credentials or profile credentials.
 *
 * @property credentialMode The mode that determines how AWS credentials are provided.
 * @property accessKeyCredentialOpts Optional configuration for access key-based credentials.
 * @property profileCredentialOpts Optional configuration for profile-based credentials.
 */
@Serializable
data class CredentialOpts(
    val credentialMode: CredentialMode,
    val accessKeyCredentialOpts: AccessKeyCredentialOpts? = null,
    val profileCredentialOpts: ProfileCredentialOpts? = null,
)

/**
 * Configuration options for implementing exponential backoff retry logic.
 * This configuration is typically used to manage retry behaviors in operations
 * where transient errors are expected, such as network requests or other potentially
 * unstable operations.
 *
 * @property maxRetries The maximum number of retry attempts to be made. Defaults to 10.
 * @property baseDelayInMS The initial delay, in milliseconds, before the first retry attempt.
 * This value is usually multiplied by a backoff factor for subsequent retries. Defaults to 1000 ms (1 second).
 * @property maxDelayInMS The maximum delay, in milliseconds, allowed between retries.
 * This helps cap the retry backoff growth. Defaults to 15000 ms (15 seconds).
 */
@Serializable
data class ExponentialBackoffRetryOpts(
    val maxRetries: Int? = 10,
    val baseDelayInMS: Long? = ONE * SECOND,
    val maxDelayInMS: Long? = FIFTEEN * SECOND
)

/**
 * Represents the options for providing AWS Access Key credentials.
 *
 * This class encapsulates the credentials required to authenticate with AWS services
 * using Access Key ID and Secret Access Key. It also provides an optional session token
 * for scenarios requiring temporary security credentials.
 *
 * @property accessKeyId The Access Key ID provided by AWS for authentication.
 * @property secretAccessKey The Secret Access Key associated with the Access Key ID.
 * @property sessionToken The optional session token for temporary security credentials,
 * if applicable.
 */
@Serializable
data class AccessKeyCredentialOpts(
    val accessKeyId: String,
    val secretAccessKey: String,
    val sessionToken: String? = null
)

/**
 * Represents the configuration options for AWS profile-based credentials.
 *
 * @property profileName The name of the AWS profile to be used for authentication. The profile name
 * corresponds to the configuration set in the user's AWS credentials file.
 */
@Serializable
data class ProfileCredentialOpts(
    val profileName: String,
)
