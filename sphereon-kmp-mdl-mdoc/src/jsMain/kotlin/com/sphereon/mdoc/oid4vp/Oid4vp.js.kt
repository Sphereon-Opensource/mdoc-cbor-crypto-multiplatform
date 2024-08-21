@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import com.sphereon.crypto.cose.CoseAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
actual sealed external interface IOid4VPFormat {
    @SerialName("mso_mdoc")
    actual val msoMdoc: IOid4VPSupportedAlgorithm
}

@JsExport
actual sealed external interface IOid4VPSupportedAlgorithm {
    actual val alg: Array<CoseAlgorithm>
}

@JsExport
actual sealed external interface IOid4VPPresentationDefinition {
    actual val id: String
    @SerialName("input_descriptors")
    actual val inputDescriptors: Array<out IOid4VPInputDescriptor>
}

@JsExport
actual sealed external interface IOid4VPInputDescriptor {
    actual val id: String
    actual val format: IOid4VPFormat
    actual val constraints: IOid4VPConstraints
}


@JsExport
actual sealed external interface IOid4VPConstraints {
    @SerialName("limit_disclosure")
    actual val limitDisclosure: Oid4VPLimitDisclosure
    actual val fields: Array<out IOid4VPConstraintField>
}

@JsExport
actual sealed external interface IOid4VPConstraintField {
    @SerialName("path")
    actual val path: Array<String>
    @SerialName("intent_to_retain")
    actual val intentToRetain: Boolean
}

@JsExport
actual sealed external interface IOid4VPPresentationSubmission {
    @SerialName("definition_id")
    actual val definitionId: String
    actual val id: String
    @SerialName("descriptor_map")
    actual val descriptorMap: Array<out IOid4vpSubmissionDescriptor>

}

@JsExport
actual sealed external interface IOid4vpSubmissionDescriptor {
    actual val id: String
    actual val format: Oid4VPFormats
    actual val path: String
}
