@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.mdoc.data.device.IssuerSignedItemCbor
import com.sphereon.mdoc.data.device.IssuerSignedItemJson
import com.sphereon.mdoc.data.mdl.DataElementDef
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport
import kotlin.js.JsName


expect sealed interface IOid4VPPresentationDefinition {
    val id: String

    @SerialName("input_descriptors")
    val inputDescriptors: Array<out IOid4VPInputDescriptor>
}

@Serializable
@JsExport
data class Oid4VPPresentationDefinition(
    @SerialName("id")
    override val id: String,
    @SerialName("input_descriptors")
    override val inputDescriptors: Array<Oid4VPInputDescriptor>
) :
    IOid4VPPresentationDefinition {
    object Static {
        fun fromDTO(presentationDefinition: IOid4VPPresentationDefinition) =
            with(presentationDefinition) {
                Oid4VPPresentationDefinition(id, inputDescriptors = inputDescriptors.map { Oid4VPInputDescriptor.Static.fromDTO(it) }.toTypedArray())
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Oid4VPPresentationDefinition) return false

        if (id != other.id) return false
        if (!inputDescriptors.contentEquals(other.inputDescriptors)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + inputDescriptors.contentHashCode()
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
    val msoMdoc: IOid4VPSupportedAlgorithm
}

@Serializable
@JsExport
data class Oid4VPFormat(
    @SerialName("mso_mdoc")
    override val msoMdoc: Oid4VPSupportedAlgorithm
) : IOid4VPFormat {
    object Static {
        fun fromDTO(dto: IOid4VPFormat) =
            with(dto) { Oid4VPFormat(msoMdoc = Oid4VPSupportedAlgorithm.Static.fromDTO(msoMdoc)) }
    }
}


expect sealed interface IOid4VPSupportedAlgorithm {
    val alg: Array<CoseAlgorithm>
}

@Serializable
@JsExport
data class Oid4VPSupportedAlgorithm(
    @SerialName("alg")
    override val alg: Array<CoseAlgorithm>
) : IOid4VPSupportedAlgorithm {
    object Static {
        fun fromDTO(dto: IOid4VPSupportedAlgorithm) = with(dto) { Oid4VPSupportedAlgorithm(alg = alg) }
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
    val limitDisclosure: Oid4VPLimitDisclosure
    val fields: Array<out IOid4VPConstraintField>
}

@Serializable
@JsExport
data class Oid4VPConstraints(
    @SerialName("limit_disclosure")
    override val limitDisclosure: Oid4VPLimitDisclosure = Oid4VPLimitDisclosure.REQUIRED,
    @SerialName("fields")
    override val fields: Array<Oid4VPConstraintField>
) : IOid4VPConstraints {
    object Static {
        fun fromDTO(constraints: IOid4VPConstraints) = with(constraints) {
            Oid4VPConstraints(
                limitDisclosure = limitDisclosure,
                fields = fields.map { Oid4VPConstraintField.Static.fromDTO(it) }.toTypedArray()
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Oid4VPConstraints) return false

        if (limitDisclosure != other.limitDisclosure) return false
        if (!fields.contentEquals(other.fields)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = limitDisclosure.hashCode()
        result = 31 * result + fields.contentHashCode()
        return result
    }
}

expect sealed interface IOid4VPConstraintField {
    @SerialName("path")
    val path: Array<String>

    @SerialName("intent_to_retain")
    val intentToRetain: Boolean
}

@JsExport
@Serializable
data class Oid4VPConstraintField(
    @SerialName("path")
    override val path: Array<String> = arrayOf(),

    @SerialName("intent_to_retain")
    override val intentToRetain: Boolean
) : IOid4VPConstraintField {
    init {
        this.assertValidPath()
    }

    object Static {
        @JsName("fromElementIdentifiers")
        fun fromElementIdentifiers(nameSpace: String, elementIdentifiers: Array<String>, intentToRetain: Boolean): Oid4VPConstraintField {
            return Oid4VPConstraintField(intentToRetain = intentToRetain, path = elementIdentifiers.map { "$['$nameSpace']['$it']" }.toTypedArray())
        }

        @JsName("fromIssuerSignedItemJson")
        fun fromIssuerSignedItemJson(nameSpace: String, issuerSignedItemJson: IssuerSignedItemJson, intentToRetain: Boolean): Oid4VPConstraintField {
            return Oid4VPConstraintField(intentToRetain = intentToRetain, path = arrayOf("$['$nameSpace']['${issuerSignedItemJson.key}']"))
        }

        @JsName("fromIssuerSignedItemCbor")
        fun fromIssuerSignedItemCbor(
            nameSpace: String,
            issuerSignedItemCbor: IssuerSignedItemCbor<*>,
            intentToRetain: Boolean
        ): Oid4VPConstraintField {
            return Oid4VPConstraintField(
                intentToRetain = intentToRetain,
                path = arrayOf("$['$nameSpace']['${issuerSignedItemCbor.elementIdentifier.value}']")
            )
        }

        @JsName("fromDTO")
        fun fromDTO(dto: IOid4VPConstraintField) = with(dto) { Oid4VPConstraintField(intentToRetain = intentToRetain, path = path) }


        @JsName("fromDataElementDef")
        fun fromDataElementDef(dataElementDef: DataElementDef, intentToRetain: Boolean): Oid4VPConstraintField {
            return Oid4VPConstraintField(
                intentToRetain = intentToRetain,
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
        if (!Regex("^\\\$\\['(\\w+\\.?)+'\\]\\['\\w+'\\]\$").matches(pathEntry)) {
            throw IllegalArgumentException("Path entry in the OID4VP constraint field is not valid: $pathEntry")
        }
    }

}

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
    val definitionId: String
    val id: String

    @SerialName("descriptor_map")
    val descriptorMap: Array<out IOid4vpSubmissionDescriptor>

}

@Serializable
@JsExport
data class Oid4VPPresentationSubmission(
    @SerialName("definition_id")
    override val definitionId: String,
    @SerialName("id")
    override val id: String,
    @SerialName("descriptor_map")
    override val descriptorMap: Array<Oid4vpSubmissionDescriptor>
) : IOid4VPPresentationSubmission {
    object Static {
        fun fromPresentationDefinition(pd: IOid4VPPresentationDefinition, id: String): Oid4VPPresentationSubmission =
            Oid4VPPresentationSubmission(
                definitionId = pd.id,
                id = id,
                descriptorMap = pd.inputDescriptors.map { Oid4vpSubmissionDescriptor.Static.fromInputDescriptor(it) }.toTypedArray()
            )

    }
}

expect sealed interface IOid4vpSubmissionDescriptor {
    val id: String
    val format: Oid4VPFormats
    val path: String
}

@Serializable
@JsExport
data class Oid4vpSubmissionDescriptor(
    @SerialName("id")
    override val id: String,
    @SerialName("format")
    override val format: Oid4VPFormats = Oid4VPFormats.MSO_MDOC,
    @SerialName("path")
    override val path: String = "$"
) : IOid4vpSubmissionDescriptor {
    object Static {
        fun fromInputDescriptor(descriptor: IOid4VPInputDescriptor): Oid4vpSubmissionDescriptor =
            with(descriptor) { Oid4vpSubmissionDescriptor(id = id) }
    }
}
