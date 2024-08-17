package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport

@JsExport
class StringLabel(value: cddl_tstr) : CoseLabel<cddl_tstr>(value, CDDL.tstr, LabelType.String) {


    override fun encode(builder: ByteStringBuilder) {
        val encodedValue = value.encodeToByteArray()
        Cbor.encodeLength(builder, majorType!!, encodedValue.size)
        builder.append(value.encodeToByteArray())
    }

    companion object {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborString> {
            val (newOffset, value) = Cbor.decodeLength(encodedCbor, offset)
            return Pair(newOffset, CborString(value.toString()))
        }
    }
}
