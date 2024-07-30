package com.sphereon.mdoc.data.device

import com.sphereon.cbor.cddl_uint
import com.sphereon.kmp.bigIntFromNumber
import com.sphereon.kmp.toBigInt

import kotlin.js.JsExport

@JsExport
enum class DeviceRetrievalStatusCode(
    val statusCode: cddl_uint,
    val statusMessage: String,
    val description: String,
    val actionsRequired: String
) {
    OK(
        0L.toBigInt(), "OK", "Normal processing. This status message shall be " +
                "returned if no other status is returned", "No specific action required"
    ),
    GENERAL_ERROR(
        10L.toBigInt(),
        "General Error",
        "The mdoc returns an error without any given reason.",
        "The mdoc reader may inspect the\n" +
                "problem. The mdoc reader may\n" +
                "continue the transaction"
    ),
    CBOR_DECODING_ERROR(
        11L.toBigInt(),
        "CBOR decoding error",
        "The mdoc indicates an error during CBOR decoding that the data received is not valid CBOR. Returning this status code is optional.",
        "The mdoc reader may inspect the\n" +
                "problem. The mdoc reader may\n" +
                "continue the transaction."
    ),
    CBOR_VALIDATION_ERROR(
        12L.toBigInt(),
        "CBOR validation error",
        "The mdoc indicates an error during CBOR validation,\n" +
                "e.g. wrong CBOR structures. Returning this status code is optional",
        "The mdoc reader may inspect the\n" +
                "problem. The mdoc reader may\n" +
                "continue the transaction."
    );

    companion object {
        fun fromStatusCode(statusCode: cddl_uint) = entries.first {
            it.statusCode == statusCode
        }
    }
}
