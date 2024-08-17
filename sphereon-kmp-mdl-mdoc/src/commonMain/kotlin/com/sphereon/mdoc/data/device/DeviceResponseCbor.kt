package com.sphereon.mdoc.data.device

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.Cbor
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborInt
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.JsonView
import com.sphereon.cbor.cborViewArrayToCborItem
import com.sphereon.cbor.StringLabel
import com.sphereon.kmp.LongKMP
import com.sphereon.mdoc.data.DeviceResponseDocumentError
import com.sphereon.mdoc.data.DocType
import kotlin.js.JsExport

@JsExport
data class DeviceResponseJson(
    /**
     * version is the version for the DeviceResponse structure. In the current version of this document
     * its value shall be “1.0”.
     *
     */
    val version: String = "1.0",

    /**
     * documents contains an array of all returned documents. documentErrors can contain error codes for
     * documents that are not returned. status contains a status code according to 8.3.2.1.2.3.
     */
    val documents: Array<DocumentJson>?,

    //    fixme
    val documentErrors: Array<DeviceResponseDocumentError>?,

    val status: LongKMP = LongKMP(0)

) : JsonView<DeviceResponseCbor>() {
    override fun toCbor(): DeviceResponseCbor {
        TODO("Not yet implemented")
    }

}


@JsExport
data class DeviceResponseCbor(
    /**
     * version is the version for the DeviceResponse structure. In the current version of this document
     * its value shall be “1.0”.
     *
     */
    val version: CborString = CborString("1.0"),

    /**
     * documents contains an array of all returned documents. documentErrors can contain error codes for
     * documents that are not returned. status contains a status code according to 8.3.2.1.2.3.
     */
    val documents: Array<DocumentCbor>?,

    val documentErrors: Array<DeviceResponseDocumentError>?,

    val status: CborUInt = CborUInt(0)

) : CborView<DeviceResponseCbor, DeviceResponseJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceResponseCbor> {
        return CborMap.builder(this)
            .put(VERSION, version, false)
            .put(
                DOCUMENTS,
                documents?.cborViewArrayToCborItem(),
                true
            )
            .put(
                DOCUMENT_ERRORS,
                if (documentErrors?.isNotEmpty() == true) CborArray(documentErrors.map {
                    CborMap(it.toMutableMap())
                }.toMutableList()) else null,
                true
            )
            .put(STATUS, status, false)
            .end()
    }

    override fun toJson(): DeviceResponseJson {
        TODO("Not yet implemented")
    }

    companion object {
        val VERSION = StringLabel("version")
        val DOCUMENTS = StringLabel("documents")
        val DOCUMENT_ERRORS = StringLabel("documentErrors")
        val STATUS = StringLabel("status")

        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DeviceResponseCbor {
            return DeviceResponseCbor(
                VERSION.required(m),
                DocumentCbor.fromDeviceResponse(DOCUMENTS.optional(m)),
                DOCUMENT_ERRORS.optional<CborArray<CborMap<DocType, CborInt>>>(m)?.value?.map {
                    it.value.toMap()
                }?.toTypedArray(),
                STATUS.required(m)
            )
        }

        fun cborDecode(encoded: ByteArray): DeviceResponseCbor = fromCborItem(Cbor.decode(encoded))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DeviceResponseCbor) return false

        if (version != other.version) return false
        if (documents != null) {
            if (other.documents == null) return false
            if (!documents.contentEquals(other.documents)) return false
        } else if (other.documents != null) return false
        if (documentErrors != null) {
            if (other.documentErrors == null) return false
            if (!documentErrors.contentEquals(other.documentErrors)) return false
        } else if (other.documentErrors != null) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        var result = version.hashCode()
        result = 31 * result + (documents?.contentHashCode() ?: 0)
        result = 31 * result + (documentErrors?.contentHashCode() ?: 0)
        result = 31 * result + status.hashCode()
        return result
    }
}
