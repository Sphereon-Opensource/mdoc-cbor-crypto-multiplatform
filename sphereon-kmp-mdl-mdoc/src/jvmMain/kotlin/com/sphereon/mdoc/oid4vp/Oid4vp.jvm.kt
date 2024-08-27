@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import com.sphereon.crypto.cose.CoseAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

actual sealed interface IOid4VPFormat {
    @SerialName("mso_mdoc")
    actual val mso_mdoc: IOid4VPSupportedAlgorithm
}

actual sealed interface IOid4VPSupportedAlgorithm {
    @SerialName("alg")
    actual val alg: Array<String>
}

actual sealed interface IOid4VPPresentationDefinition {
    actual val id: String

    @SerialName("input_descriptors")
    actual val input_descriptors: Array<out IOid4VPInputDescriptor>
}

actual sealed interface IOid4VPInputDescriptor {
    actual val id: String
    actual val format: IOid4VPFormat
    actual val constraints: IOid4VPConstraints
}


actual sealed interface IOid4VPConstraints {
    @SerialName("limit_disclosure")
    actual val limit_disclosure: String
    actual val fields: Array<out IOid4VPConstraintField>
}

actual sealed interface IOid4VPConstraintField {
    actual val path: Array<String>

    @SerialName("intent_to_retain")
    actual val intent_to_retain: Boolean
}


actual sealed interface IOid4VPPresentationSubmission {
    @SerialName("definition_id")
    actual val definition_id: String
    actual val id: String

    @SerialName("descriptor_map")
    actual val descriptor_map: Array<out IOid4vpSubmissionDescriptor>

}

actual sealed interface IOid4vpSubmissionDescriptor {
    actual val id: String
    actual val format: String
    actual val path: String
}
