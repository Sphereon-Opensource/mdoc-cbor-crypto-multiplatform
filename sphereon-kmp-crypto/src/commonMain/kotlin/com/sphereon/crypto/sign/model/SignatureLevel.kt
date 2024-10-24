package com.sphereon.crypto.sign.model

import kotlin.js.JsExport

@JsExport
@kotlinx.serialization.Serializable
enum class SignatureLevel(val form: SignatureForm) {
//    XML_NOT_ETSI, XAdES_BES, XAdES_EPES, XAdES_T, XAdES_LT, XAdES_C, XAdES_X, XAdES_XL, XAdES_A, XAdES_BASELINE_B, XAdES_BASELINE_T, XAdES_BASELINE_LT, XAdES_BASELINE_LTA,

    CMS_NOT_ETSI(SignatureForm.CAdES),
    CAdES_BES(SignatureForm.CAdES), CAdES_EPES(SignatureForm.CAdES), CAdES_T(SignatureForm.CAdES), CAdES_LT(SignatureForm.CAdES), CAdES_C(
        SignatureForm.CAdES
    ),
    CAdES_X(SignatureForm.CAdES), CAdES_XL(SignatureForm.CAdES),
    CAdES_A(SignatureForm.CAdES), CAdES_BASELINE_B(SignatureForm.CAdES), CAdES_BASELINE_T(SignatureForm.CAdES), CAdES_BASELINE_LT(SignatureForm.CAdES), CAdES_BASELINE_LTA(
        SignatureForm.CAdES
    ),

    PDF_NOT_ETSI(SignatureForm.PKCS7), PKCS7_B(SignatureForm.PKCS7), PKCS7_T(SignatureForm.PKCS7), PKCS7_LT(SignatureForm.PKCS7), PKCS7_LTA(
        SignatureForm.PKCS7
    ),
    PAdES_BASELINE_B(SignatureForm.PAdES), PAdES_BASELINE_T(SignatureForm.PAdES), PAdES_BASELINE_LT(SignatureForm.PAdES), PAdES_BASELINE_LTA(
        SignatureForm.PAdES
    ),

    JSON_NOT_ETSI(SignatureForm.JAdES), JAdES_BASELINE_B(SignatureForm.JAdES), JAdES_BASELINE_T(SignatureForm.JAdES), JAdES_BASELINE_LT(SignatureForm.JAdES), JAdES_BASELINE_LTA(
        SignatureForm.JAdES
    );

}
