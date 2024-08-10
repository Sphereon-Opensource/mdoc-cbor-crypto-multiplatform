package com.sphereon.jose.jwk

import kotlin.js.JsExport

@JsExport
enum class JoseKeyOperations(val value: String, val description: String) {

    SIGN("sign","The key is used to create signatures.  Requires private key fields"),
    VERIFY("verify", "The key is used for verification of signatures"),
    ENCRYPT("encrypt", "The key is used for key transport encryption."),
    DECRYPT("decrypt", "The key is used for key transport decryption"),
    WRAP_KEY("wrap key", "The key is used for key wrap encryption."),
    UNWRAP_KEY("unwrap key", "The key is used for key wrap decryption. Requires private key fields"),
    DERIVE_KEY("derive key", "The key is used for deriving keys.  Requires private key fields"),
    DERIVE_BITS(
        "derive bits",
        "The key is used for deriving bits not to be used as a key.  Requires private key fields."
    ),
    MAC_CREATE("MAC create", "The key is used for creating MACs. "),
    MAC_VERIFY("MAC verify",  "The key is used for validating MACs.");

    companion object {
        fun fromValue(value: String): JoseKeyOperations {
            return entries.find { entry -> entry.value == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}
