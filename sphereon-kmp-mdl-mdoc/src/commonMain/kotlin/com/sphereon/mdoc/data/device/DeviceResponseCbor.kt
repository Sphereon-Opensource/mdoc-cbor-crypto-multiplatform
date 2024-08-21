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
import com.sphereon.json.JsonView
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborViewArrayToCborItem
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.kmp.LongKMP
import com.sphereon.mdoc.data.DeviceResponseDocumentErrorCbor
import com.sphereon.mdoc.data.DeviceResponseDocumentErrorJson
import com.sphereon.mdoc.data.DocType
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport
import kotlin.js.JsName

@JsExport
@Serializable
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
    val documentErrors: Array<DeviceResponseDocumentErrorJson>?,

    val status: LongKMP = LongKMP(0)

) : JsonView() {
    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
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

    val documentErrors: Array<DeviceResponseDocumentErrorCbor>?,

    val status: CborUInt = CborUInt(0)

) : CborView<DeviceResponseCbor, DeviceResponseJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {
    override fun cborBuilder(): CborBuilder<DeviceResponseCbor> {
        return CborMap.Static.builder(this)
            .put(Static.VERSION, version, false)
            .put(
                Static.DOCUMENTS,
                documents?.cborViewArrayToCborItem(),
                true
            )
            .put(
                Static.DOCUMENT_ERRORS,
                if (documentErrors?.isNotEmpty() == true) CborArray(documentErrors.map {
                    CborMap(it.toMutableMap())
                }.toMutableList()) else null,
                true
            )
            .put(Static.STATUS, status, false)
            .end()
    }

    override fun toJson(): DeviceResponseJson {
        TODO("Not yet implemented")
    }

    object Static {
        val VERSION = StringLabel("version")
        val DOCUMENTS = StringLabel("documents")
        val DOCUMENT_ERRORS = StringLabel("documentErrors")
        val STATUS = StringLabel("status")

        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): DeviceResponseCbor {
            return DeviceResponseCbor(
                VERSION.required(m),
                DocumentCbor.Static.fromDeviceResponse(DOCUMENTS.optional(m)),
                DOCUMENT_ERRORS.optional<CborArray<CborMap<DocType, CborInt>>>(m)?.value?.map {
                    it.value.toMap()
                }?.toTypedArray(),
                STATUS.required(m)
            )
        }

        @JsName("cborDecode")
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
