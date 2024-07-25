import kotlinx.coroutines.await
import kotlin.js.Promise

@JsModule("pkijs")
@JsNonModule
external object pkijs {
    class CertificateChainValidationEngine(certs: Array<Certificate>, trustedCerts: Array<Certificate>)
    class Certificate {
        companion object {
            fun fromBER(externalArgument: ByteArray = definedExternally): Certificate
        }
    }
}

@JsModule("@sphereon-internal/mdoc-js-api")
@JsNonModule
external object MdocJSApi {
    fun verifyCertificateChain(
        chainDER: Array<ByteArray>? = definedExternally,
        chainPEM: Array<String>? = definedExternally,
        trustedPEM: Array<String>
    ): Promise<VerifyResult>
}


@JsExport
interface X509CallbacksJS {
    fun verifyCertificateChain(
        chainDER: Array<ByteArray>? = null,
        chainPEM: Array<String>? = null,
        trustedPEM: Array<String>
    ): Promise<VerifyResult>
}

@JsExport
object X509CallbackService : X509CallbacksJS {
    private lateinit var callback: X509CallbacksJS
    private var disabled = false
    fun disable(): X509CallbackService {
        this.disabled = true
        return this
    }

    fun enable(): X509CallbackService {
        this.disabled = false
        return this
    }

    fun register(callbacks: X509CallbacksJS) {
        this.callback = callbacks
        this.enable()
    }

    override fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): Promise<VerifyResult> {
        if (this.disabled) {
            Promise.resolve(
                VerifyResult(
                    name = "x509",
                    message = "X509 verification has been disabled",
                    error = false,
                    critical = false
                )
            )
        } else if (!this::callback.isInitialized) {
            throw IllegalStateException("X509Callbacks have not been initialized. Please register your X509CallbacksJS implementation, or register a default implementaion")
        }
        return this.callback.verifyCertificateChain(chainDER, chainPEM, trustedPEM)
    }
}

internal object X509ServiceJS : X509Service {
    val callbacks: X509CallbacksJS = X509CallbackService
    override suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): VerifyResult {
        if (chainDER == null && chainPEM == null) {
            return VerifyResult(
                name = "x5c",
                error = true,
                message = "Please provide either a chain in DER format or PEM format",
                critical = true
            )
        }
        if (chainDER != null) {
            val certificate = pkijs.Certificate.fromBER(chainDER[0])
            println(certificate)
        }
        return try {
            callbacks.verifyCertificateChain(chainDER, chainPEM, trustedPEM).await()
        } catch (e: Exception) {
            VerifyResult(
                name = "x5c",
                error = true,
                message = "Certificate chain verification failed ${e.message}",
                critical = true
            )
        }

    }

}

actual fun x509Service(): X509Service = X509ServiceJS
