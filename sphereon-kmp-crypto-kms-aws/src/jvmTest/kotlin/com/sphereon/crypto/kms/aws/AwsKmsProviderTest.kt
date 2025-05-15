package com.sphereon.crypto.kms.aws

import aws.sdk.kotlin.services.kms.model.NotFoundException
import com.sphereon.crypto.KeyEncoding
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.generic.*
import com.sphereon.crypto.jose.IJwk
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.kms.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.*

class AwsKmsProviderTest {
    private lateinit var awsKmsCryptoProvider: AwsKmsCryptoProvider

    var managedKeyPair: ManagedKeyPair? = null

    @BeforeTest
    fun setUp() {
        val awsConfig = AwsKmsClientConfig(
            applicationId = "aws-kms-test",
            region = BuildKonfig.AWS_REGION ?: throw IllegalArgumentException("Missing AWS region env var AWS_REGION"),
            credentialOpts = CredentialOpts(
                credentialMode = CredentialMode.ACCESS_KEY,
                accessKeyCredentialOpts = AccessKeyCredentialOpts(
                    accessKeyId = BuildKonfig.AWS_ACCESS_KEY_ID ?: throw IllegalArgumentException("Missing AWS access key id env var AWS_ACCESS_KEY_ID"),
                    secretAccessKey = BuildKonfig.AWS_SECRET_ACCESS_KEY ?: throw IllegalArgumentException("Missing AWS secret access key env var AWS_SECRET_ACCESS_KEY")
                )
            ),
            exponentialBackoffRetryOpts = ExponentialBackoffRetryOpts(
                maxRetries = 10, // let's try max 10 times
                baseDelayInMS = 500, // Wait 0.5 seconds the first time
                maxDelayInMS = 15000 // Wait for max 15 seconds eventually
            )
        )
        val settings = KeyProviderSettings(id = "aws-kms-test", config = KeyProviderConfig(type = KeyProviderType.AWS_KMS, aws = awsConfig))
        awsKmsCryptoProvider = AwsKmsCryptoProvider(settings)
        runBlocking {
            managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
                alg = SignatureAlgorithm.ECDSA_SHA256, keyOperations = arrayOf(
                    KeyOperations.SIGN, KeyOperations.VERIFY
                )
            )
            println(managedKeyPair)
        }
    }

    @Test
    fun testSupportedCurves() {
        val curves = awsKmsCryptoProvider.supportedCurves()
        assertContentEquals(
            arrayOf(Curve.P_256, Curve.P_384, Curve.P_521), curves
        )
    }

    @Test
    fun testSupportedKeyTypes() {
        val keyTypes = awsKmsCryptoProvider.supportedKeyTypes()
        assertContentEquals(
            arrayOf(KeyType.EC), keyTypes
        )
    }

    @Test
    fun testSupportedDigests() {
        val digests = awsKmsCryptoProvider.supportedDigests()
        assertContentEquals(
            arrayOf(DigestAlg.SHA256, DigestAlg.SHA384, DigestAlg.SHA512), digests
        )
    }

//    @Test
//    fun testGenerateKeyAsyncECDSA_SHA256() = runTest {
//        val kp = managedKeyPair ?: throw AssertionError("Managed key pair is null")
//        assertNotNull(kp)
//        assertNotNull(kp.cborToManagedKeyInfo().key.kid)
//        assertEquals(JwaKeyType.EC, kp.jose.publicJwk.kty)
//        assertEquals(JwaAlgorithm.ES256, kp.jose.publicJwk.alg)
//        println(kp.jose.publicJwk.toString())
//
//        awsKmsCryptoProvider.deleteKey(kp.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
//
//    }
//
//    @Test
//    fun testGenerateKeyAsyncECDSA_SHA384() = runTest {
//        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
//            alg = SignatureAlgorithm.ECDSA_SHA384, keyOperations = arrayOf(
//                KeyOperations.SIGN, KeyOperations.VERIFY
//            )
//        )
//        assertNotNull(managedKeyPair)
//        assertNotNull(managedKeyPair.joseToManagedKeyInfo().key.kid)
//        assertEquals(JwaKeyType.EC, managedKeyPair.jose.publicJwk.kty)
//        assertEquals(JwaAlgorithm.ES384, managedKeyPair.jose.publicJwk.alg)
//        awsKmsCryptoProvider.deleteKey(managedKeyPair.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
//    }
//
//    @Test
//    fun testGenerateKeyAsyncECDSA_SHA512() = runTest {
//        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
//            alg = SignatureAlgorithm.ECDSA_SHA512, keyOperations = arrayOf(
//                KeyOperations.SIGN, KeyOperations.VERIFY
//            )
//        )
//        assertNotNull(managedKeyPair)
//        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
//        assertEquals(JwaKeyType.EC, managedKeyPair.jose.publicJwk.kty)
//        assertEquals(JwaAlgorithm.ES512, managedKeyPair.jose.publicJwk.alg)
//        awsKmsCryptoProvider.deleteKey(managedKeyPair.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
//    }

    @Test
    fun testValidRawSignatureAndVerification() = runTest {
//        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair!!.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = awsKmsCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo, input = "test".encodeToByteArray(), false
        )
        assertNotNull(signature)
        val verification = awsKmsCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo, signature = signature, input = "test".encodeToByteArray()
        )
        assertTrue(verification)
//        awsKmsCryptoProvider.deleteKey(managedKeyPair.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
    }

    @Test
    fun testInvalidRawSignatureAndVerification() = runTest {
//        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair!!.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = awsKmsCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo, input = "test".encodeToByteArray(), false
        )
        assertNotNull(signature)
        val verification = awsKmsCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo, signature = signature, input = "test2".encodeToByteArray()
        )
        assertFalse(verification)
//        awsKmsCryptoProvider.deleteKey(managedKeyPair.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
    }

    @Test
    fun testGenerateKeyThrowsExceptionForUnsupportedAlgorithm() = runTest {
        val unsupportedAlg = SignatureAlgorithm.ED25519
        val exception = assertFailsWith<IllegalArgumentException> {
            awsKmsCryptoProvider.generateKeyAsync(alg = unsupportedAlg)
        }
        assertEquals("Signature algorithm ED25519 is not supported by AWS KMS", exception.message)
    }

    @Test
    fun testSupportedAlg() {
        val algorithms = awsKmsCryptoProvider.supportedSignatureAlgorithms()
        assertContentEquals(
            arrayOf(
                SignatureAlgorithm.ECDSA_SHA256,
                SignatureAlgorithm.ECDSA_SHA384,
                SignatureAlgorithm.ECDSA_SHA512
            ), algorithms
        )
    }

    @Test
    fun testGetKeyUsingKid() = runTest {
        // Ensure we have a key to test with
        val keyPair = managedKeyPair ?: throw AssertionError("Managed key pair is null")
        val keyInfo = KeyInfo<IJwk>(kid = keyPair.kid)

        // Get the key using kid
        val retrievedKey = awsKmsCryptoProvider.getKey(keyInfo)

        // Verify the key was retrieved correctly
        assertNotNull(retrievedKey)
        assertEquals(keyPair.kid, retrievedKey.kid)
        assertEquals(keyPair.kmsKeyRef, retrievedKey.kmsKeyRef)
    }

    @Test
    fun testGetKeyUsingKmsKeyRef() = runTest {
        // Ensure we have a key to test with
        val keyPair = managedKeyPair ?: throw AssertionError("Managed key pair is null")

        val keyInfo = KeyInfo<IJwk>(kmsKeyRef = keyPair.kmsKeyRef)

        // Get the key using kmsKeyRef
        val retrievedKey = awsKmsCryptoProvider.getKey(keyInfo)

        // Verify the key was retrieved correctly
        assertNotNull(retrievedKey)
        assertEquals(keyPair.kid, retrievedKey.kid)
        assertEquals(keyPair.kmsKeyRef, retrievedKey.kmsKeyRef)
    }

    @Test
    fun testGetKeyNotFound() {
        // Create a key info with a non-existent key ID
        val keyInfo = KeyInfo<IJwk>(kid = "non-existent-key-id")

        // Attempt to get the key and expect an exception
        val exception = assertFailsWith<NotFoundException> {
            awsKmsCryptoProvider.getKey(keyInfo)
        }
    }

    @Test
    fun testGetKeyWithNullValues() {
        // Create a key info with null kid and kmsKeyRef
        val keyInfo = KeyInfo<IJwk>(kid = null, kmsKeyRef = null)

        // Attempt to get the key and expect an exception
        val exception = assertFailsWith<IllegalArgumentException> {
            awsKmsCryptoProvider.getKey(keyInfo)
        }

        // Verify the exception message
        assertEquals("KMS key reference is required", exception.message)
    }
}
