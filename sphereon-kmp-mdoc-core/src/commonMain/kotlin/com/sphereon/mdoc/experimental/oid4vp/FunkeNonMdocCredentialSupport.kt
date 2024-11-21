package com.sphereon.mdoc.experimental.oid4vp

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.cbor.MapBuilder
import com.sphereon.cbor.StringLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.toStringLabel
import com.sphereon.json.JsonView
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.mdoc.oid4vp.Oid4VPFormat
import com.sphereon.mdoc.oid4vp.Oid4VPFormatIdentifier
import com.sphereon.mdoc.oid4vp.Oid4VPSupportedAlgorithm
import com.sphereon.mdoc.transfer.device.ProtocolInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.js.JsExport
import kotlin.js.JsName


const val OID4VP_PROTOCOL_INFO_LITERAL = "oid4vp"
val OID4VP_PROTOCOL_INFO_LABEL = OID4VP_PROTOCOL_INFO_LITERAL.toStringLabel()

@JsExport
@Serializable
data class CredentialFormatJson(val alg: Array<String>) : JsonView() {
    override fun toCbor(): Any {
        return CredentialFormatCbor(CborArray(alg.map { CborString(it) }.toMutableList()))
    }

    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CredentialFormatJson) return false

        if (!alg.contentEquals(other.alg)) return false

        return true
    }

    override fun hashCode(): Int {
        return alg.contentHashCode()
    }

}

@JsExport
data class CredentialFormatCbor(val alg: CborArray<CborString>) :
    CborView<CredentialFormatCbor, CredentialFormatJson, CborArray<CborString>>(CDDL.list) {
    override fun cborBuilder(): CborBuilder<CredentialFormatCbor> {
        return CborMap.Static.builder(this).put(Static.ALG, alg, false).end()
    }

    override fun toJson(): CredentialFormatJson {
        return CredentialFormatJson(alg.value.map { it.value }.toTypedArray())
    }

    object Static {
        val ALG = StringLabel("alg")
    }
}

/**
 * SPHEREON Funke: Experimental credential format extension
 *
 * This file contains some data structures that are an extension to the ISO 18013-5 and -7 specs. They are not official and highly experimental!
 * Obviously there are other areas of the code base impacted by this extension as well. The code paths/extensions are labeled with the above identifier
 * The implementation is based on https://docs.google.com/document/d/1kRrs1fxufY1wXz-WDLkqy3i7jE7Kd8nY/edit
 *
 * It adds support for OID4VP directly in DeviceEngagement, Device Request and Response, allowing different credential formats like SD-JWT next to mdl/mdocs
 */
@JsExport
@Serializable
data class Oid4vpRequestProtocolJson(val format: MutableMap<String, CredentialFormatJson>) : JsonView() {

    /**
     * Converts the `Oid4vpRequestProtocolJson` instance to its CBOR representation.
     *
     * This method maps each string in the `credentialFormat` array to a `CborString` and encases them in a
     * `CborArray`. The resulting `CborArray` is then used to create a new `Oid4vpRequestProtocolCbor` instance.
     *
     * @return an `Oid4vpRequestProtocolCbor` instance containing the CBOR representation of the `credentialFormat`.
     */
    override fun toCbor() =
        Oid4vpRequestProtocolCbor(format.map { CborString(it.key) to it.value.toCbor() as CredentialFormatCbor }.toMap().toMutableMap())


    /**
     * Converts the object to a JSON string representation.
     * The JSON string format will include the `credentialFormat` property as an array of strings.
     *
     * @return A JSON string representing the object.
     */
    override fun toJsonString(): String {
        return mdocJsonSerializer.encodeToString(this)
    }
}


/**
 * Represents a CBOR encoded OID4VP request protocol.
 *
 * This class facilitates the conversion between CBOR and JSON representations
 * of the OID4VP request protocol, specifically handling the "credentialFormat".
 *
 * @property format Array of CBOR strings representing the credential format.
 */
@JsExport
data class Oid4vpRequestProtocolCbor(val format: MutableMap<CborString, CredentialFormatCbor>) :
    CborView<Oid4vpRequestProtocolCbor, Oid4vpRequestProtocolJson, CborMap<StringLabel, AnyCborItem>>(CDDL.map) {

    fun getFormatIdentifiers() = format.map { Oid4VPFormatIdentifier.Static.fromValue(it.key.value)!! }.toSet().toTypedArray()

    fun hasFormatIdentifier(identifier: Oid4VPFormatIdentifier) = getFormatIdentifiers().contains(identifier)

    fun getSupportedAlgorithms(identifier: Oid4VPFormatIdentifier): Oid4VPSupportedAlgorithm? {
        val algs = format[CborString(identifier.value)]?.alg?.value?.map { it.value } ?: return null
        if (algs.isEmpty()) {
            return null
        }
        return Oid4VPSupportedAlgorithm(algs.toTypedArray())
    }

    fun getFormatsAndAlgorithms(): Map<Oid4VPFormatIdentifier, Oid4VPSupportedAlgorithm> =
        getFormatIdentifiers().associateWith { getSupportedAlgorithms(it)!! }

    fun toOid4vpCredentialFormat(): Oid4VPFormat {
        val algsPerFormat = getFormatIdentifiers()
        val json = Json.encodeToJsonElement(algsPerFormat).jsonObject
        return Json.decodeFromJsonElement(json)

    }

    fun hasCredentialFormat(format: Oid4VPFormat) {

    }

    /**
     * Constructs a CBOR (Concise Binary Object Representation) map builder for the current instance.
     *
     * This method initializes a [CborMap] builder with the current instance and populates it with
     * the `credentialFormat` associated with the `Static.CREDENTIAL_FORMAT` key, without overwriting
     * any existing entries.
     *
     * @return The final [CborMap] after adding the required entries and calling [MapBuilder.end].
     */
    override fun cborBuilder(): CborBuilder<Oid4vpRequestProtocolCbor> {
        val builder = CborMap.Static.builder(this)
        format.forEach { builder.put(it.key, it.value.toCbor(), false) }
        return builder.end()
    }

    /**
     * Converts the CBOR representation to its corresponding JSON representation.
     *
     * @return an instance of [Oid4vpRequestProtocolJson] containing the JSON representation
     *         of the credential format.
     */
    override fun toJson() = Oid4vpRequestProtocolJson(format.map { it.key.value to it.value.toJson() }.toMap().toMutableMap())

    /**
     * Object that holds static definitions for commonly used constants.
     */
    object Static {
        /**
         * Represents the format for credentials used in the OID4VP request protocol.
         *
         * This label is a key used in CborMap to denote the credential format and is utilized in serializing/
         * deserializing between CBOR and JSON representations.
         */
        val CREDENTIAL_FORMAT = StringLabel("credentialFormat")


        fun fromProtocolInfo(protocolInfo: ProtocolInfo): Oid4vpRequestProtocolCbor {
            require(protocolInfo.value is CborMap<*, *>) { "Protocol info needs to be a map if it contains Oid4vpRequest" }
            val protocolInfoMap =
                (protocolInfo.value as CborMap<*, *>).asMap[OID4VP_PROTOCOL_INFO_LABEL]
            checkNotNull(protocolInfoMap) { "No ${OID4VP_PROTOCOL_INFO_LABEL.value} key present in the protocol info" }
            return fromCborItem(protocolInfoMap as CborMap<StringLabel, AnyCborItem>)
        }


        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<StringLabel, AnyCborItem>): Oid4vpRequestProtocolCbor {
            return Oid4vpRequestProtocolCbor(
                CREDENTIAL_FORMAT.required(m)
            )
        }

        @JsName("cborDecode")
        fun cborDecode(encoded: ByteArray): Oid4vpRequestProtocolCbor = fromCborItem(cborSerializer.decode(encoded))
    }
}


class Oid4vpDeviceRequestCbor
