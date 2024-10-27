package com.sphereon.json

import com.sphereon.mdoc.oid4vp.IOid4VPPresentationDefinition
import com.sphereon.mdoc.oid4vp.IOid4VPPresentationSubmission
import com.sphereon.mdoc.oid4vp.Oid4VPPresentationDefinition
import com.sphereon.mdoc.oid4vp.Oid4VPPresentationSubmission
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@JsName("oid4vpJsonSerializer")
val oid4vpJsonSerializer = MdocJsonSupport.serializer

@JsExport
@JsName("Oid4vpJsonSupport")
object Oid4vpJsonSupport {
    private val module: SerializersModule = SerializersModule {
//        include(CborJsonSupport.module)
//        include(CryptoJsonSupport.module)
//        include(MdocJsonSupport.module)

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
        polymorphicDefaultDeserializer(IOid4VPPresentationSubmission::class, { Oid4VPPresentationSubmission.serializer() })
        polymorphicDefaultDeserializer(IOid4VPPresentationDefinition::class, { Oid4VPPresentationDefinition.serializer() })

    }
    val serializer = Json { serializersModule = module; encodeDefaults = true; isLenient = true; prettyPrint = true; ignoreUnknownKeys = true }
}
