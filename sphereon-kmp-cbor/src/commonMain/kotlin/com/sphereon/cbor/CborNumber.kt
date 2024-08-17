package com.sphereon.cbor

import com.sphereon.kmp.numberToKmpLong

abstract class CborNumber<Type : Number>(value: Type, cddl: CDDLType) : CborItem<Type>(value, cddl)
fun Long.toCborIntFromLong() = run {
    if (this >= 0) {
        CborUInt(this.numberToKmpLong())
    } else {
        CborNInt((-this).numberToKmpLong())
    }
}
