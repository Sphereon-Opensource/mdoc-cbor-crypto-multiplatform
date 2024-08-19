package com.sphereon.cbor

import com.sphereon.kmp.LongKMP
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport
import kotlin.math.abs

@JsExport
class NumberLabel(value: Int) : CoseLabel<LongKMP>(LongKMP(abs(value)), if (value >= 0) CDDL.uint else CDDL.nint, LabelType.Int) {
    override fun toJsonSimple(): JsonElement {
        return JsonPrimitive(toValue())
    }
    override fun encode(builder: ByteStringBuilder) {
        if (majorType == MajorType.NEGATIVE_INTEGER) {
            CborNInt(value).encode(builder)
        } else {
            CborUInt(value).encode(builder)
        }
    }
}
