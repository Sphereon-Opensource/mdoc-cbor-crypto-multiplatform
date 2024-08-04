package com.sphereon.mdoc

import com.sphereon.cbor.cose.COSE_Sign1
import com.sphereon.cbor.cose.CoseKeyCbor
import com.sphereon.crypto.CoseCryptoServiceJS
import com.sphereon.crypto.CoseCryptoServiceJSAdapter
import com.sphereon.crypto.CryptoServiceJS
import com.sphereon.crypto.VerifyResult
import com.sphereon.crypto.VerifySignatureResult
import com.sphereon.crypto.X509VerificationResult
import com.sphereon.crypto.coseService
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.getDateTime
import com.sphereon.mdoc.data.IssuerAuthValidation
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectJson
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

/**
 *
 * This is the JS version exposing the functions as async/Promises. It delegates everything to the coroutine version
 *
 * 9.3.1 Inspection procedure for issuer data authentication
 *
 * 1. Validate the certificate included in the MSO header according to 9.3.3.
 * 2. Verify the digital signature of the IssuerAuth structure (see 9.1.2.4) using the working_public_
 * key, working_public_key_parameters, and working_public_key_algorithm from the certificate
 * validation procedure of step 1.
 *
 * === Step 3 and 4 are related to MSO. We have a separate validator that uses this validator for those ===
 * 3. Calculate the digest value for every IssuerSignedItem returned in the DeviceResponse structure
 * according to 9.1.2.5 and verify that these calculated digests equal the corresponding digest values
 * in the MSO.
 * 4. Verify that the DocType in the MSO matches the relevant DocType in the Documents structure.
 * ========================================================================================================
 *
 * 5. Validate the elements in the ValidityInfo structure, i.e. verify that:
 * — the 'signed' date is within the validity period of the certificate in the MSO header,
 * — the current timestamp shall be equal or later than the ‘validFrom’ element,
 * — the 'validUntil' element shall be equal or later than the current timestamp.
 *
 */
@JsExport
object IssuerAuthValidationJS {
    private val NAME = "IssuerAuthValidationJS"

    /**
     * * 1. Validate the certificate included in the MSO header according to 9.3.3.
     */
    fun verifyCertificateChainAsync(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>,
//        x509Service: X509Service = CryptoService.X509, // todo, look into this. Do we want to expose it here anyway?
        trustedCerts: Array<String>?// = x509Service.getTrustedCerts()
    ): Promise<X509VerificationResult<CoseKeyCbor>> = CoroutineScope(context = CoroutineName(NAME)).promise {
        IssuerAuthValidation.verifyCertificateChain(
            issuerAuth = issuerAuth,
//            x509Service = x509Service,
            trustedCerts = trustedCerts
        )
    }


    /**
     * 2. Verify the digital signature of the IssuerAuth structure (see 9.1.2.4) using the working_public_
     * key, working_public_key_parameters, and working_public_key_algorithm from the certificate
     * validation procedure of step 1.
     */
    fun verifySign1Async(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>,
        coseCryptoService: CoseCryptoServiceJS = CryptoServiceJS.COSE, // TODO: Test this with other services
        keyInfo: com.sphereon.crypto.KeyInfo<CoseKeyCbor>?
    ): Promise<VerifySignatureResult<CoseKeyCbor>> =
        CoroutineScope(context = CoroutineName(NAME)).promise {
            IssuerAuthValidation.verifySign1(issuerAuth = issuerAuth, coseCryptoService = if (coseCryptoService === CryptoServiceJS.COSE) coseService() else CoseCryptoServiceJSAdapter(coseCryptoService) , keyInfo = keyInfo) }


    /**
     *  3. Calculate the digest value for every IssuerSignedItem returned in the DeviceResponse structure
     *  * according to 9.1.2.5 and verify that these calculated digests equal the corresponding digest values
     *  * in the MSO.
     *
     *  This is a READER method.
     */
    fun verifyDigestsAsync(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>,
//        deviceResponse: DeviceResponseCbor
    ): Promise<VerifyResult> = CoroutineScope(context = CoroutineName(NAME)).promise {
        IssuerAuthValidation.verifyDigests(issuerAuth/*, deviceResponse*/)
    }


    /**
     * 4. Verify that the DocType in the MSO matches the relevant DocType in the Documents structure.
     *
     *  This is a READER method.
     */
    fun verifyDocTypeAsync(issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>): Promise<VerifyResult> =
        CoroutineScope(context = CoroutineName(NAME)).promise {
            IssuerAuthValidation.verifyDocType(issuerAuth)
        }

    /**
     * 5. Validate the elements in the ValidityInfo structure, i.e. verify that:
     * — the 'signed' date is within the validity period of the certificate in the MSO header, <-- FIXME, we need an additional x509 function
     * — the current timestamp shall be equal or later than the ‘validFrom’ element,
     * — the 'validUntil' element shall be equal or later than the current timestamp.
     */
    fun verifyValidityInfoAsync(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>,
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ): Promise<VerifyResult> = CoroutineScope(context = CoroutineName(NAME)).promise {
        IssuerAuthValidation.verifyValidityInfo(issuerAuth, dateTimeUtils, timeZoneId, clockSkewAllowedInSec)
    }

}

