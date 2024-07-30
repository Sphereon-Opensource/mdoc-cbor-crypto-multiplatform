@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
package com.sphereon.jose.jwa
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


/**
 * Represents the possible key types defined by the JSON Web Algorithms (JWA) specification.
 * These key types are used for cryptographic operations.
 *
 * @property id The identifier of the key type.
 */
@Serializable
enum class JwaKeyType(val id: String) {
    EC("EC"),
    RSA("RSA"),
    oct("oct"),
    OKP("OKP")
}
