package com.sphereon.mdoc

import com.sphereon.json.JsonView
import com.sphereon.crypto.CryptoJsonSupport
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import com.sphereon.mdoc.oid4vp.IOid4VPPresentationDefinition
import com.sphereon.mdoc.oid4vp.Oid4VPPresentationDefinition
import kotlinx.serialization.builtins.serializer
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
        include(CryptoJsonSupport.serializersModule)

        // Ensures we can do polymorphic serialization of both the Key and Private Key entries using the IKeyEntry interface
        polymorphic(IOid4VPPresentationDefinition::class) {
            subclass(
                Oid4VPPresentationDefinition::class
            )
        }
        polymorphicDefaultDeserializer(IOid4VPPresentationDefinition::class, { Oid4VPPresentationDefinition.serializer() })
        polymorphic(JsonView::class) {
            subclass(CoseKeyJson::class)
            subclass(IssuerSignedItemJson::class)
        }

    }
    val serializer = Json { serializersModule = this.serializersModule; encodeDefaults = true }
}
