package com.sphereon.crypto.sign.client.enums

@kotlinx.serialization.Serializable
enum class TimestampContainerForm {
    /** Used to timestamp a PDF document */
    PDF,

    /** Used to timestamp provided document(s) and creates an ASiC-E container */
    ASiC_E,

    /** Used to timestamp provided document(s) and creates an ASiC-S container */
    ASiC_S;

}
