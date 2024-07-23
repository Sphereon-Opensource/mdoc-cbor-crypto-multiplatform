package com.sphereon.cbor

import kotlin.js.JsExport

@JsExport
class CborUndefined : CborSimple<cddl_null>(Unit, CDDL.undefined) {

    init {
        if (value != Unit) {
            throw IllegalArgumentException("Undefined requires value Unit")
        }
    }
}
