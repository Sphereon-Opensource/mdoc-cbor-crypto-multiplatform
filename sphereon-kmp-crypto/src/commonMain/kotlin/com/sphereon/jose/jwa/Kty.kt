@file:OptIn(ExperimentalJsExport::class)
@file:JsExport
package com.sphereon.jose.jwa
import com.sphereon.cbor.cose.CoseKeyType
import com.sphereon.cbor.cose.CoseSignatureAlgorithm
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.ES256
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.ES384
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.ES512
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.EdDSA
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.HS256
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.HS256_64
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.HS384
import com.sphereon.cbor.cose.CoseSignatureAlgorithm.HS512
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
