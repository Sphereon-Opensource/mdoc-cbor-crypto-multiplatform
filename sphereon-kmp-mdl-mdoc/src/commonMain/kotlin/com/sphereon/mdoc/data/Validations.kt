package com.sphereon.mdoc.data

import com.sphereon.crypto.CryptoService
import com.sphereon.crypto.ICoseCryptoService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IVerifyResults
import com.sphereon.crypto.IX509Service
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.VerifyResult
import com.sphereon.crypto.VerifyResults
import com.sphereon.crypto.cose.COSE_Sign1
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.getDateTime
import com.sphereon.mdoc.MdocConst
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import kotlin.js.ExperimentalJsCollectionsApi
import kotlin.js.JsExport

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
object Validations {

    suspend fun fromDocument(
        document: DocumentCbor,
        x509Service: IX509Service = CryptoService.X509,
        coseCryptoService: ICoseCryptoService = CryptoService.COSE,
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null,
        trustedCerts: Array<String>? = x509Service.getTrustedCerts(),
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ) = withParams(
        issuerAuth = null,
        document = document,
        mdocVerificationTypes = MdocVerification.Static.DOCUMENT,
        x509Service = x509Service,
        coseCryptoService = coseCryptoService,
        keyInfo = keyInfo,
        trustedCerts = trustedCerts,
        dateTimeUtils = dateTimeUtils,
        timeZoneId = timeZoneId,
        clockSkewAllowedInSec = clockSkewAllowedInSec
    )

    suspend fun fromIssuerAuth(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>,
        x509Service: IX509Service = CryptoService.X509,
        coseCryptoService: ICoseCryptoService = CryptoService.COSE,
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null,
        trustedCerts: Array<String>? = x509Service.getTrustedCerts(),
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ) = withParams(
        issuerAuth = issuerAuth,
        document = null,
        mdocVerificationTypes = MdocVerification.Static.ISSUER_AUTH,
        x509Service = x509Service,
        coseCryptoService = coseCryptoService,
        keyInfo = keyInfo,
        trustedCerts = trustedCerts,
        dateTimeUtils = dateTimeUtils,
        timeZoneId = timeZoneId,
        clockSkewAllowedInSec = clockSkewAllowedInSec
    )

    @OptIn(ExperimentalJsCollectionsApi::class)
    suspend fun withParams(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>? = null,
        document: DocumentCbor? = null,
        mdocVerificationTypes: MdocVerificationTypes = MdocVerification.Static.ALL,
        x509Service: IX509Service = CryptoService.X509,
        coseCryptoService: ICoseCryptoService = CryptoService.COSE,
        keyInfo: IKeyInfo<ICoseKeyCbor>? = null,
        trustedCerts: Array<String>? = x509Service.getTrustedCerts(),
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ): IVerifyResults<ICoseKeyCbor> {
        if (issuerAuth === null && document == null) {
            return VerifyResults(
                error = true,
                keyInfo = null,
                verifications = arrayOf(
                    VerifyResult(
                        name = MdocConst.MDOC_LITERAL,
                        critical = true,
                        error = true,
                        message = "Either an mdoc or an issuerAith object needs to be provided for verification"
                    )
                )
            )
        } else if (issuerAuth !== null && document !== null && document.issuerSigned.issuerAuth !== issuerAuth) {
            // Both are provided, although mdoc was enough. Make sure the objects are actually the same
            return VerifyResults(
                error = true,
                keyInfo = null,
                verifications = arrayOf(
                    VerifyResult(
                        name = MdocConst.MDOC_LITERAL,
                        critical = true,
                        error = true,
                        message = "Both an mdoc and issuer auth object were supplied for verification, but the issuerAuth of the mdoc is different from the provided mdoc. To prevent this only supply the mdoc"
                    )
                )
            )
        }
        val verificationTypes: MdocVerificationTypes = mdocVerificationTypes.ifEmpty { MdocVerification.Static.ALL }
        val auth = document?.issuerSigned?.issuerAuth ?: issuerAuth ?: throw AssertionError()

        val verifications = verificationTypes.map {
            when (it) {
                MdocVerification.CERTIFICATE_CHAIN -> IssuerAuthValidation.verifyCertificateChain(auth, x509Service, trustedCerts)
                MdocVerification.ISSUER_AUTH_SIGNATURE -> IssuerAuthValidation.verifySign1(auth, coseCryptoService, keyInfo)
                MdocVerification.VALIDITY -> IssuerAuthValidation.verifyValidityInfo(auth, dateTimeUtils, timeZoneId, clockSkewAllowedInSec)
                MdocVerification.DOC_TYPE -> IssuerAuthValidation.verifyDocType(document)
                MdocVerification.DIGEST_VALUES -> IssuerAuthValidation.verifyDigests(auth)
            }
        }

        return VerifyResults(
            error = verifications.find { it.error }?.error ?: false,
            keyInfo = if (keyInfo === null) null else KeyInfo.Static.fromDTO(keyInfo),
            verifications = verifications.map { verification -> VerifyResult.Static.fromDTO(verification) }.toTypedArray(),
        )


    }


}

@JsExport
enum class MdocVerification {
    CERTIFICATE_CHAIN,
    ISSUER_AUTH_SIGNATURE,
    DIGEST_VALUES,
    DOC_TYPE,
    VALIDITY;

    object Static {
        val ALL: MdocVerificationTypes = entries.toSet()
        val ISSUER_AUTH: MdocVerificationTypes = setOf(CERTIFICATE_CHAIN, ISSUER_AUTH_SIGNATURE, VALIDITY)
        val DOCUMENT: MdocVerificationTypes = ALL
    }
}

typealias MdocVerificationTypes = Set<MdocVerification>

