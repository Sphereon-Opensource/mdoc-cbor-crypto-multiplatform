package com.sphereon.crypto.kms

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.IResolvedKeyInfo
import com.sphereon.crypto.generic.KeyType
import kotlin.js.JsExport

// TODO: DidResolverService


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

interface IPublicKeyResolver {
    /**
     * Resolves the public key asynchronously given the key information and additional optional parameters.
     *
     * @param keyInfo The key information containing metadata and the cryptographic key to be resolved.
     * @param identifierMethod An optional parameter to specify the method used to identify the key (e.g., jwk, kid, cose_key, x5c).
     * @param trustedCerts An optional array of trusted certificates that may be used in the resolution process.
     * @param verifyX509CertificateChain An optional boolean indicating whether the X.509 certificate chain should be verified.
     * @return An `IResolvedKeyInfo` object containing the resolved key information.
     */
    @JsExport.Ignore
    suspend fun <KeyType : IKey> resolvePublicKeyAsync(
        keyInfo: IKeyInfo<KeyType>,
        identifierMethod: IdentifierMethod? = null,
        trustedCerts: Array<String>? = null,
        verifyX509CertificateChain: Boolean? = null
    ): IResolvedKeyInfo<KeyType>
}

/**
 * Interface for a service that resolves keys based on identifier methods and key types.
 */
@JsExport
interface IKeyResolverService : IPublicKeyResolver {

    /**
     * Retrieves the unique identifier.
     *
     * @return The unique identifier as a String.
     */
    fun getId(): String

    /**
     * Retrieves all supported identifier methods.
     *
     * @return An array of supported identifier methods.
     */
    fun allSupportedIdentifierMethods(): Array<IdentifierMethod>

    /**
     * Retrieves an array of all supported KeyType values.
     *
     * @return An array containing all the supported KeyType enums.
     */
    fun allSupportedKeyTypes(): Array<KeyType>

    /**
     * Provides a mapping of supported identifier methods to their corresponding array of key types.
     *
     * @return A Map where the key is an IdentifierMethod and the value is an array of KeyType instances supported by that method.
     */
    fun supportedKeyTypesAndIdentifierMethods(): Map<IdentifierMethod, Array<KeyType>>

    /**
     * Retrieves the supported key types for a given identifier method.
     *
     * @param identifierMethod The identifier method for which to retrieve supported key types.
     * @return An array of supported key types corresponding to the provided identifier method.
     */
    fun getSupportedKeyTypes(identifierMethod: IdentifierMethod): Array<KeyType>

    /**
     * Retrieves a list of supported identifier methods for the specified key type.
     *
     * @param keyType the type of key for which to get the supported identifier methods
     * @return an array of IdentifierMethod representing the supported methods for the provided key type
     */
    fun getSupportedIdentifierMethods(keyType: KeyType): Array<IdentifierMethod>


}
