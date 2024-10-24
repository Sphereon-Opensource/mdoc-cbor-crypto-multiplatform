package com.sphereon.crypto.sign.model

import kotlin.js.JsExport

/**
 * Allows to bind a Key identifier, signature Config Id and a Key provider Id. Typically used in requests/responses.
 */
@JsExport
@kotlinx.serialization.Serializable
data class ConfigKeyBinding(
    /**
     * The Key identifier.
     */
    val kid: String,

    /**
     * The signature Config Id.
     */
    val signatureConfigId: String? = null,

    /**
     * The Key provider Id.
     */
    val keyProviderId: String
)
