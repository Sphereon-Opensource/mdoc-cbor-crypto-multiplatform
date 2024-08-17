@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
package com.sphereon.crypto.jose
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * Represents the possible key types defined by the JSON Web Algorithms (JWA) specification.
 * These key types are used for cryptographic operations.
 *
 * @property id The identifier of the key type.
 */
@JsExport
enum class JwaKeyType(val id: String) {
    EC("EC"),
    RSA("RSA"),
    oct("oct"),
    OKP("OKP");

    companion object {
        fun fromValue(value: String): JwaKeyType {
            return JwaKeyType.entries.find { entry -> entry.id == value }
                ?: throw IllegalArgumentException("Unknown value $value")
        }
    }
}
