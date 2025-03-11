package com.sphereon.crypto.kms.aws

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.*
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyType
import com.sphereon.crypto.*
import com.sphereon.crypto.generic.*
import com.sphereon.crypto.jose.*
import com.sphereon.crypto.kms.model.KeyProviderSettings
import com.sphereon.kmp.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.X509EncodedKeySpec

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
        val (keySpec, curve) = when (signingAlgorithm) {
            SignatureAlgorithm.ECDSA_SHA256 -> KeySpec.EccNistP256 to Curve.P_256
            SignatureAlgorithm.ECDSA_SHA384 -> KeySpec.EccNistP384 to Curve.P_384
            SignatureAlgorithm.ECDSA_SHA512 -> KeySpec.EccNistP521 to Curve.P_521
            else -> throw IllegalArgumentException("Unsupported signature algorithm: $signingAlgorithm")
        }

        // Create key in AWS KMS
        val client = getAWSKmsClient()
        val createKeyResponse = client.createKey(CreateKeyRequest {
            keyUsage = KeyUsageType.SignVerify
            this.keySpec = keySpec
        })

        val kid = createKeyResponse.keyMetadata?.keyId
            ?: throw IllegalStateException("Failed to retrieve key ID after creation")

        // Get public key in DER format
        val getPublicKeyResponse = client.getPublicKey(GetPublicKeyRequest {
            keyId = kid
        })
        val publicKeyDer: ByteArray = getPublicKeyResponse.publicKey
            ?: throw IllegalStateException("Public key not found")

        return toManagedKeyPair(publicKeyDer, kid, curve)
    }

    private fun toManagedKeyPair(publicKeyDer: ByteArray, kid: String, curve: Curve): ManagedKeyPair {
        // Create JWK from public key
        val ecKey = createJwkFromPublicKey(publicKeyDer, kid, curve)
        val joseKeyPair = JoseKeyPair(null, ecKey.toJwk())

        return ManagedKeyPair(
            kms = getId(),
            kmsKeyRef = kid,
            kid = kid,
            jose = joseKeyPair,
            cose = CoseKeyPair(null, CoseJoseKeyMappingService.toCoseKey(ecKey.toJwk()))
        )
    }

    override suspend fun createRawSignatureAsync(
        keyInfo: IKeyInfo<*>,
        input: ByteArray,
        requireX5Chain: Boolean
    ): ByteArray {
        val keyId = keyInfo.kmsKeyRef ?: throw IllegalArgumentException("KMS key reference is required")
        val algorithm = keyInfo.signatureAlgorithm ?: throw IllegalArgumentException("Signature algorithm is required")

        val client = getAWSKmsClient()
        val signResponse = client.sign(SignRequest {
            this.keyId = keyId
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
        val keyId = keyInfo.kmsKeyRef ?: throw IllegalArgumentException("KMS key reference is required")
        val algorithm = keyInfo.signatureAlgorithm ?: throw IllegalArgumentException("Signature algorithm is required")

        val client = getAWSKmsClient()
        try {
            val verifyResponse = client.verify(VerifyRequest {
                this.keyId = keyId
                message = input
                this.signature = signature
                this.signingAlgorithm = algorithm.toSigningAlgorithmSpec()
            })
            return verifyResponse.signatureValid ?: false
        } catch (e: KmsInvalidSignatureException) {
            logger.debug("Signature validation failed: ${e.message}")
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

    private fun createJwkFromPublicKey(publicKeyDer: ByteArray, keyId: String, curve: Curve): ECKey {
        val keyFactory = KeyFactory.getInstance("EC")
        val spec = X509EncodedKeySpec(publicKeyDer)
        val publicKey = keyFactory.generatePublic(spec) as ECPublicKey

        return ECKey.Builder(curve, publicKey)
            .keyID(keyId)
            .build()
    }

    private fun ECKey.toJwk(): Jwk {
        val jwaAlg = when (this.curve) {
            Curve.P_256 -> JwaAlgorithm.ES256
            Curve.P_384 -> JwaAlgorithm.ES384
            Curve.P_521 -> JwaAlgorithm.ES512
            else -> throw IllegalArgumentException("Unsupported curve: ${this.curve}")
        }

        return Jwk.Builder()
            .withKid(this.keyID)
            .withKty(this.keyType.toJwaKeyType())
            .withAlg(jwaAlg)
            .withX(this.x.toString())
            .withY(this.y.toString())
            .withKeyOps(arrayOf(JoseKeyOperations.SIGN))
            .build()
    }

    private fun KeyType.toJwaKeyType(): JwaKeyType {
        return when (this) {
            KeyType.EC -> JwaKeyType.EC
            else -> throw IllegalArgumentException("Unsupported key type: $this")
        }
    }

    override fun listKeys(): Array<IManagedKeyInfo<*>> {
        return runBlocking {
            val client = getAWSKmsClient()
            client.listKeys().keys?.map { entry: KeyListEntry ->
                getKey(KeyInfo<IJwk>(kmsKeyRef = entry.keyId!!))
            }
        }.orEmpty().toTypedArray()


    }

    override fun getKey(keyInfo: IKeyInfo<*>): IManagedKeyInfo<*> {
        return runBlocking {
            getAWSKmsClient().use { client ->
                client.getPublicKey(GetPublicKeyRequest { keyId = keyInfo.kmsKeyRef }).publicKey?.let {
                    toManagedKeyPair(
                        it,
                        keyInfo.kmsKeyRef!!,
                        Curve.P_256
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
                client.scheduleKeyDeletion(ScheduleKeyDeletionRequest {
                    keyId = keyInfo.kmsKeyRef
                    pendingWindowInDays = 7
                }).keyState == KeyState.PendingDeletion
            }
        }
    }

    override fun keyVisibility(): KeyVisibility {
        return KeyVisibility.PUBLIC
    }
}

