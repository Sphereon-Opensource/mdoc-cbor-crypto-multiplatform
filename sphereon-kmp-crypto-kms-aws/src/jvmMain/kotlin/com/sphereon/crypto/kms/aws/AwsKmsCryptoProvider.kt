package com.sphereon.crypto.kms.aws

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.*
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import com.sphereon.crypto.*
import com.sphereon.crypto.generic.*
import com.sphereon.crypto.jose.*
import com.sphereon.crypto.kms.model.KeyProviderSettings
import com.sphereon.kmp.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.*

private val logger = Logger("sphereon:kmp:kms:aws")

actual class AwsKmsCryptoProvider actual constructor(
    settings: KeyProviderSettings
) : BaseAwsKmsCryptoProvider(settings) {

    private suspend fun getAWSKmsClient(): KmsClient {
        return withContext(Dispatchers.IO) {
            KmsClient {
                region = awsConfig.region
                credentialsProvider = StaticCredentialsProvider(
                    Credentials(
                        accessKeyId = awsConfig.credentialOpts.accessKeyCredentialOpts!!.accessKeyId,
                        secretAccessKey = awsConfig.credentialOpts.accessKeyCredentialOpts!!.secretAccessKey,
                    )
                )
            }
        }
    }

    override suspend fun generateKeyAsync(
        kmsKeyRef: String?,
        use: JwkUse?,
        keyOperations: Array<out KeyOperations>?,
        alg: SignatureAlgorithm?
    ): ManagedKeyPair {
        val signingAlgorithm = alg ?: SignatureAlgorithm.ECDSA_SHA256

        if (!isSupportedSignatureAlgorithm(signingAlgorithm)) {
            throw IllegalArgumentException("Signature algorithm ${signingAlgorithm.cryptoAlgorithm.name} is not supported by AWS KMS")
        }

        // Map signature algorithm to key spec and curve
        val (keySpec, _) = when (signingAlgorithm) {
            SignatureAlgorithm.ECDSA_SHA256 -> KeySpec.EccNistP256 to Curve.P_256
            SignatureAlgorithm.ECDSA_SHA384 -> KeySpec.EccNistP384 to Curve.P_384
            SignatureAlgorithm.ECDSA_SHA512 -> KeySpec.EccNistP521 to Curve.P_521
            else -> throw IllegalArgumentException("Unsupported signature algorithm: $signingAlgorithm")
        }

        // Create key in AWS KMS
        val client = getAWSKmsClient()
        val createKeyResponse = client.createKey(CreateKeyRequest {
            this.keyUsage = KeyUsageType.SignVerify
            this.keySpec = keySpec
        })

        val kid = createKeyResponse.keyMetadata?.keyId ?: throw IllegalStateException("Failed to retrieve key ID after creation")

        // generate random keyref if none is passed
        val validatedKeyRef = kmsKeyRef ?: "${awsConfig.applicationId}-${System.currentTimeMillis()}"
        val formattedKeyRef = if (validatedKeyRef.startsWith("alias/")) validatedKeyRef else "alias/$validatedKeyRef"

        // Always create an alias for the key, either with the provided kmsKeyRef or the generated one
        client.createAlias(
            CreateAliasRequest {
                this.aliasName = formattedKeyRef
                this.targetKeyId = kid
            })


        // Get public key in DER format
        val getPublicKeyResponse = client.getPublicKey(GetPublicKeyRequest {
            keyId = kid
        })
        val publicKeyDer: ByteArray = getPublicKeyResponse.publicKey
            ?: throw IllegalStateException("Public key not found")

        return toManagedKeyPair(publicKeyDer = publicKeyDer, kid = kid, kmsKeyRef = formattedKeyRef)
    }

    private fun toManagedKeyPair(publicKeyDer: ByteArray, kid: String, kmsKeyRef: String): ManagedKeyPair {
        // Create JWK from public key
        val jwk = createJwkFromPublicKey(publicKeyDer, kid)
        val joseKeyPair = JoseKeyPair(null, jwk)

        return ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = kmsKeyRef,
            kid = kid,
            jose = joseKeyPair,
            cose = CoseKeyPair(null, CoseJoseKeyMappingService.toCoseKey(jwk))
        )
    }

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        var algorithm = keyInfo.signatureAlgorithm
        if (algorithm == null) {
            // No alg supplied. Although the AWS SDK lists the signature param as optional it really is not. So let's lookup the key in this case
            val key = getKey(keyInfo)
            algorithm = key.signatureAlgorithm
                ?: throw IllegalArgumentException("Key does not have a signature algorithm set")
        }

        // Create a digest of the input data
        val hashedInput = MessageDigest.getInstance(algorithm.digestAlgorithm?.name).digest(input)

        val client = getAWSKmsClient()
        val signResponse = client.sign(SignRequest {
            this.keyId = determineAwsKeyId(keyInfo)
            message = hashedInput
            messageType = MessageType.Digest
            this.signingAlgorithm = algorithm.toSigningAlgorithmSpec()
        })

        return signResponse.signature!!
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        var algorithm = keyInfo.signatureAlgorithm
        if (algorithm == null) {
            // No alg supplied. Although the AWS SDK lists the signature param as optional it really is not. So let's lookup the key in this case
            val key = getKey(keyInfo)
            algorithm = key.signatureAlgorithm
                ?: throw IllegalArgumentException("Key does not have a signature algorithm set")
        }

        val client = getAWSKmsClient()
        try {
            // Create a digest of the input data
            val hashedInput = MessageDigest.getInstance(algorithm.digestAlgorithm?.name).digest(input)

            val verifyResponse = client.verify(VerifyRequest {
                this.keyId = determineAwsKeyId(keyInfo)
                message = hashedInput
                messageType = MessageType.Digest
                this.signature = signature
                this.signingAlgorithm = algorithm.toSigningAlgorithmSpec()
            })
            return verifyResponse.signatureValid
        } catch (e: KmsInvalidSignatureException) {
            logger.debug("Signature validation failed for ${determineAwsKeyId(keyInfo)}: ${e.message}")
            return false
        }
    }

    private fun SignatureAlgorithm.toSigningAlgorithmSpec(): SigningAlgorithmSpec {
        return when (this) {
            SignatureAlgorithm.ECDSA_SHA256 -> SigningAlgorithmSpec.EcdsaSha256
            SignatureAlgorithm.ECDSA_SHA384 -> SigningAlgorithmSpec.EcdsaSha384
            SignatureAlgorithm.ECDSA_SHA512 -> SigningAlgorithmSpec.EcdsaSha512
            else -> throw IllegalArgumentException("Unsupported signature algorithm: $this")
        }
    }

    /**
     * Creates a JWK (JSON Web Key) representation from the DER-encoded ECDSA public key.
     *
     * This implementation supports curves P-256, P-384, and P-521.
     *
     * @param publicKeyDer DER-encoded public key bytes.
     * @param keyId The key identifier to embed in the JWK.
     * @return A map representing the JWK.
     * @throws IllegalArgumentException if the key is not a valid EC key or if it uses an unsupported curve.
     */
    fun createJwkFromPublicKey(publicKeyDer: ByteArray, keyId: String): Jwk {
        // Parse the DER-encoded public key and cast it to an ECPublicKey.
        val keyFactory = KeyFactory.getInstance("EC")
        val keySpec = X509EncodedKeySpec(publicKeyDer)
        val publicKey = keyFactory.generatePublic(keySpec) as? ECPublicKey
            ?: throw IllegalArgumentException("Provided key is not a valid EC public key.")

        // Extract the affine coordinates (x and y)
        val ecPoint = publicKey.w
        val params = publicKey.params
        val fieldSize = params.curve.field.fieldSize
        // Compute byte length for coordinates
        val coordinateLength = (fieldSize + 7) / 8

        // Convert BigInteger to a fixed-length byte array (unsigned representation)
        fun bigIntToFixedLengthBytes(value: BigInteger, length: Int): ByteArray {
            val bytes = value.toByteArray()
            return when {
                bytes.size == length -> bytes
                bytes.size == length + 1 && bytes[0].toInt() == 0 -> bytes.copyOfRange(1, bytes.size)
                bytes.size < length -> {
                    // Left pad with zeros if necessary
                    ByteArray(length).apply {
                        System.arraycopy(bytes, 0, this, length - bytes.size, bytes.size)
                    }
                }
                else -> bytes.copyOfRange(bytes.size - length, bytes.size)
            }
        }

        val xBytes = bigIntToFixedLengthBytes(ecPoint.affineX, coordinateLength)
        val yBytes = bigIntToFixedLengthBytes(ecPoint.affineY, coordinateLength)

        // Encode x and y using Base64 URL encoding (without padding)
        val encoder = Base64.getUrlEncoder().withoutPadding()
        val xEncoded = encoder.encodeToString(xBytes)
        val yEncoded = encoder.encodeToString(yBytes)

        // Map the field size to the corresponding JWK "crv" value
        val (crv, alg) = when (fieldSize) {
            256 -> JwaCurve.P_256 to JwaAlgorithm.ES256
            384 -> JwaCurve.P_384 to JwaAlgorithm.ES384
            521 -> JwaCurve.P_521 to JwaAlgorithm.ES512
            else -> throw IllegalArgumentException("Unsupported EC curve with field size $fieldSize")
        }

        return Jwk.Builder()
            .withKid(keyId)
            .withKty(JwaKeyType.EC)
            .withAlg(alg)
            .withCrv(crv)
            .withUse(JwkUse.sig.value)
            .withX(xEncoded)
            .withY(yEncoded)
            .withKeyOps(arrayOf(JoseKeyOperations.SIGN, JoseKeyOperations.VERIFY))
            .build()
    }

    override fun listKeys(): Array<IManagedKeyInfo<*>> {
        return runBlocking {
            val client = getAWSKmsClient()
            client.listKeys().keys?.map { entry: KeyListEntry ->
                getKey(KeyInfo<IJwk>(kid = entry.keyId!!))
            }
        }.orEmpty().toTypedArray()


    }

    override fun getKey(keyInfo: IKeyInfo<*>): IManagedKeyInfo<*> {
        return runBlocking {
            getAWSKmsClient().use { client ->
                val awsKeyId = determineAwsKeyId(keyInfo)
                val response = client.getPublicKey(GetPublicKeyRequest { keyId = awsKeyId })
                val publicKey = response.publicKey
                    ?: throw IllegalArgumentException("Public key not found for key ID $awsKeyId")

                // Extract the actual key ID from the response, removing ARN prefix if present
                val actualKeyId = extractKeyIdFromArn(response.keyId ?: keyInfo.kid ?: awsKeyId)

                // Determine the key reference (alias or key ID)
                val kmsKeyRef = determineKeyReference(client, keyInfo, actualKeyId, defaultKeyId = awsKeyId)

                // Create the managed key pair and convert to managed key info
                toManagedKeyPair(publicKey, actualKeyId, kmsKeyRef).joseToManagedKeyInfo()
            }
        }
    }

    /**
     * Extracts the key ID from an ARN if it's in ARN format
     */
    private fun extractKeyIdFromArn(keyId: String): String {
        return if (keyId.startsWith("arn:aws:kms:") && keyId.contains(":key/")) {
            keyId.substringAfterLast("/")
        } else {
            keyId
        }
    }

    /**
     * Determines the key reference (alias or key ID) to use
     */
    private suspend fun determineKeyReference(
        client: KmsClient,
        keyInfo: IKeyInfo<*>,
        keyId: String,
        defaultKeyId: String
    ): String {
        // If kmsKeyRef is provided, use it
        val kmsKeyRef = keyInfo.kmsKeyRef
        if (kmsKeyRef != null) {
            return kmsKeyRef
        }

        // If looking up by kid (UUID), try to find an alias
        val kid = keyInfo.kid
        if (kid != null) {
            val aliases = client.listAliases(ListAliasesRequest { 
                this.keyId = keyId 
            }).aliases ?: emptyList()

            // Return the first alias if available
            if (aliases.isNotEmpty()) {
                return aliases[0].aliasName ?: defaultKeyId
            }
        }

        // Default to the key ID
        return defaultKeyId
    }

    override fun storeKey(keyInfo: IResolvedKeyInfo<*>, kms: String, kmsKeyRef: String): IManagedKeyInfo<*> {
        throw UnsupportedOperationException("AWS KMS does not support storing keys, it generates them only")
    }

    override fun deleteKey(keyInfo: IKeyInfo<*>): Boolean {
        return runBlocking {
            getAWSKmsClient().use { client ->
                val keyId = if (keyInfo.kid != null) keyInfo.kid else getKey(keyInfo).kid ?: determineAwsKeyId(keyInfo)  // We fetch the key first, since deletion can only happen via kid and not an alias!
                client.scheduleKeyDeletion(ScheduleKeyDeletionRequest {
                    this.keyId = keyId
                    pendingWindowInDays = 7
                }).keyState == KeyState.PendingDeletion
            }
        }
    }

    override fun keyVisibility(): KeyVisibility {
        return KeyVisibility.PUBLIC
    }
}

/**
 * Determines the AWS KMS key ID to use based on the provided key information.
 *
 * AWS KMS supports the following key identifier formats:
 * - Key ID: 1234abcd-12ab-34cd-56ef-1234567890ab
 * - Key ARN: arn:aws:kms:us-east-2:111122223333:key/1234abcd-12ab-34cd-56ef-1234567890ab
 * - Alias name: alias/ExampleAlias
 * - Alias ARN: arn:aws:kms:us-east-2:111122223333:alias/ExampleAlias
 *
 * @param keyInfo The key information containing kid and/or kmsKeyRef
 * @return The appropriate AWS KMS key identifier
 * @throws IllegalArgumentException if no key reference is provided
 */
fun determineAwsKeyId(keyInfo: IKeyInfo<*>): String {
    val keyIdArg =  keyInfo.kmsKeyRef ?: keyInfo.kid ?: throw IllegalArgumentException("KMS key reference is required")

    // If the key ID is already in one of the valid formats, return it as is
    if (isValidAwsKeyFormat(keyIdArg)) {
        return keyIdArg
    }

    // Otherwise, assume it's an alias name without the "alias/" prefix
    return "alias/$keyIdArg".also {
        logger.debug("Determined key ID for keyref: ${keyInfo.kmsKeyRef}, kid: ${keyInfo.kid} to be $it")
    }
}

/**
 * Checks if the provided key identifier is in a valid AWS KMS format.
 *
 * @param keyId The key identifier to check
 * @return true if the key identifier is in a valid format, false otherwise
 */
private fun isValidAwsKeyFormat(keyId: String): Boolean {
    // Check if it's a Key ARN
    if (keyId.startsWith("arn:aws:kms:") && keyId.contains(":key/")) {
        return true
    }

    // Check if it's an Alias ARN
    if (keyId.startsWith("arn:aws:kms:") && keyId.contains(":alias/")) {
        return true
    }

    // Check if it's an Alias name
    if (keyId.startsWith("alias/")) {
        return true
    }

    // Check if it's a Key ID (UUID format)
    val uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
    if (keyId.matches(Regex(uuidPattern, RegexOption.IGNORE_CASE))) {
        return true
    }

    return false
}
