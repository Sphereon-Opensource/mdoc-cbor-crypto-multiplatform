@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.cbor.cose

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
enum class HashAlgorithm(val hashName: String) {
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512")
}

