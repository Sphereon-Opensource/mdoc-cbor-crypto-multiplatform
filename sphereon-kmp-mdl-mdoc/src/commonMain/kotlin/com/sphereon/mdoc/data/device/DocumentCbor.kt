package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.StringLabel
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.mdoc.data.DocumentErrorsCbor
import com.sphereon.mdoc.data.DocumentErrorsJson
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@Serializable
data class DocumentJson (
    /**
     * In the Document structure, the document type of the returned document is indicated by the
     * docType element. The document type shall match the document type as indicated in the issuer data
     *
     * authentication (see 9.1.2) and mdoc authentication structures (see 9.1.3). errors can contain error
     * codes for data elements that are not returned.
     */
    val docType: String,

    /**
     * IssuerSigned contains the mobile security object for issuer data authentication and the data elements
     * protected by issuer data authentication. nameSpaces contains the returned data elements as part of
     * their corresponding namespaces.
     */
    val issuerSigned: IssuerSignedJson,

    /**
     * DeviceSigned contains the mdoc authentication structure and the data elements protected by mdoc
     * authentication. nameSpaces contains the returned data elements as part of their corresponding
     * namespaces. nameSpaces is a mandatory element because the element is authenticated using mdoc
     * authentication. The DeviceNameSpaces structure can be an empty structure. The DeviceAuth structure
     * contains either the DeviceSignature or the DeviceMac element, both are defined in 9.1.3.
     */
    // fixme
    val deviceSigned: DeviceSignedJson,

    /**
     * If the device retrieval mdoc response structure does not include some data element or document
     * requested in the device retrieval mdoc request, an error code may be returned as part of the
     * documentErrors or errors structures.
     * If present, ErrorCode shall contain an error code according to 8.3.2.1.2.3.
     */
    // fixme
    val errors: DocumentErrorsJson?
): JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DocumentCbor {
        TODO("Not yet implemented")
    }
}


@JsExport
data class DocumentCbor (
    /**
     * In the Document structure, the document type of the returned document is indicated by the
     * docType element. The document type shall match the document type as indicated in the issuer data
     *
     * authentication (see 9.1.2) and mdoc authentication structures (see 9.1.3). errors can contain error
     * codes for data elements that are not returned.
     */
    val docType: CborString,

    /**
     * IssuerSigned contains the mobile security object for issuer data authentication and the data elements
     * protected by issuer data authentication. nameSpaces contains the returned data elements as part of
     * their corresponding namespaces.
     */
    val issuerSigned: IssuerSignedCbor,

    /**
     * DeviceSigned contains the mdoc authentication structure and the data elements protected by mdoc
     * authentication. nameSpaces contains the returned data elements as part of their corresponding
     * namespaces. nameSpaces is a mandatory element because the element is authenticated using mdoc
     * authentication. The DeviceNameSpaces structure can be an empty structure. The DeviceAuth structure
     * contains either the DeviceSignature or the DeviceMac element, both are defined in 9.1.3.
     */
    val deviceSigned: DeviceSignedCbor,

    /**
     * If the device retrieval mdoc response structure does not include some data element or document
     * requested in the device retrieval mdoc request, an error code may be returned as part of the
     * documentErrors or errors structures.
     * If present, ErrorCode shall contain an error code according to 8.3.2.1.2.3.
     */
    val errors: DocumentErrorsCbor?
) : CborView<DocumentCbor, DocumentJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DocumentCbor> {
        TODO("Not yet implemented")
    }

    override fun toJson(): DocumentJson {
        TODO("Not yet implemented")
    }

    object Static {
        val DOC_TYPE = StringLabel("docType")
        val ISSUER_SIGNED = StringLabel("issuerSigned")
        val DEVICE_SIGNED = StringLabel("deviceSigned")
        val ERRORS = StringLabel("errors")

        @JsName("fromDeviceResponse")
        fun fromDeviceResponse(items: CborArray<AnyCborItem>?): Array<DocumentCbor>? {
            if (items == null || items.value.isEmpty()) {
                return null
            }
            return items.value.map { fromCborItem(it as CborMap<StringLabel, AnyCborItem>) }.toTypedArray()

        }

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) =
            DocumentCbor(DOC_TYPE.required(m), IssuerSignedCbor.Static.fromCborItem(ISSUER_SIGNED.required(m)), DEVICE_SIGNED.required(m), ERRORS.optional(m))

        @JsName("cborDecode")
        fun cborDecode(data: ByteArray): DocumentCbor = fromCborItem(cborSerializer.decode(data))
    }
}
