@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.sphereon.mdoc.oid4vp

import com.sphereon.crypto.cose.CoseAlgorithm
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JsExport
@Serializable
actual sealed external interface IOid4VPFormat {
    @SerialName("mso_mdoc")
    actual val msoMdoc: IOid4VPSupportedAlgorithm
}

@JsExport
@Serializable
actual sealed external interface IOid4VPSupportedAlgorithm {
    actual val alg: Array<CoseAlgorithm>
}

@JsExport
@Serializable
actual sealed external interface IOid4VPPresentationDefinition {
    actual val id: String
    @SerialName("input_descriptors")
    actual val inputDescriptors: Array<out IOid4VPInputDescriptor>
}

@JsExport
@Serializable
actual sealed external interface IOid4VPInputDescriptor {
    actual val id: String
    actual val format: IOid4VPFormat
    actual val constraints: IOid4VPConstraints
}


@JsExport
@Serializable
actual sealed external interface IOid4VPConstraints {
    @SerialName("limit_disclosure")
    actual val limitDisclosure: Oid4VPLimitDisclosure
    actual val fields: Array<out IOid4VPConstraintField>
}

@JsExport
@Serializable
actual sealed external interface IOid4VPConstraintField {
    actual val path: Array<String>
    @SerialName("intent_to_retain")
    actual val intentToRetain: Boolean
}
