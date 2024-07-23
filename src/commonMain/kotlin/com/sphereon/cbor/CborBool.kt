package com.sphereon.cbor

import kotlin.js.JsExport

@JsExport
abstract class CborBool(value: cddl_bool, cddl: CDDL) : CborSimple<cddl_bool>(value, cddl)

@JsExport
class CborTrue : CborBool(true, CDDL.True) {
    init {
        if (value != true) {
            throw IllegalArgumentException("True requires value ${true}")
        }
    }
}

@JsExport
class CborFalse : CborBool(false, CDDL.False) {
    init {
        if (value != false) {
            throw IllegalArgumentException("False requires value ${false}")
        }
    }
}

fun cddl_bool.toCborBool() = CDDL.bool.newBool(this)
