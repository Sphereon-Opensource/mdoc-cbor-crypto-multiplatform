package com.sphereon.crypto.cose

import com.sphereon.cbor.CborUInt
import kotlin.js.JsExport

@JsExport
enum class CoseKeyOperations(val paramName: String, val value: Int, val description: String) {

    SIGN("sign", 1, "The key is used to create signatures.  Requires private key fields"),
    VERIFY("verify", 2, "The key is used for verification of signatures"),
    ENCRYPT("encrypt", 3, "The key is used for key transport encryption."),
    DECRYPT("decrypt", 4, "The key is used for key transport decryption"),
    WRAP_KEY("wrap key", 5, "The key is used for key wrap encryption."),
    UNWRAP_KEY("unwrap key", 6, "The key is used for key wrap decryption. Requires private key fields"),
    DERIVE_KEY("derive key", 7, "The key is used for deriving keys.  Requires private key fields"),
    DERIVE_BITS(
        "derive bits",
        8,
        "The key is used for deriving bits not to be used as a key.  Requires private key fields."
    ),
    MAC_CREATE("MAC create", 9, "The key is used for creating MACs. "),
    MAC_VERIFY("MAC verify", 10, "The key is used for validating MACs.");

    fun toCbor(): CborUInt {
        return CborUInt(this.value)
    }

    companion object {
        fun fromValue(value: Int): CoseKeyOperations {
            return entries.find { entry -> entry.value == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}
