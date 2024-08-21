package com.sphereon.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.js.JsExport

@JsExport
val cborJsonSerializer = CborJsonSupport.serializer

@JsExport
object CborJsonSupport {
    val serializersModule = SerializersModule {
    }
    val serializer = Json { serializersModule = this.serializersModule }
}
