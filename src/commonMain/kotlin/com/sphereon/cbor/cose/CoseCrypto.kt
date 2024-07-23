import com.sphereon.cbor.cose.CoseKeyCbor
import com.sphereon.cbor.cose.CoseKeyType
import com.sphereon.cbor.cose.CoseSign1Cbor
import com.sphereon.cbor.cose.CoseSign1InputCbor
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.CryptographyProviderApi
import dev.whyoleg.cryptography.algorithms.asymmetric.ECDSA
import dev.whyoleg.cryptography.serialization.asn1.BitArray
import dev.whyoleg.cryptography.serialization.asn1.ObjectIdentifier
import dev.whyoleg.cryptography.serialization.pem.PEM
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlin.js.JsExport




interface CoseService {
    suspend fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo? = null
    ): Result<CoseSign1Cbor<CborType, JsonType>>

    suspend fun <CborType, JsonType> verify1(input: CoseSign1Cbor<CborType, JsonType>, keyInfo: KeyInfo? = null): Result<VerifyResults>
}

expect fun coseService(): CoseService

class CoseCryptoService {
    fun <CborType, JsonType> sign1(
        input: CoseSign1InputCbor<CborType, JsonType>,
        keyInfo: KeyInfo?
    ): CoseSign1Cbor<CborType, JsonType> {
        TODO("Not yet implemented")
    }

    @OptIn(CryptographyProviderApi::class)
    fun <CborType, JsonType> verify1(
        input: CoseSign1Cbor<CborType, JsonType>,
        keyInfo: KeyInfo?
    ): VerifyResults {
        val sigAlg = input.protectedHeader.alg ?: input.unprotectedHeader?.alg
        val keyAlg = sigAlg?.keyType ?: if (keyInfo?.coseKey?.alg != null) CoseKeyType.entries.first { it.value == keyInfo.coseKey.alg.value.toInt() } else null
        if (keyAlg == null) {
            return VerifyResults(
                error = true,
                verifications = arrayOf(
                    VerifyResult(
                        name = "key",
                        error = true,
                        message = "No Key algorithm found or provided",
                        critical = true
                    )
                ),
                keyInfo = keyInfo
            )
        }

        if (keyAlg != CoseKeyType.EC2) {
            return VerifyResults(
                error = true,
                verifications = arrayOf(
                    VerifyResult(
                        name = "key",
                        error = true,
                        message = "Key Algorithm ${keyAlg.name} (${keyAlg.description}) is not supported",
                        critical = true
                    )
                ),
                keyInfo = keyInfo
            )
        }
        CryptographyProvider.Registry.registerProvider(CryptographyProvider.Default)
        val provider = CryptographyProvider.Default.get(ECDSA)
        val curveName = sigAlg?.name?.replace("ES", "ES-") ?: "ES-256"
        if (!curveName.startsWith("ES-")) {
            return VerifyResults(
                error = true,
                verifications = arrayOf(
                    VerifyResult(
                        name = "key",
                        error = true,
                        message = "Signature Algorithm curve $curveName is not supported",
                        critical = true
                    )
                ),
                keyInfo = keyInfo
            )
        }
        val x5Chain = input.protectedHeader.x5chain ?: input.unprotectedHeader?.x5chain
        if (x5Chain?.value == null) {
            return VerifyResults(
                error = true,
                verifications = arrayOf(
                    VerifyResult(
                        name = "x5chain",
                        error = true,
                        message = "No X5chain found",
                        critical = true
                    )
                ),
                keyInfo = keyInfo
            )
        }
        val dec = PEM.decode("-----BEGIN CERTIFICATE-----\n" +
                "MIICdDCCAhugAwIBAgIBAjAKBggqhkjOPQQDAjCBiDELMAkGA1UEBhMCREUxDzANBgNVBAcMBkJlcmxpbjEdMBsGA1UECgwUQnVuZGVzZHJ1Y2tlcmVpIEdtYkgxETAPBgNVBAsMCFQgQ1MgSURFMTYwNAYDVQQDDC1TUFJJTkQgRnVua2UgRVVESSBXYWxsZXQgUHJvdG90eXBlIElzc3VpbmcgQ0EwHhcNMjQwNTMxMDgxMzE3WhcNMjUwNzA1MDgxMzE3WjBsMQswCQYDVQQGEwJERTEdMBsGA1UECgwUQnVuZGVzZHJ1Y2tlcmVpIEdtYkgxCjAIBgNVBAsMAUkxMjAwBgNVBAMMKVNQUklORCBGdW5rZSBFVURJIFdhbGxldCBQcm90b3R5cGUgSXNzdWVyMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEOFBq4YMKg4w5fTifsytwBuJf/7E7VhRPXiNm52S3q1ETIgBdXyDK3kVxGxgeHPivLP3uuMvS6iDEc7qMxmvduKOBkDCBjTAdBgNVHQ4EFgQUiPhCkLErDXPLW2/J0WVeghyw+mIwDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8EBAMCB4AwLQYDVR0RBCYwJIIiZGVtby5waWQtaXNzdWVyLmJ1bmRlc2RydWNrZXJlaS5kZTAfBgNVHSMEGDAWgBTUVhjAiTjoDliEGMl2Yr+ru8WQvjAKBggqhkjOPQQDAgNHADBEAiAbf5TzkcQzhfWoIoyi1VN7d8I9BsFKm1MWluRph2byGQIgKYkdrNf2xXPjVSbjW/U/5S5vAEC5XxcOanusOBroBbU=\n" +
                "-----END CERTIFICATE-----")
//        val decoded = DER.decodeFromByteArray<Certificate>(bytes = x5Chain.value[0].value)
        println(dec)
        /*val decoded = provider.publicKeyDecoder(EC.Curve.P256).decodeFrom(EC.PublicKey.Format.DER, input.signature.value)
        decoded.signatureVerifier()

        if (x5Chain != null) {

        }

        provider.get(ECDSA)
//        TODO("Not yet implemented")
         */
        return VerifyResults(error = false, verifications = arrayOf(), keyInfo = keyInfo)
    }

}



@Serializable
class Certificate(val tbsCertificate: @Polymorphic Any, val signatureAlgorithm: SimpleAlgorithmIdentifier, val signature: BitArray)

@Serializable
class SimpleAlgorithmIdentifier(
    val algorithm: ObjectIdentifier,
    val parameters: Nothing?,
)
