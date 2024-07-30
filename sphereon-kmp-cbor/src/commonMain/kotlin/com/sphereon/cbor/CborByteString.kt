package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport

@JsExport
open class CborByteString(value: cddl_bstr) :
    CborItem<cddl_bstr>(value, CDDL.bstr) {
    override fun encode(builder: ByteStringBuilder) {
        Cbor.encodeLength(builder, majorType!!, value.size)
        builder.append(value)
    }

    fun <T : AnyCborItem> cborDecode() = cborSerializer.decode<T>(value)

    @OptIn(ExperimentalStdlibApi::class)
    fun toHexString(): String = this.value.toHexString()

    override fun equals(other: Any?): Boolean = other is CborByteString && value.contentEquals(other.value)

    override fun hashCode(): Int = value.contentHashCode()

    override fun toString(): String {
        val sb = StringBuilder("bstr(")
        for (b in value) {
            sb.append(HEX_DIGITS[b.toInt().and(0xff) shr 4])
            sb.append(HEX_DIGITS[b.toInt().and(0x0f)])
        }
        sb.append(")")
        return sb.toString()
    }

    companion object {
        fun fromCborItem(value: AnyCborItem) = CborByteString(cborSerializer.encode(value))

        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborByteString> {
            val (payloadBegin, length) = Cbor.decodeLength(encodedCbor, offset)
            val payloadEnd = payloadBegin + length.toInt()
            val slice = encodedCbor.sliceArray(IntRange(payloadBegin, payloadEnd - 1))
            return Pair(payloadEnd, CborByteString(slice))
        }
    }


}

fun cddl_bstr.toCborByteString() = CborByteString(this)
class CborByteStringIndefLength(value: List<cddl_bstr>) : CborItem<List<cddl_bstr>>(value, CDDL.bstr_indef_length) {
    override fun encode(builder: ByteStringBuilder) {
        val majorTypeShifted = (majorType!!.type shl 5)
        builder.append((majorTypeShifted + 31).toByte())
        value.forEach {
            Cbor.encodeLength(builder, majorType, it.size)
            builder.append(it)
        }
        builder.append(0xff.toByte())
    }

    companion object {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborByteStringIndefLength> {
            val majorTypeShifted = (MajorType.BYTE_STRING.type shl 5)
            val marker = (majorTypeShifted + 31).toByte()
            check(encodedCbor[offset] == marker)
            val chunks = mutableListOf<ByteArray>()
            var cursor = offset + 1
            while (true) {
                if (encodedCbor[cursor].toInt().and(0xff) == 0xff) {
                    // BREAK code, we're done
                    cursor += 1
                    break
                }
                val (chunkEndOffset, chunk) = Cbor.decodeWithOffset(encodedCbor, cursor)
                check(chunk is CborByteString)
                chunks.add(chunk.value)
                check(chunkEndOffset > cursor)
                cursor = chunkEndOffset
            }
            return Pair(cursor, CborByteStringIndefLength(chunks))
        }

        private val HEX_DIGITS = "0123456789abcdef".toCharArray()
    }
}

fun CborArray<CborByteString>.toHexStringArray() = this.value.map { it.toHexString() }.toTypedArray()

fun Array<HexString>.toCborByteArray() = CborArray(this.map { it.toCborByteString() }.toMutableList())
typealias HexString = String
