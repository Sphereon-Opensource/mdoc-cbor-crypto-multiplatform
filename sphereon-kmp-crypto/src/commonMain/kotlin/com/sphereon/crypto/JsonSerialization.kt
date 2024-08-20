package com.sphereon.crypto

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.js.JsExport

@JsExport
val cryptoJsonSerializer = CryptoJsonSupport.serializer

@JsExport
object CryptoJsonSupport {
    val serializersModule = SerializersModule {

    /*    // Ensures we can do polymorphic serialization of both the Key and Private Key entries using the IKeyEntry interface
        polymorphic(IJwk::class) {
            subclass(Jwk::class)
        }*/
       /* polymorphic(ICoseKeyJson::class) {
            subclass(CoseKeyJson::class)
        }*/
    }
    val serializer = Json { serializersModule = this@CryptoJsonSupport.serializersModule }
}
