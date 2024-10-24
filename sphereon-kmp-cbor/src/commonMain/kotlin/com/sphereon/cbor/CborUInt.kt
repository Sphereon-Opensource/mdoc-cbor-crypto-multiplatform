package com.sphereon.cbor

import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.uintToKmpLong
import com.sphereon.kmp.ulongToKmpLong
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
open class CborUInt(value: LongKMP /*UInt*/) : AbstractCborInt<cddl_uint>(value/*.toLong()*/, CDDL.uint) {
    @JsName("fromNumber")
    constructor(value: Number) : this(LongKMP(value.toLong()))

    override fun validate() {
        if (value.toLong() < 0) {
//            throw IllegalArgumentException("Negative number ${value} not allowed for Cbor uint type")
        }
    }

    override fun encode(builder: ByteStringBuilder) {
        Cbor.encodeLength(builder, majorType!!, value.toULong())
    }

    object Static {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborUInt> {
            val (newOffset, value) = Cbor.decodeLength(encodedCbor, offset)
            return Pair(newOffset, CborUInt(value.ulongToKmpLong()))
        }
    }
}

fun CborUInt.toUInt() = value.toUInt()
fun UInt.toCborUIntFromUint() = CborUInt(this.uintToKmpLong())
fun cddl_uint.toCborUInt() = CborUInt(this)
