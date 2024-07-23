import kotlinx.coroutines.await
import kotlin.js.Promise

@JsModule("pkijs")
@JsNonModule
external object pkijs {
    class CertificateChainValidationEngine( certs: Array<Certificate>, trustedCerts: Array<Certificate>)
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

object X509ServiceJS : X509Service {
    override suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): VerifyResult {
        if (chainDER == null && chainPEM == null) {
            return VerifyResult(name = "x5c", error = true, message = "Please provide either a chain in DER format or PEM format", critical = true)
        }
        if (chainDER != null) {
            val certificate = pkijs.Certificate.fromBER(chainDER[0])
            println(certificate)
        }
        return try {
            MdocJSApi.verifyCertificateChain(chainDER, chainPEM, trustedPEM).await()
        } catch (e: Exception) {
            VerifyResult(name = "x5c", error = true, message = "Certificate chain verification failed ${e.message}", critical = true)
        }

    }

}
actual fun x509Service(): X509Service = X509ServiceJS
