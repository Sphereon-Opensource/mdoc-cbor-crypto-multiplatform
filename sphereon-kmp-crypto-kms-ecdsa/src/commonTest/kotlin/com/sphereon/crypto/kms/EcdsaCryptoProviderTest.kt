package com.sphereon.crypto.kms


import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.kms.provider.ecdsa.EcDSACryptoProvider
import dev.whyoleg.cryptography.CryptographyProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class EcDSACryptoProviderTest {
    private lateinit var ecdsaCryptoProvider: EcDSACryptoProvider

    @BeforeTest
    fun setUp() {
        val provider = CryptographyProvider.Default
        ecdsaCryptoProvider = EcDSACryptoProvider(id = "test-ecdsa", provider = provider)
    }

    @Test
    fun testSupportedCurves() {
        val curves = ecdsaCryptoProvider.supportedCurves()
        assertContentEquals(
            arrayOf(Curve.P_256, Curve.P_384, Curve.P_521),
            curves
        )
    }

    @Test
    fun testIsSupportedCurve() {
        assertTrue(ecdsaCryptoProvider.isSupportedCurve(Curve.P_256))
        assertFalse(ecdsaCryptoProvider.isSupportedCurve(Curve.X25519))
    }

    @Test
    fun testSupportedDigests() {
        val digests = ecdsaCryptoProvider.supportedDigests()
        assertContentEquals(
            arrayOf(DigestAlg.SHA256, DigestAlg.SHA384, DigestAlg.SHA512),
            digests
        )
    }

    @Test
    fun testGenerateKeyAsync() = runTest {
        val managedKeyPair = ecdsaCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
    }

    @Test
    fun testValidRawSignatureAndVerification() = runTest {
        val managedKeyPair = ecdsaCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = ecdsaCryptoProvider.createRawSignatureAsync(keyInfo = keyInfo, input = "test".encodeToByteArray(), false)
        assertNotNull(signature)
        val verification = ecdsaCryptoProvider.isValidRawSignatureAsync(keyInfo = keyInfo, signature = signature, input = "test".encodeToByteArray())
        assertTrue(verification)

    }

    @Test
    fun testInalidRawSignatureAndVerification() = runTest {
        val managedKeyPair = ecdsaCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = ecdsaCryptoProvider.createRawSignatureAsync(keyInfo = keyInfo, input = "test".encodeToByteArray(), false)
        assertNotNull(signature)
        val verification = ecdsaCryptoProvider.isValidRawSignatureAsync(keyInfo = keyInfo, signature = signature, input = "test2".encodeToByteArray())
        assertFalse(verification)

    }


    @Test
    fun testGenerateKeyThrowsExceptionForUnsupportedCurve() = runTest {
        val unsupportedAlg = SignatureAlgorithm.ED25519
        val exception = assertFailsWith<IllegalArgumentException> {
            ecdsaCryptoProvider.generateKeyAsync(alg = unsupportedAlg)
        }
        assertEquals("Curve ${unsupportedAlg.curve} not supported for EcDSA", exception.message)
    }

    @Test
    fun testSupportedKeyTypes() {
        val keyTypes = ecdsaCryptoProvider.supportedKeyTypes()
        assertContentEquals(arrayOf(KeyType.EC), keyTypes)
    }

    @Test
    fun testSupportedAlg() {
        val algorithms = ecdsaCryptoProvider.supportedSignatureAlgorithms()
        assertContentEquals(
            arrayOf(SignatureAlgorithm.ECDSA_SHA256, SignatureAlgorithm.ECDSA_SHA384, SignatureAlgorithm.ECDSA_SHA512),
            algorithms
        )
    }
}
