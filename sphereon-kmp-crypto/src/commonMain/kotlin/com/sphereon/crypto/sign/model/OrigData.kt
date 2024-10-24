package com.sphereon.crypto.sign.model

import com.sphereon.kmp.Base64UrlSerializer
import kotlin.js.JsExport

/**
 * Represent the original data at the start of a signature/digest process or data which gets included in a signature.
 * This data eventually will be merged with a signature
 */
@JsExport
@kotlinx.serialization.Serializable
data class OrigData(
    @kotlinx.serialization.Serializable(with = Base64UrlSerializer::class) val value: ByteArray,
    val mimeType: String? = null,
    val name: String? = "document"
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OrigData

        if (!value.contentEquals(other.value)) return false
        if (mimeType != other.mimeType) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }
}
