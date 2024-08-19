package com.sphereon.cbor

import com.sphereon.kmp.Encoding
import com.sphereon.kmp.encodeTo
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

class RawCbor(value: cddl_bstr) : CborItem<ByteArray>(value, CDDL.bstr) {
    override fun toJsonSimple(): JsonElement {
        return JsonPrimitive(toValue().encodeTo(Encoding.BASE64URL))
    }
    override fun encode(builder: ByteStringBuilder) {
        builder.append(value)
    }
}
