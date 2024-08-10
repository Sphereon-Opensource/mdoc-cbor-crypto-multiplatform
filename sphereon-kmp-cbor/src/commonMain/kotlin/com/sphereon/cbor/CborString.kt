package com.sphereon.cbor

import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import io.matthewnelson.encoding.base64.Base64
import io.matthewnelson.encoding.core.Decoder.Companion.decodeToByteArray
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport
import kotlin.uuid.Uuid

@JsExport
class CborString(value: cddl_tstr) : CborItem<cddl_tstr>(value, CDDL.tstr) {
    override fun encode(builder: ByteStringBuilder) {
        val encodedValue = value.encodeToByteArray()
        Cbor.encodeLength(builder, majorType!!, encodedValue.size)
        builder.append(value.encodeToByteArray())
    }

    companion object {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborString> {
            val (payloadBegin, length) = Cbor.decodeLength(encodedCbor, offset)
            val payloadEnd = payloadBegin + length.toInt()
            val slice = encodedCbor.sliceArray(IntRange(payloadBegin, payloadEnd - 1))
            return Pair(payloadEnd, CborString(slice.decodeToString()))
        }
    }
}

fun cddl_tstr.toCborString() = CborString(this)
fun Array<String>.toCborStringArray() = CborArray(this.map { it.toCborString() }.toMutableList())
fun CborArray<CborString>.toStringArray() = this.value.map { it.value }.toTypedArray()

private val HEX_ALPHABET = "0123456789abcdefABCDEF"

@OptIn(ExperimentalStdlibApi::class)
fun cddl_tstr.toCborByteString(encoding: Encoding? = null): CborByteString {
    if (encoding === Encoding.HEX || (encoding === null && this.length % 2 == 0)) {
        if (this.equals(this.filter { HEX_ALPHABET.contains(it) })) {
            return CborByteString(this.hexToByteArray())
        }
    }
    if ((encoding === Encoding.BASE64URL || encoding === Encoding.BASE64 ||
                (encoding === null &&
                        (this == this.filter { (Base64.Default.CHARS + "=").contains(it) } ||
                                this == this.filter { (Base64.UrlSafe.CHARS + "=").contains(it) })))
    ) {
        if (encoding === Encoding.BASE64URL || this.contains("-") || this.contains("_")) {
            return CborByteString(this.decodeToByteArray(Base64.UrlSafe))
        }
        // Base64
        return CborByteString(this.decodeToByteArray(Base64.Default))
    }
    // UTF-8
    return CborByteString(this.decodeFrom(Encoding.UTF8))
}
