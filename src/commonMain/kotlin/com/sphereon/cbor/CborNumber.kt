package com.sphereon.cbor

import com.sphereon.kmp.bigIntFromNumber

abstract class CborNumber<Type : Number>(value: Type, cddl: CDDLType) : CborItem<Type>(value, cddl)
fun Long.toCborIntFromLong() = run {
    if (this >= 0) {
        CborUInt(this.bigIntFromNumber())
    } else {
        CborNInt((-this).bigIntFromNumber())
    }
}
