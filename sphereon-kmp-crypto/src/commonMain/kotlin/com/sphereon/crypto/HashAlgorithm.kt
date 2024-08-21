@file:OptIn(ExperimentalJsExport::class)
@file:JsExport

package com.sphereon.crypto

import dev.whyoleg.cryptography.CryptographyAlgorithmId
import dev.whyoleg.cryptography.algorithms.digest.Digest
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.js.JsName

@Serializable
@JsExport
enum class HashAlgorithm(val hashName: String) {
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512");

    @JsExport.Ignore
    fun toCryptoGraphicAlgorithm(): CryptographyAlgorithmId<Digest> {
        return if (this == SHA256) dev.whyoleg.cryptography.algorithms.digest.SHA256
        else if (this == SHA384) dev.whyoleg.cryptography.algorithms.digest.SHA384
        else if (this == SHA512) dev.whyoleg.cryptography.algorithms.digest.SHA512
        else throw IllegalArgumentException()
    }

    object Static {
        @JsName("fromValue")
        fun fromValue(name: String): HashAlgorithm {
            return HashAlgorithm.entries.find { entry -> entry.hashName === name }
                ?: throw IllegalArgumentException("Unknown value $name")
        }
    }
}

