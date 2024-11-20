package com.sphereon.crypto.kms

import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.Logger
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.EnabledIf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val logger = Logger("sphereon:kmp:kms:azure-keyvault:test")

@EnabledIf("checkForAzureKeyVaultCredentials")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AzureKeyVaultProviderTest {
    private lateinit var azureKeyVaultCryptoProvider: AzureKeyVaultCryptoProvider

    companion object {
        @JvmStatic
        fun checkForAzureKeyVaultCredentials(): Boolean {
            val envKeys = arrayOf(
                BuildKonfig.AZURE_KEYVAULT_URL,
                BuildKonfig.AZURE_KEYVAULT_TENANT_ID,
                BuildKonfig.AZURE_KEYVAULT_CLIENT_ID,
                BuildKonfig.AZURE_KEYVAULT_CLIENT_SECRET
            )
            envKeys.forEach {
                if (it == null) {
                    logger.debug("Azure Key Vault credentials not found. Skipping tests.")
                    return false
                }
            }
            return true
        }
    }

    @BeforeTest
    fun setUp() {
        val azureConfig = AzureKeyvaultClientConfig(
            keyvaultUrl = BuildKonfig.AZURE_KEYVAULT_URL!!,
            tenantId = BuildKonfig.AZURE_KEYVAULT_TENANT_ID!!,
            credentialOpts = CredentialOpts(
                credentialMode = CredentialMode.SERVICE_CLIENT_SECRET, // Use a client id and secret to authenticate as an app
                secretCredentialOpts = SecretCredentialOpts(
                    clientId = BuildKonfig.AZURE_KEYVAULT_CLIENT_ID!!,
                    clientSecret = BuildKonfig.AZURE_KEYVAULT_CLIENT_SECRET!!
                )
            ),
            hsmType = HSMType.KEYVAULT, // Either KEYVAULT as HSM (FIPS140 Level-2), or MANAGED_HSM
            applicationId = "azure-keyvault-test", // This can be randomly choosen
            exponentialBackoffRetryOpts = ExponentialBackoffRetryOpts(
                maxRetries = 10, // let's try max 10 times
                baseDelayInMS = 500, // Wait 0,5 seconds the first time
                maxDelayInMS = 15000 // Wait for max 15 seconds eventually
            )
        )
        azureKeyVaultCryptoProvider = AzureKeyVaultCryptoProvider(
            id = "azure-keyvault-test",
            config = azureConfig
        )

        azureKeyVaultCryptoProvider = AzureKeyVaultCryptoProvider(id = "test-azure-key-vault", config = azureConfig)
    }

    @Test
    fun testSupportedCurves() {
        val curves = azureKeyVaultCryptoProvider.supportedCurves()
        assertContentEquals(
            arrayOf(Curve.P_256, Curve.Secp256k1, Curve.P_384, Curve.P_521),
            curves
        )
    }

    @Test
    fun testSupportedKeyTypes() {
        val keyTypes = azureKeyVaultCryptoProvider.supportedKeyTypes()
        assertContentEquals(
            arrayOf(KeyType.EC, KeyType.RSA),
            keyTypes
        )
    }

    @Test
    fun testSupportedDigests() {
        val digests = azureKeyVaultCryptoProvider.supportedDigests()
        assertContentEquals(
            arrayOf(DigestAlg.SHA256, DigestAlg.SHA384, DigestAlg.SHA512),
            digests
        )
    }

    @Test
    fun testGenerateECKeyAsync() = runTest {
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(
            alg = SignatureAlgorithm.ECDSA_SHA256, keyOperations = arrayOf(
                KeyOperations.SIGN, KeyOperations.VERIFY
            )
        )
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
    }

    @Test
    fun testGenerateRSAKeyAsync() = runTest {
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(
            alg = SignatureAlgorithm.RSA_SHA256, keyOperations = arrayOf(
                KeyOperations.SIGN, KeyOperations.VERIFY, KeyOperations.WRAP_KEY, KeyOperations.UNWRAP_KEY
            )
        )
        assertNotNull(managedKeyPair)
        assertNotNull(managedKeyPair.cborToManagedKeyInfo().key.kid)
    }

    @Test
    fun testValidRawSignatureAndVerification() = runTest {
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = azureKeyVaultCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo,
            input = "test".encodeToByteArray(),
            false
        )
        assertNotNull(signature)
        val verification = azureKeyVaultCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo,
            signature = signature,
            input = "test".encodeToByteArray()
        )
        assertTrue(verification)
    }

    @Test
    fun testInvalidRawSignatureAndVerification() = runTest {
        val managedKeyPair = azureKeyVaultCryptoProvider.generateKeyAsync(alg = SignatureAlgorithm.ECDSA_SHA256)
        val keyInfo = managedKeyPair.joseToManagedKeyInfo()
        assertNotNull(keyInfo)
        val signature = azureKeyVaultCryptoProvider.createRawSignatureAsync(
            keyInfo = keyInfo,
            input = "test".encodeToByteArray(),
            false
        )
        assertNotNull(signature)
        val verification = azureKeyVaultCryptoProvider.isValidRawSignatureAsync(
            keyInfo = keyInfo,
            signature = signature,
            input = "test2".encodeToByteArray()
        )
        assertFalse(verification)
    }

    @Test
    fun testGenerateKeyThrowsExceptionForUnsupportedCurve() = runTest {
        val unsupportedAlg = SignatureAlgorithm.ED25519
        val exception = assertFailsWith<IllegalArgumentException> {
            azureKeyVaultCryptoProvider.generateKeyAsync(alg = unsupportedAlg)
        }
        assertEquals("Curve is not supported by Azure Key Vault", exception.message)
    }

    @Test
    fun testSupportedAlg() {
        val algorithms = azureKeyVaultCryptoProvider.supportedSignatureAlgorithms()
        assertContentEquals(
            arrayOf(
                SignatureAlgorithm.ECDSA_SHA256,
                SignatureAlgorithm.ECDSA_SHA384,
                SignatureAlgorithm.ECDSA_SHA512,
                SignatureAlgorithm.RSA_SHA256,
                SignatureAlgorithm.RSA_SHA384,
                SignatureAlgorithm.RSA_SHA512,
                SignatureAlgorithm.RSA_SSA_PSS_SHA256_MGF1,
                SignatureAlgorithm.RSA_SSA_PSS_SHA384_MGF1,
                SignatureAlgorithm.RSA_SSA_PSS_SHA512_MGF1
            ),
            algorithms
        )
    }
}
