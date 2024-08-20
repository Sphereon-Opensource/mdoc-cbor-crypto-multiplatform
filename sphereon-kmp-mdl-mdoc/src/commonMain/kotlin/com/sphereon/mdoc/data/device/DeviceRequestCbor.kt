package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_tstr
import com.sphereon.mdoc.mdocJsonSerializer
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 */
@JsExport
data class DeviceRequestJson(
    val version: cddl_tstr,
    val docRequests: MutableList<DocRequestJson>
) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun toCbor(): DeviceRequestCbor {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceRequestJson) return false

        if (version != other.version) return false
        if (docRequests != other.docRequests) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + docRequests.hashCode()
        return result
    }

    override fun toString(): String {
        return "DeviceRequestSimple(version='$version', docRequests=$docRequests)"
    }


}

/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 */
@JsExport
data class DeviceRequestCbor(
    /**
     * version is the version for the DeviceRequest structure: in the current version of this document its value
     * shall be “1.0”. If
     */
    val version: CborString = CborString("1.0"),

    /**
     * docRequests contains an array of all requested documents.
     */
    val docRequests: Array<DocRequestCbor>

) : CborView<DeviceRequestCbor, DeviceRequestJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceRequestCbor> =
        CborMap.Static.builder(this).put(Static.VERSION, version, optional = false).put(
            Static.DOC_REQUESTS,
            CborArray(docRequests.map { it.toCbor() }.toMutableList()),
            optional = false
        )
            .end()


    override fun toJson(): DeviceRequestJson {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceRequestCbor) return false

        if (version != other.version) return false
        if (docRequests != other.docRequests) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + docRequests.hashCode()
        return result
    }

    override fun toString(): String {
        return "DeviceRequestCbor(version=$version, docRequests=$docRequests)"
    }


    object Static {
        val VERSION = StringLabel("version")
        val DOC_REQUESTS = StringLabel("docRequests")

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DeviceRequestCbor {
            return DeviceRequestCbor(
                VERSION.required(m),
                DOC_REQUESTS.required<CborArray<CborMap<StringLabel, AnyCborItem>>>(m).value.map {
                    DocRequestCbor.Static.fromCborItem(it)
                }.toTypedArray()
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): DeviceRequestCbor = fromCborItem(cborSerializer.decode(encoded))
    }

    class Builder

}

