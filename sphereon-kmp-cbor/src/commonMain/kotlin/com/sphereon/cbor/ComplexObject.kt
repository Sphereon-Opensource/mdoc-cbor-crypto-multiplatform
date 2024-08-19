package com.sphereon.cbor

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
abstract class CborView<CborViewType: Any, JsonViewType : JsonView, CborType2>(cddl: CDDLType) : CborBaseItem(cddl) {
    abstract fun cborBuilder(): CborBuilder<CborViewType>
    open fun cborEncode(): ByteArray {
        return cborBuilder().encodedBuild()
    }


    @Suppress("UNCHECKED_CAST")
    open fun toCbor(): CborType2 {
        return cborBuilder().build() as CborType2
    }

    abstract fun toJson(): JsonViewType
}

@JsExport
abstract class CborView2<CborViewType, JsonViewType2, CborType2>(cddl: CDDLType) : CborBaseItem(cddl) {
    abstract fun cborBuilder(): CborBuilder<CborViewType>
    open fun cborEncode(): ByteArray {
        return cborBuilder().encodedBuild()
    }


    @Suppress("UNCHECKED_CAST")
    open fun toCbor(): CborType2 {
        return cborBuilder().build() as CborType2
    }

    abstract fun toJson(): JsonViewType2
}


@Serializable
@JsExport
abstract class JsonViewOld<CborType: Any>: JsonView() {
    abstract override fun toCbor(): CborType
}
@Serializable
@JsExport
abstract class JsonView {
    abstract fun toCbor(): Any
}
