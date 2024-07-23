actual fun x509Service(): X509Service = X509ServiceJvm

internal object X509ServiceJvm : X509Service {
    override suspend fun verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedPEM: Array<String>
    ): VerifyResult = VerifyResult(name = "jvmCertChain", error = false)
}
