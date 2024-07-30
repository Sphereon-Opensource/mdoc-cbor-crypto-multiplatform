package com.sphereon.cbor.cose

import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborNInt
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.MajorType
import com.sphereon.kmp.LongKMP
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport
import kotlin.math.abs

@JsExport
class NumberLabel(value: Int) : CoseLabel<LongKMP>(LongKMP(abs(value)), if (value >= 0) CDDL.uint else CDDL.nint, LabelType.Int) {
    override fun encode(builder: ByteStringBuilder) {
        if (majorType == MajorType.NEGATIVE_INTEGER) {
            CborNInt(value).encode(builder)
        } else {
            CborUInt(value).encode(builder)
        }
    }
}
