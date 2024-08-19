package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlin.js.JsExport

@JsExport
class CborAny<Type : Any>(value: Type) : CborItem<Type>(value, CDDL.any) {
    override fun encode(builder: ByteStringBuilder) {
        builder.append(cborSerializer.encode((this.cddl as CDDL).newCborItem(this.value)))
    }

    override fun toJsonSimple(): JsonElement {
        TODO("No to Json on a Cbor Any yet")
    }
}
