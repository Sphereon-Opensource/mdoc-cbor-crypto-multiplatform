package com.sphereon.crypto.kms.model

import kotlin.js.JsExport

/**
 * IdentifierMethod represents the various methods used for identifying cryptographic keys.
 */
@JsExport
enum class IdentifierMethod {
    /**
     * The JWK (JSON Web Key) class represents a cryptographic key used for signing, encrypting,
     * and validating tokens standardized in the JSON Web Key (JWK) specification.
     */
    jwk,

    /**
     * The Kid class represents a young individual with basic attributes and behaviors.
     *
     */
    kid,

    /**
     * The `cose_key` class represents a COSE (CBOR Object Signing and Encryption) key object.
     *
     * COSE keys are used in various cryptographic operations including signing, encryption,
     * key agreement, and message authentication codes. This class provides methods to
     * initialize, manage, and utilize COSE keys in compliance with the COSE specification.
     *
     */
    cose_key,

    /**
     * The x5c class represents a concept or entity related to the larger system or application.
     */
    x5c
}
