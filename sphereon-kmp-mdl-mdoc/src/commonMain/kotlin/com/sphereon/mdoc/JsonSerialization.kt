package com.sphereon.mdoc

import com.sphereon.cbor.JsonView
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.js.JsExport

@JsExport
val mdocJsonSerializer = MdocJsonSupport.serializer

@JsExport
object MdocJsonSupport {
    val serializersModule = SerializersModule {

        // Ensures we can do polymorphic serialization of both the Key and Private Key entries using the IKeyEntry interface

        polymorphic(JsonView::class) {
            subclass(CoseKeyJson::class)
            subclass(IssuerSignedItemJson::class)
        }

    }
    val serializer = Json { serializersModule = this@MdocJsonSupport.serializersModule }
}
