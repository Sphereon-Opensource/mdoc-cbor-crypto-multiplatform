package com.sphereon.crypto.kms.aws

import com.sphereon.crypto.KeyEncoding
import com.sphereon.crypto.KeyVisibility
import com.sphereon.crypto.generic.*
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

    @Test
    fun testGenerateKeyAsyncECDSA_SHA256() = runTest {
        val kp = managedKeyPair ?: throw AssertionError("Managed key pair is null")
        assertNotNull(kp)
        assertNotNull(kp.cborToManagedKeyInfo().key.kid)
        assertEquals(JwaKeyType.EC, kp.jose.publicJwk.kty)
        assertEquals(JwaAlgorithm.ES256, kp.jose.publicJwk.alg)
        println(kp.jose.publicJwk.toString())

        awsKmsCryptoProvider.deleteKey(kp.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))

    }

    @Test
    fun testGenerateKeyAsyncECDSA_SHA384() = runTest {
        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
            alg = SignatureAlgorithm.ECDSA_SHA384, keyOperations = arrayOf(
                KeyOperations.SIGN, KeyOperations.VERIFY
            )
        )
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.joseToManagedKeyInfo().key.kid)
        assertEquals(JwaKeyType.EC, managedKeyPair.jose.publicJwk.kty)
        assertEquals(JwaAlgorithm.ES384, managedKeyPair.jose.publicJwk.alg)
        awsKmsCryptoProvider.deleteKey(managedKeyPair.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
    }

    @Test
    fun testGenerateKeyAsyncECDSA_SHA512() = runTest {
        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
            alg = SignatureAlgorithm.ECDSA_SHA512, keyOperations = arrayOf(
                KeyOperations.SIGN, KeyOperations.VERIFY
            )
        )
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
        assertEquals(JwaKeyType.EC, managedKeyPair.jose.publicJwk.kty)
        assertEquals(JwaAlgorithm.ES512, managedKeyPair.jose.publicJwk.alg)
        awsKmsCryptoProvider.deleteKey(managedKeyPair.toManagedKeyInfo<Jwk>(visibility = KeyVisibility.PUBLIC, keyEncoding = KeyEncoding.JOSE))
    }

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
        assertEquals("Signature algorithm Ed25519 is not supported by AWS KMS", exception.message)
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
}
