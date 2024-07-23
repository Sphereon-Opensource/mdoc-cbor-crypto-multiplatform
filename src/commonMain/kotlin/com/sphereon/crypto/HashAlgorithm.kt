@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.crypto

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable
enum class HashAlgorithm(val hashName: String) {
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512")
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    val byteIterator = chunkedSequence(2)
        .map { it.toInt(16).toByte() }
        .iterator()

    return ByteArray(length / 2) { byteIterator.next() }
}
