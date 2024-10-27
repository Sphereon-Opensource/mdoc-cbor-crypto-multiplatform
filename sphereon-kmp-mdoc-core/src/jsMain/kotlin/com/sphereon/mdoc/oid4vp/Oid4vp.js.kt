@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import com.sphereon.crypto.cose.CoseAlgorithm
import kotlinx.serialization.SerialName

@JsExport
actual sealed external interface IOid4VPFormat {
    @JsName("mso_mdoc")
    @SerialName("mso_mdoc")
    actual val mso_mdoc: IOid4VPSupportedAlgorithm?
}

@JsExport
actual sealed external interface IOid4VPSupportedAlgorithm {
    @JsName("alg")
    actual val alg: Array<String>
}

@JsExport
actual sealed external interface IOid4VPPresentationDefinition {
    @JsName("id")
    actual val id: String
    @SerialName("input_descriptors")
    actual val input_descriptors: Array<out IOid4VPInputDescriptor>
}

@JsExport
actual sealed external interface IOid4VPInputDescriptor {
    @JsName("id")
    actual val id: String
    @JsName("format")
    actual val format: IOid4VPFormat
    @JsName("constraints")
    actual val constraints: IOid4VPConstraints
}


@JsExport
actual sealed external interface IOid4VPConstraints {
    @SerialName("limit_disclosure")
    @JsName("limit_disclosure")
    actual val limit_disclosure: String
    @JsName("fields")
    actual val fields: Array<out IOid4VPConstraintField>
}

@JsExport
actual sealed external interface IOid4VPConstraintField {
    @SerialName("path")
    actual val path: Array<String>
    @SerialName("intent_to_retain")
    actual val intent_to_retain: Boolean
}

@JsExport
actual sealed external interface IOid4VPPresentationSubmission {
    @JsName("definition_id")
    @SerialName("definition_id")
    actual val definition_id: String
    actual val id: String
    @JsName("descriptor_map")
    @SerialName("descriptor_map")
    actual val descriptor_map: Array<out IOid4vpSubmissionDescriptor>

}

@JsExport
actual sealed external interface IOid4vpSubmissionDescriptor {
    @JsName("id")
    actual val id: String
    @JsName("format")
    actual val format: String
    @JsName("path")
    actual val path: String
}
