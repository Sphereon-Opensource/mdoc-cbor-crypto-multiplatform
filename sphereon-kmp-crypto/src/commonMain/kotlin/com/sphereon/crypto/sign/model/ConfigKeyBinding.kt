package com.sphereon.crypto.sign.model

import kotlin.js.JsExport

/**
 * Represents the configuration for key binding used in the signing process.
 *
 * We only have ids here as we also want to expose these in path params of a REST API
 *
 * @property kid The Key identifier.
 * @property settingsId The settings configuration ID. This value is optional.
 * @property keyProviderId The key provider ID.
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
    val settingsId: String? = null,

    /**
     * The Key provider Id.
     */
    val keyProviderId: String
)
