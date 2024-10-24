package com.sphereon.crypto

import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.generic.KeyOperationsMapping
import com.sphereon.crypto.generic.KeyTypeMapping
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.internal.throwMissingFieldException
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
    fun getKtyMapping(): KeyTypeMapping

    /**
     * Retrieves the algorithm mapping for the key.
     *
     * @return the corresponding AlgorithmMapping instance, or null if not available
     */
    fun getAlgMapping(): SignatureAlgorithm?

    /**
     * Retrieves an array of key operations mappings for the current key.
     *
     * @return An array of KeyOperationsMapping objects representing the key operations,
     *         or null if there are no operations available.
     */
    fun getKeyOperationsMapping(): Array<KeyOperationsMapping>?

    /**
     * Retrieves the X.509 certificate chain (x5c) from the key.
     *
     * @return An array of strings representing the x5c certificate chain, or null if not available.
     */
    fun getX5cArray(): Array<String>?
}


expect interface IKeyInfo<out KeyType : IKey> {
    val kid: String?
    val signatureAlgorithm: SignatureAlgorithm?

    /*val jwk: JWK,*/
    val key: KeyType?
    val opts: Map<*, *>?
}


expect interface IResolvedKeyInfo<out KeyType : IKey> : IKeyInfo<KeyType> {
    // Same as the above, but now wit a key guaranteed to be present (resolved)
    override val key: KeyType
}


@JsExport
@Serializable
data class KeyInfo<out KeyType : IKey>(
    override val kid: String? = null, /*val jwk: JWK,*/
    override val key: KeyType? = null,
    @Transient // fixme:
    override val opts: Map<*, *>? = null,
    override val signatureAlgorithm: SignatureAlgorithm? = null,
) : IKeyInfo<KeyType> {

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

    fun resolve(resolver: ((keyInfo: IKeyInfo<out KeyType>) -> IResolvedKeyInfo<@UnsafeVariance KeyType>)) = resolver(this)

    object Static {
        fun <KeyType : IKey> fromDTO(dto: IKeyInfo<out KeyType>) =
            with(dto) { KeyInfo(kid = kid, key = key, opts = opts, signatureAlgorithm = signatureAlgorithm) }
    }
}

@JsExport
@Serializable
data class ResolvedKeyInfo<KeyType : IKey>(
    override val kid: String? = null, /*val jwk: JWK,*/
    override val key: KeyType,
    @Transient // fixme:
    override val opts: Map<*, *>? = null,
    override val signatureAlgorithm: SignatureAlgorithm? = null
) : IResolvedKeyInfo<KeyType> {
    fun toKeyInfo() = KeyInfo(kid = kid, key = key, opts = opts, signatureAlgorithm = signatureAlgorithm)

    object Static {
        fun <KeyType : IKey> fromDTO(dto: IResolvedKeyInfo<out KeyType>) =
            with(dto) { ResolvedKeyInfo(kid = kid, key = key, opts = opts, signatureAlgorithm = signatureAlgorithm) }

        fun <KeyType : IKey> fromKeyInfo(dto: IKeyInfo<*>, key: KeyType?): ResolvedKeyInfo<KeyType> =
            with(dto) {
                ResolvedKeyInfo(
                    kid = kid,
                    key = key ?: dto.key?.let { it as KeyType } ?: throw IllegalArgumentException("No key passed in and key info also had no key"),
                    opts = opts,
                    signatureAlgorithm = signatureAlgorithm
                )
            }
    }
}

