package com.sphereon.crypto.generic

/**
 * Enum class representing various cryptographic algorithms.
 *
 * @property internalName The internal name of the algorithm.
 * @property oid The object identifier (OID) associated with the algorithm.
 * @property padding The padding scheme used by the algorithm, if applicable.
 */
@kotlinx.serialization.Serializable
enum class CryptoAlg(val internalName: String, val oid: String, val padding: String? = null) {

    /**
     * The RSA object represents the RSA encryption algorithm with specific characteristics.
     *
     * @property internalName The internal name of the RSA algorithm.
     * @property oid The Object Identifier (OID) for the RSA algorithm.
     * @property padding The padding scheme to be used with the RSA algorithm. The default is "RSA/ECB/PKCS1Padding".
     */
    RSA("RSA", "1.2.840.113549.1.1.1", "RSA/ECB/PKCS1Padding"),

    /**
     * Represents the SHA-256 with RSA cryptographic algorithm.
     *
     * This algorithm combines the SHA-256 cryptographic hash function with RSA encryption,
     * as defined in PKCS#1 v1.5 standard.
     *
     * @property internalName Internal name of the algorithm.
     * @property oid Object Identifier of the algorithm.
     */
    SHA256_WITH_RSA("SHA256withRSA", "1.2.840.113549.1.1.11"), // PKCS#1 v1.5 is implied for SHA256withRSA

//    DSA("DSA", "1.2.840.10040.4.1", "DSA"),

    /**
     * An enum constant representing the ECDSA (Elliptic Curve Digital Signature Algorithm) cryptographic algorithm.
     *
     * @param internalName The internal name of the algorithm.
     * @param oid The object identifier (OID) associated with the ECDSA algorithm.
     */
    ECDSA("ECDSA", "1.2.840.10045.2.1"),

//    PLAIN_ECDSA("PLAIN-ECDSA", "0.4.0.127.0.7.1.1.4.1", "PLAIN-ECDSA"),

    /**
     * The `X25519` enum constant represents the X25519 public-key elliptic curve Diffie-Hellman function.
     * It is part of the `CryptoAlg` enum class, which enumerates various cryptographic algorithms.
     *
     * @param internalName The internal name of the algorithm.
     * @param oid The object identifier (OID) associated with the algorithm.
     */
    X25519("X25519", "1.3.101.110"),

    /**
     * Represents the X448 key exchange algorithm.
     *
     * X448 is a Diffie-Hellman function over curve448, which is a specific elliptic curve.
     * This algorithm is used to facilitate secure key exchange between parties over an insecure channel.
     *
     * @property internalName The internal name used to identify the algorithm.
     * @property oid The object identifier associated with the algorithm.
     */
    X448("X448", "1.3.101.111"),

    /**
     * The ED25519 enumeration represents the Ed25519 signature algorithm.
     *
     * Ed25519 is part of the Edwards-curve Digital Signature Algorithm (EdDSA) family,
     * specifically designed for high performance and secure digital signatures.
     *
     * Internal name: Ed25519
     * OID: 1.3.101.112
     */
    ED25519("Ed25519", "1.3.101.112"),

    /**
     * ED448 is a cryptographic algorithm that uses the Edwards-curve digital signature algorithm (EdDSA)
     * over the Curve448 elliptic curve. It is known for its high security level and efficiency.
     *
     * @property internalName The internal name of the algorithm, used for identification purposes.
     * @property oid The object identifier (OID) associated with the ED448 algorithm.
     */
    ED448("Ed448", "1.3.101.113"),

    /**
     * Represents the HMAC (Hash-based Message Authentication Code) algorithm within the CryptoAlg enum.
     *
     * HMAC is a specific construction for calculating a message authentication code (MAC) involving a cryptographic hash function in combination with a secret cryptographic key.
     *
     * This instance has its `internalName` set to "HMAC" and an empty OID string.
     */
    HMAC("HMAC", "");

    /**
     * Companion object for the CryptoAlg enum that provides utility methods for the enum.
     */
    companion object {
        /**
         * Retrieves the CryptoAlg instance corresponding to the given internal name.
         *
         * @param name The internal name of the cryptographic algorithm to retrieve.
         * @return The CryptoAlg instance that matches the provided internal name.
         * @throws IllegalArgumentException If no matching CryptoAlg is found for the provided name.
         */
        fun from(name: String): CryptoAlg = entries.find { it.internalName == name }
            ?: throw IllegalArgumentException("Algorithm $name not found")

    }
}
