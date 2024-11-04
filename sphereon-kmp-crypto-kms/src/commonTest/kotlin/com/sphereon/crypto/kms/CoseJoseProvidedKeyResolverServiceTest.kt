package com.sphereon.crypto.kms

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IX509ServiceMarkerType
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.jose.JwaKeyType
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.kms.model.IdentifierMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CoseJoseProvidedKeyResolverServiceTest {

    private lateinit var resolverService: CoseJoseProvidedKeyResolverService<IX509ServiceMarkerType>

    @BeforeTest
    fun setUp() {
        resolverService = CoseJoseProvidedKeyResolverService()
    }

    @Test
    fun testGetId() {
        val id = resolverService.getId()
        assertEquals("jose_cose_resolver", id, "The service ID should be 'jose_cose_resolver'")
    }

    @Test
    fun testAllSupportedIdentifierMethods() {
        val methods = resolverService.allSupportedIdentifierMethods()
        assertTrue(methods.contains(IdentifierMethod.jwk), "Supported identifier methods should include 'jwk'")
        assertTrue(methods.contains(IdentifierMethod.cose_key), "Supported identifier methods should include 'cose_key'")
    }

    @Test
    fun testAllSupportedKeyTypes() {
        val keyTypes = resolverService.allSupportedKeyTypes()
        // Assuming KeyType.Static.asList contains the expected key types
        assertTrue(keyTypes.isNotEmpty(), "Supported key types should not be empty")
    }

    @Test
    fun testSupportedKeyTypesAndIdentifierMethods() {
        val map = resolverService.supportedKeyTypesAndIdentifierMethods()
        assertTrue(map.isNotEmpty(), "Supported key types and identifier methods map should not be empty")
        assertTrue(map[IdentifierMethod.jwk]?.isNotEmpty() == true, "Supported key types for 'jwk' should not be empty")
        assertTrue(map[IdentifierMethod.cose_key]?.isNotEmpty() == true, "Supported key types for 'cose_key' should not be empty")
    }

    @Test
    fun testGetSupportedKeyTypes() {
        val keyTypes = resolverService.getSupportedKeyTypes(IdentifierMethod.jwk)
        assertTrue(keyTypes.isNotEmpty(), "Supported key types for 'jwk' should not be empty")
    }

    @Test
    fun testGetSupportedIdentifierMethods() {
        // Assuming 'KeyType' has a static list of key types named 'Static'
        val keyType = KeyType.Static.asList.firstOrNull()
        assertNotNull(keyType, "Static key types list should not be empty")
        val methods = resolverService.getSupportedIdentifierMethods(keyType)
        assertTrue(methods.isNotEmpty(), "Supported identifier methods for the key type should not be empty")
    }


    @Test
    fun testResolvePublicKeyAsync() = runTest {
        // Assuming IKeyInfo and IResolvedKeyInfo are provided with mock implementations
        val mockKeyInfo = createMockKeyInfo()
        val identifierMethod = IdentifierMethod.jwk
        val trustedCerts = arrayOf("cert1", "cert2")
        val verifyX509 = false

        val resolvedKeyInfo = resolverService.resolvePublicKeyAsync(mockKeyInfo, identifierMethod, trustedCerts, verifyX509)
        assertNotNull(resolvedKeyInfo, "Resolved key info should not be null")
        // Further assertions can be made based on IResolvedKeyInfo's expected behavior
    }

    private fun createMockKeyInfo(): IKeyInfo<IKey> {
        return KeyInfo(kid = "kid", key = Jwk(kty = JwaKeyType.EC))
    }
}
