package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport

@JsExport
class StringLabel(value: cddl_tstr) : CoseLabel<cddl_tstr>(value, CDDL.tstr, LabelType.String) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(toValue())
    }

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
