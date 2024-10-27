package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.json.JsonView
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.mdoc.data.DocumentErrorsCbor
import com.sphereon.mdoc.data.DocumentErrorsJson
import com.sphereon.mdoc.oid4vp.IOid4VPPresentationDefinition
import com.sphereon.mdoc.oid4vp.Oid4VPPresentationDefinition
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName




@JsExport
@Serializable
data class DocumentJson(
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
    val deviceSigned: DeviceSignedJson? = null,

    /**
     * If the device retrieval mdoc response structure does not include some data element or document
     * requested in the device retrieval mdoc request, an error code may be returned as part of the
     * documentErrors or errors structures.
     * If present, ErrorCode shall contain an error code according to 8.3.2.1.2.3.
     */
    val errors: DocumentErrorsJson? = null
) : JsonView() {
    @Transient
    val MSO = issuerSigned.MSO
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DocumentCbor {
        TODO("Not yet implemented")
    }

    fun fromIssuerSigned(issuerSigned: IssuerSignedJson) = issuerSigned.toDocument()
}


@JsExport
data class DocumentCbor(
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
    val deviceSigned: DeviceSignedCbor?,  // required when presenting. No null default on purpose!

    /**
     * If the device retrieval mdoc response structure does not include some data element or document
     * requested in the device retrieval mdoc request, an error code may be returned as part of the
     * documentErrors or errors structures.
     * If present, ErrorCode shall contain an error code according to 8.3.2.1.2.3.
     */
    val errors: DocumentErrorsCbor? = null
) : CborView<DocumentCbor, DocumentJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {

    val MSO = issuerSigned.MSO
    override fun cborBuilder(): CborBuilder<DocumentCbor> {
        val builder = CborMap.Static.builder(this)
            .put(Static.DOC_TYPE, docType, false)
            .put(Static.ISSUER_SIGNED, issuerSigned.toCbor(), false)
            .put(Static.DEVICE_SIGNED, deviceSigned?.toCbor(), true)
//            .put(Static.ERRORS, errors, true)
        return builder.end()
    }

    override fun toJson(): DocumentJson {
        return DocumentJson(docType = docType.value, issuerSigned = issuerSigned.toJson(), deviceSigned = deviceSigned?.toJson() /*errors = FIXME */)
    }

    fun limitDisclosures(docRequest: DocRequestCbor): IssuerSignedCbor {
        return docRequest.limitDisclosures(issuerSigned)
    }

    fun limitDisclosureFromPresentationDefinition(
        presentationDefinition: IOid4VPPresentationDefinition,
        deviceSigned: DeviceSignedCbor? = null
    ): DocumentCbor {
        val docRequest = Oid4VPPresentationDefinition.Static.fromDTO(presentationDefinition).toDocRequest()
        if (docRequest.itemsRequest.docType !== this.docType) {
            throw IllegalArgumentException("Document request docType ${docRequest.itemsRequest.docType} does not match docType ${this.docType}")
        }
        return DocumentCbor(docType = this.docType, issuerSigned = limitDisclosures(docRequest), deviceSigned = deviceSigned ?: this.deviceSigned)
    }


    fun toSingleDocDeviceResponse(
        presentationDefinition: IOid4VPPresentationDefinition,
    ): DeviceResponseCbor {
        // device signing
        val docRequest = Oid4VPPresentationDefinition.Static.fromDTO(presentationDefinition).toDocRequest()
        val issuerSigned = limitDisclosures(docRequest)
        val mdoc = DocumentCbor(docType = this.docType, issuerSigned = issuerSigned, deviceSigned = deviceSigned)
        return DeviceResponseCbor(documents = arrayOf(mdoc))
    }

    fun getNameSpaces() = issuerSigned.nameSpaces?.value?.map { it.key.value }?.toTypedArray() ?: arrayOf()

    object Static {
        val DOC_TYPE = StringLabel("docType")
        val ISSUER_SIGNED = StringLabel("issuerSigned")
        val DEVICE_SIGNED = StringLabel("deviceSigned")
        val ERRORS = StringLabel("errors")

        fun fromIssuerSigned(issuerSigned: IssuerSignedCbor) = issuerSigned.toDocument()

        @JsName("fromDeviceResponse")
        fun fromDeviceResponse(items: CborArray<AnyCborItem>?): Array<DocumentCbor>? {
            if (items == null || items.value.isEmpty()) {
                return null
            }
            return items.value.map { fromCborItem(it as CborMap<StringLabel, AnyCborItem>) }.toTypedArray()

        }

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>) =
            DocumentCbor(
                DOC_TYPE.required(m),
                IssuerSignedCbor.Static.fromCborItem(ISSUER_SIGNED.required(m)),
                DEVICE_SIGNED.optional(m),
                ERRORS.optional(m)
            )

        @JsName("cborDecode")
        fun cborDecode(data: ByteArray): DocumentCbor = fromCborItem(cborSerializer.decode(data))
    }
}
