package com.sphereon.crypto.generic

import dev.whyoleg.cryptography.CryptographyAlgorithmId
import kotlinx.serialization.Serializable
import org.kotlincrypto.core.digest.Digest
import org.kotlincrypto.hash.sha2.SHA256
import org.kotlincrypto.hash.sha2.SHA384
import org.kotlincrypto.hash.sha2.SHA512
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Retrieves a cryptographic digest instance based on the specified algorithm.
 *
 * @param digestAlgorithm The algorithm to use for the digest computation. Defaults to SHA256 if not specified.
 * @return A Digest instance initialized with the specified algorithm.
 * @throws IllegalArgumentException if the specified algorithm is not supported.
 */
// Initialize the cryptography provider
fun getDigest(digestAlgorithm: DigestAlg = DigestAlg.SHA256): Digest {
    return when (digestAlgorithm) {
        DigestAlg.SHA256 -> SHA256()
        DigestAlg.SHA384 -> SHA384()
        DigestAlg.SHA512 -> SHA512()
        else -> throw IllegalArgumentException("digestAlgorithm $digestAlgorithm is not yet supported")
    }
}

/**
 * Generates a hash value for the given input data using the specified digest algorithm.
 *
 * @param dataInput The input data to be hashed as a ByteArray.
 * @param digestAlgorithm The digest algorithm to be used for generating the hash, default is SHA-256.
 * @return A ByteArray representing the hash value of the input data.
 */
fun hash(dataInput: ByteArray, digestAlgorithm: DigestAlg = DigestAlg.SHA256): ByteArray {
    val digest = getDigest(digestAlgorithm)
    digest.update(dataInput)
    return digest.digest()
}

/**
 * Enum class representing various digest algorithms with metadata properties.
 *
 * @property internalName The internal name of the algorithm.
 * @property javaName The name of the algorithm in Java.
 * @property oid The object identifier (OID) of the algorithm.
 * @property xmlId Optional XML identifier for the algorithm.
 * @property jadesId Optional JADeS identifier for the algorithm.
 * @property httpHeaderId Optional HTTP header identifier for the algorithm.
 * @property saltLength Optional salt length associated with the algorithm.
 */
@JsExport
@Serializable
enum class DigestAlg(
    val internalName: String,
    val javaName: String,
    val oid: String,
    val xmlId: String? = null,
    val jadesId: String? = null,
    val httpHeaderId: String? = null,
    val saltLength: Int? = 0
) {
    /**
     * Represents an algorithm identifier for a non-existent or undefined digest algorithm.
     *
     * This is typically used to signify the absence of a digest algorithm within a given context.
     *
     * @param internalName Internal name representation of the algorithm.
     * @param javaName Java-specific name representation of the algorithm.
     * @param oid Object Identifier, a globally unique identifier for the algorithm.
     * @param xmlId XML namespace identifier associated with the algorithm.
     * @param jadesId Specific identifier used within a given cryptographic framework.
     * @param httpHeaderId HTTP header name associated with the algorithm.
     * @param saltLength The expected length of the salt if the algorithm uses one.
     */
    NONE("", "", "", ""),
    /**
     * Represents the SHA-256 hashing algorithm.
     *
     * @constructor
     * Initializes an instance of SHA256 with predefined constants.
     *
     * @param name The standard name of the algorithm.
     * @param jcaStandardName The standard name defined by JCA (Java Cryptography Architecture).
     * @param oid The object identifier (OID) for this algorithm.
     * @param xmlNamespace The XML namespace for this algorithm.
     * @param alias An alias name for this algorithm.
     * @param fullName The full name of the algorithm.
     * @param hashLength The output length of the hash in bytes.
     */
    SHA256("SHA256", "SHA-256", "2.16.840.1.101.3.4.2.1", "http://www.w3.org/2001/04/xmlenc#sha256", "S256", "SHA-256", 32),
    /**
     * The SHA384 class represents a specific hash algorithm implementation of the SHA-384 standard.
     *
     * @param id Identifier for the algorithm.
     * @param name Name of the algorithm.
     * @param oid Object Identifier associated with the algorithm.
     * @param uri URI identifying the algorithm specification.
     * @param jceName Java Cryptography Extension name for the algorithm.
     * @param gnuName GNU name for the algorithm.
     * @param digestLength The length of the digest produced by the algorithm, in bytes.
     */
    SHA384("SHA384", "SHA-384", "2.16.840.1.101.3.4.2.2", "http://www.w3.org/2001/04/xmlenc#sha384", "S384", "SHA-384", 48),
    /**
     * This class represents the SHA-512 cryptographic hash function, a member of the SHA-2 family.
     *
     * SHA-512 produces a 512-bit hash value, commonly rendered as a hexadecimal number of 128 digits.
     * It is widely used for data integrity, ensuring that the data has not been altered.
     *
     * @property id A string identifier for the algorithm, typically "SHA512".
     * @property algorithm The name of the hash algorithm, usually "SHA-512".
     * @property oid The object identifier in dot notation (e.g., "2.16.840.1.101.3.4.2.3").
     * @property uri The XML Encryption URI for SHA-512.
     * @property abbreviation A common abbreviation for SHA-512, such as "S512".
     * @property name The full name of the hash algorithm, usually "SHA-512".
     * @property digestLength The output length of the hash, in bytes (for SHA-512, usually 64 bytes).
     */
    SHA512("SHA512", "SHA-512", "2.16.840.1.101.3.4.2.3", "http://www.w3.org/2001/04/xmlenc#sha512", "S512", "SHA-512", 64),
    /**
     * Represents the SHA3-256 digest algorithm.
     *
     * @param internalName Internal identifier for the algorithm.
     * @param javaName Standard name for the algorithm as recognized by Java cryptography libraries.
     * @param oid Object Identifier (OID) specific to the algorithm.
     * @param xmlId XML identifier compatible with XML signatures.
     * @param jadesId Identifier used within JADES systems.
     * @param httpHeaderId HTTP header identifier for the algorithm.
     * @param saltLength Length of the salt used in the algorithm, in bytes.
     */
    SHA3_256("SHA3-256", "SHA3-256", "2.16.840.1.101.3.4.2.8", "http://www.w3.org/2007/05/xmldsig-more#sha3-256", "S3-256", null, 32),
    /**
     *
     */
    SHA3_384("SHA3-384", "SHA3-384", "2.16.840.1.101.3.4.2.9", "http://www.w3.org/2007/05/xmldsig-more#sha3-384", "S3-384", null, 48),
    /**
     *
     */
    SHA3_512("SHA3-512", "SHA3-512", "2.16.840.1.101.3.4.2.10", "http://www.w3.org/2007/05/xmldsig-more#sha3-512", "S3-512", null, 64);

    /**
     * Converts the current DigestAlg instance to its corresponding CryptographyAlgorithmId.
     *
     * @return The CryptographyAlgorithmId corresponding to the current DigestAlg instance.
     * @throws IllegalArgumentException if the DigestAlg instance is not supported.
     */
    @JsExport.Ignore
    fun toCryptoGraphicAlgorithm(): CryptographyAlgorithmId<dev.whyoleg.cryptography.algorithms.Digest> {
        return if (this == SHA256) dev.whyoleg.cryptography.algorithms.SHA256
        else if (this == SHA384) dev.whyoleg.cryptography.algorithms.SHA384
        else if (this == SHA512) dev.whyoleg.cryptography.algorithms.SHA512
        else throw IllegalArgumentException("$internalName is not yet supported")
    }

    /**
     * Utility object containing static utility functions.
     */
    object Static {
        /**
         * Checks if the provided digest algorithm is either null or represents a 'NONE' value.
         *
         * @param digestAlg the digest algorithm to be checked.
         * @return true if the digest algorithm is null or represents 'NONE', false otherwise.
         */
        fun isNone(digestAlg: DigestAlg?): Boolean {
            return digestAlg == null || digestAlg == NONE
        }

        /**
         * Finds and returns a `DigestAlg` entry that matches the provided name.
         *
         * @param name The name to search for, which can be the internal name, HTTP header identifier, or Java name of the `DigestAlg` entry.
         * @return The matching `DigestAlg` entry.
         * @throws IllegalArgumentException If the provided name does not match any `DigestAlg` entry.
         */
        @JsName("fromValue")
        fun fromValue(name: String): DigestAlg {
            return DigestAlg.entries.find { entry -> entry.internalName == name || entry.httpHeaderId == name || entry.javaName == name}
                ?: throw IllegalArgumentException("Unknown value $name")
        }
    }
}
