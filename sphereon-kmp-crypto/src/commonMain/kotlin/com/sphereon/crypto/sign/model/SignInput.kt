package com.sphereon.crypto.sign.model

import com.sphereon.crypto.SigningException
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.kmp.Base64Serializer
import com.sphereon.kmp.Base64UrlSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * A data class representing the input required for a signing operation.
 *
 * @property input The data to be signed, serialized using Base64UrlSerializer.
 * @property signMode The mode of signing, either `DIGEST` or `DOCUMENT`. Defaults to `SigningMode.DIGEST`.
 * @property signDate The date and time when the data was signed, serialized using InstantIso8601Serializer. Defaults to the current system time.
 * @property algorithm The algorithm used for the signature which must include a digest algorithm if `signMode` is `DIGEST`.
 * @property name An optional name for the document being signed, defaulting to "document".
 * @property binding A configuration key binding associated with this signing operation.
 *
 * @throws SigningException If `signMode` is `DIGEST` and the provided algorithm does not include a digest algorithm.
 */
@Serializable
@JsExport
data class SignInput(
    @Serializable(with = Base64Serializer::class) val input: ByteArray,
    val signMode: SigningMode = SigningMode.DIGEST,

    @Serializable(with = InstantIso8601Serializer::class)
    val signDate: Instant = Clock.System.now(),
    val algorithm: SignatureAlgorithm,
    val name: String? = "document",
    val binding: ConfigKeyBinding,
    val packaging: SignaturePackaging = SignaturePackaging.ENVELOPED,
) {

    init {
        if (signMode === SigningMode.DIGEST && algorithm.digestAlgorithm == null) {
            throw SigningException("When signmode is DIGEST, a digest algorithm needs to be present on the signature algorithm")
        }
    }
    /**
     * Compares this `SignInput` instance with another object to determine equality.
     *
     * @param other The object to be compared with this instance.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SignInput

        if (!input.contentEquals(other.input)) return false
        if (signMode != other.signMode) return false
        if (signDate != other.signDate) return false
        if (algorithm != other.algorithm) return false
        if (name != other.name) return false
        if (binding != other.binding) return false

        return true
    }

    /**
     * Computes a hash code for the SignInput object.
     *
     * @return The hash code value of this SignInput instance.
     */
    override fun hashCode(): Int {
        var result = input.contentHashCode()
        result = 31 * result + signMode.hashCode()
        result = 31 * result + signDate.hashCode()
        result = 31 * result + (algorithm.hashCode() ?: 0)
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (binding.hashCode())
        return result
    }
}
