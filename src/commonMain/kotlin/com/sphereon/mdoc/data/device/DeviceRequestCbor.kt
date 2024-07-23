package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.cddl_tstr
import com.sphereon.cbor.cose.StringLabel
import kotlin.js.JsExport

/**
 * 8.3.2.1.2.1 Device retrieval mdoc request
 */
@JsExport
data class DeviceRequestJson(
    val version: cddl_tstr,
    val docRequests: MutableList<DocRequestJson>
) : JsonView<DeviceRequestCbor>() {
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
    val docRequests: CborArray<DocRequestCbor>

) : CborView<DeviceRequestCbor, DeviceRequestJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceRequestCbor> =
        CborMap.builder(this).put(VERSION, version, optional = false).put(
            DOC_REQUESTS,
            CborArray(docRequests.value.map { it.toCbor() }.toMutableList()),
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


    companion object {
        val VERSION = StringLabel("version")
        val DOC_REQUESTS = StringLabel("docRequests")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DeviceRequestCbor {
            return DeviceRequestCbor(
                VERSION.required(m),
                CborArray(DOC_REQUESTS.required<CborArray<CborMap<StringLabel, AnyCborItem>>>(m).value.map {
                    DocRequestCbor.fromCborItem(it)
                }.toMutableList())
            )
        }

        fun cborDecode(encoded: ByteArray): DeviceRequestCbor = fromCborItem(cborSerializer.decode(encoded))
    }

    class Builder

}

