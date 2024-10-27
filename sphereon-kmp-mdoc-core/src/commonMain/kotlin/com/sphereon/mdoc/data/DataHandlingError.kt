package com.sphereon.mdoc.data

import com.sphereon.cbor.cddl_int
import com.sphereon.kmp.LongKMP

enum class DataHandlingError(
    val errorCode: DataHandlingErrorCode,
    val errorCodeMessage: String,
    val description: String
) {
    OK(
        DataHandlingErrorCode.OK,
        "Data not returned",
        "NThe mdoc does not provide the requested document or data element without\n" +
                "any given reason. This element may be used in all cases."
    ),
    RFU(
        DataHandlingErrorCode.RFU,
        "RFU",
        "RFU"
    ),
    APPLICATION_SPECIFIC(
        DataHandlingErrorCode.APPLICATION_SPECIFIC,
        "These error codes may be used for application-specific purposes.",
        "These error codes may be used for application-specific purposes."
    );

    object Static {
        fun fromErrorCode(errorCode: cddl_int): DataHandlingError {
            return entries.find { it.errorCode == DataHandlingErrorCode.Static.fromErrorCodeValue(errorCode) }!!
        }
    }
}

enum class DataHandlingErrorCode(val value: cddl_int) {
    OK(LongKMP(0)), RFU(LongKMP(1)), APPLICATION_SPECIFIC(LongKMP(-1));

    object Static {
        fun fromErrorCodeValue(errorCode: cddl_int): DataHandlingErrorCode {
            return if (errorCode == OK.value) {
                OK
            } else if (errorCode >= RFU.value) {
                RFU
            } else {
                // erroCode <= ErrorCode.APPLICATION_SPECIFIC
                APPLICATION_SPECIFIC
            }
        }
    }
}
