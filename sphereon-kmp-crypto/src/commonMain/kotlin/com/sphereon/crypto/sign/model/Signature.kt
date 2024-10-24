package com.sphereon.crypto.sign.model

import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.Base64UrlSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class Signature(
    @Serializable(with = Base64UrlSerializer::class)
    val value: ByteArray,
    val algorithm: SignatureAlgorithm,
    val signMode: SigningMode,
    val keyInfo: IResolvedKeyInfo<*>,
    val providerId: String,
    val date: Instant
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Signature

        if (!value.contentEquals(other.value)) return false
        if (algorithm != other.algorithm) return false
        if (signMode != other.signMode) return false
        if (keyInfo != other.keyInfo) return false
        if (providerId != other.providerId) return false
        if (date != other.date) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + algorithm.hashCode()
        result = 31 * result + signMode.hashCode()
        result = 31 * result + keyInfo.hashCode()
        result = 31 * result + providerId.hashCode()
        result = 31 * result + date.hashCode()
        return result
    }
}
