package com.sphereon.crypto.sign.model

import kotlin.js.JsExport

/**
 * Enum class representing various levels of digital signatures, each associated with a specific form of signature.
 *
 * @property form The form of digital signature associated with the signature level.
 */
@JsExport
@kotlinx.serialization.Serializable
enum class SignatureLevel(val form: SignatureForm) {
//    XML_NOT_ETSI, XAdES_BES, XAdES_EPES, XAdES_T, XAdES_LT, XAdES_C, XAdES_X, XAdES_XL, XAdES_A, XAdES_BASELINE_B, XAdES_BASELINE_T, XAdES_BASELINE_LT, XAdES_BASELINE_LTA,

    /**
     * Represents a CMS-based signature that is not compliant with ETSI standards.
     *
     * This signature level makes use of the CMS (Cryptographic Message Syntax) format but does not adhere
     * to the European Telecommunications Standards Institute (ETSI) specifications for electronic signatures.
     * It provides flexibility for non-standard use cases where compliance with ETSI is not required.
     */
    CMS_NOT_ETSI(SignatureForm.CAdES),
    /**
     * Represents the CAdES-BES signature level, which is a CMS-based signature
     * according to the CAdES (CMS Advanced Electronic Signatures) standard.
     * This level ensures that certain minimal requirements are met,
     * providing evidence that the signature creation data was valid at the time of signing.
     */
    CAdES_BES(SignatureForm.CAdES), /**
     * Represents the CMS Advanced Electronic Signatures (CAdES) Enhanced Policy Electronic Signatures (EPES) signature level.
     *
     * This signature level adheres to the CAdES specifications defined by EN 319 122, and includes additional data that is specific
     * to the signature policy applied. This adds a level of assurance that the signature complies with certain regulations or standards.
     */
    CAdES_EPES(SignatureForm.CAdES), /**
     * Signature level CAdES_T, which represents a CMS-based signature that includes a timestamp.
     *
     * Associated with the *CAdES* signature form according to EN 319 122.
     */
    CAdES_T(SignatureForm.CAdES), /**
     * Represents the CAdES LT (Long Term) signature level, which is a CMS-based signature
     * as specified in EN 319 122. This level ensures the long-term validity and integrity of
     * the digital signature by incorporating timestamp tokens and other data required for
     * validating the signature over an extended period.
     */
    CAdES_LT(SignatureForm.CAdES), /**
     * Represents the CAdES-C signature level from the CMS-based signature formats.
     *
     * This signature level extends the CAdES-BES by adding additional attributes for evidence records.
     *
     * @property form The underlying signature form associated with this signature level.
     */
    CAdES_C(
        SignatureForm.CAdES
    ),
    /**
     * Represents the CAdES-X signature level which adheres to the CMS-based signature standard EN 319 122.
     * It specifies advanced electronic signatures providing long-term validation.
     */
    CAdES_X(SignatureForm.CAdES), /**
     * Represents the CAdES-X Long Term Validation (XL) signature level.
     *
     * CAdES-X Long Term (XL) is a type of CAdES signature form defined for ensuring
     * the long-term validity of the signature. It includes additional validation data
     * to extend the validity period of a signature beyond the point when the signer's
     * certificate might expire or be revoked.
     */
    CAdES_XL(SignatureForm.CAdES),
    /**
     * Represents the CAdES-A (Archival) signature level.
     *
     * This signature level extends the CAdES-X Long term by adding an attribute that ensures
     * the preservation of the signatures to long-term existence.
     */
    CAdES_A(SignatureForm.CAdES), /**
     * Represents the CAdES Baseline B signature level.
     *
     * This level of signature compliance is defined by SignatureForm.CAdES
     * and represents a CMS-based signature according to EN 319 122.
     */
    CAdES_BASELINE_B(SignatureForm.CAdES), /**
     * CAdES Baseline T level signature according to EN 319 122.
     * This signature level adds a trusted timestamp to a CAdES BES or EPES signature.
     *
     * @constructor Instantiates the CAdES_BASELINE_T signature level.
     * @param form The form of the signature, in this case, CAdES.
     */
    CAdES_BASELINE_T(SignatureForm.CAdES), /**
     * Represents the CAdES Baseline LT signature level.
     *
     * CAdES (CMS Advanced Electronic Signatures) is a set of extensions to the
     * Cryptographic Message Syntax (CMS) standard that provides added features
     * to the standard CMS signatures.
     *
     * Baseline LT level is one of the baseline profiles defined for CAdES
     * that ensures long-term validity of the signature. This profile includes
     * timestamping and revocation information.
     *
     * @property form The form of the signature, which is always `SignatureForm.CAdES` for this level.
     */
    CAdES_BASELINE_LT(SignatureForm.CAdES), /**
     * Represents a CAdES Baseline LTA signature level.
     *
     * CAdES (CMS Advanced Electronic Signatures) is a set of extensions to CMS (Cryptographic Message Syntax) signed data
     * to provide advanced electronic signatures.
     *
     * Baseline LTA is a conformance level of CAdES defined by ETSI that includes features from Baseline LT and adds the
     * capacity to manage long-term archival of signatures and data.
     */
    CAdES_BASELINE_LTA(
        SignatureForm.CAdES
    ),

    /**
     * Enum constant used to specify a non-ETSI compliant PDF signature form.
     * It utilizes the `PKCS7` signature standard according to ISO 32000.
     *
     * @property form Represents the signature form standard which, in this case, is `PKCS7`.
     */
    PDF_NOT_ETSI(SignatureForm.PKCS7), /**
     * Represents a basic PKCS7 signature level.
     *
     * This level is standardized according to the ISO 32000 specification and denotes
     * a PDF-based signature.
     */
    PKCS7_B(SignatureForm.PKCS7), /**
     * Indicates a PKCS7 digital signature with timestamp.
     *
     * This signature form is based on ISO 32000 and includes timestamp information.
     *
     * @param form the signature form used, which is SignatureForm.PKCS7.
     */
    PKCS7_T(SignatureForm.PKCS7), /**
     * Represents a PKCS7_LT digital signature level.
     * This signature level is based on the PKCS7 standard according to ISO 32000.
     */
    PKCS7_LT(SignatureForm.PKCS7), /**
     * Represents the highest level of compliance for PKCS#7 signatures.
     *
     * PKCS7_LTA stands for PKCS#7 Long Term Archival.
     * This level of signature ensures the long-term preservation of the validity of the signature
     * by including all necessary validation information (e.g., certificates, OCSP responses).
     *
     * @property form The form of the signature, which is PKCS7.
     */
    PKCS7_LTA(
        SignatureForm.PKCS7
    ),
    /**
     * Enum value representing a Baseline B level PAdES (PDF Advanced Electronic Signature).
     * This is one of the baseline profiles defined for electronic signatures in PDFs
     * according to the EN 319 142 standard.
     */
    PAdES_BASELINE_B(SignatureForm.PAdES), /**
     * Enum value representing a PDF-based signature incorporating timestamp information,
     * following the PAdES (PDF Advanced Electronic Signatures) Baseline T profile as specified in EN 319 142.
     */
    PAdES_BASELINE_T(SignatureForm.PAdES), /**
     * Represents the PAdES Baseline LT (Long Term) signature level.
     *
     * PAdES Baseline LT is a profile for PDF signatures that ensures long-term validation.
     * This signature form is based on the PDF Advanced Electronic Signatures (PAdES) standard,
     * specifically following the EN 319 142 standard.
     *
     * The primary goal of this profile is to ensure that electronic signatures
     * remain valid over long periods by incorporating timestamping and other
     * long-term validation features.
     */
    PAdES_BASELINE_LT(SignatureForm.PAdES), /**
     * Represents the highest level of the PAdES signature, PAdES_BASELINE_LTA.
     *
     * PAdES_BASELINE_LTA ensures long-term availability and integrity of the signed
     * document, adhering to the PDF Advanced Electronic Signatures (PAdES) standard.
     * This implies the inclusion of validation data necessary to verify the signature
     * over an extended period of time.
     *
     * @constructor
     * @param form The signature form associated with this level, specifically SignatureForm.PAdES.
     */
    PAdES_BASELINE_LTA(
        SignatureForm.PAdES
    ),

    /**
     * Represents a JSON-based signature that does not conform to the ETSI standards.
     *
     * This signature level specifies the use of the JAdES (JSON Advanced Electronic Signatures)
     * form as defined in TS 119 182.
     */
    JSON_NOT_ETSI(SignatureForm.JAdES), /**
     * Represents the JAdES Baseline B signature level.
     * This is a JSON-based signature form compliant with the ETSI TS 119 182 standard.
     */
    JAdES_BASELINE_B(SignatureForm.JAdES), /**
     * Represents the JAdES (JSON Advanced Electronic Signature) Baseline T level.
     */
    JAdES_BASELINE_T(SignatureForm.JAdES), /**
     * Represents the JAdES Baseline LT signature level.
     * The JAdES (JSON Advanced Electronic Signatures) Baseline LT is a specific signature level in the JAdES standard.
     *
     * @property form The form of digital signature associated with the signature level, in this case, JAdES.
     */
    JAdES_BASELINE_LT(SignatureForm.JAdES), /**
     * This represents the JAdES Baseline Level LTA signature form.
     * JAdES, or JSON Advanced Electronic Signatures, is a framework
     * for creating advanced electronic signatures in a JSON format
     * according to TS 119 182 standard.
     * The Baseline LTA (Long Term Archival) level is designed for
     * long-term validation and archiving.
     */
    JAdES_BASELINE_LTA(
        SignatureForm.JAdES
    );


}
