@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import assertedPathEntry
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.json.HasToJsonString
import com.sphereon.json.oid4vpJsonSerializer
import com.sphereon.json.toJsonDTO
import com.sphereon.kmp.Uuid
import com.sphereon.mdoc.data.device.DeviceItemsRequestCbor
import com.sphereon.mdoc.data.device.DocRequestCbor
import com.sphereon.mdoc.data.device.DocRequestJson
import com.sphereon.mdoc.data.device.IssuerSignedItemCbor
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import com.sphereon.mdoc.data.mdl.DataElementDef
import com.sphereon.mdoc.oid4vp.Oid4VPFormatIdentifier.entries
import kotlinx.serialization.EncodeDefault
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

    fun toJsonObject(): JsonObject = oid4vpJsonSerializer.parseToJsonElement(oid4vpJsonSerializer.encodeToString(this)).jsonObject
    fun toDTO() = toJsonDTO<IOid4VPPresentationDefinition>(this)


    fun toDocRequestJson(): DocRequestJson = toDocRequest().toJson()


    fun toSerializedJson() = oid4vpJsonSerializer.encodeToString(this)

    object Static {
        fun fromDTO(presentationDefinition: IOid4VPPresentationDefinition) =
            with(presentationDefinition) {
                Oid4VPPresentationDefinition(
                    id,
                    input_descriptors = input_descriptors.map { Oid4VPInputDescriptor.Static.fromDTO(it) }.toTypedArray()
                )
            }
    }

    override fun toJsonString() = toSerializedJson()

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

        fun fromJsonObject(jsonObject: JsonObject): Oid4VPInputDescriptor =
            with(jsonObject) {
                Oid4VPInputDescriptor(
                    id = get("id")!!.jsonPrimitive.content,
                    format = TODO(),
                    constraints = TODO()
                )
            }
    }
}

expect sealed interface IOid4VPFormat {
    @SerialName("mso_mdoc")
    val mso_mdoc: IOid4VPSupportedAlgorithm?

    // SPHEREON Funke: Experimental credential format extension
    @SerialName("vc+sd-jwt")
    val vc_sd_jwt: IOid4VPSupportedAlgorithm?
}

@Serializable
@JsExport
data class Oid4VPFormat(
    @SerialName("mso_mdoc") override val mso_mdoc: Oid4VPSupportedAlgorithm? = null,
    // SPHEREON Funke: Experimental credential format extension

    @EncodeDefault(EncodeDefault.Mode.NEVER)
    @SerialName("vc+sd-jwt") override val vc_sd_jwt: Oid4VPSupportedAlgorithm? = null
) : IOid4VPFormat {
    init {
        if (mso_mdoc != null && vc_sd_jwt != null) {
            throw IllegalArgumentException("requires that either mso_mdoc or vc+sd_jwt is present, but both are present")
        } else if (mso_mdoc == null && vc_sd_jwt == null) {
            throw IllegalArgumentException("requires that either mso_mdoc or vc+sd_jwt is present, but both are absent")
        } else if (mso_mdoc != null && mso_mdoc.algorithmObjects.isEmpty()) {
            throw IllegalArgumentException("ISO 18015-7 requires that mso_mdoc contains at least one algorithm")
        } else if (vc_sd_jwt != null && vc_sd_jwt.algorithmObjects.isEmpty()) {
            throw IllegalArgumentException("requires that vc+sd_jwt contains at least one algorithm")
        }
    }

    fun validateAlgorithms(): Boolean {
        if (mso_mdoc != null) {
            return mso_mdoc.algorithmObjects.isNotEmpty()
        } else if (vc_sd_jwt != null) {
            return vc_sd_jwt.algorithmObjects.isNotEmpty()
        }
        return false
    }

    fun hasFormat(format: Oid4VPFormatIdentifier) = Json.encodeToString(this).contains(format.value)


    object Static {
        fun fromDTO(dto: IOid4VPFormat) =
            with(dto) {
                Oid4VPFormat(
                    mso_mdoc = mso_mdoc?.let { Oid4VPSupportedAlgorithm.Static.fromDTO(it) },
                    // SPHEREON Funke: Experimental credential format extension
                    vc_sd_jwt = vc_sd_jwt?.let { Oid4VPSupportedAlgorithm.Static.fromDTO(it) }
                )
            }
    }
}


expect sealed interface IOid4VPSupportedAlgorithm {
    val alg: Array<String>
}

@Serializable
@JsExport
data class Oid4VPSupportedAlgorithm(
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

    @SerialName("fields")
    val fields: Array<out IOid4VPConstraintField>
}

@Serializable
@JsExport
data class Oid4VPConstraints(
    @SerialName("fields")
    override val fields: Array<Oid4VPConstraintField>,

    @SerialName("limit_disclosure")
    @EncodeDefault(EncodeDefault.Mode.ALWAYS)
    override val limit_disclosure: String = "required"
) : IOid4VPConstraints {
    init {
        if (limit_disclosure != "required") {
            throw IllegalArgumentException("Limit disclosure must have the value 'required' according to ISO 18013-7")
        }
    }

    object Static {
        fun fromDTO(constraints: IOid4VPConstraints) = with(constraints) {
            Oid4VPConstraints(
                fields = fields.map { Oid4VPConstraintField.Static.fromDTO(it) }.toTypedArray(),
                limit_disclosure = "required"
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
enum class Oid4VPFormatIdentifier(val value: String) {
    @JsName("MSO_MDOC")
    @SerialName("mso_mdoc")
    MSO_MDOC("mso_mdoc"),

    // SPHEREON Funke: Experimental credential format extension
    @JsName("SD_JWT_VC")
    @SerialName("vc+sd-jwt")
    SD_JWT_VC("vc+sd-jwt");

    object Static {
        fun fromValue(value: String) = entries.find { value == it.value }
    }
}

object Oid4VPFormatsSerializer : KSerializer<Oid4VPFormatIdentifier> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Oid4VPFormats", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Oid4VPFormatIdentifier) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): Oid4VPFormatIdentifier {
        val value = decoder.decodeString()
        return Oid4VPFormatIdentifier.Static.fromValue(value) ?: throw IllegalArgumentException("Invalid value for format ${value}")
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
    override val format: String,

    @SerialName("path")
    override val path: String
) : IOid4vpSubmissionDescriptor {


    object Static {
        fun fromInputDescriptor(descriptor: IOid4VPInputDescriptor): Oid4vpSubmissionDescriptor =
            with(descriptor) {
                val formatId = if (format.vc_sd_jwt?.alg?.isNotEmpty() == true) Oid4VPFormatIdentifier.SD_JWT_VC else Oid4VPFormatIdentifier.MSO_MDOC
                val path = if (formatId == Oid4VPFormatIdentifier.MSO_MDOC) "$" else descriptor.constraints.fields[0].path[0] // fixme
                Oid4vpSubmissionDescriptor(
                    id = id,
                    format = formatId.value,
                    path = path
                )
            }

        fun fromDTO(dto: IOid4vpSubmissionDescriptor) = with(dto) { Oid4vpSubmissionDescriptor(id = id, format = format, path = path) }
    }
}
