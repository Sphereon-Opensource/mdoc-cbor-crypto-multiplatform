package com.sphereon.crypto


import com.sphereon.crypto.cose.CoseKeyCbor
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import kotlin.js.Promise
import kotlin.test.Test




object JsCallback: IX509ServiceJS {
    override fun <KeyType : IKey> verifyCertificateChain(
        chainDER: Array<ByteArray>?,
        chainPEM: Array<String>?,
        trustedCerts: Array<String>?,
        verificationProfile: X509VerificationProfile?
    ): Promise<IX509VerificationResult<KeyType>> {
        return Promise.resolve(X509VerificationResult(critical = true, error = false, message = "test success"))
    }

    override fun getTrustedCerts(): Array<String>? {
        return null
    }

}

class X509CertificateServiceJSTest {

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun shouldValidateCert(): TestResult {

        return runTest {
            println("Running verifyCertChainJS test")
            val x509 = CryptoServicesJS.x509(JsCallback)
            val result = x509.verifyCertificateChain<CoseKeyCbor>(
                chainDER = arrayOf(
                    "308202743082021ba003020102020102300a06082a8648ce3d040302308188310b3009060355040613024445310f300d06035504070c064265726c696e311d301b060355040a0c1442756e646573647275636b6572656920476d62483111300f060355040b0c0854204353204944453136303406035504030c2d535052494e442046756e6b6520455544492057616c6c65742050726f746f747970652049737375696e67204341301e170d3234303533313038313331375a170d3235303730353038313331375a306c310b3009060355040613024445311d301b060355040a0c1442756e646573647275636b6572656920476d6248310a3008060355040b0c01493132303006035504030c29535052494e442046756e6b6520455544492057616c6c65742050726f746f74797065204973737565723059301306072a8648ce3d020106082a8648ce3d0301070342000438506ae1830a838c397d389fb32b7006e25fffb13b56144f5e2366e764b7ab511322005d5f20cade45711b181e1cf8af2cfdeeb8cbd2ea20c473ba8cc66bddb8a3819030818d301d0603551d0e0416041488f84290b12b0d73cb5b6fc9d1655e821cb0fa62300c0603551d130101ff04023000300e0603551d0f0101ff040403020780302d0603551d1104263024822264656d6f2e7069642d6973737565722e62756e646573647275636b657265692e6465301f0603551d23041830168014d45618c08938e80e588418c97662bfabbbc590be300a06082a8648ce3d040302034700304402201b7f94f391c43385f5a8228ca2d5537b77c23d06c14a9b531696e4698766f219022029891dacd7f6c573e35526e35bf53fe52e6f0040b95f170e6a7bac381ae805b5".hexToByteArray()
                ),
                chainPEM = arrayOf("chainpem1", "chainpem2"), trustedCerts = arrayOf(
                    "chainpem1"
                ),
                verificationProfile = null
            )
            println("Done verifyCertChain test:")
            println(result.toString())
        }

    }
}
