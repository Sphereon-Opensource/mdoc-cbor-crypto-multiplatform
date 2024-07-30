package com.sphereon.cbor

import kotlin.js.JsExport

@JsExport
abstract class CborView<CborViewType, JsonViewType : JsonView<CborViewType>, CborType>(cddl: CDDLType) : CborBaseItem(cddl) {
    abstract fun cborBuilder(): CborBuilder<CborViewType>
    open fun cborEncode(): ByteArray {
        return cborBuilder().encodedBuild()
    }


    @Suppress("UNCHECKED_CAST")
    open fun toCbor(): CborType {
        return cborBuilder().build() as CborType
    }

    abstract fun toJson(): JsonViewType
}

@JsExport
abstract class JsonView<CborType> {
    abstract fun toCbor(): CborType
}
