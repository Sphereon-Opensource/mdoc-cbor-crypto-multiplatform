package com.sphereon.crypto.kms

import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.kms.model.IdentifierMethod
import com.sphereon.crypto.sign.IRawSignatureService
import kotlin.js.JsExport

/**
 * IKeyManagerService provides an interface for managing key management systems (KMS) and key resolver services.
 * It extends the IRawSignatureService and IPublicKeyResolver interfaces.
 */
@JsExport
interface IKeyManagerService : IRawSignatureService, IPublicKeyResolver {
    /**
     * Retrieves the default Key Management System (KMS) identifier.
     *
     * This function returns the identifier associated with the default KMS,
     * which is used for cryptographic operations such as key generation and signature management.
     *
     * @return The identifier of the default KMS as a string.
     */
    fun defaultKmsId(): String

    /**
     * Provides the default resolver identifier.
     *
     * @return The default resolver ID as a String.
     */
    fun defaultResolverId(): String

    /**
     * Registers a new Key Management System (KMS) with the Key Manager Service.
     *
     * @param kms The `IKeyManagementSystem` instance to be registered.
     * @param makeDefaultKms Optional parameter to make the registered KMS the default KMS.
     *                       Defaults to false if not provided.
     */
    fun registerKms(kms: IKeyManagementSystem, makeDefaultKms: Boolean? = false)

    /**
     * Retrieves an array of Key Management System (KMS) IDs.
     *
     * @return An array of strings representing the IDs of the registered KMSs.
     */
    fun getKmsIds(): Array<String>

    /**
     * Retrieves an instance of `IKeyManagementSystem` for the specified key management system (KMS) identifier.
     *
     * @param id The identifier of the key management system. Defaults to the result of `defaultKmsId()`.
     * @return An instance of `IKeyManagementSystem` corresponding to the specified KMS identifier.
     */
    fun getKmsById(id: String = defaultKmsId()): IKeyManagementSystem

    /**
     * Retrieves the Key Management System (KMS) that supports the specified signature algorithm.
     *
     * @param signatureAlgorithm the signature algorithm for which the corresponding KMS is being requested.
     * @return the KMS that supports the specified signature algorithm.
     */
    fun getKmsBySignatureAlgorithm(signatureAlgorithm: SignatureAlgorithm): IKeyManagementSystem

    /**
     * Retrieves an instance of IKeyResolverService by its identifier.
     *
     * @param id The identifier of the desired key resolver service. Defaults to 'defaultResolverId()' if not specified.
     * @return An instance of IKeyResolverService associated with the given identifier.
     */
    fun getResolverById(id: String = defaultResolverId()): IKeyResolverService

    /**
     * Retrieves an instance of `IKeyResolverService` based on the given identifier method, key type, or resolver ID.
     *
     * This function facilitates the resolution of key management or cryptographic operations by using a specified
     * identifier method, key type, or a unique resolver identifier.
     *
     * @param identifierMethod The method used to identify cryptographic keys, can be `null`. When provided, it restricts the resolvers to those which support the specified identifier
     *  method.
     * @param keyType The type of key to be resolved, can be `null`. When provided, it restricts the resolvers to those which support the specified key type.
     * @param resolverId The unique identifier of the resolver, can be `null`. When provided, it directly selects the resolver with the specified ID.
     * @return An instance of `IKeyResolverService` that matches the provided criteria.
     */
    fun getResolverByKeyTypeOrIdentifier(
        identifierMethod: IdentifierMethod? = null,
        keyType: KeyType? = null,
        resolverId: String? = null
    ): IKeyResolverService

    /**
     * Registers a key resolver service with the key manager service.
     *
     * @param resolver The IKeyResolverService instance to be registered.
     * @param makeDefaultResolver A Boolean indicating whether the provided resolver should be set as the default resolver. Default is false.
     */
    fun registerResolver(resolver: IKeyResolverService, makeDefaultResolver: Boolean? = false)


    /**
     * Asynchronously generates a cryptographic key pair based on the specified elliptic curve mapping.
     *
     * @param curve The elliptic curve mapping used to generate the key pair. This parameter defines the types of curves supported
     *              for cryptographic operations and includes mappings for both COSE and JOSE curves.
     * @return A `CryptoProviderKeyPair` containing both COSE and JOSE key pairs.
     */
    @JsExport.Ignore
    suspend fun generateKeyAsync(
        kms: String? = null, // Either we look for the signature algo supported or use the default KMS
        kmsKeyRef: String? = null,
        use: JwkUse? = null,
        keyOperations: Array<out KeyOperations>? = null,
        alg: SignatureAlgorithm? = null
    ): ManagedKeyPair

    fun getResolverIds(): Array<String>
    fun getKms(kms: String?, alg: SignatureAlgorithm?): IKeyManagementSystem
}

/**
 * Interface `IKeyManagementSystem` provides a blueprint for managing cryptographic keys, including their generation,
 * supported types, and cryptographic curves. Extends the `IRawSignatureService` to offer functionalities for
 * generating and verifying cryptographic signatures.
 */
@JsExport
interface IKeyManagementSystem : IRawSignatureService {

    fun getId(): String

    /**
     * Retrieves an array of supported key type mappings.
     *
     * The supported key types represent the mappings between COSE (CBOR Object Signing and Encryption)
     * key types and their corresponding JWA (JSON Web Algorithms) key types. This method provides the
     * available key type mappings for cryptographic operations.
     *
     * @return An array of supported `KeyTypeMapping` objects.
     */
    fun supportedKeyTypes(): Array<KeyType>

    /**
     * Provides a list of supported algorithms by the crypto provider.
     *
     * @return An array of AlgorithmMapping objects representing the supported algorithms.
     */
    fun supportedSignatureAlgorithms(): Array<SignatureAlgorithm>

    fun supportedDigests(): Array<DigestAlg> // convenience, derived from signature algos above

    /**
     * Retrieves an array of supported elliptic curve mappings.
     *
     * @return An array of `CurveMapping` instances representing the supported elliptic curves
     *         for cryptographic operations.
     */
    fun supportedCurves(): Array<Curve>

    /**
     * Checks if the provided elliptic curve mapping is supported by the crypto provider.
     *
     * @param curve The curve mapping to be checked for support.
     * @return true if the curve is supported, false otherwise.
     */
    fun isSupportedCurve(curve: Curve): Boolean

    /**
     * Asynchronously generates a cryptographic key pair based on the specified elliptic curve mapping.
     *
     * @param curve The elliptic curve mapping used to generate the key pair. This parameter defines the types of curves supported
     *              for cryptographic operations and includes mappings for both COSE and JOSE curves.
     * @return A `CryptoProviderKeyPair` containing both COSE and JOSE key pairs.
     */
    @JsExport.Ignore
    suspend fun generateKeyAsync(
        use: JwkUse? = null,
        keyOperations: Array<out KeyOperations>? = null,
        alg: SignatureAlgorithm? = null
    ): ManagedKeyPair
}
