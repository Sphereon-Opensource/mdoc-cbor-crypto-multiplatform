package com.sphereon.crypto.kms

import kotlinx.serialization.Contextual
import kotlin.js.JsExport

@kotlinx.serialization.Serializable
@JsExport
data class PasswordInputCallback(

    val password: CharArray,

    val protectionAlgorithm: String? = null,
    @Contextual
    val protectionParameters: Any? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PasswordInputCallback

        if (!password.contentEquals(other.password)) return false
        if (protectionAlgorithm != other.protectionAlgorithm) return false
        if (protectionParameters != other.protectionParameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = password.contentHashCode()
        result = 31 * result + (protectionAlgorithm?.hashCode() ?: 0)
        result = 31 * result + (protectionParameters?.hashCode() ?: 0)
        return result
    }
}
