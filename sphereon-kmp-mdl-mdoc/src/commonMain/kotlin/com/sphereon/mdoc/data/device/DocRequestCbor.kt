package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborEncodedItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborTagged
import com.sphereon.cbor.CborView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_bstr
import com.sphereon.cbor.cddl_tstr
import com.sphereon.crypto.cose.COSE_Sign1
import com.sphereon.crypto.cose.CoseSign1Cbor
import com.sphereon.crypto.cose.CoseSign1Json
import com.sphereon.json.JsonView
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.mdoc.data.RequestInfo
import com.sphereon.mdoc.tx.device.ReaderAuthenticationCbor
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName

typealias docRequestBuilder = DocRequestCbor.Builder

/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 *
 */
@JsExport
@Serializable
data class DocRequestJson(
    /**
     * ItemRequestBytes contains the ItemsRequest structure as a tagged (24) CBOR bytestring data item.
     */
    val itemsRequest: DeviceItemsRequestJson,

    /**
     * ReaderAuth is used for mdoc reader authentication as defined in 9.1.4.
     */
    val readerAuth: CoseSign1Json? = null
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DocRequestCbor =
        DocRequestCbor(itemsRequest = itemsRequest.toCbor(), readerAuth = readerAuth?.toCbor() as COSE_Sign1<ReaderAuthenticationCbor>?)

    fun getNameSpaces(): Array<String> = itemsRequest.nameSpaces.map { it.key }.toTypedArray()
    fun getDocType(): String = itemsRequest.docType
    fun getIdentifiers(nameSpace: String) = itemsRequest.getIdentifiers(nameSpace)
    fun limitDisclosures(issuerSigned: IssuerSignedJson): IssuerSignedJson = issuerSigned.limitDisclosures(this)
}

/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 *
 */
@JsExport
data class DocRequestCbor(
    /**
     * ItemRequestBytes contains the ItemsRequest structure as a tagged (24) CBOR bytestring data item.
     */
    val itemsRequest: DeviceItemsRequestCbor,

    /**
     * ReaderAuth is used for mdoc reader authentication as defined in 9.1.4.
     */
    val readerAuth: COSE_Sign1<ReaderAuthenticationCbor>? = null
) : CborView<DocRequestCbor, DocRequestJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {

    fun getNameSpaces(): Array<String> = itemsRequest.nameSpaces.value.map { it.key.value }.toTypedArray()
    fun getDocType(): String = itemsRequest.docType.value

    fun getIdentifiers(nameSpace: String) = itemsRequest.getIdentifiers(nameSpace)

    fun limitDisclosures(issuerSigned: IssuerSignedCbor): IssuerSignedCbor = issuerSigned.limitDisclosures(this)

    object Static {
        val ITEMS_REQUEST = StringLabel("itemsRequest")
        val READER_AUTH = StringLabel("readerAuth")

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DocRequestCbor {
            val cborTagged: CborEncodedItem<cddl_bstr> = ITEMS_REQUEST.required(m)
            val cborMap: CborMap<StringLabel, AnyCborItem> = cborTagged.cborDecode()
            return DocRequestCbor(
                DeviceItemsRequestCbor.Static.fromCborItem(cborMap),
                READER_AUTH.optional<CborArray<AnyCborItem>?>(m)?.let { CoseSign1Cbor.Static.fromCborItem(it) }
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): DocRequestCbor = fromCborItem(Cbor.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<DocRequestCbor> {
        return CborMap.Static.builder(this).putTagged(
            Static.ITEMS_REQUEST,
            CborTagged.Static.ENCODED_CBOR,
            CborByteString(cborSerializer.encode(itemsRequest.toCbor()))
        )
            .put(Static.READER_AUTH, readerAuth?.toCbor(), true)
            .end()
    }

    override fun toJson(): DocRequestJson =
        DocRequestJson(itemsRequest = itemsRequest.toJson(), readerAuth = readerAuth?.toJson())


    data class Builder(
        var deviceItemsRequestBuilder: DeviceItemsRequestCbor.Builder? = null,
        var readerAuth: COSE_Sign1<ReaderAuthenticationCbor>? = null
    ) {

        private fun builder() =
            deviceItemsRequestBuilder ?: throw IllegalArgumentException("A builder is not initialized!")

        init {
            if (this.deviceItemsRequestBuilder == null) {
                this.deviceItemsRequestBuilder = DeviceItemsRequestCbor.Builder(this)
            }
        }

        @JsName("docType")
        fun docType(docType: cddl_tstr, requestInfo: RequestInfo? = null): DeviceItemsRequestCbor.Builder {
            return builder().withDocType(docType).withRequestInfo(requestInfo)

        }

        @JsName("withReaderAuth")
        fun withReaderAuth(readerAuth: COSE_Sign1<ReaderAuthenticationCbor>) =
            apply { this.readerAuth = readerAuth }


        fun build(): DocRequestCbor {
            val itemsRequestDataItem = builder().build()
            return DocRequestCbor(itemsRequestDataItem, readerAuth)
        }

        override fun toString(): String {
            return "Builder(deviceItemsRequestBuilder=$deviceItemsRequestBuilder, readerAuth=$readerAuth)"
        }


    }

}
