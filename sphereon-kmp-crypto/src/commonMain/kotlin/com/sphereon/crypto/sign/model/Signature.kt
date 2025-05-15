package com.sphereon.crypto.sign.model

import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.Base64Serializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * Data class that represents a cryptographic signature.
 *
 * @property value The byte array representing the signature, serialized in Base64 URL format.
 * @property algorithm The algorithm used for creating the signature.
 * @property signMode The mode used for signing.
 * @property keyInfo The resolved key information used for the signature.
 * @property providerId The ID of the provider used for signing.
 * @property date The timestamp when the signature was created.
 */
@JsExport
@Serializable
data class Signature(
    @Serializable(with = Base64Serializer::class)
    val value: ByteArray,
    val algorithm: SignatureAlgorithm,
    val signMode: SigningMode,
    val keyInfo: IResolvedKeyInfo<*>,
    val level: SignatureLevel,
    val date: Instant
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Signature) return false

        if (!value.contentEquals(other.value)) return false
        if (algorithm != other.algorithm) return false
        if (signMode != other.signMode) return false
        if (keyInfo != other.keyInfo) return false
        if (level != other.level) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + signMode.hashCode()
        result = 31 * result + keyInfo.hashCode()
        result = 31 * result + level.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }
}
