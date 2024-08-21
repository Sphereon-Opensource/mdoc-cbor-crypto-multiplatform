@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import com.sphereon.crypto.cose.CoseAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
actual sealed interface IOid4VPFormat {
    @SerialName("mso_mdoc")
    actual val msoMdoc: IOid4VPSupportedAlgorithm
}

@Serializable
actual sealed interface IOid4VPSupportedAlgorithm {
    actual val alg: Array<CoseAlgorithm>
}

@Serializable
actual sealed interface IOid4VPPresentationDefinition {
    actual val id: String

    @SerialName("input_descriptors")
    actual val inputDescriptors: Array<out IOid4VPInputDescriptor>
}

@Serializable
actual sealed interface IOid4VPInputDescriptor {
    actual val id: String
    actual val format: IOid4VPFormat
    actual val constraints: IOid4VPConstraints
}


@Serializable
actual sealed interface IOid4VPConstraints {
    @SerialName("limit_disclosure")
    actual val limitDisclosure: Oid4VPLimitDisclosure
    actual val fields: Array<out IOid4VPConstraintField>
}

@Serializable
actual sealed interface IOid4VPConstraintField {
    actual val path: Array<String>

    @SerialName("intent_to_retain")
    actual val intentToRetain: Boolean
}


actual sealed interface IOid4VPPresentationSubmission {
    @SerialName("definition_id")
    actual val definitionId: String
    actual val id: String

    @SerialName("descriptor_map")
    actual val descriptorMap: Array<out IOid4vpSubmissionDescriptor>

}

actual sealed interface IOid4vpSubmissionDescriptor {
    actual val id: String
    actual val format: Oid4VPFormats
    actual val path: String
}
