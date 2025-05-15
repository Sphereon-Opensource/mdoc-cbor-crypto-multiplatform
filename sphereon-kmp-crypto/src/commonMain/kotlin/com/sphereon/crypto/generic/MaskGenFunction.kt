package com.sphereon.crypto.generic

import kotlin.js.JsExport

/**
 * Defines the mask generation function, an algorithm used in certain cryptographic operations
 * such as signing and key generation.
 */
@JsExport
@kotlinx.serialization.Serializable
enum class MaskGenFunction {
    /**
     * Represents the MGF1 (Mask Generation Function 1) algorithm enumeration.
     *
     * It is commonly used in cryptographic operations such as in the PKCS#1 standard for RSA-OAEP and RSA-PSS.
     */
    MGF1
}
