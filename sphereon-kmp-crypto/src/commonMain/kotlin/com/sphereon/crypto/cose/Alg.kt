package com.sphereon.crypto.cose

import com.sphereon.crypto.generic.DigestAlg
import com.sphereon.crypto.jose.AlgorithmType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Enum class representing COSE algorithms.
 *
 * @property id The identifier for the algorithm.
 * @property value The integer value associated with the algorithm.
 * @property keyType The type of key used by the algorithm.
 * @property hash The hash algorithm used by the algorithm, if applicable.
 * @property tagLength The tag length of the algorithm, if applicable.
 * @property curve The elliptic curve used by the algorithm, if applicable.
 * @property description A brief description of the algorithm.
 * @property type The type of algorithm, either for signature or encryption.
 */
@JsExport
@Serializable(with = CoseAlgSerializer::class)
enum class CoseAlgorithm(
    @JsName("id")
    val id: String,

    @JsName("value")
    val value: Int,

    @JsName("keyType")
    val keyType: CoseKeyType?,

    @JsName("hash")
    val hash: DigestAlg?,

    @JsName("tagLength")
    val tagLength: Int?,

    @JsName("curve")
    val curve: CoseCurve?,

    @JsName("description")
    val description: String,

    @JsName("type")
    val type: AlgorithmType
) {
    /**
     * Represents the ES256 algorithm configuration for COSE (CBOR Object Signing and Encryption).
     *
     * This algorithm uses ECDSA (Elliptic Curve Digital Signature Algorithm) and the P-256 curve with SHA-256 hash.
     * It is primarily used for creating and verifying digital signatures in a compact and efficient manner.
     *
     * @param name The name of the algorithm (ES256).
     * @param algId The COSE algorithm identifier specific to ES256 (-7).
     * @param keyType The type of key used (EC2 for Elliptic Curve keys).
     * @param hashAlg The hash algorithm used (SHA256).
     * @param param Not used in this configuration, hence null.
     * @param curve The elliptic curve used (P-256 curve).
     * @param description A brief description of the algorithm ("ECDSA w/ SHA-256").
     * @param algType The type of algorithm, which in this case is SIGNATURE.
     */
    ES256(
        "ES256",
        -7,
        CoseKeyType.EC2,
        DigestAlg.SHA256,
        null,
        CoseCurve.P_256,
        "ECDSA w/ SHA-256", AlgorithmType.SIGNATURE
    ),

    /**
     * A class that represents the ES256K algorithm.
     *
     * This algorithm is defined to use the "ES256K" type identifier and
     * corresponds to the ECDSA using the secp256k1 curve with the SHA-256 hash algorithm.
     *
     * @property name Represents the name of the algorithm, which is "ES256K".
     * @property identifier Numerical identifier of the algorithm, which is -47.
     * @property keyType Type of the cryptographic key used in the algorithm, which is EC2.
     * @property hashAlgorithm The hash algorithm used, which is SHA-256.
     * @property additionalParameter Any additional parameter, remains null in this case.
     * @property curve Represents the elliptic curve used, which is secp256k1.
     * @property description Brief description of the algorithm, which is "ECDSA secp256k1 curve w/ SHA-256".
     * @property algorithmType Indicates the type of the algorithm, which in this case is a SIGNATURE type.
     */
    ES256K(
        "ES256K",
        -47,
        CoseKeyType.EC2,
        DigestAlg.SHA256,
        null,
        CoseCurve.secp256k1,
        "ECDSA secp256k1 curve w/ SHA-256", AlgorithmType.SIGNATURE
    ),

    /**
     * This class represents the ES384 signature algorithm, incorporating elliptic curve
     * cryptography with a prime curve (P-384) and SHA-384 hash function.
     *
     * @property name The name of the algorithm ("ES384").
     * @property algorithmId The unique identifier for the algorithm (-35).
     * @property keyType The type of COSE key used (CoseKeyType.EC2).
     * @property hashAlgorithm The hash algorithm utilized (HashAlgorithm.SHA384).
     * @property additionalParams Any additional parameters associated with the algorithm (null here).
     * @property curve The elliptic curve used (CoseCurve.P_384).
     * @property description A textual description of the algorithm ("ECDSA w/ SHA-384").
     * @property algorithmType The type of the algorithm (AlgorithmType.SIGNATURE).
     */
    ES384(
        "ES384",
        -35,
        CoseKeyType.EC2,
        DigestAlg.SHA384,
        null,
        CoseCurve.P_384,
        "ECDSA w/ SHA-384", AlgorithmType.SIGNATURE
    ),

    /**
     * ES512 is an implementation of the ECDSA signature algorithm using the SHA-512 hash algorithm.
     *
     * @param name The name of the algorithm, which is "ES512".
     * @param id The identifier associated with this algorithm, which is -36.
     * @param keyType The type of key used, in this case, CoseKeyType.EC2 (Elliptic Curve).
     * @param hashAlgorithm The hash algorithm used, which is SHA512.
     * @param curve The elliptic curve used, which is CoseCurve.P_521.
     * @param algorithmType The type of algorithm, which in this case is AlgorithmType.SIGNATURE.
     */
    ES512(
        "ES512",
        -36,
        CoseKeyType.EC2,
        DigestAlg.SHA512,
        null,
        CoseCurve.P_521,
        "ECDSA w/ SHA-512", AlgorithmType.SIGNATURE
    ),

    /**
     * The EdDSA class represents the Edwards-curve Digital Signature Algorithm used in cryptographic operations.
     *
     * @param name The name of the algorithm.
     * @param algorithmId The unique identifier for the algorithm.
     * @param keyType The type of cryptographic key used for the algorithm (OKP for Octet Key Pair).
     * @param tag Optional tag associated with the algorithm.
     * @param parameters Optional parameters for the algorithm.
     * @param curve The cryptographic curve associated with the algorithm (e.g., P-521).
     * @param algorithmName The display name for the algorithm.
     * @param algorithmType The type of cryptographic operation performed by the algorithm (e.g., SIGNATURE).
     */
    EdDSA("EdDSA", -8, CoseKeyType.OKP, null, null, CoseCurve.P_521, "EdDSA", AlgorithmType.SIGNATURE),

    /**
     * The HS256_64 class represents an HMAC algorithm with SHA-256, truncated to 64 bits.
     *
     * @property id The algorithm identifier.
     * @property keyType The type of key used by the algorithm. In this case, it's null.
     * @property hash The hash algorithm used, which is SHA-256.
     * @property tagLength The length of the tag, which is 64 bits for this algorithm.
     * @property curve The elliptic curve used, which is null for this algorithm.
     * @property description A brief description of the algorithm.
     * @property type The type of algorithm, which is a signature algorithm.
     */
    HS256_64(
        "HS256/64",
        4,
        null,
        DigestAlg.SHA256,
        64,
        null,
        "HMAC w/ SHA-256 truncated to 64 bits", AlgorithmType.SIGNATURE
    ),

    /**
     * A class representing the HS256 algorithm, which stands for HMAC with SHA-256.
     *
     * @property name The name of the algorithm ("HS256").
     * @property priority The priority level of the algorithm.
     * @property keyType The type of key used, in this case, symmetric.
     * @property hashAlgorithm The hashing algorithm used, which is SHA-256.
     * @property keySize The size of the key, in bits.
     * @property additionalInfo Any additional information about the algorithm, which is null.
     * @property description A brief description of the algorithm ("HMAC w/ SHA-256").
     * @property algorithmType The type of the algorithm, which is a signature algorithm.
     */
    HS256("HS256", 5, CoseKeyType.Symmetric, DigestAlg.SHA256, 256, null, "HMAC w/ SHA-256", AlgorithmType.SIGNATURE),

    /**
     * The HS384 enumeration represents the HMAC with SHA-384 algorithm.Specifies the parameters used for the algorithm, including the name, key length, key type, hash algorithm
     * , bit length, and description. It is categorized under the `SIGNATURE` algorithm type.
     *
     * @property name The name of the algorithm, which is "HS384".
     * @property value A unique identifier for the algorithm, which is 6.
     * @property keyType The type of key associated with the algorithm, which is `CoseKeyType.Symmetric`.
     * @property hashAlgorithm The hash algorithm used, which is `HashAlgorithm.SHA384`.
     * @property bitLength The bit length used in the algorithm, which is 384.
     * @property parameterSpec The parameter specifications for the algorithm, which is null.
     * @property description A brief description of the algorithm, which is "HMAC w/ SHA-384".
     * @property algorithmType The category of the algorithm, which is `AlgorithmType.SIGNATURE`.
     */
    HS384("HS384", 6, CoseKeyType.Symmetric, DigestAlg.SHA384, 384, null, "HMAC w/ SHA-384", AlgorithmType.SIGNATURE),

    /**
     * Represents the HS512 (HMAC using SHA-512) cryptographic algorithm.
     *
     * @property name The algorithm name.
     * @property id The identifier for the algorithm.
     * @property keyType The type of key used by the algorithm (symmetric in this case).
     * @property hashAlgorithm The hashing algorithm used in conjunction with HMAC.
     * @property keySize The size of the key used by the algorithm in bits.
     * @property parameter Additional parameters for the algorithm, if any.
     * @property description A textual description of the algorithm.
     * @property algorithmType The category of the algorithm (e.g., signature, encryption).
     */
    HS512("HS512", 7, CoseKeyType.Symmetric, DigestAlg.SHA512, 512, null, "HMAC w/ SHA-512", AlgorithmType.SIGNATURE),

    /**
     * Represents the PS256 algorithm configuration for signing using RSASSA-PSS with SHA-256.
     *
     * @property algorithmName The name of the algorithm.
     * @property algorithmId The assigned ID for the algorithm.
     * @property keyType The type of key used by the algorithm.
     * @property hashAlgorithm The hashing algorithm associated with the signing process.
     * @property keySize The size of the key in bits.
     * @property enum The designated curve for ECC algorithms.
     * @property algorithmDescription A textual description of the algorithm.
     * @property algorithmType The general type of the algorithm: SIGNATURE, ENCRYPTION, etc.
     */
    PS256(
        "PS256",
        -37,
        CoseKeyType.RSA,
        DigestAlg.SHA256,
        256,
        CoseCurve.P_256,
        "RSASSA-PSS w/ SHA-256", AlgorithmType.SIGNATURE
    ),

    /**
     * The `PS384` class represents an algorithm identifier for using the RSASSA-PSS signature scheme
     * with the SHA-384 hash function.
     *
     * @property algorithmName The name of the algorithm, which is "PS384".
     * @property algorithmId Numerical identifier for the algorithm.
     * @property keyType The key type used with this algorithm, in this case, RSA.
     * @property hashAlgorithm The hash function used, specifically SHA-384.
     * @property keyLength The key length used with the algorithm, which is 384 bits.
     * @property curve The cryptographic curve associated with this algorithm, in this case, P-384.
     * @property description A textual description of the algorithm stating "RSASSA-PSS w/ SHA-384".
     * @property algorithmType The type of algorithm, categorized under AlgorithmType.SIGNATURE.
     */
    PS384(
        "PS384",
        -38,
        CoseKeyType.RSA,
        DigestAlg.SHA384,
        384,
        CoseCurve.P_384,
        "RSASSA-PSS w/ SHA-384", AlgorithmType.SIGNATURE
    ),

    /**
     * Class representing the PS512 cryptographic algorithm.
     *
     * The PS512 algorithm is an instance of the RSASSA-PSS (RSA Signature Scheme with Appendix - Probabilistic Signature Scheme)
     * using the SHA-512 hash algorithm. It is used for generating and verifying digital signatures.
     *
     * @property name The name of the algorithm.
     * @property identifier A unique identifier for this algorithm.
     * @property keyType The key type associated with this algorithm, which is of type RSA.
     * @property hashAlgorithm The hash algorithm used, which is SHA-512.
     * @property keySize The size of the key used in this algorithm in bits (512 bits).
     * @property curve The cryptographic curve used with this algorithm.
     * @property description A human-readable description of the algorithm.
     * @property algorithmType The type of this algorithm, which is used for signature creation and verification.
     *
     * @param name The name of the algorithm.
     * @param identifier A unique identifier for this algorithm.
     * @param keyType The key type associated with this algorithm.
     * @param hashAlgorithm The hash algorithm used in the signature creation and verification.
     * @param keySize The size of the key in bits.
     * @param curve The elliptic curve used with this algorithm.
     * @param description A description of the algorithm.
     * @param algorithmType The category or type of the algorithm.
     */
    PS512(
        "PS512",
        -39,
        CoseKeyType.RSA,
        DigestAlg.SHA512,
        512,
        CoseCurve.P_521,
        "RSASSA-PSS w/ SHA-512", AlgorithmType.SIGNATURE
    ),

    /**
     * Represents the AES-GCM encryption algorithm with a 128-bit key.
     *
     * @property name The name of the algorithm.
     * @property id The unique identifier for the algorithm.
     * @property keyType The type of key used by this algorithm. In this case, it is symmetric.
     * @property hash The hash algorithm associated with this encryption algorithm, if any.
     * @property tagLength The length of the authentication tag.
     * @property curve The elliptic curve associated with this algorithm, if any.
     * @property description The description of the algorithm.
     * @property type The type of algorithm, which is encryption in this case.
     */
// Encryption
    A128GCM(
        "A128GCM",
        1,
        CoseKeyType.Symmetric,
        hash = null,
        128,
        curve = null,
        "AES-GCM mode w/ 128-bit key, 128-bit tag", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the encryption algorithm AES-GCM mode with a 192-bit key and 128-bit tag length in the COSE (CBOR Object Signing and Encryption) specification.
     *
     * @property id The algorithm identifier.
     * @property value A numerical representation associated with the algorithm.
     * @property keyType The type of key associated with this algorithm, in this case, Symmetric.
     * @property hash The hash algorithm used, which is null for this algorithm.
     * @property tagLength The length of the tag in bits, here it is 192 bits.
     * @property curve The elliptic curve used, which is null for this algorithm.
     * @property description A brief textual description of the algorithm, namely "AES-GCM mode w/ 192-bit key, 128-bit tag".
     * @property type The type of algorithm, in this case, it is an encryption algorithm.
     */
    A192GCM(
        "A192GCM",
        2,
        CoseKeyType.Symmetric,
        hash = null,
        192,
        curve = null,
        "AES-GCM mode w/ 192-bit key, 128-bit tag", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the AES-GCM encryption algorithm with a 256-bit key and a 128-bit tag.
     *
     * @property id The unique identifier of the algorithm.
     * @property value An integer constant representing the algorithm.
     * @property keyType The type of keys used with the algorithm, in this case, symmetric keys.
     * @property hash The hash algorithm associated with this encryption algorithm. This is `null` for this algorithm.
     * @property tagLength The length of the authentication tag, which is 128 bits.
     * @property curve The elliptic curve associated with the algorithm. This is `null` for this algorithm.
     * @property description A textual description of the algorithm.
     * @property type The type of the algorithm, which is encryption.
     */
    A256GCM(
        "A256GCM",
        3,
        CoseKeyType.Symmetric,
        hash = null,
        256,
        curve = null,
        "AES-GCM mode w/ 256-bit key, 128-bit tag", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the AES Key Wrap with a 128-bit key.
     *
     * @property id The algorithm identifier.
     * @property value The algorithm value.
     * @property keyType The key type used by this algorithm.
     * @property hash The hash algorithm, if applicable.
     * @property tagLength The length of the tag, if applicable.
     * @property curve The elliptic curve used, if applicable.
     * @property description A description of the algorithm.
     * @property type The type of algorithm, e.g., encryption, signature.
     */
    A128KW(
        "A128KW",
        -3,
        CoseKeyType.Symmetric,
        hash = null,
        128,
        curve = null,
        "AES Key Wrap w/ 128-bit key", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the AES Key Wrap algorithm using a 192-bit key.
     *
     * @param id The identifier for the algorithm.
     * @param value The integer value associated with the algorithm.
     * @param keyType The type of key used by the algorithm, in this case, symmetric.
     * @param hash The hash algorithm associated with the algorithm, if any.
     * @param tagLength The length of the tag, if applicable.
     * @param curve The elliptic curve used by the algorithm, if applicable.
     * @param description A textual description of the algorithm.
     * @param type The category or family to which the algorithm belongs, in this case, encryption.
     */
    A192KW(
        "A192KW",
        -4,
        CoseKeyType.Symmetric,
        hash = null,
        192,
        curve = null,
        "AES Key Wrap w/ 192-bit key", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the 256-bit AES Key Wrap (A256KW) encryption algorithm.
     *
     * @property id The name identifier of the algorithm.
     * @property value An integer representation of the algorithm.
     * @property keyType The COSE key type associated with the algorithm.
     * @property hash The hash algorithm used, if any (null in this case).
     * @property tagLength The length of the tag, unused for this algorithm.
     * @property curve The elliptic curve used, if any (null in this case).
     * @property description A brief description of the algorithm.
     * @property type The type of the algorithm, set to `AlgorithmType.ENCRYPTION`.
     */
    A256KW(
        "A256KW",
        -5,
        CoseKeyType.Symmetric,
        hash = null,
        256,
        curve = null,
        "AES Key Wrap w/ 256-bit key", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the ChaCha20/Poly1305 encryption algorithm with a 256-bit key and a 128-bit tag.
     *
     * @property id The algorithm identifier string.
     * @property value The fixed length of the IV in bytes (24 bytes).
     * @property keyType The key type associated with this algorithm, which is Symmetric in this case.
     * @property hash The hash algorithm used (if any), which is null for this algorithm.
     * @property tagLength The length of the authentication tag in bits (128 bits).
     * @property curve The elliptic curve used (if any), which is null for this algorithm.
     * @property description A brief description of the algorithm.
     * @property type Specifies that this algorithm is used for encryption.
     */
    CHACHA20_POLY1305(
        "ChaCha20/Poly1305",
        24,
        CoseKeyType.Symmetric,
        hash = null,
        128,
        curve = null,
        "ChaCha20/Poly1305 w/ 256-bit key, 128-bit tag", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the DIRECT CoseAlgorithm which specifies direct use of Content Encryption Key (CEK).
     *
     * @property id The unique identifier for the algorithm "direct".
     * @property value The integer value representing the algorithm, -6 for DIRECT.
     * @property keyType The type of key that is appropriate for this algorithm, Symmetric in this case.
     * @property hash The hash algorithm associated with this encryption, null as not applicable.
     * @property tagLength The tag length for the algorithm, null as not applicable.
     * @property curve The elliptic curve parameter, null as not applicable.
     * @property description A brief description of the algorithm.
     * @property type The type of the algorithm, ENCRYPTION in this case.
     */
    DIRECT(
        "direct",
        -6,
        CoseKeyType.Symmetric,
        hash = null,
        tagLength = null,
        curve = null,
        "Direct use of CEK", AlgorithmType.ENCRYPTION
    ),

    /**
     * HMAC256_64 is a COSE (CBOR Object Signing and Encryption) algorithm
     * defined for Hash-based Message Authentication Code (HMAC) using the
     * SHA-256 hash function, truncated to 64 bits. This algorithm is used
     * for encryption purposes and employs symmetric keys.
     *
     * @property id An identifier for the algorithm, where the value is set to "HMAC 256/64".
     * @property value A fixed integer value representing the algorithm within COSE specifications.
     * @property keyType Specifies the type of key used by the algorithm. For HMAC256_64, it is CoseKeyType.Symmetric.
     * @property hash Indicates the hash function used, which is SHA-256 for this algorithm.
     * @property tagLength Specifies the tag length of the HMAC output, truncated to 64 bits.
     * @property curve Not applicable for this algorithm and thus set to null.
     * @property description A textual description of the algorithm: "HMAC w/ SHA-256 truncated to 64 bits".
     * @property type Defines the type of the algorithm, which is set to AlgorithmType.ENCRYPTION for HMAC256_64.
     */
    HMAC256_64(
        "HMAC 256/64",
        4,
        CoseKeyType.Symmetric,
        hash = DigestAlg.SHA256,
        tagLength = 64,
        curve = null,
        "HMAC w/ SHA-256 truncated to 64 bits", AlgorithmType.ENCRYPTION
    ),

    /**
     * This class represents the HMAC256_256 algorithm, a cryptographic hash
     * function that uses SHA-256 for HMAC (Hash-based Message Authentication Code).
     * It produces a hash length of 256 bits.
     *
     * @param name The name of the algorithm.
     * @param keyLength The length of the key.
     * @param keyType The type of the key, defined by CoseKeyType, set to Symmetric.
     * @param hash The hash algorithm used, in this case SHA-256.
     * @param tagLength The length of the tag, set to 256 bits.
     * @param curve The curve used, set to null as it is not applicable.
     * @param description A description of the algorithm.
     * @param algorithmType The type of algorithm, set to ENCRYPTION.
     */
    HMAC256_256(
        "HMAC 256/256",
        5,
        CoseKeyType.Symmetric,
        hash = DigestAlg.SHA256,
        tagLength = 256,
        curve = null,
        "HMAC w/ SHA-256", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents an HMAC algorithm with the SHA-384 hash function and a tag length of 384 bits.
     *
     * @property id Descriptive identifier for the algorithm.
     * @property value Parameter indicating the value associated with the algorithm.
     * @property keyType Specifies the type of key used with this algorithm. In this case, it is a symmetric key.
     * @property hash The hash algorithm used, which is SHA-384.
     * @property tagLength Specifies the length of the authentication tag which is 384 bits.
     * @property curve The elliptic curve information, which is not applicable for this algorithm and thus set to null.
     * @property description A brief description of the algorithm, stating it as HMAC with SHA-384.
     * @property type The algorithm type which is defined as an encryption algorithm.
     */
    HMAC384_384(
        "HMAC 384/384",
        6,
        CoseKeyType.Symmetric,
        hash = DigestAlg.SHA384,
        tagLength = 384,
        curve = null,
        "HMAC w/ SHA-384", AlgorithmType.ENCRYPTION
    ),

    /**
     * Represents the HMAC 512/512 algorithm, which uses the SHA-512 hash function.
     *
     * @property algorithmName The name of the algorithm ("HMAC 512/512").
     * @property id The identifier for the algorithm (7).
     * @property keyType The type of key used (Symmetric).
     * @property hash The hash algorithm used (SHA512).
     * @property tagLength The length of the tag produced (512 bits).
     * @property curve The elliptic curve used, if any (null for symmetric algorithms).
     * @property description A brief description of the algorithm ("HMAC w/ SHA-512").
     * @property type The type of algorithm (ENCRYPTION).
     */
    HMAC512_512(
        "HMAC 512/512",
        7,
        CoseKeyType.Symmetric,
        hash = DigestAlg.SHA512,
        tagLength = 512,
        curve = null,
        "HMAC w/ SHA-512", AlgorithmType.ENCRYPTION
    )

    ;

    /**
     * Utility methods for handling COSE algorithms.
     */
    object Static {
        /**
         * Retrieves a `CoseAlgorithm` entry based on the provided integer value and optional algorithm type.
         *
         * @param value The integer value representing the algorithm.
         * @param type The optional `AlgorithmType` to filter the algorithm search; if not provided, any type will be matched.
         * @return The corresponding `CoseAlgorithm` entry if found, otherwise `null`.
         */
        @JsName("fromValue")
        fun fromValue(value: Int?, type: AlgorithmType? = null): CoseAlgorithm? {
            return CoseAlgorithm.entries.find { entry -> entry.value == value && (type === null || type === entry.type) }
        }

        /**
         * Finds a `CoseAlgorithm` based on the provided name.
         *
         * @param name The name or id of the desired `CoseAlgorithm`.
         * @return The matching `CoseAlgorithm`.
         * @throws IllegalArgumentException If no matching `CoseAlgorithm` is found.
         */
        @JsName("fromName")
        fun fromName(name: String): CoseAlgorithm =
            CoseAlgorithm.entries.find { it.id == name || it.name == name } ?: throw IllegalArgumentException("Unknown cose algorithm: $name")

        /**
         * Returns a list of `CoseAlgorithm` entries filtered by the specified `AlgorithmType`.
         *
         * @param type The optional `AlgorithmType` to filter the `CoseAlgorithm` entries. If `null`, all entries are returned.
         * @return An array of `CoseAlgorithm` entries that match the specified `AlgorithmType`.
         */
        @JsName("asList")
        fun asList(type: AlgorithmType? = null): Array<CoseAlgorithm> {
            return CoseAlgorithm.entries.filter { entry -> type === null || entry.keyType == type }.toTypedArray()
        }

    }
}


/**
 * An internal object that provides serialization and deserialization for the `CoseAlgorithm` enum class.
 */
internal object CoseAlgSerializer : KSerializer<CoseAlgorithm> {
    /**
     * The serialized descriptor for the `CoseAlgorithm` enum, represented as an integer.
     * This descriptor is utilized by Kotlin serialization to convert between the enum type and its
     * corresponding integer value during the serialization and deserialization process.
     */
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CoseAlgorithm", PrimitiveKind.INT)

    /**
     * Serializes a `CoseAlgorithm` object into its integer value using the provided encoder.
     *
     * @param encoder Encoder to which the CoseAlgorithm value will be serialized
     * @param value The CoseAlgorithm instance to be serialized
     */
    override fun serialize(encoder: Encoder, value: CoseAlgorithm) {
        encoder.encodeInt(value.value)
    }

    /**
     * Deserializes the given decoder to a CoseAlgorithm object.
     *
     * @param decoder the decoder from which to deserialize the CoseAlgorithm.
     * @return the deserialized CoseAlgorithm instance.
     * @throws IllegalArgumentException if the decoded value does not correspond to a valid CoseAlgorithm.
     */
    override fun deserialize(decoder: Decoder): CoseAlgorithm {
        val value = decoder.decodeInt()
        return CoseAlgorithm.Static.fromValue(value) ?: throw IllegalArgumentException("Invalid cose algorithm")
    }
}
