package com.sphereon.crypto.sign.model

import com.sphereon.crypto.SigningException
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.kmp.Base64UrlSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@Serializable
@JsExport
data class SignInput(
    @Serializable(with = Base64UrlSerializer::class) val input: ByteArray,
    val signMode: SigningMode = SigningMode.DIGEST,

    @Serializable(with = InstantIso8601Serializer::class)
    val signingDate: Instant,
    val digestAlgorithm: DigestAlg?,
    val name: String? = "document",
    val binding: ConfigKeyBinding
) {

    init {
        if (signMode === SigningMode.DIGEST && digestAlgorithm == null) {
            throw SigningException("When signmode is DIGEST, a digest algorithm needs to be provided")
        }
    }
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SignInput

        if (!input.contentEquals(other.input)) return false
        if (signMode != other.signMode) return false
        if (signingDate != other.signingDate) return false
        if (digestAlgorithm != other.digestAlgorithm) return false
        if (name != other.name) return false
        if (binding != other.binding) return false

        return true
    }

    override fun hashCode(): Int {
        var result = input.contentHashCode()
        result = 31 * result + signMode.hashCode()
        result = 31 * result + signingDate.hashCode()
        result = 31 * result + (digestAlgorithm?.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (binding.hashCode())
        return result
    }
}
