package com.sphereon.crypto.cose

import com.sphereon.cbor.AbstractCborInt
import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.encodeToBase64Array
import com.sphereon.cbor.encodeToCborByteArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.cbor.toCborStringArray
import com.sphereon.cbor.toNumberLabel
import com.sphereon.cbor.toStringArray
import com.sphereon.crypto.cryptoJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.numberToKmpLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport

@JsExport
@Serializable
data class CoseHeaderJson(

    /**
     * 3.1.  Common COSE Headers Parameters are listed below
     *
     * See https://www.rfc-editor.org/rfc/rfc8152 Table 2
     *
     */
    val alg: CoseSignatureAlgorithm? = null,
    val crit: Array<String>? = null,
    val contentType: String? = null,
    val kid: String? = null,
    val iv: String? = null,
    val partialIv: String? = null,
    val x5chain: Array<String>? = null,
) : JsonView() {
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)

    override fun toCbor(): CoseHeaderCbor = CoseHeaderCbor(
        alg = alg,
        crit = crit?.toCborStringArray(),
        contentType = contentType?.toCborString(),
        kid = kid?.toCborByteString(Encoding.UTF8),
        partialIv = partialIv?.toCborByteString(Encoding.UTF8),
        iv = iv?.toCborByteString(Encoding.UTF8),
        x5chain = x5chain?.encodeToCborByteArray(Encoding.BASE64) // base64 not url
    )

    // TODO: To JOSE
}


@JsExport
data class CoseHeaderCbor(

    /**
     * 3.1.  Common COSE Headers Parameters are listed below
     *
     * See https://www.rfc-editor.org/rfc/rfc8152 Table 2
     *
     */

    val alg: CoseSignatureAlgorithm? = null,
    val crit: CborArray<CborString>? = null,
    val contentType: CborString? = null,
    val kid: CborByteString? = null,
    val iv: CborByteString? = null,
    val partialIv: CborByteString? = null,
    val x5chain: CborArray<CborByteString>? = null,
) : CborView<CoseHeaderCbor, CoseHeaderJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map) {
    companion object {
        val ALG = NumberLabel(1)
        val CRIT = NumberLabel(2)
        val CONTENT_TYPE = NumberLabel(3)
        val KID = NumberLabel(4)
        val IV = NumberLabel(5)
        val PARTIAL_IV = NumberLabel(6)
        val X5CHAIN = NumberLabel(33)

        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>): CoseHeaderCbor {
            val algValue = ALG.optional<AbstractCborInt<LongKMP>>(m)
                ?.let { if (it.cddl == CDDL.nint) -it.value.toInt() else it.value.toInt() }

            /**
             * If a single certificate is conveyed, it is placed in a CBOR byte string.
             * If multiple certificates are conveyed, a CBOR array of byte strings is used, with each certificate being in its own byte string.
             */
            var x5Chain = X5CHAIN.optional<AnyCborItem>(m)
            if (x5Chain is CborByteString) {
                x5Chain = CborArray(mutableListOf(x5Chain))
            }
            @Suppress("UNCHECKED_CAST") return CoseHeaderCbor(
                alg = CoseSignatureAlgorithm.Static.fromValue(algValue),
                crit = CRIT.optional(m),
                contentType = CONTENT_TYPE.optional(m),
                kid = KID.optional(m),
                iv = IV.optional(m),
                partialIv = PARTIAL_IV.optional(m),
                x5chain = if (x5Chain != null) x5Chain as CborArray<CborByteString> else null
            )
        }

        fun cborDecode(encoded: ByteArray) = fromCborItem(cborSerializer.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<CoseHeaderCbor> {
        return CborMap.builder(this).put(ALG, alg?.value?.numberToKmpLong()?.toInt()?.toNumberLabel(), true)
            .put(CRIT, crit, true).put(CONTENT_TYPE, contentType, true).put(KID, kid, true).put(IV, iv, true)
            .put(PARTIAL_IV, partialIv, true)
            /**
             * If a single certificate is conveyed, it is placed in a CBOR byte string.
             * If multiple certificates are conveyed, a CBOR array of byte strings is used, with each certificate being in its own byte string.
             *
             */
            .put(X5CHAIN, if (x5chain?.value?.size == 1) x5chain.value[0] else x5chain, true).end()

    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toJson(): CoseHeaderJson = CoseHeaderJson(
        alg = alg,
        crit = crit?.toStringArray(),
        contentType = contentType?.toString(),
        kid = kid?.encodeTo(Encoding.UTF8),
        iv = iv?.encodeTo(Encoding.UTF8),
        partialIv = partialIv?.encodeTo(Encoding.UTF8),
        x5chain = x5chain?.encodeToBase64Array()
    )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoseHeaderCbor) return false

        if (alg != other.alg) return false
        if (crit != other.crit) return false
        if (contentType != other.contentType) return false
        if (kid != other.kid) return false
        if (iv != other.iv) return false
        if (partialIv != other.partialIv) return false
        if (x5chain != other.x5chain) return false

        return true
    }

    override fun hashCode(): Int {
        var result = alg?.hashCode() ?: 0
        result = 31 * result + (crit?.hashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (kid?.hashCode() ?: 0)
        result = 31 * result + (iv?.hashCode() ?: 0)
        result = 31 * result + (partialIv?.hashCode() ?: 0)
        result = 31 * result + (x5chain?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "CoseHeaderCbor(alg=$alg, crit=$crit, contentType=$contentType, kid=$kid, iv=$iv, partialIv=$partialIv, x5chain=$x5chain)"
    }


}
