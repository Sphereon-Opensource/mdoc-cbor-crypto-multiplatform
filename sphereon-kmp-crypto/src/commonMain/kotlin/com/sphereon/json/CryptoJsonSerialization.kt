package com.sphereon.json

import com.sphereon.crypto.cose.CoseKeyJson
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("cryptoJsonSerializer")
val cryptoJsonSerializer = CryptoJsonSupport.serializer

@JsExport
@JsName("CryptoJsonSupport")
object CryptoJsonSupport {
    val module: SerializersModule = SerializersModule {
//        include(CborJsonSupport.serializersModule)

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
    val serializer = Json { serializersModule = module }
}
