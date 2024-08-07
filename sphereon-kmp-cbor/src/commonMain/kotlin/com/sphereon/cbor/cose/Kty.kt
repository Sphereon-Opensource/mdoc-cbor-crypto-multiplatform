package com.sphereon.cbor.cose

import com.sphereon.cbor.CborUInt
import kotlin.js.JsExport


/**
 * This parameter is used to identify the family of keys for this
 *       structure and, thus, the set of key-type-specific parameters to be
 *       found. This parameter MUST be present in a key object.
 *       Implementations MUST verify that the key type is appropriate for
 *       the algorithm being processed.  The key type MUST be included as
 *       part of the trust decision process.
 */
@JsExport
enum class CoseKeyType(val value: Int, val description: String) {
    OKP(1, "Octet Key Pair"),
    EC2(2, "Elliptic Curve Keys w/ x- and y-coordinate pair"),
    Symmetric(4, "Symmetric Keys"),
    Reserved(0, "Reserved");

    fun toCbor(): CborUInt {
        return CborUInt(this.value)
    }

    companion object {
        fun fromValue(value: Int): CoseKeyType {
            return CoseKeyType.entries.find { entry -> entry.value == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}
