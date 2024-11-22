package com.sphereon.crypto.kms.azure

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

class AzureKeyVaultProviderTest {
    private lateinit var azureKeyVaultCryptoProvider: AzureKeyvaultCryptoProvider

    @BeforeTest
    fun setUp() {
        val azureConfig = AzureKeyvaultClientConfig(
            applicationId = "azure-keyvault-test", // This can be randomly choosen
            keyvaultUrl = BuildKonfig.AZURE_KEYVAULT_URL!!,
            tenantId = BuildKonfig.AZURE_KEYVAULT_TENANT_ID!!,
            credentialOpts = CredentialOpts(
                credentialMode = CredentialMode.SERVICE_CLIENT_SECRET, // Use a client id and secret to authenticate as an app
                secretCredentialOpts = SecretCredentialOpts(
                    clientId = BuildKonfig.AZURE_KEYVAULT_CLIENT_ID!!,
                    clientSecret = BuildKonfig.AZURE_KEYVAULT_CLIENT_SECRET!!
                )
            ),
            exponentialBackoffRetryOpts = ExponentialBackoffRetryOpts(
                maxRetries = 10, // let's try max 10 times
                baseDelayInMS = 500, // Wait 0,5 seconds the first time
                maxDelayInMS = 15000 // Wait for max 15 seconds eventually
            )
        )

        azureKeyVaultCryptoProvider = AzureKeyvaultCryptoProvider(config = azureConfig)
    }

    @Test
    fun testSupportedCurves() {
        val curves = azureKeyVaultCryptoProvider.supportedCurves()
        assertContentEquals(
            arrayOf(Curve.P_256, Curve.Secp256k1, Curve.P_384, Curve.P_521), curves
        )
    }

    @Test
    fun testSupportedKeyTypes() {
        val keyTypes = azureKeyVaultCryptoProvider.supportedKeyTypes()
        assertContentEquals(
            arrayOf(KeyType.EC, KeyType.RSA), keyTypes
        )
    }

    @Test
    fun testSupportedDigests() {
        val digests = azureKeyVaultCryptoProvider.supportedDigests()
        assertContentEquals(
            arrayOf(DigestAlg.SHA256, DigestAlg.SHA384, DigestAlg.SHA512), digests
        )
    }

    @Test
    fun testGenerateKeyAsyncECDSA_SHA256() = runTest {
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(
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
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(
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
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(
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
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = azureKeyVaultCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo, input = "test".encodeToByteArray(), false
        )
        assertNotNull(signature)
        val verification = azureKeyVaultCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo, signature = signature, input = "test".encodeToByteArray()
        )
        assertTrue(verification)
    }

    @Test
    fun testInvalidRawSignatureAndVerification() = runTest {
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = azureKeyVaultCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo, input = "test".encodeToByteArray(), false
        )
        assertNotNull(signature)
        val verification = azureKeyVaultCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo, signature = signature, input = "test2".encodeToByteArray()
        )
        assertFalse(verification)
    }

    @Test
    fun testGenerateKeyThrowsExceptionForUnsupportedAlgorithm() = runTest {
        val unsupportedAlg = SignatureAlgorithm.ED25519
        val exception = assertFailsWith<IllegalArgumentException> {
            azureKeyVaultCryptoProvider.generateKeyAsync(alg = unsupportedAlg)
        }
        assertEquals("Signature algorithm ED25519 is not supported by Azure Key Vault", exception.message)
    }

    @Test
    fun testSupportedAlg() {
        val algorithms = azureKeyVaultCryptoProvider.supportedSignatureAlgorithms()
        assertContentEquals(
            arrayOf(
                SignatureAlgorithm.ECDSA_SHA256,
                SignatureAlgorithm.ECDSA_SHA384,
                SignatureAlgorithm.ECDSA_SHA512
            ), algorithms
        )
    }
}
