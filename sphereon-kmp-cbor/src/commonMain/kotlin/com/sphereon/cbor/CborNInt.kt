package com.sphereon.cbor

import com.sphereon.kmp.LongKMP
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
class CborNInt(value: cddl_nint) : AbstractCborInt<cddl_nint>(value, CDDL.nint) {

    @JsName("fromLong")
    constructor(value: Long) : this(LongKMP(value))
    override fun validate() {
        if (value.toLong() > 0) {
//            throw IllegalArgumentException("Positive number (${value}) not allowed for Cbor nint type")
        }
    }

    override fun encode(builder: ByteStringBuilder) {
        Cbor.encodeLength(builder, majorType!!, value.toInt() - 1)
    }


    object Static {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborNInt> {
            val (newOffset, value) = Cbor.decodeLength(encodedCbor, offset)
            return Pair(newOffset, CborNInt(LongKMP(value.toLong() + 1L)))
        }
    }
}
