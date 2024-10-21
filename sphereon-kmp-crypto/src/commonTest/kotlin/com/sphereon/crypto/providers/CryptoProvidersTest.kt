package com.sphereon.crypto.providers


import com.sphereon.crypto.AlgorithmMapping
import com.sphereon.crypto.CurveMapping
import com.sphereon.crypto.HashAlgorithm
import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.KeyTypeMapping
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaCurve
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
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
        ecdsaCryptoProvider = EcDSACryptoProvider(provider)
    }

    @Test
    fun testSupportedCurves() {
        val curves = ecdsaCryptoProvider.supportedCurves()
        assertContentEquals(
            arrayOf(CurveMapping.P_256, CurveMapping.P_384, CurveMapping.P_521),
            curves
        )
    }

    @Test
    fun testIsSupportedCurve() {
        assertTrue(ecdsaCryptoProvider.isSupportedCurve(CurveMapping.P_256))
        assertFalse(ecdsaCryptoProvider.isSupportedCurve(CurveMapping.X25519))
    }

    @Test
    fun testSupportedDigests() {
        val digests = ecdsaCryptoProvider.supportedDigests()
        assertContentEquals(
            arrayOf(HashAlgorithm.SHA256, HashAlgorithm.SHA384, HashAlgorithm.SHA512),
            digests
        )
    }

    @Test
    fun testGenerateKeyAsync() = runTest {
        val curve = CurveMapping.P_256
        val result = ecdsaCryptoProvider.generateKeyAsync(curve = curve, alg = AlgorithmMapping.ES256)
        assertNotNull(result)
    }

    @Test
    fun testGenerateKeyThrowsExceptionForUnsupportedCurve() = runTest {
        val unsupportedCurve = CurveMapping.X25519
        val exception = assertFailsWith<IllegalArgumentException> {
            ecdsaCryptoProvider.generateKeyAsync(curve = unsupportedCurve)
        }
        assertEquals("Curve $unsupportedCurve not supported for EcDSA", exception.message)
    }

    @Test
    fun testSupportedKeyTypes() {
        val keyTypes = ecdsaCryptoProvider.supportedKeyTypes()
        assertContentEquals(arrayOf(KeyTypeMapping.EC2), keyTypes)
    }

    @Test
    fun testSupportedAlg() {
        val algorithms = ecdsaCryptoProvider.supportedAlg()
        assertContentEquals(
            arrayOf(AlgorithmMapping.ES256, AlgorithmMapping.ES384, AlgorithmMapping.ES512),
            algorithms
        )
    }

    @Test
    fun testResolvePublicKey() {
        val key = Jwk(alg = JwaAlgorithm.ES256, kty = JwaKeyType.EC, crv = JwaCurve.P_256)
        val keyInfo = KeyInfo(key = key)
        assertEquals(key, ecdsaCryptoProvider.resolvePublicKey(keyInfo))
    }

    @Test
    fun testResolvePublicKeyThrowsExceptionForMissingKey() {
        val keyInfo = object : IKeyInfo<IKey> {
            override val kid: String = "kid"
            override val key: IKey? = null
            override val opts: Map<*, *>? = null
        }
        val exception = assertFailsWith<IllegalArgumentException> {
            ecdsaCryptoProvider.resolvePublicKey(keyInfo)
        }
        assertEquals("Key needs to be present in current version. Cannot resolve by kid", exception.message)
    }

}
