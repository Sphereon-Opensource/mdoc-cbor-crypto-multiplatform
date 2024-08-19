package com.sphereon.crypto

import com.sphereon.crypto.jose.IJwk
import com.sphereon.crypto.jose.IJwkJson
import com.sphereon.crypto.jose.Jwk
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.js.JsExport

val cryptoJsonSerializer = CryptoJsonSupport.serializer

@JsExport
object CryptoJsonSupport {
    val serializersModule = SerializersModule {

        // Ensures we can do polymorphic serialization of both the Key and Private Key entries using the IKeyEntry interface
        polymorphic(IKey::class) {
            subclass(IJwk::class)
            subclass(IJwkJson::class)
            subclass(Jwk::class)
        }
    }
    val serializer = Json { serializersModule = this@CryptoJsonSupport.serializersModule }
}
