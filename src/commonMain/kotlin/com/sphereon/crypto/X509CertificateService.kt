import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

interface X509Service {
    suspend fun verifyCertificateChain(chainDER: Array<ByteArray>? = null, chainPEM: Array<String>? = null, trustedPEM: Array<String>): VerifyResult
}

expect fun x509Service(): X509Service


object CallbackServices {
    val x509 = x509Service()

}

object X509CertificateService: X509Service {
    @JvmStatic
    private var callbacks: X509Service? = null

    fun register(callbacks: X509Service): X509CertificateService {
        this.callbacks = callbacks
        return this
    }

    @JsExport.Ignore
    override suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): VerifyResult {
        if (callbacks === null) {
            throw Exception("No X509 Certificate Service callbacks set")
        }
        return callbacks!!.verifyCertificateChain(chainDER, chainPEM, trustedPEM)
    }
}
