package com.sphereon.cbor

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport

@JsExport
abstract class CborBool(value: cddl_bool, cddl: CDDL) : CborSimple<cddl_bool>(value, cddl) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(toValue())
    }
}

@JsExport
class CborTrue : CborBool(true, CDDL.True) {
    init {
        if (!value) {
            throw IllegalArgumentException("True requires value ${true}")
        }
    }
}

@JsExport
class CborFalse : CborBool(false, CDDL.False) {
    init {
        if (value) {
            throw IllegalArgumentException("False requires value ${false}")
        }
    }
}

fun cddl_bool.toCborBool() = CDDL.bool.newBool(this)
