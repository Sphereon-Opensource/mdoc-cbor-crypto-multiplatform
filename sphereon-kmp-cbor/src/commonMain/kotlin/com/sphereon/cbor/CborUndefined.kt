package com.sphereon.cbor

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport

@JsExport
class CborUndefined : CborSimple<cddl_null>(Unit, CDDL.undefined) {

    init {
        if (value != Unit) {
            throw IllegalArgumentException("Undefined requires value Unit")
        }
    }

    override fun toJsonSimple(): JsonElement = JsonPrimitive(null)
}
