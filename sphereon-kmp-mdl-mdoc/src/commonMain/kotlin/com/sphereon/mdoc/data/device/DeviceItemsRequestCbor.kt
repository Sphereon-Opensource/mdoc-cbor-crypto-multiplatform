package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborAny
import com.sphereon.cbor.CborBool
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborSimple
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.toCborBool
import com.sphereon.cbor.toCborString
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.mdoc.data.DataElementCbor
import com.sphereon.mdoc.data.DataElementIdentifier
import com.sphereon.mdoc.data.DocType
import com.sphereon.mdoc.data.IntentToRetain
import com.sphereon.mdoc.data.RequestInfo
import com.sphereon.mdoc.data.mdl.Mdl.MDL_NAMESPACE
import com.sphereon.mdoc.data.mdl.Mdl.MDL_NAMESPACE_CBOR
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport

typealias deviceItemsRequestBuilder = DeviceItemsRequestCbor.Builder

@JsExport
@Serializable
data class DeviceItemsRequestJson(
    val docType: cddl_tstr,
    val nameSpaces: MutableMap<String, MutableMap<String, Boolean>> = mutableMapOf(),
    @Contextual
    //fixme: The map value should be Any
    val requestInfo: MutableMap<String, String>? = null
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)

    override fun toCbor(): DeviceItemsRequestCbor {
        return DeviceItemsRequestCbor(docType.toCborString(), CborMap(mutableMapOf(* nameSpaces.map {
            Pair(it.key.toCborString(), CborMap(
                mutableMapOf(* it.value.map { elt -> Pair(elt.key.toCborString(), elt.value.toCborBool()) }
                    .toTypedArray())
            ))
        }.toTypedArray())), requestInfo?.let {
            CborMap(mutableMapOf(* it.map { entry -> Pair(entry.key.toCborString(), CborAny(entry.value)) }
                .toTypedArray()))
        })
    }

    fun getIdentifiers(nameSpace: String): Map<String, Boolean> {
        return nameSpaces.get(nameSpace)?.toMap() ?: emptyMap()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceItemsRequestJson) return false

        if (docType != other.docType) return false
        if (nameSpaces != other.nameSpaces) return false
        if (requestInfo != other.requestInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = docType.hashCode()
        result = 31 * result + nameSpaces.hashCode()
        result = 31 * result + (requestInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "DeviceItemsJson(docType='$docType', nameSpaces=$nameSpaces, requestInfo=$requestInfo)"
    }


}


@JsExport
data class DeviceItemsRequestCbor(
    /**
     * docType is the requested document type
     */
    val docType: CborString,

    /**
     * NameSpaces contains the requested data elements and the namespace they belong to.
     */
    val nameSpaces: CborMap<CborString, CborMap<DataElementIdentifier, IntentToRetain>>,

    /**
     * requestInfo may be used by the mdoc reader to provide additional information. This document does
     * not define any key-value pairs for use in requestInfo. An IA infrastructure shall ignore any key-value
     * pairs that it is not able to interpret.
     */
    val requestInfo: CborMap<CborString, AnyCborItem>? = null

) : CborView<DeviceItemsRequestCbor, DeviceItemsRequestJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    object Static {
        val DOC_TYPE = StringLabel("docType")
        val NAME_SPACES = StringLabel("nameSpaces")
        val REQUEST_INFO = StringLabel("requestInfo")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DeviceItemsRequestCbor {
            return DeviceItemsRequestCbor(DOC_TYPE.required(m), NAME_SPACES.required(m), REQUEST_INFO.optional(m))
        }

        fun decodeCbor(encoded: ByteArray): DeviceItemsRequestCbor = fromCborItem(Cbor.decode(encoded))

    }

    override fun cborBuilder(): CborBuilder<DeviceItemsRequestCbor> {
        val builder = CborMap.Static.builder(this)
            .put(Static.DOC_TYPE, docType)
            .put(Static.NAME_SPACES, nameSpaces)
            .put(Static.REQUEST_INFO, requestInfo, true)

        return builder.end()
    }

    override fun toJson(): DeviceItemsRequestJson {
        return DeviceItemsRequestJson(docType.value, mutableMapOf(* nameSpaces.value.map {
            Pair(it.key.value,
                mutableMapOf(* it.value.value.map { elt -> Pair(elt.key.value, elt.value.value) }
                    .toTypedArray())
            )

        }.toTypedArray()))
    }

    fun getIdentifiers(nameSpace: String): Map<String, Boolean> {
        return nameSpaces.get<Map<String, Boolean>?>(nameSpace.toCborString())?.map { Pair(it.key, it.value) }?.toMap() ?: emptyMap()
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceItemsRequestCbor) return false

        if (docType != other.docType) return false
        if (nameSpaces != other.nameSpaces) return false
        if (requestInfo != other.requestInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = docType.hashCode()
        result = 31 * result + nameSpaces.hashCode()
        result = 31 * result + (requestInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "DeviceItemsRequestCbor(docType=$docType, nameSpaces=$nameSpaces, requestInfo=$requestInfo)"
    }


    data class Builder(
        var docRequestBuilder: DocRequestCbor.Builder? = null,
        private var docType: DocType? = MDL_NAMESPACE.toCborString(),
        private var requestInfo: RequestInfo? = null,
    ) {

        private val nameSpaceBuilders: MutableMap<String, DeviceRequestNameSpace.Builder> = mutableMapOf()

        init {
            if (docRequestBuilder == null) {
                this.docRequestBuilder = docRequestBuilder(this)
            }
        }


        fun withDocType(docType: cddl_tstr) = apply { this.docType = docType.toCborString() }
        fun withRequestInfo(requestInfo: RequestInfo?) = apply { this.requestInfo = requestInfo }

        fun nameSpace(nameSpace: cddl_tstr): DeviceRequestNameSpace.Builder {
            val builder = DeviceRequestNameSpace.Builder(this, nameSpace.toCborString())
            addUsingBuilder(builder)
            return builder
        }

        fun addUsingElements(nameSpace: cddl_tstr, vararg elements: DataElementCbor) = apply {
            getOrPut(nameSpace).addElements(*elements)
        }

        fun add(nameSpace: cddl_tstr, identifier: cddl_tstr, intentToRetain: Boolean = false) =
            apply {
                getOrPut(nameSpace).add(identifier, intentToRetain)
            }

        fun addUsingBuilder(vararg builders: DeviceRequestNameSpace.Builder) = apply {
            builders.forEach { this.nameSpaceBuilders[it.nameSpace.value] = it }
        }


        private fun getOrPut(nameSpace: String): DeviceRequestNameSpace.Builder {
            return nameSpaceBuilders.getOrPut(
                nameSpace
            ) { DeviceRequestNameSpace.Builder(this, nameSpace.toCborString()) }
        }


        fun build(): DeviceItemsRequestCbor {
            if (docType == null) {
                throw IllegalArgumentException("DocType cannot be null")
            }
            val nameSpacePairs = nameSpaceBuilders.map {
                Pair<CborString, CborMap<CborString, CborBool>>(
                    it.key.toCborString(),
                    CborMap(it.value.dataElements)
                )
            }
            return DeviceItemsRequestCbor(docType!!, CborMap(mutableMapOf(*nameSpacePairs.toTypedArray())))
        }


        fun buildDocRequest(): DocRequestCbor {
            return docRequestBuilder?.build()
                ?: throw IllegalArgumentException("Cannot build document as builder is null")
        }

        override fun toString(): String {
            return "Builder(docRequestBuilder=$docRequestBuilder, docType=$docType, requestInfo=$requestInfo, nameSpaceBuilders=$nameSpaceBuilders)"
        }


    }


}

@JsExport
data class DeviceRequestNameSpace(val nameSpace: CborString, val dataElements: MutableMap<CborString, CborBool>) {


    class Builder(
        val itemsRequestBuilder: DeviceItemsRequestCbor.Builder? = null,
        val nameSpace: CborString = MDL_NAMESPACE_CBOR,
        val dataElements: MutableMap<CborString, CborBool> = mutableMapOf()
    ) {

        fun add(identifier: cddl_tstr, intentToRetain: Boolean = false) = apply {
            dataElements[CborString(identifier)] = if (intentToRetain) CborSimple.Static.TRUE else CborSimple.Static.FALSE
        }

        fun addElements(vararg elements: DataElementCbor) = apply {
            elements.map { add(it.identifier.value, it.intentToRetain.value) }
        }

        fun end(): DeviceItemsRequestCbor.Builder {
            if (itemsRequestBuilder == null) {
                throw IllegalArgumentException("NameSpaces Builder cannot be null when calling end")
            }
            return itemsRequestBuilder
        }

        fun build() = DeviceRequestNameSpace(nameSpace, dataElements)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceRequestNameSpace) return false

        if (nameSpace != other.nameSpace) return false
        if (dataElements != other.dataElements) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nameSpace.hashCode()
        result = 31 * result + dataElements.hashCode()
        return result
    }

    override fun toString(): String {
        return "DeviceRequestNameSpace(nameSpace=$nameSpace, dataElements=$dataElements)"
    }
}
