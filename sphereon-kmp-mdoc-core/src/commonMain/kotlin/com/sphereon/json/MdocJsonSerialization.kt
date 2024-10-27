package com.sphereon.json

import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import com.sphereon.mdoc.oid4vp.IOid4VPPresentationSubmission
import com.sphereon.mdoc.oid4vp.Oid4VPPresentationSubmission
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("mdocJsonSerializer")
val mdocJsonSerializer = MdocJsonSupport.serializer

@JsExport
@JsName("MdocJsonSupport")
object MdocJsonSupport {
    val module: SerializersModule = SerializersModule {
//        include(CborJsonSupport.module)
//        include(CryptoJsonSupport.module)

        // Ensures we can do polymorphic serialization
        /*polymorphic(IOid4VPPresentationDefinition::class) {
            subclass(
                Oid4VPPresentationDefinition::class
            )
        }
        polymorphic(IOid4VPPresentationSubmission::class) {
            subclass(
                Oid4VPPresentationSubmission::class
            )
        }*/
     /*   polymorphicDefaultDeserializer(IOid4VPPresentationSubmission::class, { Oid4VPPresentationSubmission.serializer() })
        polymorphicDefaultDeserializer(IOid4VPPresentationDefinition::class, { Oid4VPPresentationDefinition.serializer() })*/
        polymorphic(JsonView::class) {
            subclass(CoseKeyJson::class)
            subclass(IssuerSignedItemJson::class)
        }

    }
    val serializer = Json { serializersModule = module; encodeDefaults = true; isLenient = true; prettyPrint = true; ignoreUnknownKeys = true }
}
