package com.sphereon.cbor

import kotlin.js.JsExport

@JsExport
class CborUndefined(value: cddl_undefined? = Unit) : CborSimple<cddl_null>(value ?: Unit, CDDL.undefined) {

    init {
        if (value != Unit) {
            throw IllegalArgumentException("Undefined requires value Unit")
        }
    }
}
