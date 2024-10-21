package com.sphereon.crypto.jose

import com.sphereon.crypto.AlgorithmMapping
import com.sphereon.crypto.cose.CoseKeyCbor
import com.sphereon.crypto.cose.CoseKeyJson
import com.sphereon.crypto.cose.ICoseKeyCbor
import com.sphereon.crypto.cose.ICoseKeyJson
import com.sphereon.crypto.IKey
import com.sphereon.crypto.KeyOperationsMapping
import com.sphereon.crypto.KeyTypeMapping
import com.sphereon.crypto.toCoseCurve
import com.sphereon.crypto.toCoseKeyOperations
import com.sphereon.crypto.toCoseKeyType
import com.sphereon.crypto.toCoseAlgorithm
import com.sphereon.crypto.toJoseCurve
import com.sphereon.crypto.toJoseKeyOperations
import com.sphereon.crypto.toJoseKeyType
import com.sphereon.crypto.toJoseSignatureAlgorithm
import com.sphereon.json.cryptoJsonSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.JsExport


/**

Represents a JSON Web Key (JWK) that is serializable to and from JSON format.

This interface defines the properties of a JWK as specified by the JSON Web Key (JWK) specification. It extends the `IKey` interface and includes properties for common cryptographic
 *  key parameters used in various algorithms.

 */
expect interface IJwkJson : IKey {
    /**
     * The algorithm name used for cryptographic operations.
     * The value may be null if the algorithm is not specified or initialized.
     */
    override val alg: String?
    /**
     * Represents the curve parameter identifier for elliptic curve cryptography.
     * It defines the specific curve to be used in cryptographic operations.
     * The value of this property can be null if no curve has been specified.
     */
    override val crv: String?
    /**
     * A nullable string that represents a specific data element
     * which can be overridden in subclasses.
     */
    override val d: String?
    /**
     * The `e` variable holds a nullable String value that can be used to store or manipulate
     * string data. This variable can also represent the absence of a string by holding a `null` value.
     */
    val e: String?
    /**
     * The shared secret (symmetric key) parameter.
     *
     * This value represents a symmetric key used in cryptographic operations. The key might
     * be used for various purposes such as encryption, decryption, or other algorithm-specific
     * symmetric key operations.
     *
     * The parameter can be null if not specified or applicable for the given cryptographic context.
     */
    val k: String?
    /**
     * Represents the operations that can be performed on the key.
     * This property holds an array of strings, each indicating a specific
     * permissible operation such as "encrypt", "decrypt", "sign", etc.
     *
     * It is optional and may be null, depending on whether the key
     * supports any specified operations.
     */
    override val key_ops: Array<String>?
    /**
     * A unique identifier for the cryptographic key.
     * This property holds an optional string that represents
     * the Key ID (kid), which is used to uniquely identify a key
     * in key management operations. This can be useful for
     * referencing, rotating, or managing keys in a secure manner.
     * It may be null if no specific Key ID is attributed.
     */
    override val kid: String?
    /**
     * The 'kty' (Key Type) parameter identifies the cryptographic algorithm family used with the key.
     * This parameter is a part of the JSON Web Key (JWK) specification.
     * It is one of the primary fields in a JWK and indicates the type of key, such as RSA, EC, oct (symmetric keys).
     */
    override val kty: String
    /**
     * Represents a nullable string value that can hold any string value
     * or be null. This variable can be used in scenarios where the
     * value may not always be available or needed.
     */
    val n: String?
    /**
     * Represents the intended use of the cryptographic key.
     *
     * This parameter specifies the purpose for which the key is meant, such as signing (sig)
     * or encryption (enc). Understanding the intended use helps in ensuring that the key is
     * only utilized in contexts that match its designated purpose, thus supporting correct
     * application and enhanced security.
     */
    val use: String?
    /**
     * Represents a nullable string that can be overridden by subclasses.
     * The purpose and usage of this variable should be defined
     * in the context of the class that overrides it.
     */
    override val x: String?
    /**
     * Variable `x5c` holds an optional array of strings.
     * It can be used to store a collection of string values.
     * The array may be null.
     */
    val x5c: Array<String>?
    /**
     * This variable represents a unique identifier known as the x5t (X.509 certificate SHA-1 thumbprint).
     * The x5t is used mainly in security contexts to uniquely identify an X.509 certificate.
     * It is a nullable string, meaning that it can either hold a SHA-1 thumbprint encoded as a Base64Url
     * string, or be null if no thumbprint is available.
     */
    val x5t: String?
    /**
     * The `x5u` variable represents a nullable string that is typically used
     * to hold a URL pointing to a JSON Web Key Set (JWKS). This URL can be
     * dereferenced to obtain the key material necessary for validating JSON
     * Web Tokens (JWTs) or other cryptographic operations.
     */
    val x5u: String?

    /**
     * Represents the base64url-encoded SHA-256 thumbprint (digest) of the DER encoding of
     * the X.509 certificate associated with a cryptographic key.
     *
     * This property is used to verify the integrity and authenticity of the X.509 certificate
     * by providing a unique fingerprint of the certificate's content. The thumbprint is
     * often used in security protocols and systems to ensure that the certificate has not
     * been tampered with and to quickly compare certificates.
     */
    @SerialName("x5t#S256")
    val x5t_S256: String?
    /**
     * This property represents an optional string value.
     * It overrides a property from a superclass or an interface.
     * The value can be null, indicating that no string is assigned.
     */
    override val y: String?
}


/**
 * Represents a JSON Web Key (JWK).
 */
expect interface IJwk : IKey {
    /**
     * Represents the JSON Web Algorithm (JWA) used in the Key object.
     * This property is an override of the `alg` value defined in the `IJwk` interface.
     *
     * Possible values include cryptographic algorithms used for signing and encryption
     * as specified in the JSON Web Algorithms (JWA) specification.
     *
     * @see JwaAlgorithm
     */
    override val alg: JwaAlgorithm?
    /**
     * Represents the elliptic curve parameter for the JSON Web Algorithms (JWA).
     * This variable specifies which curve is used in elliptic curve cryptography (ECC) operations.
     * It is an instance of the `JwaCurve` class, which encapsulates details of the curve being used.
     */
    override val crv: JwaCurve?
    /**
     * Represents the private or secret part of the cryptographic key.
     *
     * This property is primarily utilized in asymmetric key operations,
     * such as decryption and signing, which require access to the private key material.
     *
     * It may be null if the key is meant to be used only for public key operations
     * (e.g., encryption and verification) or if the private part is not available or applicable.
     */
    override val d: String?
    /**
     * Represents the exponent value in the RSA algorithm within the JSON Web Key (JWK) structure.
     *
     * The 'e' parameter is a component of the public key and is often set to a common default value.
     * It is used in conjunction with the 'n' parameter (modulus) to form the RSA public key.
     *
     * Can be null if not provided or applicable.
     */
    val e: String?
    /**
     * Represents the symmetric key material used for cryptographic operations.
     *
     * This variable can hold a string representation of the symmetric key, which is typically
     * used in algorithms such as HMAC or AES. It may be null if the key is not specified or
     * not relevant in the context of asymmetric cryptographic operations.
     */
    val k: String?
    /**
     * An array of allowed cryptographic operations for the key.
     *
     * This property specifies which cryptographic operations are permissible
     * for the associated key. `key_ops` is used to restrict the usage of cryptographic keys
     * to specific operations such as encryption, decryption, signing, or verifying.
     *
     * This array contains elements of `JoseKeyOperations` which defines the various possible
     * operations that can be performed with the key.
     *
     * The property returns `null` if no specific operations are assigned.
     */
    override val key_ops: Array<JoseKeyOperations>?
    /**
     * The `kid` variable represents a key identifier that is used to
     * uniquely identify a specific cryptographic key. The value is a
     * nullable string, which means it can either hold a key identifier
     * as a string or be null if no key identifier is assigned.
     */
    override val kid: String?
    /**
     * Represents the key type (`kty`) for the `IJwk` interface implementation.
     *
     * This property holds the type of the key as defined by the JSON Web Algorithms (JWA) specification.
     * It is used to identify the key type for cryptographic operations, ensuring compatibility
     * between different security frameworks and allowing the key to be correctly interpreted
     * and utilized across various implementations.
     */
    override val kty: JwaKeyType
    /**
     * The 'n' parameter in a JSON Web Key (JWK).
     *
     * Represents the 'modulus' value for RSA keys.
     *
     * This value is used in conjunction with the 'e' (exponent) parameter to form the public key component
     * for RSA encryption and signature verification.
     *
     * It may be null, indicating that the modulus is not provided or not applicable.
     */
    val n: String?
    /**
     * Indicates the intended use of the key.
     *
     * This variable represents the public key use, such as `sig` (signature) or `enc` (encryption),
     * which helps to specify the intended purpose of the key.
     */
    val use: String?
    /**
     * Represents the 'x' coordinate parameter for an elliptic curve key or a similar cryptographic key component.
     *
     * This value is typically used in the context of keys that rely on elliptic curve algorithms,
     * and it is essential for cryptographic operations involving such keys.
     *
     * The representation is flexible and can accommodate various types of data required by different
     * cryptographic standards.
     */
    override val x: String?
    /**
     * Represents the X.509 certificate chain associated with the key.
     *
     * This property holds an array of strings, each representing an X.509 certificate
     * in the chain. The certificates are typically base64-encoded DER (Distinguished Encoding Rules)
     * representations. The first certificate in the array is usually the end-entity certificate (the
     * certificate associated with the key) followed by intermediate and root CA certificates.
     *
     * It can be null if no x5c certificate chain is provided.
     */
    val x5c: Array<String>?
    /**
     * Represents the `x5t` (X.509 certificate SHA-1 thumbprint) parameter.
     *
     * This value is the SHA-1 thumbprint (also known as a fingerprint) of the DER encoding of an X.509
     * certificate associated with the key. It is used for ensuring the integrity and authenticity
     * of the associated certificate in various cryptographic operations and protocols.
     *
     * The `x5t` field can be null if the key does not include a X.509 certificate or if the thumbprint
     * is not available.
     */
    val x5t: String?
    /**
     * The URI of the X.509 certificate.
     *
     * This property holds the URI that references the X.509 public key certificate or certificate chain,
     * which is often used in the context of JSON Web Keys (JWKs) to provide a trust anchor for the key.
     * The certificate can be used for verifying the authenticity and integrity of the associated key.
     */
    val x5u: String?

    /**
     * Represents the X.509 certificate SHA-256 thumbprint in JWT (JSON Web Token).
     *
     * This value is the base64url-encoded SHA-256 hash of the DER encoding of the X.509 certificate.
     * It is used to uniquely identify the certificate.
     *
     * This attribute allows the receiver to validate that a token was signed by the corresponding
     * private key, enforcing stronger security in cryptographic operations.
     */
    @SerialName("x5t#S256")
    val x5t_S256: String?
    /**
     * Represents a nullable string value.
     * This value might be null or contain some string content.
     */
    override val y: String?
}

/**
 * Represents a JSON Web Key (JWK) as defined by the JSON Web Key (JWK) specification.
 *
 * @property alg The algorithm intended for use with the key.
 * @property crv The cryptographic curve used with the key.
 * @property d The private key value.
 * @property e The public exponent for the key.
 * @property k The symmetric key value.
 * @property key_ops The allowed operations for the key.
 * @property kid The key ID.
 * @property kty The key type.
 * @property n The modulus value for the RSA key.
 * @property use The intended use of the key.
 * @property x The x coordinate of the elliptic curve point.
 * @property x5c The X.509 certificate chain.
 * @property x5t The X.509 certificate SHA-1 thumbprint.
 * @property x5u The URL to the X.509 certificate or certificate chain.
 * @property x5t_S256 The X.509 certificate SHA-256 thumbprint.
 * @property y The y coordinate of the elliptic curve point.
 */
@JsExport
@Serializable
data class Jwk(
    override val alg: JwaAlgorithm? = null,
    override val crv: JwaCurve? = null,
    override val d: String? = null,
    override val e: String? = null,
    override val k: String? = null,
    override val key_ops: Array<JoseKeyOperations>? = null,
    override val kid: String? = null,
    override val kty: JwaKeyType,
    override val n: String? = null,
    override val use: String? = null,
    override val x: String? = null,
    override val x5c: Array<String>? = null,
    override val x5t: String? = null,
    override val x5u: String? = null,

    @SerialName("x5t#S256")
    override val x5t_S256: String? = null,
    override val y: String? = null,
) : IJwk {

    /**
     * Represents additional JSON attributes not explicitly defined in the JWK specification.
     *
     * This property may hold any arbitrary data in the form of a JSON object, allowing for
     * flexibility in extending the standard JWK structure with custom attributes.
     *
     * For instance, if a particular JWK implementation or usage scenario requires storing
     * extra metadata, this property can encapsulate those needs without interfering with
     * the predefined fields.
     *
     * Note: This field is optional and may be null if no additional attributes are present.
     */
    override val additional: JsonObject?
        get() = null //TODO("Not yet implemented")

    /**
     * Retrieves the algorithm mapping for the current JWK (JSON Web Key) instance.
     *
     * @return the algorithm mapping derived from the 'alg' field, or null if 'alg' is not defined.
     */
    override fun getAlgMapping(): AlgorithmMapping? {
        return alg?.let { AlgorithmMapping.Static.fromJose(it) }
    }

    /**
     * Retrieves the key type mapping for the current instance.
     *
     * @return KeyTypeMapping that corresponds to the current instance's JOSE key type.
     */
    override fun getKtyMapping(): KeyTypeMapping {
        return KeyTypeMapping.Static.fromJose(this.kty)
    }

    /**
     * Returns an array of KeyOperationsMapping objects derived from the key operations specified
     * in the JWK (JSON Web Key) object. If no key operations are defined, it returns null.
     *
     * @return An array of KeyOperationsMapping objects or null if no key operations are defined.
     */
    override fun getKeyOperationsMapping(): Array<KeyOperationsMapping>? {
        return key_ops?.map { KeyOperationsMapping.Static.fromJose(it) }?.toTypedArray()
    }

    /**
     * Retrieves the X.509 certificate chain array (x5c) associated with the JSON Web Key (JWK).
     *
     * @return An array of strings representing the X.509 certificate chain, or null if it is not set.
     */
    override fun getX5cArray(): Array<String>? {
        return x5c
    }

    /**
     * The `Builder` class is used to construct instances of the `Jwk` class with various optional properties.
     */
    class Builder {
        /**
         * The `alg` variable holds an instance of the `JwaAlgorithm` class, representing the algorithm
         * used for JSON Web Algorithms (JWA). It's used in conjunction with JSON Web Tokens (JWT) for
         * signing or encryption operations.
         *
         * This variable can be `null`, indicating that no specific algorithm has been assigned yet.
         *
         * The `JwaAlgorithm` class encompasses various cryptographic algorithms standardized for use
         * in JSON-based tokens. The choice of algorithm affects the security and performance
         * characteristics of the cryptographic operations.
         */
        var alg: JwaAlgorithm? = null
        /**
         * Represents the JSON Web Algorithm (JWA) curve used in cryptographic operations.
         * This variable can hold different types of curves such as P-256, P-384, and P-521.
         * The value is nullable, indicating that the curve might not always be set.
         */
        var crv: JwaCurve? = null
        /**
         * This variable holds a nullable String which can be used to store text data.
         * Initially, it is set to null which indicates that it does not contain any value currently.
         * It can be assigned a non-null value later during the program execution.
         */
        var d: String? = null
        /**
         * The 'e' parameter of a JWK (JSON Web Key) representing the RSA public exponent.
         * Typically used with RSA public keys.
         */
        var e: String? = null
        /**
         * Represents the symmetric key used in cryptographic operations.
         *
         * This property stores the key value, which is used in JWK (JSON Web Key) structures.
         * The key can be null, indicating that no key value has been set.
         */
        var k: String? = null
        /**
         * The operations that the key is intended to be used for.
         * Examples of possible values include "sign", "verify", "encrypt", etc.
         */
        var key_ops: Array<JoseKeyOperations>? = null
        /**
         * Represents a child entity, potentially containing the name or
         * identifier of a child. This can be used in contexts where
         * distinguishing or identifying a child is required.
         *
         * The value is initialized to null, indicating no value has been set.
         * It can be assigned a non-null string to represent the name/identifier.
         *
         * Example use cases include tracking children in a school system,
         * managing child elements in hierarchical data structures, and other
         * similar scenarios where child identification is necessary.
         */
        var kid: String? = null
        /**
         * Defines the key type (kty) parameter in the JWK (JSON Web Key) as specified by the JSON Web Algorithms (JWA).
         * This property indicates the specific cryptographic algorithm family used by the key.
         *
         * Possible values are:
         * - EC: Elliptic Curve
         * - RSA: RSA Encryption
         * - oct: Octet sequence (used to represent symmetric keys)
         * - OKP: Octet Key Pair (used for algorithms like EdDSA)
         *
         * This parameter is critical and its absence will cause the build process to fail,
         * as it is essential for the cryptographic operations that the key will perform.
         */
        var kty: JwaKeyType? = null
        /**
         * A nullable String variable `n` that can be used to store a string value or null.
         * It is initialized to null by default.
         */
        var n: String? = null
        /**
         * A nullable string variable that can be used to store any text or String data.
         * The variable is initially set to null, indicating that it has no value assigned.
         * It can be updated to hold a non-null string as needed throughout the program.
         * Common use cases include temporary string storage, input/output handling, and data manipulation.
         */
        var use: String? = null
        /**
         * A nullable String variable that can be used to store text data.
         *
         * This variable can hold either a String value or null. Initially, it is set to null.
         *
         * It can be used in scenarios where the string value might not always be available or optional.
         */
        var x: String? = null
        /**
         * Holds an array of x5c certificates as strings.
         *
         * This variable can be used to store x5c certificates,
         * which are typically Base64-encoded DER representations of X.509 certificates.
         *
         * It is nullable and can be set to null initially.
         */
        var x5c: Array<String>? = null
        /**
         * Represents the X.509 certificate SHA-1 thumbprint.
         *
         * The `x5t` parameter contains the base64url-encoded SHA-1 thumbprint of the DER-encoded X.509 certificate.
         * It is used to reference a specific certificate when performing cryptographic operations.
         * This value is optional and can be null if not specified.
         */
        var x5t: String? = null
        /**
         * x5u variable holds a URL-safe representation of a JWK Set used in the application.
         *
         * The value is a nullable String. When populated, it should contain a URL that points to a JSON Web Key Set.
         * This variable can be used in processes involving security keys or cryptographic operations.
         *
         * Default value is null.
         */
        var x5u: String? = null
        /**
         * The base64url-encoded SHA-256 thumbprint of the X.509 certificate associated with the key.
         * This value provides a strong reference to the certificate when it is used in JSON Web Key (JWK) objects.
         * It is an optional field and may be null if not specified.
         */
        var x5t_S256: String? = null
        /**
         * A nullable variable that holds a string value.
         * It can be assigned a non-null string or a null value.
         * Initially, it is set to null.
         */
        var y: String? = null

        /**
         * Sets the JSON Web Algorithm (JWA) for this builder instance.
         *
         * @param alg An instance of [JwaAlgorithm], or null if the algorithm is to be unset.
         */
        fun withAlg(alg: JwaAlgorithm? = null) = apply { this.alg = alg }
        /**
         * Sets the JwaCurve for the current instance.
         *
         * @param crv The JwaCurve to be set for this instance.
         * @return The current instance with the specified JwaCurve applied.
         */
        fun withCrv(crv: JwaCurve?) = apply { this.crv = crv }
        /**
         * Sets the 'd' parameter for this builder instance.
         *
         * @param d The 'd' value to set, or null if not applicable.
         */
        fun withD(d: String?) = apply { this.d = d }
        /**
         * Sets the `e` parameter of the Builder and returns the Builder instance.
         *
         * @param e the value to set for the `e` parameter. It can be null.
         */
        fun withE(e: String?) = apply { this.e = e }
        /**
         * Sets the 'k' (key) parameter for the Builder.
         *
         * @param k the key value to set, can be nullable.
         */
        fun withK(k: String?) = apply { this.k = k }
        /**
         * Sets the key operations for the current object.
         *
         * @param key_ops An array of JoseKeyOperations specifying the permissible operations for the key.
         * Setting this to null will clear the currently set key operations.
         */
        fun withKeyOps(key_ops: Array<JoseKeyOperations>?) = apply { this.key_ops = key_ops }
        /**
         * Sets the 'kid' (Key ID) parameter for the JWK (JSON Web Key) being constructed.
         *
         * @param kid the Key ID to set for the JWK, or null if no Key ID should be assigned.
         * @return the Builder instance with the updated 'kid' property.
         */
        fun withKid(kid: String?) = apply { this.kid = kid }
        /**
         * Sets the key type for this object.
         *
         * @param kty The JwaKeyType to be set. Can be null.
         */
        fun withKty(kty: JwaKeyType?) = apply { this.kty = kty }
        /**
         * Sets the 'n' field and returns the current Builder instance.
         *
         * @param n The 'n' value to set.
         */
        fun withN(n: String?) = apply { this.n = n }
        /**
         * Sets the 'use' parameter for the Builder instance.
         *
         * @param use A string indicating the intended use of the key (e.g., "sig" for signature or "enc" for encryption).
         */
        fun withUse(use: String?) = apply { this.use = use }
        /**
         * Sets the `x` property of the Builder object.
         *
         * @param x the value to set for the `x` property
         */
        fun withX(x: String?) = apply { this.x = x }
        /**
         * Sets the x5c property and returns the current object.
         *
         * @param x5c an array of strings to set the x5c property. It can be null.
         */
        fun withX5c(x5c: Array<String>?) = apply { this.x5c = x5c }
        /**
         * Sets the x5t (X.509 certificate SHA-1 thumbprint) value.
         *
         * @param x5t the X.509 certificate SHA-1 thumbprint to be set. Can be nullable.
         */
        fun withX5t(x5t: String?) = apply { this.x5t = x5t }
        /**
         * Sets the `x5u` attribute.
         *
         * @param x5u The new value for the `x5u` attribute. This parameter can be null.
         */
        fun withX5u(x5u: String?) = apply { this.x5u = x5u }
        /**
         * Sets the x5t_S256 parameter for the instance and returns the modified instance.
         *
         * @param x5t_S256 The x5t_S256 value to set. It is a nullable String.
         */
        fun withX5t_S256(x5t_S256: String?) = apply { this.x5t_S256 = x5t_S256 }
        /**
         * Sets the `y` parameter for the Builder instance.
         *
         * @param y The value to set for the `y` parameter. It can be a nullable string.
         */
        fun withY(y: String?) = apply { this.y = y }


        /**
         * Constructs a new `Jwk` instance using the provided properties.
         *
         * @return a new instance of `Jwk` initialized with the set properties.
         * @throws IllegalArgumentException if the `kty` property is missing.
         */
        fun build(): Jwk = Jwk(
            alg = alg,
            crv = crv,
            d = d,
            e = e,
            k = k,
            key_ops = key_ops,
            kid = kid,
            kty = kty ?: throw IllegalArgumentException("kty value missing"),
            n = n,
            use = use,
            x = x,
            x5c = x5c,
            x5t = x5t,
            x5u = x5u,
            x5t_S256 = x5t_S256,
            y = y
        )

    }

    /**
     * Converts the JSON Web Key (JWK) to a COSE key in JSON format.
     *
     * @return A `CoseKeyJson` object representing the COSE key in JSON format.
     */
// Name is like other extensions functions to not class with JS
    fun jwkToCoseKeyJson(): CoseKeyJson =
        CoseKeyJson.Builder()
            .withKty(this@Jwk.kty.toCoseKeyType() ?: throw IllegalArgumentException("kty value missing"))
            .withAlg(alg?.toCoseAlgorithm())
            .withCrv(crv?.toCoseCurve())
            .withD(d)
//                    .withE(e)
//                    .withK(k)
            .withKeyOps(key_ops?.map { it.toCoseKeyOperations() }?.toTypedArray())
            .withKid(kid)
//                    .withN(n)
//                    .withUse(use)
            .withX(x)
//                    .withX5t(x5t) // todo
            .withY(y)
            .build()


    /**
     * Converts a JWK (JSON Web Key) to a COSE key in CBOR encoding.
     *
     * This function transforms the current JWK instance into its corresponding
     * COSE Key representation and then serializes it into CBOR format.
     *
     * @return the CBOR-encoded representation of the COSE key.
     */
// Name is like other extensions functions to not class with JS
    fun jwkToCoseKeyCbor(): CoseKeyCbor = this.jwkToCoseKeyJson().toCbor()

    /**
     * Converts the current object into a JSON object representation.
     *
     * Uses the `cryptoJsonSerializer` to encode the object to a JSON element and retrieves the JSON object from it.
     *
     * @return The JSON object representation of the current object.
     */
    fun toJsonObject() = cryptoJsonSerializer.encodeToJsonElement(serializer(), this).jsonObject


    /**
     * Object containing utility functions for creating JWK (JSON Web Keys) from various input types,
     * such as IJwkJson, JsonObject, IJwk, ICoseKeyJson, and ICoseKeyCbor.
     */
    object Static {
        /**
         * Converts a JSON representation of a JWK (JSON Web Key) to a Jwk object.
         *
         * @param jwk The JSON representation of the JWK.
         * @return The Jwk object.
         */
        fun fromJson(jwk: IJwkJson): Jwk = with(jwk) {
            return Jwk(
                alg = JwaAlgorithm.Static.fromValue(alg),
                crv = JwaCurve.Static.fromValue(crv),
                d = d,
                e = e,
                k = k,
                key_ops = key_ops?.map { JoseKeyOperations.Static.fromValue(it) }?.toTypedArray(),
                kid = kid,
                kty = JwaKeyType.Static.fromValue(kty),
                n = n,
                use = use,
                x = x,
                x5c = x5c?.map { it }?.toTypedArray(),
                x5t = x5t,
                x5u = x5u,
                x5t_S256 = x5t_S256,
                y = y,
            )
        }

        /**
         * Constructs a [Jwk] instance from a given [JsonObject].
         *
         * @param jwk The JSON object representing the JWK.
         * @return A [Jwk] instance populated with the values from the JSON object.
         */
        fun fromJsonObject(jwk: JsonObject): Jwk = with(jwk) {
            return@fromJsonObject Jwk(
                alg = get("alg")?.jsonPrimitive?.content?.let {
                    JwaAlgorithm.Static.fromValue(it)
                },
                crv = get("crv")?.jsonPrimitive?.content?.let { JwaCurve.Static.fromValue(it) },
                d = get("d")?.jsonPrimitive?.content,
                e = get("e")?.jsonPrimitive?.content,
                k = get("k")?.jsonPrimitive?.content,
                key_ops = get("key_ops")?.jsonArray?.map { JoseKeyOperations.Static.fromValue(it.jsonPrimitive.content) }?.toTypedArray(),
                kid = get("kid")?.jsonPrimitive?.content,
                kty = get("kty")?.jsonPrimitive?.content?.let { JwaKeyType.Static.fromValue(it) } ?: throw IllegalArgumentException("kty is missing"),
                n = get("n")?.jsonPrimitive?.content,
                use = get("use")?.jsonPrimitive?.content,
                x = get("x")?.jsonPrimitive?.content,
                x5c = get("x5c")?.jsonArray?.map { it.jsonPrimitive.content }?.toTypedArray(),
                x5t = get("x5t")?.jsonPrimitive?.content,
                x5u = get("x5u")?.jsonPrimitive?.content,
                x5t_S256 = get("x5t#S256")?.jsonPrimitive?.content,
                y = get("y")?.jsonPrimitive?.content,
            )
        }

        /**
         * Converts an instance of `IJwk` to an instance of `Jwk`.
         *
         * @param jwk The `IJwk` instance to be converted.
         * @return The equivalent `Jwk` instance with the same properties.
         */
        fun fromDTO(jwk: IJwk): Jwk = with(jwk) {
            return@fromDTO Jwk(
                alg = alg,
                crv = crv,
                d = d,
                e = e,
                k = k,
                key_ops = key_ops,
                kid = kid,
                kty = kty,
                n = n,
                use = use,
                x = x,
                x5c = x5c,
                x5t = x5t,
                x5u = x5u,
                x5t_S256 = x5t_S256,
                y = y
            )
        }

        /**
         * Converts a given COSE key JSON object to a JWK (JSON Web Key).
         *
         * @param coseKey the COSE key JSON object to convert.
         * @return the resulting JWK.
         */
        fun fromCoseKeyJson(coseKey: ICoseKeyJson): Jwk {
            with(coseKey) {
                val kty = kty.toJoseKeyType()
                return Builder()
                    .withKty(kty)
                    .withAlg(alg?.toJoseSignatureAlgorithm())
                    .withCrv(crv?.toJoseCurve())
                    .withD(d)
//                    .withE(e)
//                    .withK(k)
                    .withKeyOps(key_ops?.map { it.toJoseKeyOperations() }?.toTypedArray())
                    .withKid(kid)
//                    .withN(n)
//                    .withUse(use)
                    .withX(x)
                    .withX5c(x5chain)
//                    .withX5t(x5t) // todo
                    .withY(y)
                    .build()
            }


        }

        /**
         * Converts a COSE key in CBOR format to its JSON representation.
         *
         * @param coseKey An object implementing the ICoseKeyCbor interface, which represents
         * a COSE key encoded in CBOR format.
         * @return The JSON representation of the COSE key.
         */
        fun fromCoseKey(coseKey: ICoseKeyCbor) = fromCoseKeyJson(CoseKeyCbor.Static.fromDTO(coseKey).toJson())

    }


}


/**
 * Converts a COSE key in CBOR format to a JWK format.
 *
 * This function takes a `CoseKeyCbor` object and transforms it into a `Jwk.Static` object.
 * It is useful for converting cryptographic keys between formats commonly used in
 * web security and cryptography.
 *
 * @receiver The COSE key represented in CBOR format to be converted.
 * @return A `Jwk.Static` instance created from the given COSE key.
 */
@JsExport
fun CoseKeyCbor.cborToJwk() = Jwk.Static.fromCoseKey(this)

/**
 * Converts a COSE key in JSON format to a JWK (JSON Web Key).
 *
 * This extension function allows transforming a COSE key represented as a JSON object
 * into its equivalent JWK representation. The conversion utilizes the `fromCoseKeyJson`
 * method from the `Jwk.Static` object.
 *
 * @receiver CoseKeyJson The COSE key in JSON format to be converted.
 * @return Jwk The corresponding JWK representation of the COSE key.
 */
@JsExport
fun CoseKeyJson.jsonToJwk() = Jwk.Static.fromCoseKeyJson(this)

@JsExport
enum class JwkUse(val value: String) {
    sig("sig"),
    enc("enc")
}
