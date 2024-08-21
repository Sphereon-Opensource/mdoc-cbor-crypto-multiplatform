package com.sphereon.crypto

import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.json.CborJsonSupport
import com.sphereon.json.JsonView
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.js.JsExport

@JsExport
val cryptoJsonSerializer = CryptoJsonSupport.serializer

@JsExport
object CryptoJsonSupport {
    val serializersModule = SerializersModule {
        include(CborJsonSupport.serializersModule)

    /*    // Ensures we can do polymorphic serialization of both the Key and Private Key entries using the IKeyEntry interface
        polymorphic(IJwk::class) {
            subclass(Jwk::class)
        }*/
       /* polymorphic(ICoseKeyJson::class) {
            subclass(CoseKeyJson::class)
        }*/
        polymorphic(JsonView::class) {
            subclass(CoseKeyJson::class)
        }
    }
    val serializer = Json { serializersModule = this@CryptoJsonSupport.serializersModule }
}
