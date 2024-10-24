package com.sphereon.crypto.providers


import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.CurveMapping
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.generic.KeyTypeMapping
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
            arrayOf(DigestAlg.SHA256, DigestAlg.SHA384, DigestAlg.SHA512),
            digests
        )
    }

    @Test
    fun testGenerateKeyAsync() = runTest {
        val curve = CurveMapping.P_256
        val result = ecdsaCryptoProvider.generateKeyAsync(curve = curve, alg = SignatureAlgorithm.ECDSA_SHA256)
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
        assertContentEquals(arrayOf(KeyTypeMapping.EC), keyTypes)
    }

    @Test
    fun testSupportedAlg() {
        val algorithms = ecdsaCryptoProvider.supportedSignatureAlgorithms()
        assertContentEquals(
            arrayOf(SignatureAlgorithm.ECDSA_SHA256, SignatureAlgorithm.ECDSA_SHA384, SignatureAlgorithm.ECDSA_SHA512),
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
            override val signatureAlgorithm: SignatureAlgorithm? = null
            override val key: IKey? = null
            override val opts: Map<*, *>? = null
        }
        val exception = assertFailsWith<IllegalArgumentException> {
            ecdsaCryptoProvider.resolvePublicKey(keyInfo)
        }
        assertEquals("Key needs to be present in current version. Cannot resolve by kid", exception.message)
    }

}
