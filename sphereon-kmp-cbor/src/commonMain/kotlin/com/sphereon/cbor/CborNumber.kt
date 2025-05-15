package com.sphereon.cbor

import com.sphereon.kmp.numberToKmpLong
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

abstract class CborNumber<Type : Number>(value: Type, cddl: CDDLType) : CborItem<Type>(value, cddl) {
    override fun toJsonSimple(): JsonElement {
        return JsonPrimitive(toValue())
    }
}
fun Long.toCborIntFromLong() = run {
    if (this >= 0) {
        CborUInt(this.numberToKmpLong())
    } else {
        CborNInt((-this).numberToKmpLong())
    }
}
