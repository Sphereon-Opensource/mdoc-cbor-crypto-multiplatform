package com.sphereon.cbor

import com.sphereon.kmp.LongKMP
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport
import kotlin.math.abs

@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
abstract class AbstractCborInt<Type : Number>(value: Type, cddl: CDDL) : CborNumber<Type>(value, cddl)

@JsExport
class CborInt(value: LongKMP) :
    AbstractCborInt<Long>(
        abs(value.toLong()), /*TODO: Check nint value correctnes/offset */
        if (value.toLong() >= 0L) CDDL.uint else CDDL.nint
    ) {
    override fun encode(builder: ByteStringBuilder) {
        if (cddl == CDDL.uint) {
            CborUInt(value).encode(builder)
        } else {
            CborNInt(value).encode(builder)
        }
    }
}
