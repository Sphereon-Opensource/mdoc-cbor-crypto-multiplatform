interface X509Service {
    suspend fun verifyCertificateChain(chainDER: Array<ByteArray>? = null, chainPEM: Array<String>? = null, trustedPEM: Array<String>): VerifyResult
}

expect fun x509Service(): X509Service
