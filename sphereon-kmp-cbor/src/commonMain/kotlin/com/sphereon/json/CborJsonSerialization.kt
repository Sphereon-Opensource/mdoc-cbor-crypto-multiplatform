package com.sphereon.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("cborJsonSerializer")
val cborJsonSerializer = CborJsonSupport.serializer

@JsExport
@JsName("CborJsonSupport")
object CborJsonSupport {
     val module: SerializersModule = SerializersModule {
     }
    val serializer = Json { serializersModule = module }
}
