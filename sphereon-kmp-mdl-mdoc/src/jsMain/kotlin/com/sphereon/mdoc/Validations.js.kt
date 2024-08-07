package com.sphereon.mdoc

import com.sphereon.cbor.cose.COSE_Sign1
import com.sphereon.cbor.cose.ICoseKeyCbor
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IVerifyResults
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.getDateTime
import com.sphereon.mdoc.data.MdocVerification
import com.sphereon.mdoc.data.MdocVerificationTypes
import com.sphereon.mdoc.data.Validations
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectJson
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.promise
import kotlin.js.Promise

/**
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
 */
@JsExport
object ValidationsJS {
    private val NAME = "ValidationsJS"


    fun fromDocumentAsync(
        document: DocumentCbor,
        /*x509Service: X509Service = CryptoService.X509,
        coseCryptoService: ICoseCryptoService = CryptoService.COSE,*/
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null,
        trustedCerts: Array<String>? = null,
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ) = withParamsAsync(
        issuerAuth = null,
        document = document,
        mdocVerificationTypes = MdocVerification.document,
        /*x509Service = x509Service,
        coseCryptoService = coseCryptoService,*/
        keyInfo = keyInfo,
        trustedCerts = trustedCerts,
        dateTimeUtils = dateTimeUtils,
        timeZoneId = timeZoneId,
        clockSkewAllowedInSec = clockSkewAllowedInSec
    )

    fun fromIssuerAuthAsync(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>,
        /*x509Service: X509Service = CryptoService.X509,
        coseCryptoService: CoseCryptoService = CryptoService.COSE,*/
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null,
        trustedCerts: Array<String>? = null,
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ) = withParamsAsync(
        issuerAuth = issuerAuth,
        document = null,
        mdocVerificationTypes = MdocVerification.issuerAuth,
        /*x509Service = x509Service,
        coseCryptoService = coseCryptoService,*/
        keyInfo = keyInfo,
        trustedCerts = trustedCerts,
        dateTimeUtils = dateTimeUtils,
        timeZoneId = timeZoneId,
        clockSkewAllowedInSec = clockSkewAllowedInSec
    )

    fun withParamsAsync(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor, MobileSecurityObjectJson>? = null,
        document: DocumentCbor? = null,
        mdocVerificationTypes: MdocVerificationTypes = MdocVerification.all,
        /*x509Service: X509Service = CryptoService.X509,
        coseCryptoService: CoseCryptoService = CryptoService.COSE,*/
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null,
        trustedCerts: Array<String>? = null,
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ): Promise<IVerifyResults<ICoseKeyCbor>> = CoroutineScope(context = CoroutineName(NAME)).promise {
        Validations.withParams(
            issuerAuth = issuerAuth,
            document = document,
            mdocVerificationTypes = mdocVerificationTypes,
          /*  x509Service = x509Service,
            coseCryptoService = coseCryptoService,*/
            keyInfo = keyInfo,
            trustedCerts = trustedCerts,
            dateTimeUtils = dateTimeUtils,
            timeZoneId = timeZoneId,
            clockSkewAllowedInSec = clockSkewAllowedInSec
        )
    }
}
