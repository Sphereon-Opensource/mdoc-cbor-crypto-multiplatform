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
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_bstr
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.cose.COSE_Sign1
import com.sphereon.cbor.cose.CoseSign1Cbor
import com.sphereon.cbor.cose.StringLabel
import com.sphereon.mdoc.data.RequestInfo
import com.sphereon.mdoc.tx.device.ReaderAuthenticationCbor
import com.sphereon.mdoc.tx.device.ReaderAuthenticationJson
import kotlin.js.JsExport

typealias docRequestBuilder = DocRequestCbor.Builder

/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 *
 */
@JsExport
data class DocRequestJson(
    /**
     * ItemRequestBytes contains the ItemsRequest structure as a tagged (24) CBOR bytestring data item.
     */
    val itemsRequest: DeviceItemsRequestJson,

    /**
     * ReaderAuth is used for mdoc reader authentication as defined in 9.1.4.
     */
    val readerAuth: COSE_Sign1<ReaderAuthenticationJson, ReaderAuthenticationCbor>? = null
) : JsonView<DocRequestCbor>() {
    override fun toCbor(): DocRequestCbor {
        TODO("Not yet implemented")
    }
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
    // FIXME: ReaderAuth generic/type
    val readerAuth: COSE_Sign1<ReaderAuthenticationCbor, ReaderAuthenticationJson>? = null
) : CborView<DocRequestCbor, DocRequestJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {

    companion object {
        val ITEMS_REQUEST = StringLabel("itemsRequest")
        val READER_AUTH = StringLabel("readerAuth")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DocRequestCbor {
            val cborTagged: CborEncodedItem<cddl_bstr> = ITEMS_REQUEST.required(m)
            val cborMap: CborMap<StringLabel, AnyCborItem> = cborTagged.cborDecode()
            return DocRequestCbor(
                DeviceItemsRequestCbor.fromCborItem(cborMap),
                READER_AUTH.optional<CborArray<AnyCborItem>?>(m)?.let { CoseSign1Cbor.fromCborItem(it) }
            )
        }

        fun cborDecode(encoded: ByteArray): DocRequestCbor = fromCborItem(Cbor.decode(encoded))
    }

    override fun cborBuilder(): CborBuilder<DocRequestCbor> {
        return CborMap.builder(this).putTagged(
            ITEMS_REQUEST,
            CborTagged.ENCODED_CBOR,
            CborByteString(cborSerializer.encode(itemsRequest.toCbor()))
        )
            .put(READER_AUTH, readerAuth?.toCbor(), true)
            .end()
    }

    override fun toJson(): DocRequestJson {
        TODO("Not yet implemented")
    }

    data class Builder(
        var deviceItemsRequestBuilder: DeviceItemsRequestCbor.Builder? = null,
        var readerAuth: COSE_Sign1<ReaderAuthenticationCbor, ReaderAuthenticationJson>? = null
    ) {

        private fun builder() =
            deviceItemsRequestBuilder ?: throw IllegalArgumentException("A builder is not initialized!")

        init {
            if (this.deviceItemsRequestBuilder == null) {
                this.deviceItemsRequestBuilder = DeviceItemsRequestCbor.Builder(this)
            }
        }

        fun docType(docType: cddl_tstr, requestInfo: RequestInfo? = null): DeviceItemsRequestCbor.Builder {
            return builder().withDocType(docType).withRequestInfo(requestInfo)

        }

        fun withReaderAuth(readerAuth: COSE_Sign1<ReaderAuthenticationCbor, ReaderAuthenticationJson>) = apply { this.readerAuth = readerAuth }


        fun build(): DocRequestCbor {
            val itemsRequestDataItem = builder().build()
            return DocRequestCbor(itemsRequestDataItem, readerAuth)
        }

        override fun toString(): String {
            return "Builder(deviceItemsRequestBuilder=$deviceItemsRequestBuilder, readerAuth=$readerAuth)"
        }


    }

}
