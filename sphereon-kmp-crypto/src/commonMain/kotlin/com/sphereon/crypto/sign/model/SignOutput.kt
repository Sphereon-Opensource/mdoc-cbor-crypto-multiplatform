package com.sphereon.crypto.sign.model

import com.sphereon.kmp.Base64UrlSerializer
import kotlin.js.JsExport

/**
 * Data class representing the output of a signing operation.
 *
 * @property value The byte array of the sign input value, serialized in Base64 URL format.
 * @property signMode The mode in which the signing was done, either digest or document.
 * @property level The signature level that was used.
 * @property digestAlgorithm The digest algorithm that was used, if any.
 * @property name The name of the document, defaulting to "document".
 * @property mimeType The MIME type of the document.
 * @property signature The signature associated with the signing operation.
 */
@JsExport
@kotlinx.serialization.Serializable
data class SignOutput(
    @kotlinx.serialization.Serializable(with = Base64UrlSerializer::class)
    val value: ByteArray, // typically the sign input value / document
    val signMode: SigningMode,
    val binding: ConfigKeyBinding,
    val name: String? = "document",
    val mimeType: String?,
    val signature: Signature
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignOutput) return false

        if (!value.contentEquals(other.value)) return false
        if (signMode != other.signMode) return false
        if (binding != other.binding) return false
        if (name != other.name) return false
        if (mimeType != other.mimeType) return false
        if (signature != other.signature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value.contentHashCode()
        result = 31 * result + signMode.hashCode()
        result = 31 * result + binding.hashCode()
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        result = 31 * result + signature.hashCode()
        return result
    }
}
