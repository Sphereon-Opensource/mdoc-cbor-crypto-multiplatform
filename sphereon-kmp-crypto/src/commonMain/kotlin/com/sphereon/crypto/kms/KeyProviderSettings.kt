package com.sphereon.crypto.kms

import kotlin.js.JsExport

@JsExport
@kotlinx.serialization.Serializable
data class KeyProviderSettings(
    val id: String,
    val config: KeyProviderConfig,
    val passwordInputCallback: PasswordInputCallback? = null
)
