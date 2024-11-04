package com.sphereon.crypto.kms

import com.sphereon.crypto.generic.Curve
import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType
import com.sphereon.crypto.generic.ManagedKeyPair
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.jose.JwkUse
import com.sphereon.crypto.sign.IRawSignatureService
import kotlin.js.JsExport

@JsExport
interface IKeyManagerService : IRawSignatureService, IPublicKeyResolver {
    fun defaultKmsId(): String
    fun defaultResolverId(): String
    fun registerKms(kms: IKeyManagementSystem, makeDefaultKms: Boolean? = false)
    fun getKmsIds(): Array<String>
    fun getKmsById(id: String = defaultKmsId()): IKeyManagementSystem
    fun getKmsBySignatureAlgorithm(signatureAlgorithm: SignatureAlgorithm): IKeyManagementSystem
    fun getResolverById(id: String = defaultResolverId()): IKeyResolverService
    fun getResolverByKeyTypeOrIdentifier(
        identifierMethod: IdentifierMethod? = null,
        keyType: KeyType? = null,
        resolverId: String? = null
    ): IKeyResolverService

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
