package com.sphereon.crypto.kms.aws

import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaKeyType
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AwsKmsProviderTest {
    private lateinit var awsKmsCryptoProvider: AwsKmsCryptoProvider

    @BeforeTest
    fun setUp() {
        val awsConfig = AwsKmsClientConfig(
            applicationId = "aws-kms-test",
            region = BuildKonfig.AWS_REGION!!,
            credentialOpts = CredentialOpts(
                credentialMode = CredentialMode.ACCESS_KEY,
                accessKeyCredentialOpts = AccessKeyCredentialOpts(
                    accessKeyId = BuildKonfig.AWS_ACCESS_KEY_ID!!,
                    secretAccessKey = BuildKonfig.AWS_SECRET_ACCESS_KEY!!
                )
            ),
            exponentialBackoffRetryOpts = ExponentialBackoffRetryOpts(
                maxRetries = 10, // let's try max 10 times
                baseDelayInMS = 500, // Wait 0.5 seconds the first time
                maxDelayInMS = 15000 // Wait for max 15 seconds eventually
            )
        )

        awsKmsCryptoProvider = AwsKmsCryptoProvider(config = awsConfig)
    }

    @Test
    fun testSupportedCurves() {
        val curves = awsKmsCryptoProvider.supportedCurves()
        assertContentEquals(
            arrayOf(Curve.P_256, Curve.Secp256k1, Curve.P_384, Curve.P_521), curves
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
        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
            alg = SignatureAlgorithm.ECDSA_SHA256, keyOperations = arrayOf(
                KeyOperations.SIGN, KeyOperations.VERIFY
            )
        )
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
        assertEquals(JwaKeyType.EC, managedKeyPair.jose.publicJwk.kty)
        assertEquals(JwaAlgorithm.ES256, managedKeyPair.jose.publicJwk.alg)
    }

    @Test
    fun testGenerateKeyAsyncECDSA_SHA384() = runTest {
        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(
            alg = SignatureAlgorithm.ECDSA_SHA384, keyOperations = arrayOf(
                KeyOperations.SIGN, KeyOperations.VERIFY
            )
        )
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
        assertEquals(JwaKeyType.EC, managedKeyPair.jose.publicJwk.kty)
        assertEquals(JwaAlgorithm.ES384, managedKeyPair.jose.publicJwk.alg)
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
    }

    @Test
    fun testValidRawSignatureAndVerification() = runTest {
        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = awsKmsCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo, input = "test".encodeToByteArray(), false
        )
        assertNotNull(signature)
        val verification = awsKmsCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo, signature = signature, input = "test".encodeToByteArray()
        )
        assertTrue(verification)
    }

    @Test
    fun testInvalidRawSignatureAndVerification() = runTest {
        val managedKeyPair = awsKmsCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = awsKmsCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo, input = "test".encodeToByteArray(), false
        )
        assertNotNull(signature)
        val verification = awsKmsCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo, signature = signature, input = "test2".encodeToByteArray()
        )
        assertFalse(verification)
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
