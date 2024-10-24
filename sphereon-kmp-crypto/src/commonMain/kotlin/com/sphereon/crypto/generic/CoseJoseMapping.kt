package com.sphereon.crypto.generic

import com.sphereon.cbor.CborNumber
import com.sphereon.crypto.cose.CoseAlgorithm
import com.sphereon.crypto.cose.CoseCurve
import com.sphereon.crypto.cose.CoseKeyOperations
import com.sphereon.crypto.cose.CoseKeyType
import com.sphereon.crypto.jose.JoseKeyOperations
import com.sphereon.crypto.jose.JwaAlgorithm
import com.sphereon.crypto.jose.JwaCurve
import com.sphereon.crypto.jose.JwaKeyType
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * Represents a mapping between COSE key types and JWA key types.
 */
@JsExport
sealed class KeyTypeMapping(private val coseKeyType: CoseKeyType, private val joseKeyType: JwaKeyType) {

    /**
     * Represents a specific key type mapping for Octet Key Pairs (OKP).
     *
     * This object links the COSE key type `OKP` with its corresponding JWA key type `OKP`.
     * It ensures that cryptographic operations involving OKPs are correctly mapped between
     * COSE and JWA specifications.
     */
    object OKP : KeyTypeMapping(CoseKeyType.OKP, JwaKeyType.OKP)

    /**
     * The `EC2` object is a specific instance of `KeyTypeMapping` for Elliptic Curve Keys (EC) as per COSE (CBOR Object Signing and Encryption) and JWA (JSON Web Algorithms) specifications
     * .
     *
     * This object maps the `CoseKeyType.EC2`, which represents Elliptic Curve Keys with x- and y-coordinate pairs, to the `JwaKeyType.EC`, which are used for cryptographic operations
     * .
     */
    object EC : KeyTypeMapping(CoseKeyType.EC2, JwaKeyType.EC)

    /**
     * The `RSA` object represents the RSA key type mapping between COSE (CBOR Object Signing and Encryption)
     * and JWA (JSON Web Algorithms) key types.
     *
     * This object extends `KeyTypeMapping`, providing the specific association for RSA keys:
     * - COSE key type: `CoseKeyType.RSA`
     * - JWA key type: `JwaKeyType.RSA`
     *
     * The mapping allows for interoperability between different key representation standards, ensuring
     * RSA keys can be correctly interpreted and utilized within COSE and JWA frameworks.
     */
    object RSA : KeyTypeMapping(CoseKeyType.RSA, JwaKeyType.RSA)

    /**
     * Represents a key type for the 'jose' variable, used in the context of
     * JSON Object Signing and Encryption (JOSE).
     *
     * This variable typically holds key type information which can be used
     * for cryptographic operations including signing, encryption, and more.
     * It is part of the JOSE standard, which is designed to protect the
     * integrity and confidentiality of data transmitted between parties.
     */
    val jose = joseKeyType

    /**
     * Represents the cose key type that is used within the application.
     *
     * Initializes 'cose' with the designated COSE key type.
     */
    val cose = coseKeyType

    /**
     * The `Static` object provides utility functions for converting between COSE and JOSE key types.
     */
    object Static {
        /**
         * Immutable list containing the key types for COSE: `OKP`, `EC2`, and `RSA`.
         *
         * This list is used to map between COSE and JOSE key types.
         *
         * Each element in the list represents a specific key type used in cryptographic operations.
         */
        val asList = listOf(OKP, EC, RSA)

        /**
         * Converts a given COSE key type to the corresponding JOSE key type.
         *
         * @param cose The `CoseKeyType` object that needs to be converted to a JOSE key type.
         * @throws IllegalArgumentException If the provided `CoseKeyType` does not have a corresponding JOSE key type.
         */
        fun toJose(cose: CoseKeyType) = asList.find { it.coseKeyType === cose }?.joseKeyType
            ?: throw IllegalArgumentException("coseKeyType $cose not found")

        /**
         * Converts a given JWA key type to its corresponding COSE key type.
         *
         * @param jose The JwaKeyType representing the JSON Web Algorithm key type.
         * @return The corresponding CoseKeyType.
         * @throws IllegalArgumentException If the provided JWA key type cannot be found.
         */
        fun toCose(jose: JwaKeyType) = asList.find { it.joseKeyType === jose }?.coseKeyType
            ?: throw IllegalArgumentException("joseKeyType $jose not found")

        /**
         * Finds the corresponding `KeyTypeMapping` for the given `jose` key type.
         *
         * @param jose The `JwaKeyType` to find the corresponding `KeyTypeMapping`.
         * @throws IllegalArgumentException if the given `jose` key type*/
        fun fromJose(jose: JwaKeyType) = asList.find { it.joseKeyType === jose } ?: throw IllegalArgumentException("joseKeyType $jose not found")

        /**
         * Converts a given CoseKeyType to a specific instance if found in the list.
         *
         * @param cose the CoseKeyType to be converted.
         * @throws IllegalArgumentException if the specified CoseKeyType is not found in the list.
         */
        fun fromCose(cose: CoseKeyType) = asList.find { it.coseKeyType === cose } ?: throw IllegalArgumentException("coseKeyType $cose not found")

        /**
         * Converts a given key type (kty) to a corresponding JSON Web Algorithm (JWA) key type.
         *
         * @param kty any key type to be converted; it can be of type String, Int, CborNumber, CoseKeyType, or JwaKeyType
         * @return the converted JwaKeyType
         * @throws IllegalArgumentException if the given kty cannot be converted to a JwaKeyType
         */
        fun toJoseKty(kty: Any): JwaKeyType {
            return when (kty) {
                is String -> JwaKeyType.Static.fromValue(kty)
                is Int -> toJoseKtyFromCose(kty)
                is CborNumber<*> -> toJoseKtyFromCose(kty.value.toInt())
                is CoseKeyType -> toJose(kty)
                is JwaKeyType -> kty
                else -> throw IllegalArgumentException("Could not convert kty value $kty to Jose")
            }
        }

        /**
         * Converts the given key type to a CoseKeyType.
         *
         * @param kty the key type to be converted. It can be of type String, Int, CborNumber, CoseKeyType, or JwaKeyType.
         * @return the corresponding CoseKeyType.
         * @throws IllegalArgumentException if the key type cannot be converted.
         */
        fun toCoseKty(kty: Any): CoseKeyType {
            return when (kty) {
                is String -> toCoseKtyFromJose(kty)
                is Int -> CoseKeyType.Static.fromValue(kty)
                is CborNumber<*> -> CoseKeyType.Static.fromValue(kty.value.toInt())
                is CoseKeyType -> return kty
                is JwaKeyType -> return toCose(kty)
                else -> throw IllegalArgumentException("Could not convert kty value $kty to Jose")
            }
        }

        /**
         * Converts a COSE key type value to a JOSE key type.
         *
         * @param kty An integer representing the COSE key type.
         * @return The equivalent JOSE key type.
         * @throws IllegalArgumentException If the COSE key type is unknown.
         */
        fun toJoseKtyFromCose(kty: Int) = toJose(CoseKeyType.Static.fromValue(kty))

        /**
         * Converts a JOSE key type (represented as a string) to the corresponding COSE key type.
         *
         * @param kty The JOSE key type as a string. This should match the values defined in the JwaKeyType enum.
         * @throws IllegalArgumentException If the provided `kty` is not a valid JOSE key type.
         */
        fun toCoseKtyFromJose(kty: String) = toCose(JwaKeyType.Static.fromValue(kty))
    }
}

/**
 * Converts the COSE key type (CoseKeyType) to the corresponding JOSE key type (JwaKeyType).
 *
 * This function utilizes the static mapping defined in KeyTypeMapping to find the equivalent JOSE key type
 * for the provided COSE key type. If the COSE key type does not have an associated JOSE key type in the
 * mapping, an IllegalArgumentException is thrown.
 *
 * @receiver The COSE key type to be converted.
 * @return The corresponding JOSE key type.
 * @throws IllegalArgumentException If the COSE key type cannot be mapped to a JOSE key type.
 */
@JsExport
fun CoseKeyType.toJoseKeyType() = KeyTypeMapping.Static.toJose(this)

/**
 * Converts a `JwaKeyType` instance to the corresponding `CoseKeyType`.
 *
 * Uses the static mapping from `KeyTypeMapping` to find the appropriate
 * `CoseKeyType` for the given `JwaKeyType`.
 *
 * @return The associated `CoseKeyType`.
 * @throws IllegalArgumentException if no corresponding `CoseKeyType` is found.
 */
@JsExport
fun JwaKeyType.toCoseKeyType() = KeyTypeMapping.Static.toCose(this)



/**
 * Represents a Signature Algorithm with various algorithm properties and identifiers.
 *
 * @property coseAlgorithm The COSE (CBOR Object Signing and Encryption) algorithm identifier.
 * @property joseAlgorithm The JOSE (JSON Object Signing and Encryption) algorithm identifier.
 * @property cryptoAlgorithm The cryptographic algorithm used for the signature.
 * @property digestAlgorithm The hash algorithm used in the signature process.
 * @property maskGenFunction The mask generation function algorithm.
 */
@JsExport
@Serializable
sealed class SignatureAlgorithm(
    private val coseAlgorithm: CoseAlgorithm? = null, // we expose this as cose
    private val joseAlgorithm: JwaAlgorithm? = null, // we expose this as jose
    val cryptoAlgorithm: CryptoAlg,
    val digestAlgorithm: DigestAlg? = null,
    val maskGenFunction: MaskGenFunction? = null
) {
    /**
     * The `EdDSA` object represents an Elliptic Curve signature scheme using the Edwards-curve Digital Signature Algorithm.
     * It extends the `AlgorithmMapping` class, mapping the COSE (CBOR Object Signing and Encryption) algorithm 'EdDSA'
     * to the JWA (JSON Web Algorithms) algorithm 'EdDSA'.
     *
     * This class is used for cryptographic operations involving the EdDSA signature scheme, enabling interoperability
     * between different cryptographic frameworks and standards that support EdDSA.
     */
    object ED25519 : SignatureAlgorithm(CoseAlgorithm.EdDSA, JwaAlgorithm.EdDSA, cryptoAlgorithm = CryptoAlg.ED25519)

    /**
     * Represents the ECDSA algorithm with SHA-256 hashing.
     * It utilizes the P-256 curve for elliptic curve operations.
     * This algorithm is used for digital signatures and is mapped to the
     * COSE (CBOR Object Signing and Encryption) algorithm identifier -7 and the corresponding JWA (JSON Web Algorithm) identifier ES256.
     */
    object ECDSA_SHA256 : SignatureAlgorithm(CoseAlgorithm.ES256, JwaAlgorithm.ES256, cryptoAlgorithm = CryptoAlg.ECDSA)

    /**
     * Represents the ECDSA with SHA-384 algorithm mapping.
     *
     * This object maps the COSE algorithm ES384 to the corresponding JWA algorithm.
     *
     * COSE Algorithm: ES384
     * JWA Algorithm: ES384
     */
    object ECDSA_SHA384 : SignatureAlgorithm(CoseAlgorithm.ES384, JwaAlgorithm.ES384, cryptoAlgorithm = CryptoAlg.ECDSA)

    /**
     * Object representing the ES512 algorithm mapping.
     *
     * This object maps the COSE ES512 algorithm to the JWA ES512 algorithm.
     *
     * @see CoseAlgorithm.ES512
     * @see JwaAlgorithm.ES512
     */
    object ECDSA_SHA512 : SignatureAlgorithm(CoseAlgorithm.ES512, JwaAlgorithm.ES512, cryptoAlgorithm = CryptoAlg.ECDSA)

    /**
     * An object that maps the COSE algorithm ES256K to the JWA algorithm ES256K.
     *
     * This object is a predefined instance of the AlgorithmMapping class that represents the
     * ECDSA secp256k1 curve with SHA-256 hashing algorithm. This is commonly used for digital
     * signatures in environments where both COSE (CBOR Object Signing and Encryption) and JWA
     * (JSON Web Algorithms) standards are supported.
     */
    object ES256K : SignatureAlgorithm(CoseAlgorithm.ES256K, JwaAlgorithm.ES256K, cryptoAlgorithm = CryptoAlg.ECDSA, TODO("Curve"))

    /**
     * This object represents the HS256 algorithm, which is a specific type of HMAC utilizing SHA-256.
     *
     * It extends the AlgorithmMapping class by mapping the COSE algorithm `CoseAlgorithm.HS256`
     * to the JWA algorithm `JwaAlgorithm.HS256`. This class can be used to handle cryptographic
     * operations that require HMAC with SHA-256.
     */
    object HMAC_SHA256 : SignatureAlgorithm(CoseAlgorithm.HS256, JwaAlgorithm.HS256, cryptoAlgorithm = CryptoAlg.HMAC)

    /**
     * HS384 object represents the algorithm mapping configuration for the HMAC with SHA-384 signature algorithm.
     * It extends the AlgorithmMapping class and links the COSE and JWA algorithm identifiers for HS384.
     */
    object HMAC_SHA384 : SignatureAlgorithm(CoseAlgorithm.HS384, JwaAlgorithm.HS384, cryptoAlgorithm = CryptoAlg.HMAC)

    /**
     * An object that provides a mapping between COSE and JOSE algorithms for the HS512 (HMAC with SHA-512) algorithm.
     *
     * This object is used to map the COSE algorithm identifier `CoseAlgorithm.HS512` to the
     * corresponding JOSE algorithm identifier `JwaAlgorithm.HS512`.
     */
    object HMAC_SHA512 : SignatureAlgorithm(CoseAlgorithm.HS512, JwaAlgorithm.HS512, cryptoAlgorithm = CryptoAlg.HMAC)

    /**
     * Object PS256 represents an algorithm mapping for the PS256 algorithm.
     * It extends the `AlgorithmMapping` class by associating COSE and JWA algorithm identifiers.
     *
     * @constructor
     * Initializes the algorithm mapping for the PS256 algorithm.
     *
     * @property coseAlgorithm
     * Identifier for the COSE algorithm.
     *
     * @property jwaAlgorithm
     * Identifier for the JWA algorithm.
     */
    object RSA_SSA_PSS_SHA256_MGF1 : SignatureAlgorithm(CoseAlgorithm.PS256, JwaAlgorithm.PS256, cryptoAlgorithm = CryptoAlg.RSA, digestAlgorithm = DigestAlg.SHA256, maskGenFunction = MaskGenFunction.MGF1)

    /**
     * PS384 object represents an algorithm mapping specifically for PS384 algorithm.
     *
     * It extends AlgorithmMapping class with the parameters:
     * - CoseAlgorithm.PS384
     * - JwaAlgorithm.PS384
     *
     * This class maps the PS384 algorithm supported by COSE (RFC 8152) to the PS384 algorithm
     * recognized by JOSE (RFC 7518).
     */
    object RSA_SSA_PSS_SHA384_MGF1 : SignatureAlgorithm(CoseAlgorithm.PS384, JwaAlgorithm.PS384, cryptoAlgorithm = CryptoAlg.RSA, digestAlgorithm = DigestAlg.SHA384, maskGenFunction = MaskGenFunction.MGF1)

    /**
     * Represents the RSASSA-PSS signature algorithm using SHA-512 hashing.
     *
     * Maps the COSE algorithm identifier for RSASSA-PSS with SHA-512 to the corresponding JWA algorithm.
     * Primarily used in contexts requiring RSASSA-PSS signature with SHA-512 as specified by COSE and JOSE standards.
     */
    object RSA_SSA_PSS_SHA512_MGF1 : SignatureAlgorithm(CoseAlgorithm.PS512, JwaAlgorithm.PS512, cryptoAlgorithm = CryptoAlg.RSA, digestAlgorithm = DigestAlg.SHA512, maskGenFunction = MaskGenFunction.MGF1)


    object RSA_RAW : SignatureAlgorithm(cryptoAlgorithm = CryptoAlg.RSA)
    object RSA_SSA_PSS_RAW_MGF1: SignatureAlgorithm(cryptoAlgorithm = CryptoAlg.RSA, maskGenFunction= MaskGenFunction.MGF1)

    object RSA_SHA256: SignatureAlgorithm(coseAlgorithm = null /*TODO*/ , joseAlgorithm = JwaAlgorithm.RS256, cryptoAlgorithm = CryptoAlg.RSA, digestAlgorithm = DigestAlg.SHA256)
    object RSA_SHA384: SignatureAlgorithm(coseAlgorithm = null /*TODO*/ , joseAlgorithm = JwaAlgorithm.RS384, cryptoAlgorithm = CryptoAlg.RSA, digestAlgorithm = DigestAlg.SHA384)
    object RSA_SHA512: SignatureAlgorithm(coseAlgorithm = null /*TODO*/ , joseAlgorithm = JwaAlgorithm.RS512, cryptoAlgorithm = CryptoAlg.RSA, digestAlgorithm = DigestAlg.SHA512)

/*
    RSA_SHA3_256(CryptoAlg.RSA, DigestAlg.SHA3_256),
    RSA_SHA3_512(CryptoAlg.RSA, DigestAlg.SHA3_512),
    RSA_SSA_PSS_SHA3_256_MGF1(CryptoAlg.RSA, DigestAlg.SHA3_256, MaskGenFunction.MGF1),
    RSA_SSA_PSS_SHA3_512_MGF1(CryptoAlg.RSA, DigestAlg.SHA3_512, MaskGenFunction.MGF1),
*/

    /**
     * Holds the instance of the `joseAlgorithm` used for cryptographic operations.
     *
     * This variable is typically used to sign or verify tokens, ensuring the integrity
     * and authenticity of the transmitted data. The `joseAlgorithm` usually conforms
     * to the JSON Object Signing and Encryption (JOSE) standard, which specifies methods
     * for encryption, digital signatures, and other cryptographic processes.
     *
     * Ensure that the `joseAlgorithm` instance assigned to this variable is properly
     * configured and adheres to the security requirements of the application.
     */
    val jose = joseAlgorithm

    /**
     * The `cose` variable holds the value of the COSE (CBOR Object Signing and Encryption) algorithm.
     * This algorithm is used to perform cryptographic operations such as signing and encryption
     * on CBOR (Concise Binary Object Representation) encoded data.
     *
     * COSE provides a compact and efficient method for processing cryptographic data
     * in constrained environments.
     */
    val cose = coseAlgorithm

    /**
     * The Static object provides utility functions for converting between JOSE and COSE algorithms.
     */
    object Static {
        /**
         * A list of supported algorithm mappings used for COSE and JOSE algorithm conversions.
         *
         * This list includes various algorithms such as EdDSA, ES256K, ES256, ES384, ES512, HS256, HS384, HS512, PS256, PS384, and PS512.
         * It is utilized by functions to map between different cryptographic algorithm standards.
         */
        val asList = listOf(ED25519, ES256K, ECDSA_SHA256, ECDSA_SHA384, ECDSA_SHA512, HMAC_SHA256, HMAC_SHA384, HMAC_SHA512, RSA_SSA_PSS_SHA256_MGF1, RSA_SSA_PSS_SHA384_MGF1, RSA_SSA_PSS_SHA512_MGF1)

        /**
         * Converts a given COSE algorithm to its corresponding JOSE algorithm.
         *
         * @param cose The COSE algorithm to be converted.
         * @throws IllegalArgumentException if the given COSE algorithm does not have a corresponding JOSE algorithm.
         */
        fun toJose(cose: CoseAlgorithm?) = fromCose(cose).joseAlgorithm!!

        /**
         * Converts a given JWA algorithm to the corresponding COSE algorithm.
         *
         * @param jose The JWA algorithm to be converted.
         * @throws IllegalArgumentException if the provided JWA algorithm is not found in the mapping.
         * @return The corresponding COSE algorithm.
         */
        fun toCose(jose: JwaAlgorithm?) = fromJose(jose).coseAlgorithm!!

        /**
         * Retrieves the algorithm mapping matching the given JSON Web Algorithm (JWA) algorithm.
         *
         * @param jose The JWA algorithm to match.
         * @return The corresponding algorithm mapping, or null if not found.
         */
        fun fromJose(jose: JwaAlgorithm?) = asList.find { it.joseAlgorithm == jose } ?: throw IllegalArgumentException("jose alg $jose not found")

        /**
         * Converts a given COSE algorithm to its corresponding internal representation.
         *
         * @param cose the COSE algorithm to be converted.
         * @return the internal representation of the given COSE algorithm if found, null otherwise.
         */
        fun fromCose(cose: CoseAlgorithm?) = asList.find { it.coseAlgorithm == cose }  ?: throw IllegalArgumentException("cose alg $cose not found")

        /**
         * Converts a provided algorithm representation to a `JwaAlgorithm`.
         *
         * @param alg The algorithm value to be converted. It can be of type `String`, `Int`, `CborNumber`, `CoseAlgorithm`, or `JwaAlgorithm`.
         * @return The corresponding `JwaAlgorithm`.
         * @throws IllegalArgumentException if the provided algorithm cannot be converted to a `JwaAlgorithm`.
         */
        fun toJoseAlg(alg: Any?): JwaAlgorithm {
            return when (alg) {
                is String -> JwaAlgorithm.Static.fromValue(alg)
                    ?: throw IllegalArgumentException("cose alg $alg could not be converted to jose algorithm")

                is Int -> toJoseAlgFromCose(alg)
                is CborNumber<*> -> toJoseAlgFromCose(alg.value.toInt())
                is CoseAlgorithm -> toJose(alg)
                is JwaAlgorithm -> alg
                else -> throw IllegalArgumentException("Could not convert alg value $alg to Jose")
            }
        }

        /**
         * Converts a given algorithm representation to a `CoseAlgorithm`.
         *
         * @param alg The algorithm to be converted. It can be of type `String`, `Int`, `CborNumber`, `CoseAlgorithm`, or `JwaAlgorithm`.
         * @return The corresponding `CoseAlgorithm` for the provided algorithm representation.
         * @throws IllegalArgumentException If the algorithm cannot be converted to a `CoseAlgorithm`.
         */
        fun toCoseAlg(alg: Any?): CoseAlgorithm {
            return when (alg) {
                is String -> toCoseAlgFromJose(alg)
                is Int -> CoseAlgorithm.Static.fromValue(alg)
                    ?: throw IllegalArgumentException("coseAlgorithm $alg could not be converted to jose algorithm")

                is CborNumber<*> -> CoseAlgorithm.Static.fromValue(alg.value.toInt())
                    ?: throw IllegalArgumentException("coseAlgorithm $alg could not be converted to jose algorithm")

                is CoseAlgorithm -> return alg
                is JwaAlgorithm -> return toCose(alg)
                else -> throw IllegalArgumentException("Could not convert kty value $alg to Jose")
            }
        }

        /**
         * Converts a COSE (CBOR Object Signing and Encryption) algorithm identifier to the corresponding JOSE (JSON Object Signing and Encryption) algorithm.
         *
         * @param algorithm The integer value representing a COSE algorithm.
         * @throws IllegalArgumentException If the COSE algorithm identifier is not recognized.
         * @return The equivalent JOSE algorithm.
         */
        fun toJoseAlgFromCose(algorithm: Int?) =
            toJose(CoseAlgorithm.Static.fromValue(algorithm) ?: throw IllegalArgumentException("coseKeyType $algorithm not found"))

        /**
         * Converts a JOSE algorithm name to its corresponding COSE algorithm.
         *
         * @param algorithm The name of the JOSE algorithm to be converted.
         * @throws IllegalArgumentException If the provided JOSE algorithm name is not found.
         * @return The corresponding COSE algorithm.
         */
        fun toCoseAlgFromJose(algorithm: String?): CoseAlgorithm =
            toCose(JwaAlgorithm.Static.fromValue(algorithm) ?: throw IllegalArgumentException("joseKeyType $algorithm not found"))

    }


}

/**
 * Converts the CoseAlgorithm to its corresponding JOSE (JSON Object Signing and Encryption)
 * signature algorithm representation.
 *
 * @receiver The COSE (CBOR Object Signing and Encryption) algorithm to be converted.
 * @return The corresponding JOSE signature algorithm as defined in the AlgorithmMapping.
 */
@JsExport
fun CoseAlgorithm.toJoseSignatureAlgorithm() = SignatureAlgorithm.Static.toJose(this)

/**
 * Converts a JWA (JSON Web Algorithm) algorithm to a COSE (CBOR Object Signing and Encryption) algorithm.
 *
 * This function utilizes a static mapping to retrieve the corresponding COSE algorithm for the provided JWA algorithm.
 *
 * @receiver The JWA algorithm to be converted.
 * @return The corresponding COSE algorithm.
 * @throws IllegalArgumentException if no corresponding COSE algorithm is found.
 */
@JsExport
fun JwaAlgorithm.toCoseAlgorithm() = SignatureAlgorithm.Static.toCose(this)

// TODO Enc algos

/**
 * Represents a mapping of COSE and JOSE elliptic curves for cryptography.
 *
 * @property coseCurve The COSE curve associated with the mapping.
 * @property joseCurve The JOSE curve associated with the mapping.
 */
@JsExport
sealed class CurveMapping(
    private val coseCurve: CoseCurve,
    private val joseCurve: JwaCurve
) {
    /**
     * Object representation of P-256 elliptic curve.
     *
     * This object maps the P-256 elliptic curve to its corresponding COSE and JWA curve identifiers.
     * It extends the CurveMapping class using P-256 values from both COSE and JWA curve enumerations.
     */
    object P_256 : CurveMapping(CoseCurve.P_256, JwaCurve.P_256)

    /**
     * Represents the P-384 elliptic curve mapping between COSE (CBOR Object Signing and Encryption) and JOSE (JSON Object Signing and Encryption).
     */
    object P_384 : CurveMapping(CoseCurve.P_384, JwaCurve.P_384)

    /**
     * Object representing the P-521 elliptic curve.
     *
     * This object is a part of the CurveMapping sealed class and specifies
     * the mapping for the P-521 curve corresponding to both COSE (CBOR Object Signing and
     * Encryption) and JWA (JSON Web Algorithms).
     *
     * @see CurveMapping
     * @see CoseCurve
     * @see JwaCurve
     */
    object P_521 : CurveMapping(CoseCurve.P_521, JwaCurve.P_521)

    /**
     * Represents the Secp256k1 elliptic curve mapping used in various cryptographic standards.
     *
     * This object associates the COSE curve Secp256k1 with the JOSE curve Secp256k1.
     *
     * The Secp256k1 curve is widely used in cryptocurrencies and decentralized applications.
     */
    object Secp256k1 : CurveMapping(CoseCurve.secp256k1, JwaCurve.Secp256k1)

    /**
     * The `Ed25519` object represents the Ed25519 elliptic curve mapping.
     *
     * This object is part of the `CurveMapping` hierarchy and it specifically maps the COSE curve `Ed25519` to
     * the corresponding JWA curve `Ed25519`.
     */
    object Ed25519 : CurveMapping(CoseCurve.Ed25519, JwaCurve.Ed25519)

    /**
     * Represents the X25519 curve mapping for both COSE and JWA standards.
     * This object is used to map the X25519 curve within the `CurveMapping` sealed class.
     */
    object X25519 : CurveMapping(CoseCurve.X25519, JwaCurve.X25519)

    /**
     * Represents the JWA curve associated with the specific CurveMapping instance.
     */
    val jose = joseCurve

    /**
     * Represents the COSE curve associated with this instance of `CurveMapping`.
     *
     * The COSE (CBOR Object Signing and Encryption) curve is an elliptic curve
     * cryptography parameter defined by the COSE standard.
     */
    val cose = coseCurve

    /**
     * Object that provides static methods and properties for mapping
     * between CoseCurve and JwaCurve.
     */
    object Static {
        /**
         * A list containing instances of various elliptic curve mappings used for cryptographic operations.
         * This list includes the following curves:
         * - P_256
         * - P_384
         * - P_521
         * - Secp256k1
         * - Ed25519
         * - X25519
         *
         * The list is used for performing lookups and conversions between COSE and JOSE curve representations.
         */
        val asList = listOf(P_256, P_384, P_521, Secp256k1, Ed25519, X25519)

        fun fromJose(jose: JwaCurve?) = asList.find { it.joseCurve == jose } ?: throw IllegalArgumentException("jose curve $jose not found")

        fun fromCose(cose: CoseCurve?) = asList.find { it.coseCurve == cose } ?: throw IllegalArgumentException("cose curve $cose not found")

        /**
         * Converts a given COSE curve to its corresponding JOSE curve.
         *
         * @param cose The COSE curve that needs to be converted.
         * @throws IllegalArgumentException if the provided COSE curve is not found.
         * @return The corresponding JOSE curve.
         */
        fun toJose(cose: CoseCurve) = fromCose(cose).joseCurve

        /**
         * Converts a JwaCurve to its corresponding CoseCurve.
         *
         * @param jose The JwaCurve to be converted.
         * @throws IllegalArgumentException if the specified jose curve is not found.
         * @return The corresponding CoseCurve.
         */
        fun toCose(jose: JwaCurve) = fromJose(jose).coseCurve

    }
}

/**
 * Maps the current `CoseCurve` to its corresponding `JwaCurve`.
 *
 * This function utilizes the `CurveMapping.Static.toJose` method to find the
 * equivalent `JwaCurve` for the `CoseCurve` instance it is called on.
 *
 * @receiver The `CoseCurve` instance to be mapped to a `JwaCurve`.
 * @return The corresponding `JwaCurve` instance.
 * @throws IllegalArgumentException If the `CoseCurve` instance does not have a
 * corresponding `JwaCurve`.
 */
@JsExport
fun CoseCurve.toJoseCurve() = CurveMapping.Static.toJose(this)

/**
 * Converts the JWA elliptic curve identifier to its corresponding COSE curve identifier.
 *
 * This extension function uses the `CurveMapping.Static.toCose` method to perform the
 * conversion. If the conversion is unsuccessful, an IllegalArgumentException is thrown.
 *
 * @return the corresponding COSE curve identifier
 * @throws IllegalArgumentException if the JWA curve identifier is not found in the mapping
 */
@JsExport
fun JwaCurve.toCoseCurve() = CurveMapping.Static.toCose(this)


/**
 * A sealed class representing the mapping of COSE key operations to JOSE key operations.
 *
 * @property coseKeyOperations The COSE key operation associated with this mapping.
 * @property joseKeyOperations The JOSE key operation associated with this mapping.
 */
@JsExport
sealed class KeyOperationsMapping(
    private val coseKeyOperations: CoseKeyOperations,
    private val joseKeyOperations: JoseKeyOperations
) {
    /**
     * Represents the key operation for key wrap encryption within the context of COSE (CBOR Object Signing and Encryption)
     * and JOSE (JSON Object Signing and Encryption) standards.
     *
     * This singleton object maps the COSE key operation for wrapping keys (`CoseKeyOperations.WRAP_KEY`)
     * to the corresponding JOSE key operation (`JoseKeyOperations.WRAP_KEY`).
     */
    object WRAP_KEY : KeyOperationsMapping(CoseKeyOperations.WRAP_KEY, JoseKeyOperations.WRAP_KEY)

    /**
     * Object `DERIVE_KEY` represents a key operation for deriving keys.
     * It is a mapping between COSE and JOSE key operations that indicate
     * the key is used for deriving other keys. Requires private key fields.
     */
    object DERIVE_KEY : KeyOperationsMapping(CoseKeyOperations.DERIVE_KEY, JoseKeyOperations.DERIVE_KEY)

    /**
     * Represents a specific key operation for unwrapping a key as defined in COSE and JOSE standards.
     *
     * It maps the `UNWRAP_KEY` operation from COSE (`CoseKeyOperations.UNWRAP_KEY`) and JOSE (`JoseKeyOperations.UNWRAP_KEY`).
     *
     * The key is used for key wrap decryption and requires private key fields.
     */
    object UNWRAP_KEY : KeyOperationsMapping(CoseKeyOperations.UNWRAP_KEY, JoseKeyOperations.UNWRAP_KEY)

    /**
     * Represents the mapping for the signing key operation.
     * This object associates the COSE and JOSE key operations used for signing.
     */
    object SIGN : KeyOperationsMapping(CoseKeyOperations.SIGN, JoseKeyOperations.SIGN)

    /**
     * Represents a cryptographic operation for verification of signatures.
     *
     * This object is part of the KeyOperationsMapping class and maps the COSE key operation VERIFY
     * with the corresponding JOSE key operation VERIFY. It is used to specify that a key is intended
     * for verifying cryptographic signatures.
     */
    object VERIFY : KeyOperationsMapping(CoseKeyOperations.VERIFY, JoseKeyOperations.VERIFY)

    /**
     * This object represents the key operation for decryption.
     * It maps the DECRYPT operation defined in both COSE and JOSE standards.
     */
    object DECRYPT : KeyOperationsMapping(CoseKeyOperations.DECRYPT, JoseKeyOperations.DECRYPT)

    /**
     * DERIVE_BITS is an object that maps the COSE key operation `DERIVE_BITS` to the equivalent JOSE key operation.
     * COSE (CBOR Object Signing and Encryption) and JOSE (JSON Object Signing and Encryption) are frameworks for object security,
     * and they define sets of operations that can be performed with keys.
     *
     * The `DERIVE_BITS` operation is used for deriving bits that are not intended to be used directly as a cryptographic key.
     * It requires private key fields to be present in the key object.
     *
     * This object is a specific instance of the `KeyOperationsMapping` class, which is used to map COSE key operations to JOSE key operations.
     * By providing this mapping, it's easier to ensure compatibility between the two frameworks.
     */
    object DERIVE_BITS : KeyOperationsMapping(CoseKeyOperations.DERIVE_BITS, JoseKeyOperations.DERIVE_BITS)

    /**
     * Provides an object for the ENCRYPT key operation in both COSE and JOSE contexts.
     *
     * This object enables the key to be used for key transport encryption as specified
     * in both COSE (CBOR Object Signing and Encryption) and JOSE (JSON Object Signing and Encryption).
     *
     * In COSE, the usage is defined by the `CoseKeyOperations.ENCRYPT` enumeration value.
     * In JOSE, the usage is defined by the `JoseKeyOperations.ENCRYPT` enumeration value.
     */
    object ENCRYPT : KeyOperationsMapping(CoseKeyOperations.ENCRYPT, JoseKeyOperations.ENCRYPT)

    /**
     * The `MAC_CREATE` object represents a mapping for the MAC creation operation in both COSE and JOSE contexts.
     *
     * This object is used for the creation of Message Authentication Codes (MACs).
     *
     * It maps the COSE key operation `CoseKeyOperations.MAC_CREATE` to the corresponding JOSE key operation `JoseKeyOperations.MAC_CREATE`.
     */
    object MAC_CREATE : KeyOperationsMapping(CoseKeyOperations.MAC_CREATE, JoseKeyOperations.MAC_CREATE)

    /**
     * Represents the MAC verification key operation.
     * Maps the COSE key operation "MAC verify" to the JOSE key operation "MAC verify".
     */
    object MAC_VERIFY : KeyOperationsMapping(CoseKeyOperations.MAC_VERIFY, JoseKeyOperations.MAC_VERIFY)

    /**
     * jose represents the set of operations defined under the JSON Web Key (JWK) standard.
     * These operations include actions such as signing, verifying, encrypting, decrypting,
     * wrapping keys, unwrapping keys, deriving keys, deriving bits, creating MACs, and
     * verifying MACs.
     */
    val jose = joseKeyOperations

    /**
     * An instance of [CoseKeyOperations] which specifies the operations
     * permitted to be performed using the key.
     */
    val cose = coseKeyOperations

    /**
     * An object that provides mappings between JOSE and COSE key operations.
     */
    object Static {
        /**
         * A list containing various key operations that can be performed.
         *
         * The available operations include:
         * - WRAP_KEY: Key wrap encryption.
         * - DERIVE_KEY: Deriving keys, requires private key fields.
         * - DERIVE_BITS: Deriving bits not used as a key, requires private key fields.
         * - UNWRAP_KEY: Key wrap decryption, requires private key fields.
         * - SIGN: Creating signatures, requires private key fields.
         * - VERIFY: Verification of signatures.
         * - DECRYPT: Key transport decryption.
         * - ENCRYPT: Key transport encryption.
         * - MAC_CREATE: Creating MACs.
         * - MAC_VERIFY: Validating MACs.
         *
         * This list is used to map between different key operation representations.
         */
        val asList = listOf(
            WRAP_KEY,
            DERIVE_KEY,
            DERIVE_BITS,
            UNWRAP_KEY,
            SIGN,
            VERIFY,
            DECRYPT,
            ENCRYPT,
            MAC_CREATE,
            MAC_VERIFY
        )

        /**
         * Maps a `JoseKeyOperations` enum value to its corresponding `KeyOperationsMapping` instance.
         *
         * @param jose The `JoseKeyOperations` enum value to map.
         * @return The corresponding `KeyOperationsMapping` instance.
         * @throws IllegalArgumentException if the provided `JoseKeyOperations` value does not map to any `KeyOperationsMapping` instance.
         */
        fun fromJose(jose: JoseKeyOperations) =
            asList.find { it.joseKeyOperations === jose } ?: throw IllegalArgumentException("Illegal key operation $jose")

        /**
         * Converts a COSE key operation to its corresponding KeyOperation object.
         *
         * @param cose The COSE key operation to be converted.
         * @throws IllegalArgumentException if the provided COSE key operation is not found in the list.
         * @return The corresponding KeyOperation object.
         */
        fun fromCose(cose: CoseKeyOperations) =
            asList.find { it.coseKeyOperations === cose } ?: throw IllegalArgumentException("Illegal key operation $cose")

        /**
         * Converts a given COSE key operation to its equivalent JOSE key operation.
         *
         * @param cose The COSE key operation to be converted.
         * @throws IllegalArgumentException if the corresponding JOSE key operation is not found.
         * @return The equivalent JOSE key operation.
         */
        fun toJose(cose: CoseKeyOperations) = asList.find { it.coseKeyOperations === cose }?.joseKeyOperations
            ?: throw IllegalArgumentException("cose Curve $cose not found")

        /**
         * Converts a given JOSE key operation to its corresponding COSE key operation.
         *
         * @param jose The JOSE key operation to be converted.
         * @return The corresponding COSE key operation.
         * @throws IllegalArgumentException if the specified JOSE key operation is not found.
         */
        fun toCose(jose: JoseKeyOperations) = asList.find { it.joseKeyOperations === jose }?.coseKeyOperations
            ?: throw IllegalArgumentException("jose Curve $jose not found")
    }
}

/**
 * Converts a `CoseKeyOperations` enum to a corresponding `JoseKeyOperations` enum.
 *
 * This function utilizes a static mapping defined in the `KeyOperationsMapping` class
 * to transform the COSE key operation into a JOSE key operation.
 *
 * @receiver The `CoseKeyOperations` instance to be converted.
 * @return The corresponding `JoseKeyOperations` instance.
 * @throws IllegalArgumentException if the COSE key operation cannot be mapped to a JOSE key operation.
 */
@JsExport
fun CoseKeyOperations.toJoseKeyOperations() = KeyOperationsMapping.Static.toJose(this)

/**
 * Converts a `JoseKeyOperations` enum instance to its corresponding `CoseKeyOperations` enum instance.
 *
 * This method maps JOSE key operations like signing, verifying, encrypting,
 * decrypting, wrapping keys, unwrapping keys, deriving keys, deriving bits,
 * creating MACs, and verifying MACs to their COSE counterparts.
 *
 * @receiver the `JoseKeyOperations` instance to be converted.
 * @return the corresponding `CoseKeyOperations` instance.
 * @throws IllegalArgumentException if the JOSE key operation cannot be mapped to a COSE key operation.
 */
@JsExport
fun JoseKeyOperations.toCoseKeyOperations() = KeyOperationsMapping.Static.toCose(this)
