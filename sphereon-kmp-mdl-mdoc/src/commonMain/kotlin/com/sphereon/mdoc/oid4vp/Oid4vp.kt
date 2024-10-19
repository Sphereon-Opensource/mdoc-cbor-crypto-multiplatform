@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import assertedPathEntry
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.json.HasToJsonString
import com.sphereon.json.mdocJsonSerializer
import com.sphereon.json.toJsonDTO
import com.sphereon.kmp.Uuid
import com.sphereon.mdoc.data.device.DeviceItemsRequestCbor
import com.sphereon.mdoc.data.device.DocRequestCbor
import com.sphereon.mdoc.data.device.DocRequestJson
import com.sphereon.mdoc.data.device.IssuerSignedItemCbor
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import com.sphereon.mdoc.data.mdl.DataElementDef
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlin.js.JsExport
import kotlin.js.JsName


expect sealed interface IOid4VPPresentationDefinition {
    val id: String

    @SerialName("input_descriptors")
    val input_descriptors: Array<out IOid4VPInputDescriptor>
}

@Serializable
@JsExport
data class Oid4VPPresentationDefinition(
    @SerialName("id")
    override val id: String,
    @SerialName("input_descriptors")
    override val input_descriptors: Array<Oid4VPInputDescriptor>
) : IOid4VPPresentationDefinition, HasToJsonString {

    fun toDocRequest(): DocRequestCbor {
        val itemsBuilder = DeviceItemsRequestCbor.Builder()
        val docRequestBuilder = DocRequestCbor.Builder(deviceItemsRequestBuilder = itemsBuilder)
        input_descriptors.forEach { it.toDeviceItemsRequest(itemsBuilder) }
        return docRequestBuilder.build()
    }

    fun toJsonObject(): JsonObject = mdocJsonSerializer.parseToJsonElement(mdocJsonSerializer.encodeToString(this)).jsonObject
    fun toDTO() = toJsonDTO<IOid4VPPresentationDefinition>(this)


    fun toDocRequestJson(): DocRequestJson = toDocRequest().toJson()


    object Static {
        fun fromDTO(presentationDefinition: IOid4VPPresentationDefinition) =
            with(presentationDefinition) {
                Oid4VPPresentationDefinition(
                    id,
                    input_descriptors = input_descriptors.map { Oid4VPInputDescriptor.Static.fromDTO(it) }.toTypedArray()
                )
            }
    }

    override fun toJsonString() = mdocJsonSerializer.encodeToString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Oid4VPPresentationDefinition) return false

        if (id != other.id) return false
        if (!input_descriptors.contentEquals(other.input_descriptors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + input_descriptors.contentHashCode()
        return result
    }
}


expect sealed interface IOid4VPInputDescriptor {
    val id: String
    val format: IOid4VPFormat
    val constraints: IOid4VPConstraints
}

@Serializable
@JsExport
data class Oid4VPInputDescriptor(
    @SerialName("id")
    override val id: String,
    @SerialName("format")
    override val format: Oid4VPFormat,
    @SerialName("constraints")
    override val constraints: Oid4VPConstraints
) : IOid4VPInputDescriptor {

    fun toDeviceItemsRequest(builder: DeviceItemsRequestCbor.Builder) {
        builder.withDocType(id) // For ISO 18015-7 the input descriptor id is the doc type
        constraints.fields.forEach {
            it.path.forEach { path ->
                run {
                    val (nameSpace, identifier) = assertedPathEntry(path)
                    builder.add(nameSpace, identifier, it.intent_to_retain)
                }
            }
        }
    }


    object Static {
        fun fromDTO(inputDescriptor: IOid4VPInputDescriptor): Oid4VPInputDescriptor =
            with(inputDescriptor) {
                Oid4VPInputDescriptor(
                    id = id,
                    format = Oid4VPFormat.Static.fromDTO(format),
                    constraints = Oid4VPConstraints.Static.fromDTO(constraints)
                )
            }
    }
}

expect sealed interface IOid4VPFormat {
    @SerialName("mso_mdoc")
    val mso_mdoc: IOid4VPSupportedAlgorithm?
}

@Serializable
@JsExport
data class Oid4VPFormat(
    @SerialName("mso_mdoc") override val mso_mdoc: Oid4VPSupportedAlgorithm
) : IOid4VPFormat {
    object Static {
        fun fromDTO(dto: IOid4VPFormat) =
            with(dto) { Oid4VPFormat(mso_mdoc = Oid4VPSupportedAlgorithm.Static.fromDTO(mso_mdoc!!)) }
    }
}


expect sealed interface IOid4VPSupportedAlgorithm {
    val alg: Array<String>
}

@Serializable
@JsExport
data class Oid4VPSupportedAlgorithm(
    @SerialName("alg")
    override val alg: Array<String>
) : IOid4VPSupportedAlgorithm {

    @Transient
    val algorithmObjects = alg.map { a -> CoseAlgorithm.Static.fromName(a) }.toTypedArray()

    object Static {
        fun fromDTO(dto: IOid4VPSupportedAlgorithm) =
            with(dto) { Oid4VPSupportedAlgorithm(alg = alg) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Oid4VPSupportedAlgorithm) return false

        if (!alg.contentEquals(other.alg)) return false

        return true
    }

    override fun hashCode(): Int {
        return alg.contentHashCode()
    }
}


expect sealed interface IOid4VPConstraints {
    @SerialName("limit_disclosure")
    val limit_disclosure: String
    val fields: Array<out IOid4VPConstraintField>
}

@Serializable
@JsExport
data class Oid4VPConstraints(
    @SerialName("fields")
    override val fields: Array<Oid4VPConstraintField>
) : IOid4VPConstraints {
    @SerialName("limit_disclosure")
    override val limit_disclosure: String = "required"

    init {
        if (limit_disclosure != "required") {
            throw IllegalArgumentException("Limit disclosure must have the value 'required' according to ISO 18013-7")
        }
    }

    object Static {
        fun fromDTO(constraints: IOid4VPConstraints) = with(constraints) {
            Oid4VPConstraints(
                fields = fields.map { Oid4VPConstraintField.Static.fromDTO(it) }.toTypedArray()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Oid4VPConstraints) return false

        if (limit_disclosure != other.limit_disclosure) return false
        if (!fields.contentEquals(other.fields)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = limit_disclosure.hashCode()
        result = 31 * result + fields.contentHashCode()
        return result
    }
}

expect sealed interface IOid4VPConstraintField {
    @SerialName("path")
    val path: Array<String>

    @SerialName("intent_to_retain")
    val intent_to_retain: Boolean
}

@JsExport
@Serializable
data class Oid4VPConstraintField(
    @SerialName("path")
    override val path: Array<String> = arrayOf(),

    @SerialName("intent_to_retain")
    override val intent_to_retain: Boolean
) : IOid4VPConstraintField {
    init {
        this.assertValidPath()
    }

    object Static {
        @JsName("fromElementIdentifiers")
        fun fromElementIdentifiers(nameSpace: String, elementIdentifiers: Array<String>, intentToRetain: Boolean): Oid4VPConstraintField {
            return Oid4VPConstraintField(intent_to_retain = intentToRetain, path = elementIdentifiers.map { "$['$nameSpace']['$it']" }.toTypedArray())
        }

        @JsName("fromIssuerSignedItemJson")
        fun fromIssuerSignedItemJson(nameSpace: String, issuerSignedItemJson: IssuerSignedItemJson, intentToRetain: Boolean): Oid4VPConstraintField {
            return Oid4VPConstraintField(intent_to_retain = intentToRetain, path = arrayOf("$['$nameSpace']['${issuerSignedItemJson.key}']"))
        }

        @JsName("fromIssuerSignedItemCbor")
        fun fromIssuerSignedItemCbor(
            nameSpace: String,
            issuerSignedItemCbor: IssuerSignedItemCbor<*>,
            intentToRetain: Boolean
        ): Oid4VPConstraintField {
            return Oid4VPConstraintField(
                intent_to_retain = intentToRetain,
                path = arrayOf("$['$nameSpace']['${issuerSignedItemCbor.elementIdentifier.value}']")
            )
        }

        @JsName("fromDTO")
        fun fromDTO(dto: IOid4VPConstraintField) = with(dto) { Oid4VPConstraintField(intent_to_retain = intent_to_retain, path = path) }


        @JsName("fromDataElementDef")
        fun fromDataElementDef(dataElementDef: DataElementDef, intentToRetain: Boolean): Oid4VPConstraintField {
            return Oid4VPConstraintField(
                intent_to_retain = intentToRetain,
                path = arrayOf("$['${dataElementDef.nameSpace}']['${dataElementDef.identifier}']")
            )
        }
    }

    private fun assertValidPath() {
        if (this.path.isEmpty()) {
            throw IllegalStateException("OID4VP constraint field path cannot be empty")
        }
        path.forEach { assertValidPathEntry(it) }
    }

    private fun assertValidPathEntry(pathEntry: String) {
        // We do nothing with the result, as it will throw an exception anyway if invalid
        assertedPathEntry(pathEntry)
    }

}
/*

@JsExport
@Serializable(with = Oid4VPLimitDisclosureSerializer::class)
enum class Oid4VPLimitDisclosure(val value: String) {
    REQUIRED("required");

    object Static {
        fun fromValue(value: String) = Oid4VPLimitDisclosure.entries.find { value == it.value }
    }
}

internal object Oid4VPLimitDisclosureSerializer : KSerializer<Oid4VPLimitDisclosure> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Oid4VPLimitDisclosure", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Oid4VPLimitDisclosure) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): Oid4VPLimitDisclosure {
        val value = decoder.decodeString()
        return Oid4VPLimitDisclosure.Static.fromValue(value) ?: throw IllegalArgumentException("Invalid value for limit disclosure ${value}")
    }
}
*/


@JsExport
@Serializable(with = Oid4VPFormatsSerializer::class)
enum class Oid4VPFormats(val value: String) {
    MSO_MDOC("mso_mdoc");

    object Static {
        fun fromValue(value: String) = Oid4VPFormats.entries.find { value == it.value }
    }
}

object Oid4VPFormatsSerializer : KSerializer<Oid4VPFormats> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Oid4VPFormats", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Oid4VPFormats) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): Oid4VPFormats {
        val value = decoder.decodeString()
        return Oid4VPFormats.Static.fromValue(value) ?: throw IllegalArgumentException("Invalid value for format ${value}")
    }
}

expect sealed interface IOid4VPPresentationSubmission {
    @SerialName("definition_id")
    val definition_id: String
    val id: String

    @SerialName("descriptor_map")
    val descriptor_map: Array<out IOid4vpSubmissionDescriptor>

}

@Serializable
@JsExport
data class Oid4VPPresentationSubmission(
    @SerialName("definition_id")
    override val definition_id: String,
    @SerialName("id")
    override val id: String,
    @SerialName("descriptor_map")
    override val descriptor_map: Array<Oid4vpSubmissionDescriptor>
) : IOid4VPPresentationSubmission {

    fun assertValid(pd: IOid4VPPresentationDefinition) {
        val definition = Oid4VPPresentationDefinition.Static.fromDTO(pd)
        if (definition.id != definition_id) {
            throw IllegalArgumentException("Definition id ${definition.id} is different from definition_id in presentation submission: ${definition_id}")
        }
        definition.input_descriptors.forEach { inputDescriptor ->
            if (descriptor_map.find { mapItem -> mapItem.id === inputDescriptor.id } === null) {
                throw IllegalArgumentException("Presentation definition input descriptor id ${inputDescriptor.id} was not present in presentation submission")
            }
        }
    }

    object Static {
        fun fromPresentationDefinition(pd: IOid4VPPresentationDefinition, id: String = Uuid.v4String()): Oid4VPPresentationSubmission =
            Oid4VPPresentationSubmission(
                definition_id = pd.id,
                id = id,
                descriptor_map = pd.input_descriptors.map { Oid4vpSubmissionDescriptor.Static.fromInputDescriptor(it) }.toTypedArray()
            )

        fun fromDTO(dto: IOid4VPPresentationSubmission) = with(dto) {
            Oid4VPPresentationSubmission(
                definition_id = definition_id,
                id = id,
                descriptor_map = descriptor_map.map { Oid4vpSubmissionDescriptor.Static.fromDTO(it) }.toTypedArray()
            )
        }
    }
}

expect sealed interface IOid4vpSubmissionDescriptor {
    val id: String
    val format: String
    val path: String
}

@Serializable
@JsExport
data class Oid4vpSubmissionDescriptor(
    @SerialName("id")
    override val id: String,
    @SerialName("format")
    override val format: String = Oid4VPFormats.MSO_MDOC.value,
    @SerialName("path")
    override val path: String = "$"
) : IOid4vpSubmissionDescriptor {
    init {
        if (format != Oid4VPFormats.MSO_MDOC.value) { throw IllegalArgumentException("Value of format should be mso_mdoc") }
    }
    object Static {
        fun fromInputDescriptor(descriptor: IOid4VPInputDescriptor): Oid4vpSubmissionDescriptor =
            with(descriptor) { Oid4vpSubmissionDescriptor(id = id) }

        fun fromDTO(dto: IOid4vpSubmissionDescriptor) = with(dto) { Oid4vpSubmissionDescriptor(id = id, format = format, path = path) }
    }
}
