package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport

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
@OptIn(ExperimentalStdlibApi::class)
fun cddl_tstr.toCborByteString() = CborByteString(this.hexToByteArray())
