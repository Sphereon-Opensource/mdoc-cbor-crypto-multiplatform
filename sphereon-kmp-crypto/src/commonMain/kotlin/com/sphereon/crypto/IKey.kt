package com.sphereon.crypto

import com.sphereon.crypto.generic.KeyOperations
import com.sphereon.crypto.generic.KeyType

import com.sphereon.crypto.generic.SignatureAlgorithm
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.js.JsExport


/**
 * Represents an interface for a cryptographic key.
 */
expect interface IKey {
    /**
     * Represents the key type for the implementation of the IKey interface.
     *
     * This variable holds the type of the key (kty) as specified in COSE (CBOR Object Signing and Encryption)
     * and JWA (JSON Web Algorithms) standards.
     *
     * It is used to identify the key type for cryptographic operations, providing compatibility
     * between different security frameworks and ensuring that the key can be correctly interpreted
     * and utilized across various implementations.
     */
    val kty: Any

    /**
     * Unique identifier for the cryptographic key.
     *
     * This value is often used to differentiate between multiple keys
     * within a set, allowing for key management and retrieval based on this identifier.
     *
     * It can be null if the key identifier is not provided or not relevant
     * to the context in which the key is used.
     */
    val kid: Any?

    /**
     * Represents the algorithm associated with the key.
     *
     * Holds the algorithm identifier, which defines the cryptographic operations
     * that can be performed with the key. The value may be any type, including
     * but not limited to strings, cbor objects, depending on the
     * context in which it is used.
     */
    val alg: Any?

    /**
     * Represents the key operations applicable to the key.
     *
     * The content of this property describes what operations a key is capable of
     * performing, such as encryption, decryption, signing, and verification among others.
     * This information is used to enforce what actions can and cannot be performed
     * using the specified key, adhering to the security constraints and intended
     * usage of the key.
     */
    val key_ops: Any?

    /**
     * Represents the `crv` (Curve) parameter in a cryptographic key.
     *
     * This parameter is typically used in elliptic curve cryptography to define the specific curve
     * on which the cryptographic operations will be performed. It may be `null` if not applicable
     * or not specified.
     */
    val crv: Any?

    /**
     * Represents the 'x' coordinate parameter for an elliptic curve key or a similar cryptographic key component.
     *
     * This value is typically used in the context of keys that rely on elliptic curve algorithms,
     * and it is essential for cryptographic operations involving such keys.
     *
     * The representation is flexible and can accommodate various types of data required by different
     * cryptographic standards.
     */
    val x: Any?

    /**
     * Represents the y-coordinate of an elliptic curve point in cryptographic operations.
     *
     * Typically part of the public key for elliptic curve cryptography (ECC).
     *
     * The value can be null, indicating that the coordinate is not set or not applicable.
     */
    val y: Any?

    /**
     * Represents the private or secret part of a key in cryptographic operations.
     *
     * This property allows the inclusion of private key material in asymmetric keys,
     * which is crucial for decryption, signing, and other cryptographic operations that
     * require the private or secret part of the key.
     */
    val d: Any?

    /**
     * Represents additional information or data associated with the key.
     * This property can hold various types of supplementary data that may be needed for
     * certain cryptographic operations or key management tasks.
     */
    val additional: Any?

    /**
     * Maps key types to their appropriate values for COSE/JWA implementations.
     *
     * @return A KeyTypeMapping object containing the mappings for key types.
     */
    fun getKty(): KeyType

    /**
     * Retrieves the algorithm mapping for the key.
     *
     * @return the corresponding AlgorithmMapping instance, or null if not available
     */
    fun getSignatureAlgorithm(): SignatureAlgorithm?

    /**
     * Retrieves an array of key operations mappings for the current key.
     *
     * @return An array of KeyOperationsMapping objects representing the key operations,
     *         or null if there are no operations available.
     */
    fun getKeyOperations(): Array<KeyOperations>?

    /**
     * Retrieves the X.509 certificate chain (x5c) from the key.
     *
     * @return An array of strings representing the x5c certificate chain, or null if not available.
     */
    fun getX509CertificateChain(): Array<String>?

    fun getKidAsString(): String?

    fun getXAsString(): String?
    fun getYAsString(): String?

    fun toPublicKey(): IKey
}

@JsExport
enum class KeyVisibility {
    PUBLIC,
    PRIVATE
}


/**
 * Represents the interface for key information.
 *
 * Provides a structure to hold key-related metadata and configuration details
 * necessary for cryptographic operations.
 *
 * @param KT The specific type of key implementing the IKey interface.
 */
expect interface IKeyInfo<out KT : IKey> {

    /**
     * A nullable String variable representing the name or identifier of a kid.
     *
     * Can be `null` when the kid's name or identifier is not provided.
     */
    val kid: String?

    /**
     * Represents the algorithm used for generating and verifying digital signatures.
     * This variable may hold a specific algorithm or be null if an algorithm is not set.
     * Common algorithms include RSA, DSA, and ECDSA.
     */
    val signatureAlgorithm: SignatureAlgorithm?

    /**
     * Represents a cryptographic key that can be used for various security operations such as encryption, decryption, signing, and verification.
     *
     * This key may be optional and can be null. The actual implementation of the key is determined by the KeyType.
     */
    /*val jwk: JWK,*/
    val key: KT?

    val x5c: Array<String>?

    /**
     * Indicates the visibility status of a cryptographic key.
     *
     * This property can take a value from the `KeyVisibility` enum, representing
     * whether the key is public or private. It is used to determine the access level
     * and usability of the key within cryptographic operations.
     *
     * The possible values are:
     * - `PUBLIC`: Indicates that the key is publicly accessible.
     * - `PRIVATE`: Indicates that the key is privately held and should be restricted in its usage.
     *
     * Can be `null` if the visibility status is not specified. Public will be assumed then
     */
    val keyVisibility: KeyVisibility?

    /**
     * A map containing configuration options.
     *
     * The `opts` variable is a nullable map where both the keys and values can be of any type.
     * This map is used to store various configuration parameters that can be accessed and utilized
     * throughout the application. A null value indicates that there are no configuration options specified.
     */
    val opts: Map<*, *>?

    /**
     * Represents the Key Management System (KMS) identifier associated with the key.
     * This property might be used to specify which KMS should be utilized for operations involving the key.
     */
    val kms: String?

    /**
     * A reference to a Key Management Service (KMS) key.
     *
     * This variable holds an optional string that serves as an identifier or
     * link to a key stored in a Key Management Service. It is used to refer
     * to a specific key within the KMS without directly storing the key's value
     * in the system.
     */
    val kmsKeyRef: String?

    /**
     * Represents the type of the cryptographic key.
     *
     * It associates the key with a specific cryptographic algorithm used for operations
     * such as signing and encryption. The `KeyType` can define key types like RSA, EC (Elliptic Curve),
     * and OKP (Octet Key Pair) which are used to specify the algorithmic properties and
     * ensure interoperability between different cryptographic standards.
     *
     * This variable determines how the key can be used and identifies the mapping
     * between COSE (CBOR Object Signing and Encryption) key types and JWA (JSON Web Algorithms)
     * key types.
     */
    val keyType: KeyType?

    /**
     * Converts and returns the current key information to a public key information structure.
     *
     * @return An instance of IKeyInfo containing the public key information derived from the current key.
     */
    fun toPublicKeyInfo(): IKeyInfo<KT>
}


/**
 * Represents a resolved cryptographic key information interface.
 *
 * This interface guarantees that the key is present and resolved, providing concrete access to the key.
 *
 * @param KT Generic type that extends IKey, representing the cryptographic key type.
 */
expect interface IResolvedKeyInfo<out KT : IKey> : IKeyInfo<KT> {
    /**
     * A unique identifier that is guaranteed to be present (resolved) for the object.
     */
// Same as the above, but now wit a key guaranteed to be present (resolved)
    override val key: KT

    val x509VerificationResult: IX509VerificationResult<KT>?

    fun toResolvedPublicKeyInfo(): IResolvedKeyInfo<KT>
}


/**
 * Represents a managed cryptographic key information interface.
 *
 * This interface guarantees that the key is present and resolved, part of a KMS providing concrete access to the key.
 *
 * @param KT Generic type that extends IKey, representing the cryptographic key type.
 */
expect interface IManagedKeyInfo<KT : IKey> : IResolvedKeyInfo<KT> {
    override val kmsKeyRef: String
    override val kms: String
    fun toManagedPublicKeyInfo(): IManagedKeyInfo<KT>
}

@JsExport
@Serializable
data class KeyInfo<KT : IKey>(
    override val kid: String? = null, /*val jwk: JWK,*/
    override val key: KT? = null,
    @Transient // fixme:
    override val opts: Map<*, *>? = null,
    override val keyVisibility: KeyVisibility? = KeyVisibility.PUBLIC,
    override val signatureAlgorithm: SignatureAlgorithm? = null,
    override val x5c: Array<String>? = key?.getX509CertificateChain(),
    override val kmsKeyRef: String? = null,
    override val kms: String? = null,
    override val keyType: KeyType? = null,
) : IKeyInfo<KT> {

    override fun hashCode(): Int {
        var result = kid?.hashCode() ?: 0
        result = 31 * result + (key?.hashCode() ?: 0)
        result = 31 * result + (opts?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "KeyInfo(kid=$kid, coseKey=$key, opts=$opts)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyInfo<*>) return false

        if (kid != other.kid) return false
        if (key != other.key) return false
        if (opts != other.opts) return false

        return true
    }

    fun resolve(resolver: ((keyInfo: IKeyInfo<out KT>) -> IResolvedKeyInfo<@UnsafeVariance KT>)) = resolver(this)


    override fun toPublicKeyInfo(): IKeyInfo<out KT> = this.copy(key = key?.toPublicKey() as KT?, keyVisibility = KeyVisibility.PUBLIC)

    object Static {
        fun <KT : IKey> fromDTO(dto: IKeyInfo<out KT>) =
            with(dto) {
                KeyInfo(
                    kid = kid,
                    key = key,
                    opts = opts,
                    x5c = x5c,
                    kms = kms,
                    kmsKeyRef = kmsKeyRef,
                    keyVisibility = keyVisibility ?: KeyVisibility.PUBLIC,
                    signatureAlgorithm = signatureAlgorithm
                )
            }


    }
}

enum class KeyEncoding {
    COSE,
    JOSE
}

@JsExport
@Serializable
data class ResolvedKeyInfo<KT : IKey>(
    override val kid: String? = null, /*val jwk: JWK,*/
    override val key: KT,
    @Transient // fixme:
    override val opts: Map<*, *>? = null,
    override val keyVisibility: KeyVisibility? = KeyVisibility.PUBLIC,
    override val signatureAlgorithm: SignatureAlgorithm? = null,
    override val kmsKeyRef: String? = null,
    override val x5c: Array<String>? = null,
    override val x509VerificationResult: IX509VerificationResult<KT>? = null,
    override val kms: String? = null,
    override val keyType: KeyType? = null,
) : IResolvedKeyInfo<KT> {


    fun toKeyInfo() =
        KeyInfo(
            kid = kid,
            key = key,
            opts = opts,
            x5c = x5c,
            kmsKeyRef = kmsKeyRef,
            keyVisibility = keyVisibility ?: KeyVisibility.PUBLIC,
            signatureAlgorithm = signatureAlgorithm,
            kms = kms,
            keyType = keyType,
        )

    override fun toResolvedPublicKeyInfo(): ResolvedKeyInfo<KT> = this.copy(key = key.toPublicKey() as KT, keyVisibility = KeyVisibility.PUBLIC)

    override fun toPublicKeyInfo() = toKeyInfo().toPublicKeyInfo()

    object Static {
        fun <KT : IKey> fromDTO(dto: IResolvedKeyInfo<out KT>) =
            with(dto) {
                ResolvedKeyInfo(
                    kid = kid,
                    key = key,
                    opts = opts,
                    x5c = x5c,
                    kms = kms,
                    kmsKeyRef = kmsKeyRef,
                    x509VerificationResult = x509VerificationResult,
                    keyVisibility = keyVisibility ?: KeyVisibility.PUBLIC,
                    signatureAlgorithm = signatureAlgorithm,
                    keyType = keyType
                )
            }

        fun <KT : IKey> fromKeyInfo(dto: IKeyInfo<*>, key: KT? = null): ResolvedKeyInfo<KT> =
            with(dto) {
                ResolvedKeyInfo(
                    kid = kid,
                    key = key ?: dto.key?.let { it as KT } ?: throw IllegalArgumentException("No key passed in and key info also had no key"),
                    opts = opts,
                    x5c = x5c,
                    kmsKeyRef = kmsKeyRef,
                    keyVisibility = keyVisibility ?: KeyVisibility.PUBLIC,
                    signatureAlgorithm = signatureAlgorithm,
                    x509VerificationResult = null
                )
            }

        fun <KT : IKey> fromKey(key: KT): IResolvedKeyInfo<KT> {
            return ResolvedKeyInfo(
                key = key,
                keyType = key.getKty(),
                kid = key.getKidAsString(),
                x5c = key.getX509CertificateChain(),
                keyVisibility = if (key.d !== null) KeyVisibility.PRIVATE else KeyVisibility.PUBLIC,
                signatureAlgorithm = key.getSignatureAlgorithm()
            )
        }
    }

}

data class ManagedKeyInfo<KT : IKey>(
    override val kmsKeyRef: String,
    override val kms: String,
    private val resolvedKeyInfo: IResolvedKeyInfo<KT>
) : IManagedKeyInfo<KT> {
    override val key = resolvedKeyInfo.key

    override fun toResolvedPublicKeyInfo() = resolvedKeyInfo
    override fun toManagedPublicKeyInfo(): IManagedKeyInfo<KT> =
        this.copy(resolvedKeyInfo = resolvedKeyInfo.toResolvedPublicKeyInfo(), kmsKeyRef = kmsKeyRef, kms = kms)


    override val kid = resolvedKeyInfo.kid
    override val signatureAlgorithm = resolvedKeyInfo.signatureAlgorithm
    override val keyVisibility = resolvedKeyInfo.keyVisibility
    override val opts = resolvedKeyInfo.opts
    override fun toPublicKeyInfo() = resolvedKeyInfo.toPublicKeyInfo()

    override val x509VerificationResult = resolvedKeyInfo.x509VerificationResult
    override val x5c = resolvedKeyInfo.x5c
    override val keyType = resolvedKeyInfo.keyType
}

