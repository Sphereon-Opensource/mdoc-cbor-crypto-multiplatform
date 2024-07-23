package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport

@JsExport
abstract class CborView<CborType, JsonType : JsonView<CborType>, CborItemType>(cddl: CDDLType) : CborBaseItem(cddl) {
    abstract fun cborBuilder(): CborBuilder<CborType>
    open fun cborEncode(): ByteArray {
        return cborBuilder().encodedBuild()
    }


    @Suppress("UNCHECKED_CAST")
    open fun toCborItem(): CborItemType {
        return cborBuilder().build() as CborItemType
    }

    abstract fun toJson(): JsonType
}

@JsExport
abstract class JsonView<CborType> {
    abstract fun toCbor(): CborType
}
