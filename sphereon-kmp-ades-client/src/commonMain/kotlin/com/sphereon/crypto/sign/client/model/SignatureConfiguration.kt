package com.sphereon.crypto.sign.client.model

data class SignatureConfiguration(
//    val id: String,
    val signatureParameters: SignatureParameters,
    val timestampParameters: TimestampParameters? = null
)
