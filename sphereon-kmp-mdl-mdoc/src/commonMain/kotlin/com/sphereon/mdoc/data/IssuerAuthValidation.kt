package com.sphereon.mdoc.data

import com.sphereon.cbor.cborTDateToEpochSeconds
import com.sphereon.cbor.localDateToDateStringISO
import com.sphereon.crypto.cose.COSE_Sign1
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.CryptoConst
import com.sphereon.crypto.CryptoService
import com.sphereon.crypto.ICoseCryptoService
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IVerifyResult
import com.sphereon.crypto.IVerifySignatureResult
import com.sphereon.crypto.IX509Service
import com.sphereon.crypto.IX509VerificationResult
import com.sphereon.crypto.VerifyResult
import com.sphereon.crypto.X509VerificationProfile
import com.sphereon.crypto.X509VerificationResult
import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.getDateTime
import com.sphereon.kmp.toLocalDateTimeKMP
import com.sphereon.mdoc.MdocConst
import com.sphereon.mdoc.data.device.DocumentCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectCbor
import com.sphereon.mdoc.data.mso.MobileSecurityObjectJson

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
object IssuerAuthValidation {

    /**
     * * 1. Validate the certificate included in the MSO header according to 9.3.3.
     */
    suspend fun verifyCertificateChain(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>,
        x509Service: IX509Service = CryptoService.X509,
        trustedCerts: Array<String>? = x509Service.getTrustedCerts()
    ): IX509VerificationResult<ICoseKeyCbor> {
        val x5chain =
            issuerAuth.protectedHeader.x5chain ?: issuerAuth.unprotectedHeader?.x5chain
        if (x5chain === null || x5chain.value.isEmpty()) {
            return X509VerificationResult(
                name = CryptoConst.X509_LITERAL,
                error = true,
                critical = true,
                message = "No X.509 Chain present in the issuerAuth headers"
            )
        }
        return x509Service.verifyCertificateChain(chainDER = x5chain.value.map { it.value }
            .toTypedArray(), trustedCerts = trustedCerts, verificationProfile = X509VerificationProfile.ISO_18013_5)
    }


    /**
     * 2. Verify the digital signature of the IssuerAuth structure (see 9.1.2.4) using the working_public_
     * key, working_public_key_parameters, and working_public_key_algorithm from the certificate
     * validation procedure of step 1.
     */
    suspend fun verifySign1(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>,
        coseCryptoService: ICoseCryptoService = CryptoService.COSE,
        keyInfo: IKeyInfo<ICoseKeyCbor>?
    ): IVerifySignatureResult<ICoseKeyCbor> {
        if (keyInfo?.key?.d !== null) {
            throw AssertionError("Do not use private keys to verify!")
        }
        return coseCryptoService.verify1(issuerAuth, keyInfo)
    }


    /**
     *  3. Calculate the digest value for every IssuerSignedItem returned in the DeviceResponse structure
     *  * according to 9.1.2.5 and verify that these calculated digests equal the corresponding digest values
     *  * in the MSO.
     *
     *  This is a READER method. FIXME: Implement
     */
    suspend fun verifyDigests(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>,
//        deviceResponse: DeviceResponseCbor
    ): IVerifyResult = VerifyResult(
        error = false,
        critical = true,
        message = "Device signed verification validation not implemented yet",
        name = MdocConst.MDOC_LITERAL
    )


    /**
     * 4. Verify that the DocType in the MSO matches the relevant DocType in the Documents structure.
     *
     *  This is a READER method.
     */
    suspend fun verifyDocType(document: DocumentCbor?): IVerifyResult {
        val docTypesMatch = document !== null && document.docType == document.MSO?.docType
        return VerifyResult(
            error = !docTypesMatch,
            critical = !docTypesMatch,
            message = "Doc type verification was ${if (docTypesMatch) "successful" else "not successful. MSO ${document?.MSO?.docType}, document: ${document?.docType}"}",
            name = MdocConst.MDOC_LITERAL
        )
    }

    /**
     * 5. Validate the elements in the ValidityInfo structure, i.e. verify that:
     * — the 'signed' date is within the validity period of the certificate in the MSO header, <-- FIXME, we need an additional x509 function
     * — the current timestamp shall be equal or later than the ‘validFrom’ element,
     * — the 'validUntil' element shall be equal or later than the current timestamp.
     */
    suspend fun verifyValidityInfo(
        issuerAuth: COSE_Sign1<MobileSecurityObjectCbor>,
        allowExpiredDocuments: Boolean? = false,
        dateTimeUtils: DateTimeUtils = getDateTime(),
        timeZoneId: String? = null,
        clockSkewAllowedInSec: Int = 120,
    ): IVerifyResult {
        val mso = MobileSecurityObjectCbor.Static.decodeCoseSign1(issuerAuth)
        if (mso === null) {
            return VerifyResult(
                error = true,
                critical = true,
                message = "No MSO found in the issuer auth object",
                name = MdocConst.MDOC_LITERAL
            ).also { MdocConst.LOG.error("Error validating MSO validity: $it") }
        }

        val now =
            dateTimeUtils.epochSeconds().toLong() // toLong as the date time utils uses ints for these (valid till 2038)
        val signed = mso.validityInfo.signed.cborTDateToEpochSeconds(dateTimeUtils, timeZoneId).toLong()
        val validFrom = mso.validityInfo.validFrom.cborTDateToEpochSeconds(dateTimeUtils, timeZoneId).toLong()
        val validUntil = mso.validityInfo.validUntil.cborTDateToEpochSeconds(dateTimeUtils, timeZoneId).toLong()
        val nowStr = now.toLocalDateTimeKMP(dateTimeUtils).localDateToDateStringISO(dateTimeUtils, timeZoneId)
        val validFromStr =
            validFrom.toLocalDateTimeKMP(dateTimeUtils).localDateToDateStringISO(dateTimeUtils, timeZoneId)
        val validUntilStr =
            validUntil.toLocalDateTimeKMP(dateTimeUtils).localDateToDateStringISO(dateTimeUtils, timeZoneId)

        // FIXME: Offset used as we cannot inspect cert valid from - to yet. Needs a function on the x509Service.
        val FIXME_OFFSET = 600
        val certValidFrom = validFrom - FIXME_OFFSET
        val certValidUntil = validUntil + FIXME_OFFSET

        // the 'signed' date is within the validity period of the certificate in the MSO header
        // Let's not do a clock skew on dates that typically are far away
        if (signed < certValidFrom || signed > certValidUntil) {
            return VerifyResult(
                error = true,
                critical = true,
                message = "The signature date is not within the certificate validity range of ${
                    certValidFrom.toLocalDateTimeKMP(
                        dateTimeUtils
                    ).localDateToDateStringISO(dateTimeUtils, timeZoneId)
                } and ${
                    certValidUntil.toLocalDateTimeKMP(dateTimeUtils).localDateToDateStringISO(dateTimeUtils, timeZoneId)
                }",
                name = MdocConst.MDOC_LITERAL
            ).also { MdocConst.LOG.error("Error validating MSO validity against certificate: $it") }
        }

        // the current timestamp shall be equal or later than the ‘validFrom’ element,
        if (now + clockSkewAllowedInSec < validFrom) {
            return VerifyResult(
                error = true,
                critical = true,
                message = "The document is not yet valid. Current date/time: $nowStr and valid From $validFromStr",
                name = MdocConst.MDOC_LITERAL
            ).also { MdocConst.LOG.error("Error validating MSO validFrom against current time: $it") }
        }

        // the 'validUntil' element shall be equal or later than the current timestamp.

        if (validUntil < now - clockSkewAllowedInSec) {
            val datesEqaul = nowStr == validUntilStr
            return VerifyResult(
                error = true,
                critical = allowExpiredDocuments != true,
                message = "The document is not valid anymore. Current date/time: $nowStr ${if (datesEqaul) "(${now})" else ""} and valid Until $validUntilStr ${if (datesEqaul) "(${validUntil})" else ""}",
                name = MdocConst.MDOC_LITERAL
            ).also { MdocConst.LOG.error("Error validating MSO validUntil against current time: $it") }
        }

        return VerifyResult(
            name = MdocConst.MDOC_LITERAL,
            error = false,
            critical = false,
            message = "Signature is signed during Certificate validity and valid"
        )
    }

}
