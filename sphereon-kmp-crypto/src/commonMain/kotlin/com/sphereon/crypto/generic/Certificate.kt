package com.sphereon.crypto.generic

import com.sphereon.kmp.Base64Serializer
import com.sphereon.kmp.Base64UrlSerializer
import kotlinx.datetime.Instant
import kotlin.js.JsExport

/**
 * A X509 certificate. It is encoded in PEM format as bytes in the value field.
 */
@JsExport
@kotlinx.serialization.Serializable
data class Certificate(
    // We choose bas64 here is x5c's are base64 and not base64 url
    @kotlinx.serialization.Serializable(with = Base64Serializer::class) val value: ByteArray,
    val fingerPrint: String,
    val serialNumber: String? = null,
    val issuerDN: String,
    val subjectDN: String,
    val notBefore: Instant,
    val notAfter: Instant,
    val keyUsage: Map<String, Boolean>? = null
)
