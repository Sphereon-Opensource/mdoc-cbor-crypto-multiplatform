package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder

class RawCbor(value: cddl_bstr) : CborItem<ByteArray>(value, CDDL.bstr) {
    override fun encode(builder: ByteStringBuilder) {
        builder.append(value)
    }
}
