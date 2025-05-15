package com.sphereon.crypto.sign.client.model

import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.sign.client.enums.TimestampContainerForm

const val EXCLUSIVE = "http://www.w3.org/2001/10/xml-exc-c14n#"

@kotlinx.serialization.Serializable
data class TimestampParameterSettings(
    val visualSignatureParameters: VisualSignatureParameters? = null,
    val timestampContainerForm: TimestampContainerForm? = TimestampContainerForm.PDF,
    val digestAlgorithm: DigestAlg = DigestAlg.SHA256,
    val canonicalizationMethod: String = EXCLUSIVE
)
