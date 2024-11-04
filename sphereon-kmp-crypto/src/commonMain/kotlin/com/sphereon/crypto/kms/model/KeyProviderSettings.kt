package com.sphereon.crypto.kms.model

import kotlin.js.JsExport

/**
 * Represents the settings required for configuring a key provider.
 *
 * @property id A unique identifier for the key provider instance.
 * @property config Configuration settings specific to the type of key provider.
 * @property passwordInputCallback Optional callback function for password input, used for certain key providers.
 */
@JsExport
@kotlinx.serialization.Serializable
data class KeyProviderSettings(
    val id: String,
    val config: KeyProviderConfig,
    val passwordInputCallback: PasswordInputCallback? = null
)
