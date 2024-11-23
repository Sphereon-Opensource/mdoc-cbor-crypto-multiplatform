import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.ManagedKeyInfo
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.Jwk
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.kms.azure.AzureIdentity
import com.sphereon.crypto.kms.azure.AzureKeyVaultCryptoProvider
import com.sphereon.crypto.kms.azure.AzureKeyVaultClientConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.promise
import kotlin.js.Promise

@JsExport
@JsName("AzureKeyVaultCryptoProvider")
class AzureKeyVaultCryptoProviderJS(
    config: AzureKeyVaultClientConfig
) {
    private val keyClient: AzureKeyVaultCryptoProvider
    private val clientSecretCredential: AzureIdentity.ClientSecretCredential

    init {
        if (config.credentialOpts.secretCredentialOpts == null) {
            throw IllegalArgumentException("Azure Key Vault requires a secret credential")
        }
        clientSecretCredential =
            AzureIdentity.ClientSecretCredential(
                config.tenantId,
                config.credentialOpts.secretCredentialOpts.clientId,
                config.credentialOpts.secretCredentialOpts.clientSecret
            )
        keyClient = AzureKeyVaultCryptoProvider(config)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    class GenerateKeyRequest(
        val kmsKeyRef: String? = null,
        val use: JwkUse? = null,
        val keyOperations: Array<KeyOperations>? = null,
        val alg: SignatureAlgorithm? = null
    )

    @JsName("generateKeyAsync")
    fun generateKeyAsyncJS(request: GenerateKeyRequest): Promise<ManagedKeyPair> {
        return scope.promise {
            keyClient.generateKeyAsync(request.kmsKeyRef, request.use, request.keyOperations, request.alg)
        }
    }

    class CreateRawSignatureRequest(
        val keyInfo: IKeyInfo<*>,
        val input: ByteArray,
        val requireX5Chain: Boolean? = false
    )

    @JsName("createRawSignatureAsync")
    fun createRawSignatureAsyncJS(request: CreateRawSignatureRequest): Promise<ByteArray> {
        return scope.promise {
            keyClient.createRawSignatureAsync(
                request.keyInfo,
                request.input,
                request.requireX5Chain ?: false
            )
        }
    }

    data class IsValidRawSignatureRequest(
        val keyInfo: IKeyInfo<*>,
        val input: ByteArray,
        val signature: ByteArray
    )

    @JsName("isValidRawSignatureAsync")
    fun isValidRawSignatureAsyncJS(
        request: IsValidRawSignatureRequest
    ): Promise<Boolean> {
        return scope.promise {
            keyClient.isValidRawSignatureAsync(
                request.keyInfo,
                request.input,
                request.signature
            )
        }
    }

    @JsName("fetchKeyAsync")
    fun fetchKeyAsyncJS(keyInfo: String): Promise<ManagedKeyInfo<Jwk>> {
        return scope.promise {
            keyClient.fetchKeyAsync(keyInfo)
        }
    }
}
