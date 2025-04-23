package com.sphereon.crypto.kms.aws

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.*
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
            val algName = when (signingAlgorithm) {
                SignatureAlgorithm.ED25519 -> "Ed25519"
                else -> signingAlgorithm.toString()
            }
            throw IllegalArgumentException("Signature algorithm $algName is not supported by AWS KMS")
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

        val kid = createKeyResponse.keyMetadata?.keyId
            ?: throw IllegalStateException("Failed to retrieve key ID after creation")

        if (kmsKeyRef != null && (createKeyResponse.keyMetadata?.arn != null || createKeyResponse.keyMetadata?.keyId != null)) {
            val aliasResponse = client.createAlias(
                CreateAliasRequest {
                    this.aliasName = if (kmsKeyRef.startsWith("alias/")) kmsKeyRef else "alias/$kmsKeyRef"
                    this.targetKeyId = createKeyResponse.keyMetadata?.keyId ?: createKeyResponse.keyMetadata?.arn
                })
        }


        // Get public key in DER format
        val getPublicKeyResponse = client.getPublicKey(GetPublicKeyRequest {
            keyId = kid
        })
        val publicKeyDer: ByteArray = getPublicKeyResponse.publicKey
            ?: throw IllegalStateException("Public key not found")

        return toManagedKeyPair(publicKeyDer, kid, kmsKeyRef ?: kid)
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

        val client = getAWSKmsClient()
        val signResponse = client.sign(SignRequest {
            this.keyId = determineAwsKeyId(keyInfo)
            message = input
            this.signingAlgorithm = algorithm.toSigningAlgorithmSpec()
        })

        return signResponse.signature!!
    }

    override suspend fun isValidRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        signature: ByteArray
    ): Boolean {
        val algorithm = keyInfo.signatureAlgorithm

        val client = getAWSKmsClient()
        try {
            val verifyResponse = client.verify(VerifyRequest {
                this.keyId = determineAwsKeyId(keyInfo)
                message = input
                this.signature = signature
                this.signingAlgorithm = algorithm?.toSigningAlgorithmSpec()
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

                client.getPublicKey(GetPublicKeyRequest { keyId = determineAwsKeyId(keyInfo) }).publicKey?.let {

                    toManagedKeyPair(
                        it,
                        keyInfo.kid!!,
                        keyInfo.kmsKeyRef!!
                    ).joseToManagedKeyInfo()
                }
                    ?: throw IllegalArgumentException(
                        "Key with ID ${keyInfo.kmsKeyRef} not found in AWS KMS"
                    )
            }
        }
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

fun determineAwsKeyId(keyInfo: IKeyInfo<*>): String {
    val keyIdArg = keyInfo.kmsKeyRef ?: keyInfo.kid ?: throw IllegalArgumentException("KMS key reference is required")
    return (if (keyInfo.kmsKeyRef == keyInfo.kid || keyInfo.kmsKeyRef == null || keyIdArg.startsWith("alias/")) keyIdArg else "alias/$keyIdArg").also {
        logger.debug("Determined key ID for keyref: ${keyInfo.kmsKeyRef}, kid: ${keyInfo.kid} to be $it")
    }
}

