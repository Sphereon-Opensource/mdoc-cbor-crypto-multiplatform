package com.sphereon.crypto.cose

import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborItem
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborUInt
import com.sphereon.cbor.CborView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.encodeToArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.crypto.generic.SignatureAlgorithm
import com.sphereon.crypto.IKey
import com.sphereon.crypto.generic.KeyOperationsMapping
import com.sphereon.crypto.generic.KeyTypeMapping
import com.sphereon.json.JsonView
import com.sphereon.json.cryptoJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.encodeTo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Represents a COSE (CBOR Object Signing and Encryption) key in JSON format. This sealed interface extends
 * the IKey interface, providing necessary properties to define a COSE key. It ensures compatibility
 * with expected key attributes in the COSE ecosystem.
 */
expect sealed interface ICoseKeyJson : IKey {
    /**
     * Represents the COSE key type for the current key.
     * The COSE (CBOR Object Signing and Encryption) key type determines the algorithm and general structure of the key.
     */
    override val kty: CoseKeyType
    /**
     * A nullable string that represents the key ID (kid).
     * This identifier is used to indicate the specific key
     * in a set of keys. The value can be `null` if the key
     * ID is not specified.
     */
    override val kid: String?
    /**
     * This property holds an instance of `CoseAlgorithm`.
     * The `alg` variable is used to specify the cryptographic algorithm
     * that will be applied in operations requiring this information.
     * It can be null if no specific algorithm is set.
     */
    override val alg: CoseAlgorithm?

    /**
     * The `key_ops` array defines the permissible operations for the key.
     *
     * This property specifies an array of `CoseKeyOperations` that enumerates
     * the set of operations for which the key can be used. This is pertinent in
     * the handling and management of cryptographic keys, enforcing constraints
     * and permissions on how each key may be utilized.
     *
     * This array can be null, indicating that there are no explicit operations
     * defined or restricted for the key.
     */
    override val key_ops: Array<CoseKeyOperations>?

    /**
     * A nullable string variable that holds the base Initialization Vector (IV)
     * for cryptographic operations. The IV is a fixed-size input to a cryptographic
     * primitive that typically aims to randomize the encryption process and ensure
     * that identical plaintexts will encrypt to different ciphertexts.
     */
    val baseIV: String?

    /**
     * Represents the cryptographic curve parameter used in COSE (CBOR Object Signing and Encryption) messages.
     * This is an override of the base class property.
     *
     * @property crv The COSE curve object that defines the curve used for cryptographic operations.
     */
    override val crv: CoseCurve?
    /**
     * The optional string value that represents a certain configuration or data point
     * relevant to the class. This value can be null, indicating the absence of the
     * configuration or the specific data.
     */
    override val x: String?
    /**
     * Represents the y-coordinate or the second value which is of type String?.
     * This value can be used in calculations or representations where a nullable
     * String is appropriate.
     */
    override val y: String?
    /**
     * Represents a variable that can hold a string value or null.
     * The variable `d` is overridden in this context, potentially implying it is part of a class hierarchy.
     * This variable can be useful in scenarios where an optional string value is required.
     */
    override val d: String?
    /**
     * An optional array of strings named x5chain, which may contain a series of string elements or be null.
     * This variable could be used for storing and manipulating a collection of string data.
     */
    val x5chain: Array<String>?
    /**
     * An optional JSON object that provides additional details for this instance.
     * The contents of this JSON object are context-specific and may vary depending
     * on the particular use case or scenario in which it is used.
     */
    override val additional: JsonObject?
}

/**
 * Class CoseKeyJson represents a COSE key in JSON format.
 * It includes properties for the key type, algorithm, key operations, and various other details.
 */
@JsExport
@Serializable
class CoseKeyJson(
    override val kty: CoseKeyType,
    override val kid: String? = null,
    override val alg: CoseAlgorithm? = null,

    override val key_ops: Array<CoseKeyOperations>? = null,

    override val baseIV: String? = null,

    override val crv: CoseCurve? = null,
    override val x: String? = null,
    override val y: String? = null,
    override val d: String? = null,
    override val x5chain: Array<String>? = null,
    override val additional: JsonObject? = null
) : JsonView(), ICoseKeyJson {
    /**
     * Converts the current object to a JSON element using the cryptoJsonSerializer
     * for serialization. This method leverages the encodeToJsonElement function
     * to produce the JSON representation of the object.
     *
     * @return JSON representation of the current object as a JsonElement
     */
    fun toDto() = cryptoJsonSerializer.encodeToJsonElement(this)
    /* :ICoseKeyJson {
       cryptoJsonSerializer.encodeToDynamic(CoseKeyJson::serializer, this)
       override val kty = this@CoseKeyJson.kty
       override val kid = this@CoseKeyJson.kid
       override val alg = this@CoseKeyJson.alg
       override val key_ops = this@CoseKeyJson.key_ops
       override val baseIV = this@CoseKeyJson.baseIV
       override val crv = this@CoseKeyJson.crv
       override val x = this@CoseKeyJson.x
       override val y = this@CoseKeyJson.y
       override val d = this@CoseKeyJson.d
       override val x5chain = this@CoseKeyJson.x5chain
       override val additional = this@CoseKeyJson.additional
   }*/

    /**
     * Maps the current algorithm to a predefined AlgorithmMapping based on its CoSE value.
     *
     * @return the AlgorithmMapping corresponding to the current algorithm if it exists,
     *         or null if the algorithm is not defined.
     */
    override fun getAlgMapping(): SignatureAlgorithm? {
        return alg?.let { SignatureAlgorithm.Static.fromCose(it) }
    }

    /**
     * Retrieves the Key Type Mapping for the given Key Type (kty).
     *
     * @return A KeyTypeMapping corresponding to the specified Key Type (kty).
     */
    override fun getKtyMapping(): KeyTypeMapping {
        return KeyTypeMapping.Static.fromCose(this.kty)
    }

    /**
     * Retrieves an array of KeyOperationsMapping objects based on the current key operations.
     *
     * This method processes the key operations associated with the current instance,
     * mapping each one to a KeyOperationsMapping.Static object via the fromCose method,
     * and then converts the result to an array.
     *
     * @return An array of KeyOperationsMapping objects or null if key operations are not defined.
     */
    override fun getKeyOperationsMapping(): Array<KeyOperationsMapping>? {
        return key_ops?.map { KeyOperationsMapping.Static.fromCose(it) }?.toTypedArray()
    }

    /**
     * Retrieves the X.509 certificate chain as an array of strings.
     *
     * @return An array of strings representing the X.509 certificate chain, or null if not available.
     */
    override fun getX5cArray(): Array<String>? {
        return x5chain
    }

    /**
     * Serializes the current object to a JSON string using the provided cryptoJsonSerializer.
     *
     * @return A JSON string representation of the current object.
     */
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)

    /**
     * Converts the current `CoseKeyJson` object to its CBOR representation.
     *
     * @return an instance of `CoseKeyCbor` containing the CBOR-encoded key data.
     */
    override fun toCbor(): CoseKeyCbor =
        CoseKeyCbor.Builder().withKty(kty).withKid(kid).withAlg(alg).withKeyOps(key_ops).withBaseIV(baseIV)
            .withCrv(crv).withX(x).withY(y).withD(d).withX5Chain(x5chain).build() // todo: additional

    /**
     * Checks if this CoseKeyJson object is equal to another object.
     *
     * @param other the object to compare with.
     * @return true if the objects are equal, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoseKeyJson) return false

        if (kty != other.kty) return false
        if (kid != other.kid) return false
        if (alg != other.alg) return false
        if (key_ops != null) {
            if (other.key_ops == null) return false
            if (!key_ops.contentEquals(other.key_ops)) return false
        } else if (other.key_ops != null) return false
        if (baseIV != other.baseIV) return false
        if (crv != other.crv) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (d != other.d) return false
        if (x5chain != null) {
            if (other.x5chain == null) return false
            if (!x5chain.contentEquals(other.x5chain)) return false
        } else if (other.x5chain != null) return false
        if (additional != other.additional) return false

        return true
    }

    /**
     * Generates a hash code for the current object based on its properties.
     *
     * @return An integer hash code value representing this object.
     */
    override fun hashCode(): Int {
        var result = kty.hashCode()
        result = 31 * result + (kid?.hashCode() ?: 0)
        result = 31 * result + (alg?.hashCode() ?: 0)
        result = 31 * result + (key_ops?.contentHashCode() ?: 0)
        result = 31 * result + (baseIV?.hashCode() ?: 0)
        result = 31 * result + (crv?.hashCode() ?: 0)
        result = 31 * result + (x?.hashCode() ?: 0)
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + (d?.hashCode() ?: 0)
        result = 31 * result + (x5chain?.contentHashCode() ?: 0)
        result = 31 * result + (additional?.hashCode() ?: 0)
        return result
    }

    /**
     * Converts the CoseKeyJson object to its string representation.
     *
     * @return A string representation of the CoseKeyJson object including its kty, kid, alg, key_ops, baseIV, crv, x, y, d, x5chain, and additional properties.
     */
    override fun toString(): String {
        return "CoseKeyJson(kty=$kty, kid=$kid, alg=$alg, key_ops=${key_ops?.contentToString()}, baseIV=$baseIV, crv=$crv, x=$x, y=$y, d=$d, x5chain=${x5chain?.contentToString()}, additional=$additional)"
    }


    /**
     * The Static object provides utility methods for transforming data.
     */
    object Static {
        /**
         * Converts an instance of ICoseKeyJson DTO to a CoseKeyJson object.
         *
         * @param dto The ICoseKeyJson instance that needs to be converted.
         * @return A new instance of CoseKeyJson with properties copied from the given dto.
         */
        fun fromDTO(dto: ICoseKeyJson) = with(dto) {
            CoseKeyJson(
                kty = kty,
                kid = kid,
                alg = alg,
                key_ops = key_ops,
                baseIV = baseIV,
                crv = crv,
                x = x,
                y = y,
                d = d,
                x5chain = x5chain,
                additional = additional

            )
        }
    }


    /**
     * Builder class for constructing instances of the CoseKeyJson.
     */
    class Builder {
        /**
         * Stores the type of COSE key being constructed.
         * This parameter must be present and is used to determine the set
         * of key-type-specific parameters applicable to the key.
         * It is verified to ensure compatibility with the algorithm processed.
         */
        private lateinit var kty: CoseKeyType
        /**
         * The variable `kid` represents the Key ID associated with a COSE key.
         * This optional field is used to provide a hint about which key to use.
         * It can be null if no Key ID is set.
         */
        var kid: String? = null
        /**
         * The algorithm identifier used in the COSE key to indicate the cryptographic algorithm
         * employed. This value can be null if no specific algorithm is set.
         */
        var alg: CoseAlgorithm? = null
        /**
         * Specifies the operations that are permissible with the key.
         * This variable is an array of `CoseKeyOperations` enums.
         * It defines what actions can be performed with this key, such as
         * signing, verifying, encrypting, decrypting, etc.
         *
         * Example operations include:
         * - `SIGN` for creating signatures
         * - `VERIFY` for signature verification
         * - `ENCRYPT` for key transport encryption
         * - `DECRYPT` for key transport decryption
         *
         * This property is optional and can be null.
         */
        var key_ops: Array<CoseKeyOperations>? = null
        /**
         * The base Initialization Vector (IV) used for cryptographic operations.
         * It is an optional field represented as a nullable hexadecimal string.
         */
        var baseIV: String? = null
        /**
         * Specifies the elliptic curve used for key generation within the current `Builder` instance.
         *
         * This optional parameter corresponds to an instance of the `CoseCurve` enum,
         * which defines a set of pre-determined elliptic curves that can be used in JWA cryptographic algorithms.
         */
        var crv: CoseCurve? = null
        /**
         * Represents the 'x' parameter in the COSE key structure which may hold a value
         * associated with the key's elliptic curve x-coordinate or other relevant data.
         * This field can be null if not applicable or not set.
         */
        var x: String? = null
        /**
         * The y-coordinate of an elliptic curve key.
         *
         * This variable may be null if the y-coordinate is not provided or required.
         */
        var y: String? = null
        /**
         * Represents the private key information for the key in the COSE key object.
         * It is an optional field and can be null. The private key is crucial for
         * operations that require confidentiality and integrity, such as signing
         * and decryption.
         */
        var d: String? = null
        /**
         * Represents the x5chain field in the Builder class.
         *
         * This field is an array of strings that can be optionally set to include
         * X.509 certificate chains. It's used in the context of COSE (CBOR Object
         * Signing and Encryption), a concise binary Object Representation format.
         *
         * x5chain stands for X.509 public key certificates, and it is leveraged to provide
         * verification data for the key. The array can contain multiple certificates that
         * form a chain, allowing for thorough validation and trust assessment of the provided
         * key material.
         *
         * Usage can be initialized through the `withX5Chain` function and is a part of the
         * process when building a `CoseKeyJson` object in the `build` method.
         */
        var x5chain: Array<String>? = null
        /**
         * Optional JsonObject to store additional parameters or metadata
         * related to the key. This can be used to specify custom properties
         * or elements that are not covered by the predefined fields in the
         * key structure, like kty, alg, kid, etc.
         */
        var additional: JsonObject? = null


        /**
         * Sets the key type (kty) for the COSE key and returns the builder instance.
         *
         * @param kty The COSE key type to set.
         */
        fun withKty(kty: CoseKeyType) = apply { this.kty = kty }

        /**
         * Sets the key ID (kid) for the current builder instance.
         *
         * @param kid the key ID to set. If null, the key ID will not be set.
         * @return the current builder instance.
         */
        fun withKid(kid: String?) = apply { this.kid = kid }
        /**
         * Sets the COSE algorithm for the builder.
         *
         * @param alg the COSE algorithm to be set, can be null
         */
        fun withAlg(alg: CoseAlgorithm?) = apply { this.alg = alg }
        /**
         * Sets the key operations parameter for the COSE key.
         *
         * @param key_ops An array of `CoseKeyOperations` that define the permitted operations for the key.
         *                The array can be null to indicate no specific operations are set.
         */
        fun withKeyOps(key_ops: Array<CoseKeyOperations>?) = apply {
            this.key_ops = key_ops
        }

        /**
         * Sets the base Initialization Vector (IV) for the builder.
         *
         * @param baseIVHex A hexadecimal string representing the base IV to be set.
         */
        fun withBaseIV(baseIVHex: String?) = apply { this.baseIV = baseIVHex }
        /**
         * Sets the COSE curve value for the key.
         *
         * @param crv The `CoseCurve` instance to set.
         */
        fun withCrv(crv: CoseCurve?) = apply { this.crv = crv }
        /**
         * Sets the 'x' parameter for the builder.
         *
         * @param x The value to be set to the 'x' parameter, which may be null.
         */
        fun withX(x: String?) = apply { this.x = x }
        /**
         * Sets the 'y' field of the Builder class with the provided value.
         *
         * @param y The value to set for the 'y' field. It can be a nullable string.
         * @return The Builder instance with the updated 'y' value.
         */
        fun withY(y: String?) = apply { this.y = y }
        /**
         * Sets the value of `d` and returns the current instance.
         *
         * @param d The string value to set for `d`. Can be null.
         */
        fun withD(d: String?) = apply { this.d = d }
        /**
         * Sets the X.509 certificate chain for the key.
         *
         * @param x5c An array of strings representing the X.509 certificate chain. Each string in the array
         *            should be a base64-encoded DER certificate.
         * @return The Builder instance with the updated X.509 certificate chain.
         */
        fun withX5Chain(x5c: Array<String>?) = apply { this.x5chain = x5c }

        // FIXME
//        fun withAdditional(additional: MutableMap<LongKMP, *>?) = apply { additional?.let { this.additional = CborMap(it) }}

        /**
         * Builds a CoseKeyJson object using the current state of the Builder.
         *
         * @return A CoseKeyJson object populated with the properties set on this Builder.
         */
        fun build(): CoseKeyJson {
            return CoseKeyJson(
                kty = kty,
                alg = alg,
                kid = kid,
                key_ops = key_ops,
                baseIV = baseIV,
                crv = crv,
                x = x,
                y = y,
                d = d,
                x5chain = x5chain,
                additional = additional?.ifEmpty { null }
            )
        }
    }
}

/**
 * Represents an interface for a COSE (CBOR Object Signing and Encryption) key in CBOR (Concise Binary Object Representation) format.
 * This interface extends the IKey interface, providing additional properties specific to COSE keys.
 */


expect interface ICoseKeyCbor : IKey {
    /**
     * Represents the key type value for the COSE Key encoded using CBOR.
     *
     * This property holds the type of the key in CBOR format, which is crucial
     * for identifying the kind of cryptographic key being utilized. It ensures
     * compatibility and correct interpretation of the key type across different
     * security frameworks and cryptographic operations.
     */
    override val kty: CborUInt
    /**
     * An optional identifier (key ID) for this object, represented as a CBOR byte string.
     * This identifier is used to uniquely identify the object.
     */
    override val kid: CborByteString?
    /**
     * Represents the algorithm associated with the COSE key.
     *
     * This property holds the algorithm identifier as a CBOR unsigned integer (`CborUInt`).
     * It specifies the cryptographic algorithm that can be performed with this key.
     * The value may be null if the algorithm is not specified.
     *
     * This is used to determine the intended cryptographic operations and ensure
     * compatibility across different security frameworks and implementations.
     */
    override val alg: CborUInt?
    /**
     * Represents a list of key operations applicable to the cryptographic key.
     * The operations are enumerated in the CBOR array, where each item is
     * identified by an unsigned integer (CborUInt).
     *
     * This property is nullable, indicating that the key may not have
     * any specific operations associated with it.
     */
    override val key_ops: CborArray<CborUInt>?
    /**
     * Represents the base IV (Initialization Vector) of a cryptographic key in CBOR encoding.
     *
     * The baseIV is used in cryptographic operations to ensure that identical plaintexts encrypt to different ciphertexts,
     * adding an additional layer of security by making sure that pattern recognition and replay attacks are more difficult.
     *
     * It can be null if the base IV is not applicable or not specified for the given cryptographic key.
     */
    val baseIV: CborByteString?
    /**
     * Represents the `crv` (Curve) parameter in a cryptographic key.
     *
     * This parameter is typically used in elliptic curve cryptography to define the specific curve
     * on which the cryptographic operations will be performed. It may be `null` if not applicable
     * or not specified.
     */
    override val crv: CborUInt?
    /**
     * Represents the 'x' coordinate parameter for an elliptic curve key or a similar cryptographic key component.
     *
     * This value is typically used in the context of keys that rely on elliptic curve algorithms,
     * and it is essential for cryptographic operations involving such keys.
     *
     * The value may be null if the 'x' coordinate is not applicable or not set.
     */
    override val x: CborByteString?
    /**
     * Represents the y-coordinate of an elliptic curve point in cryptographic operations.
     *
     * Typically part of the public key for elliptic curve cryptography (ECC).
     *
     * The value can be null, indicating that the coordinate is not set or not applicable.
     */
    override val y: CborByteString?
    /**
     * Represents the private key or secret key material of the key.
     * This is typically a critical component in encryption and decryption
     * operations, and should be kept confidential and not exposed.
     */
    override val d: CborByteString?
    /**
     * Represents a CBOR (Concise Binary Object Representation) array containing CBOR byte strings.
     * This variable can be null, indicating that there may not be any data available in the array.
     */
    val x5chain: CborArray<CborByteString>?
    /**
     * A map that holds additional information where the keys are of type `NumberLabel`
     * and the values are `CborItem` objects of any type. This map can be nullable.
     */
    override val additional: CborMap<NumberLabel, CborItem<*>>?
}

/**
 * A class representing a COSE (CBOR Object Signing and Encryption) key as a CBOR (Concise Binary Object Representation) object.
 *
 * @param kty The key type.
 * @param kid The key identifier (optional).
 * @param alg The algorithm used with the key (optional).
 * @param key_ops The key operations (optional).
 * @param baseIV The base Initialization Vector (optional).
 * @param crv The elliptic curve (optional).
 * @param x The x coordinate (optional).
 * @param y The y coordinate (optional).
 * @param d The private key parameter (optional).
 * @param x5chain The X.509 certificate chain (optional).
 * @param additional Additional attributes (optional).
 */
@JsExport
data class CoseKeyCbor(
    override val kty: CborUInt,
    override val kid: CborByteString? = null,
    override val alg: CborUInt? = null,
    override val key_ops: CborArray<CborUInt>? = null,
    override val baseIV: CborByteString? = null,
    override val crv: CborUInt? = null,
    override val x: CborByteString? = null,
    override val y: CborByteString? = null,
    override val d: CborByteString? = null,
    override val x5chain: CborArray<CborByteString>? = null,
    override val additional: CborMap<NumberLabel, AnyCborItem>? = null
) : ICoseKeyCbor, CborView<CoseKeyCbor, CoseKeyJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map) {

    /**
     * Constructs and returns a CBOR (Concise Binary Object Representation) builder for the CoseKeyCbor object.
     *
     * This method configures the CBOR map with known properties and any additional key-value pairs.
     * Known properties include kty, kid, alg, key_ops, baseIV, crv, x5chain, d, x, and y.
     * Properties in the additional map will not overwrite the known properties.
     *
     * @return a CborBuilder configured with the properties of the CoseKeyCbor object.
     */
    override fun cborBuilder(): CborBuilder<CoseKeyCbor> {
        val mapBuilder = CborMap.Static.builder(this)
        additional?.value?.map { mapBuilder.put(it.key, it.value) }
        // doing these last to make sure the additional map does not overwrite known props
        mapBuilder
            .put(Static.KTY, kty)
            .put(Static.KID, kid, true)
            .put(Static.ALG, alg, true)
            .put(Static.KEY_OPS, key_ops, true)
            .put(Static.BASE_IV, baseIV, true)
            .put(Static.CRV, crv, true)
            .put(Static.X5_CHAIN, x5chain, true)
            .put(Static.D, d, true)
            .put(Static.X, x, true)
            .put(Static.Y, y, true)
        //todo additional
        return mapBuilder.end()
    }

    /**
     * Serializes the current object to a JSON representation of `CoseKeyJson`.
     *
     * @return A `CoseKeyJson` instance containing the serialized data.
     */
//todo: Probably  nice to be able to provide an encoding parm, but then we have to adjust the whole interface
    override fun toJson(): CoseKeyJson {
        return CoseKeyJson.Builder()
            .withKty(CoseKeyType.Static.fromValue(kty.value.toInt()))
            .withKid(kid?.let { kid.value.decodeToString() })
            .withAlg(alg?.let { CoseAlgorithm.Static.fromValue(it.value.toInt()) })
            .withKeyOps(key_ops?.value?.map { ko -> CoseKeyOperations.Static.fromValue(ko.value.toInt()) }
                ?.toTypedArray())
            .withBaseIV(baseIV?.value?.encodeTo(Encoding.BASE64URL))
            .withCrv(crv?.let { CoseCurve.Static.fromValue(it.value.toInt()) })
            /**
             * The "x5c" (X.509 certificate chain) parameter contains a chain of one
             *    or more PKIX certificates [RFC5280].  The certificate chain is
             *    represented as a JSON array of certificate value strings.  Each
             *    string in the array is a base64-encoded (Section 4 of [RFC4648] --
             *    not base64url-encoded) DER
             */
            .withX5Chain(x5chain?.encodeToArray(Encoding.BASE64)) // see note above about base64
            .withX(x?.value?.encodeTo(Encoding.BASE64URL))
            .withY(y?.value?.encodeTo(Encoding.BASE64URL))
            .withD(d?.value?.encodeTo(Encoding.BASE64URL))
            //todo additional
            .build()

    }

    /**
     * Retrieves the algorithm mapping based on the current algorithm value.
     *
     * @return An instance of AlgorithmMapping if the algorithm value is successfully mapped;
     *         otherwise, returns null.
     */
    override fun getAlgMapping(): SignatureAlgorithm? {
        return alg?.let { CoseAlgorithm.Static.fromValue(it.value.toInt())?.let { coseAlg -> SignatureAlgorithm.Static.fromCose(coseAlg) } }
    }

    /**
     * Retrieves the KeyTypeMapping corresponding to the current key type.
     *
     * @return An instance of KeyTypeMapping that represents the mapping derived from the current key type.
     */
    override fun getKtyMapping(): KeyTypeMapping {
        return KeyTypeMapping.Static.fromCose(CoseKeyType.Static.fromValue(this.kty.value.toInt()))
    }

    /**
     * This function returns an array of KeyOperationsMapping objects. It converts the key operations
     * (represented by their integer values) to their corresponding CoseKeyOperations and then maps these
     * to the respective KeyOperationsMapping.
     *
     * @return An array of KeyOperationsMapping objects or null if no key operations are defined.
     */
    override fun getKeyOperationsMapping(): Array<KeyOperationsMapping>? {
        return key_ops?.value?.map { KeyOperationsMapping.Static.fromCose(CoseKeyOperations.Static.fromValue(it.value.toInt())) }?.toTypedArray()
    }

    /**
     * Retrieves an array of x5c (X.509 certificate chain) encoded as Base64 strings.
     *
     * @return an array of Base64 encoded strings representing the x5c value, or null if the x5c value is not present.
     */
    override fun getX5cArray(): Array<String>? {
        // x5c is base64 not base64url! (see above)
        return x5chain?.value?.map { it.encodeTo(Encoding.BASE64) }?.toTypedArray()
    }

    /**
     * Checks if this object is equal to the specified object.
     *
     * @param other the object to compare this instance against.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoseKeyCbor) return false

        if (kty != other.kty) return false
        if (kid != other.kid) return false
        if (alg != other.alg) return false
        if (key_ops != other.key_ops) return false
        if (baseIV != other.baseIV) return false
        if (crv != other.crv) return false
        if (x != other.x) return false
        if (y != other.y) return false
        if (d != other.d) return false
        if (x5chain != other.x5chain) return false
        if (additional != other.additional) return false

        return true
    }

    /**
     * Computes the hash code for the current object.
     *
     * @return the hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = kty.hashCode()
        result = 31 * result + (kid?.hashCode() ?: 0)
        result = 31 * result + (alg?.hashCode() ?: 0)
        result = 31 * result + (key_ops?.hashCode() ?: 0)
        result = 31 * result + (baseIV?.hashCode() ?: 0)
        result = 31 * result + (crv?.hashCode() ?: 0)
        result = 31 * result + (x?.hashCode() ?: 0)
        result = 31 * result + (y?.hashCode() ?: 0)
        result = 31 * result + (d?.hashCode() ?: 0)
        result = 31 * result + (x5chain?.hashCode() ?: 0)
        result = 31 * result + (additional?.hashCode() ?: 0)
        return result
    }

    /**
     * Provides a string representation of the CoseKeyCbor object.
     *
     * @return A String containing values of the CoseKeyCbor fields.
     */
    override fun toString(): String {
        return "CoseKeyCbor(kty=$kty, kid=$kid, alg=$alg, key_ops=$key_ops, baseIV=$baseIV, crv=$crv, x=$x, y=$y, d=$d, x5chain=$x5chain, additional=$additional)"
    }


    /**
     * Builder class for constructing a `CoseKeyCbor` object.
     */
    class Builder {
        /**
         * Represents a CBOR unsigned integer type that is instantiated later in the lifecycle.
         * This variable is declared as `lateinit` to allow for instantiation at runtime,
         * ensuring that `kty` is only set when its value is definitively determined.
         *
         * It is of type `CborUInt`, which is a data type used for CBOR (Concise Binary Object Representation)
         * encoding, specifically to handle unsigned integers.
         */
        private lateinit var kty: CborUInt
        /**
         * A variable representing a potentially null CborByteString instance.
         *
         * This variable can be used to hold a value that represents a CBOR (Concise Binary Object Representation) byte string,
         * which is a part of the CBOR encoding format often used in data serialization. Being nullable, it can either hold
         * a CborByteString instance or be set to null if no value is present.
         */
        var kid: CborByteString? = null
        /**
         * Represents an optional CBOR (Concise Binary Object Representation) unsigned integer.
         *
         * This variable stores a nullable instance of `CborUInt`, which might be used for encoding or decoding
         * CBOR data structures, especially when handling unsigned integer values.
         * Initially set to null, it can be assigned a valid `CborUInt` object as needed in the program.
         */
        var alg: CborUInt? = null
        /**
         * Represents the key operations for a cryptographic key.
         *
         * @property key_ops A nullable variable of type `CborArray<CborUInt>` that
         *           holds the key operations associated with the cryptographic key.
         *           This variable can be null if no key operations are defined.
         */
        var key_ops: CborArray<CborUInt>? = null
        /**
         * `baseIV` is an optional CborByteString variable that can be used to store
         * the base Initialization Vector (IV) for cryptographic operations.
         * It is initialized to null, indicating that it may not always have a value.
         */
        var baseIV: CborByteString? = null
        /**
         * A nullable variable representing a CBOR (Concise Binary Object Representation) unsigned integer.
         * This variable may hold a value of type `CborUInt` or be null.
         */
        var crv: CborUInt? = null
        /**
         * Represents an optional CBOR (Concise Binary Object Representation) byte string.
         * The value can be null, indicating absence of a byte string.
         */
        var x: CborByteString? = null
        /**
         * Represents an optional CBOR encoded byte string.
         *
         * This variable can hold a null value or a byte string encoded using
         * the Concise Binary Object Representation (CBOR) standard. CBOR
         * is designed for small code size and small message size, making it
         * ideal for use in constrained environments.
         */
        var y: CborByteString? = null
        /**
         * A nullable variable representing a CBOR (Concise Binary Object Representation) byte string.
         * The default value is null.
         *
         * @property d The CBOR byte string.
         */
        var d: CborByteString? = null
        /**
         * A variable that holds an optional sequence of binary data items encoded as CBOR byte strings.
         * It is initialized to null and, when assigned, contains a CBOR array of CBOR byte strings.
         */
        var x5chain: CborArray<CborByteString>? = null
        /**
         * Represents an optional CBOR (Concise Binary Object Representation) map that holds additional
         * key-value pairs. The keys are of type `NumberLabel` and values can be any CBOR item.
         *
         * This can be used to store extra data or custom properties that extend the basic functionality
         * of the enclosing class without altering its core behavior.
         */
        var additional: CborMap<NumberLabel, AnyCborItem>? = null


        /**
         * Sets the key type (kty) for this builder.
         *
         * @param kty The key type to be set, represented as a [CoseKeyType].
         */
        @JsName("withKty")
        fun withKty(kty: CoseKeyType) = apply { this.kty = CborUInt(kty.value) }

        /**
         * Sets the 'kid' (Key ID) value for the current object. If the provided 'kid' is not null,
         * it converts the value to a CBOR byte string using UTF-8 encoding and assigns it to the object's 'kid' property.
         *
         * @param kid The Key ID value to set, or null if no value is provided.
         */
        @JsName("withKid")
        fun withKid(kid: String?) = apply { kid?.let { this.kid = it.toCborByteString(Encoding.UTF8) } }

        /**
         * Sets the COSE algorithm for the current builder instance.
         *
         * @param alg The algorithm to be set, which can be null. If provided, it will update the alg field.
         */
        @JsName("withAlg")
        fun withAlg(alg: CoseAlgorithm?) = apply { alg?.let { this.alg = CborUInt(it.value) } }

        /**
         * Updates the object with the provided key operations.
         *
         * @param key_ops An array of `CoseKeyOperations` to be applied. If null, no operation is performed.
         */
        @JsName("withKeyOps")
        fun withKeyOps(key_ops: Array<CoseKeyOperations>?) = apply {
            key_ops?.let {
                this.key_ops = CborArray(it.map { op -> op.toCbor() }.toMutableList())
            }
        }

        /**
         * Sets the base IV (Initialization Vector) for the COSE key using a hex string. The input hex string
         * will be converted to a CBOR (Concise Binary Object Representation) byte string in Base64URL encoding.
         *
         * @param baseIVHex A hex string representing the base IV to be set for the COSE key. If null, no action is taken.
         */
        @JsName("withBaseIV")
        fun withBaseIV(baseIVHex: String?) = apply { baseIVHex?.let { this.baseIV = it.toCborByteString(Encoding.BASE64URL) } }

        /**
         * Sets the 'crv' (Curve) value if provided.
         *
         * @param crv The CoseCurve instance to be set. Null if no curve is to be set.
         */
        @JsName("withCrv")
        fun withCrv(crv: CoseCurve?) = apply {
            crv?.let { this.crv = it.toCbor() }
        }

        /**
         * Sets the property `x` with the provided string.
         *
         * @param x The string value to set. If the input is not null, it is converted to a CBOR ByteString using BASE64URL encoding.
         */
        @JsName("withX")
        fun withX(x: String?) = apply { x?.let { this.x = it.toCborByteString(Encoding.BASE64URL) } }

        /**
         * Sets the 'y' property of the current object to a base64 URL-encoded CBOR byte string
         * if the provided 'y' string is not null.
         *
         * @param y The input string to be set and encoded if it's not null.
         */
        @JsName("withY")
        fun withY(y: String?) = apply { y?.let { this.y = it.toCborByteString(Encoding.BASE64URL) } }

        /**
         * Sets the value of `d` after encoding it to CBOR byte string with Base64URL encoding.
         * The method applies the transformation only if the provided string `d` is not null.
         *
         * @param d A nullable String to be encoded and set as the value of `d`.
         */
        @JsName("withD")
        fun withD(d: String?) = apply { d?.let { this.d = it.toCborByteString(Encoding.BASE64URL) } }

        /**
         * Sets the X.509 certificate chain (x5c) for the object.
         *
         * The "x5c" parameter contains a chain of one or more PKIX certificates as an array of
         * certificate value strings. Each string in the array is a base64-encoded representation
         * of the certificate.
         *
         * @param x5c an array of base64-encoded X.509 certificates
         */
        @JsName("withX5Chain")
        fun withX5Chain(x5c: Array<String>?) =
            apply {
                x5c?.let {
                    /**
                     * The "x5c" (X.509 certificate chain) parameter contains a chain of one
                     *    or more PKIX certificates [RFC5280].  The certificate chain is
                     *    represented as a JSON array of certificate value strings.  Each
                     *    string in the array is a base64-encoded (Section 4 of [RFC4648] --
                     *    not base64url-encoded) DER
                     */
                    this.x5chain =
                        CborArray(it.map { cert -> CborByteString(cert.decodeFrom(Encoding.BASE64)) }.toMutableList()) // see remark about base64
                }
            }

        // FIXME
//        fun withAdditional(additional: MutableMap<LongKMP, *>?) = apply { additional?.let { this.additional = CborMap(it) }}

        /**
         * Constructs a `CoseKeyCbor` instance using the properties provided
         * in the `Builder` class.
         *
         * @return A new instance of `CoseKeyCbor` with the specified properties.
         */
        fun build(): CoseKeyCbor {
            return CoseKeyCbor(
                kty = kty,
                alg = alg,
                kid = kid,
                key_ops = key_ops,
                baseIV = baseIV,
                crv = crv,
                x = x,
                y = y,
                d = d,
                x5chain = x5chain,
                additional = if (additional === null || additional?.value.isNullOrEmpty()) null else additional,
            )
        }
    }

    /**
     * An object containing predefined instances of `NumberLabel` used for encoding and decoding CBOR keys.
     */
    object Static {
        /**
         * Represents the 'Key Type' (kty) parameter in a COSE key object.
         *
         * This is a static identifier assigned the value `1` and used as a label
         * to map to a specific type of COSE key.
         */
        val KTY = NumberLabel(1)
        /**
         * KID is a predefined NumberLabel with a value of 2. It is used to identify key ID fields within the COSE key structure.
         */
        val KID = NumberLabel(2)
        /**
         * Represents the algorithm (alg) label in the COSE Key structure.
         * This label is associated with a numeric value that specifies the algorithm used.
         * The value is encapsulated in a `NumberLabel` object with a predefined numerical identifier.
         * In this case, the numeric identifier is set to 3.
         */
        val ALG = NumberLabel(3)
        /**
         * Represents a numeric label used specifically for key operations in COSE (CBOR Object Signing and Encryption).
         * The value of `KEY_OPS` is set to 4, which corresponds to the specific operation type in the COSE key operation registry.
         */
        val KEY_OPS = NumberLabel(4)
        /**
         * A constant representing the initial value of a number label, typically used for cryptographic purposes.
         *
         * This value is set to `5` and is utilized within the `CoseKeyCbor` class as a part of its properties
         * while building CBOR maps or interpreting CBOR items.
         *
         * The `NumberLabel` class, which `BASE_IV` is an instance of, is designed to handle both positive and negative integers
         * and provides functionality for JSON serialization and CBOR encoding.
         */
        val BASE_IV = NumberLabel(5)
        /**
         * Represents the COSE (CBOR Object Signing and Encryption) label for the X.509 certificate chain.
         * This label is used to identify and encode the X5Chain field within COSE keys.
         *
         * In the context of CBOR encoding/decoding, this label denotes the presence of a certificate chain
         * and is associated with the integer value 33, which is the label's assigned identifier.
         *
         * The `NumberLabel` class is used to construct this label, ensuring it conforms to the CBOR requirements
         * for integer labels, whether they are positive or negative.
         */
        val X5_CHAIN = NumberLabel(33)

        /**
         * Represents the curve (crv) parameter for Elliptic Curve (EC) and OKP key types.
         *
         * The value is managed as a number label and it is used to specify the curve for EC (key type 2) and OKP (key type 1) keys.
         */
// EC (kty 2) + OKP (kty 1)
        val CRV = NumberLabel(-1)
        /**
         * A constant instance of `NumberLabel` initialized with the value -2.
         * This instance represents a numbered label with a negative integer value,
         * which typically corresponds to a specific interpretation within the CBOR (Concise Binary Object Representation) encoding scheme.
         *
         * The `NumberLabel` class extends `CoseLabel` with a specific value type and major type based on the sign of the integer.
         * Instances of `NumberLabel` can be serialized into CBOR encoding, making them useful for compact binary representations
         * in cryptographic contexts and data serialization.
         */
        val X = NumberLabel(-2)
        /**
         * Y is a constant variable of type NumberLabel.
         *
         * This variable is initialized with the value -3, representing a specific label
         * in the context where NumberLabel is employed. NumberLabel is assumed to be a data structure
         * or class that associates numbers with certain labels or designations.
         *
         * Usage of this variable can be found in scenarios where predefined numerical labels
         * are necessary, aiding in categorization or labeling processes in the code base.
         */
        val Y = NumberLabel(-3)
        /**
         * Represents the negative integer label `D` used in COSE (CBOR Object Signing and Encryption).
         * This label is set to -4 which typically indicates a specific parameter in a COSE key structure.
         *
         * This label is encapsulated in the `NumberLabel` class which is designed to handle integer-based COSE labels.
         */
        val D = NumberLabel(-4)

        /**
         * Represents the RSA modulus in a cryptographic context.
         *
         * `N` is initialized with a placeholder value of -1.
         * Its actual value should be set to a valid RSA modulus as part of the key generation process.
         *
         * @property N The RSA modulus, initialized to -1.
         */
// RSA, kty 3, TODO
        val N = NumberLabel(-1)
        /**
         * Label representing a specific numeric identifier with the value of -2.
         * Typically used as a constant in contexts where differentiating or specifying
         * a unique identifier is required.
         *
         * @property E the constant representing the numeric label with a value of -2.
         */
        val E = NumberLabel(-2)
        /**
         * D_RSA is a constant used to label a specific number associated with RSA encryption.
         * It is used in the context of NumberLabel to represent the number -3.
         */
        val D_RSA = NumberLabel(-3)
        /**
         * Constant representing a specific instance of NumberLabel initialized with the value -4.
         *
         * This variable is used as a predefined label for numerical operations or classifications
         * where a label with the value -4 is needed. It encapsulates the negative integer -4 within
         * a NumberLabel object to provide context or categorization in various computations.
         */
        val P = NumberLabel(-4)
        /**
         * Constant variable representing a NumberLabel with a value of -5.
         *
         * This variable is used to define a NumberLabel instance which encapsulates the
         * integer value of -5. It can be used in various parts of the application where
         * a NumberLabel with this specific value is needed.
         */
        val Q = NumberLabel(-5)
        /**
         * DP is a constant representing a negative number label with a value of -6.
         *
         * This constant can be used in scenarios where a predefined negative number,
         * specifically -6, is required. The label is encapsulated within a NumberLabel
         * object, which may provide additional functionality or encapsulation benefits.
         */
        val DP = NumberLabel(-6)
        /**
         * DQ is an instance of the NumberLabel class initialized with -7.
         *
         * Used for representing a specific constant value with an associated label in the application.
         */
        val DQ = NumberLabel(-7)
        /**
         * QINV is a constant instance of NumberLabel initialized with the value -8.
         * The purpose and usage of QINV depend on the context within the encompassing application.
         * NumberLabel generally denotes a label associated with a numeric value.
         */
        val QINV = NumberLabel(-8)
        /**
         * A predefined instance of `NumberLabel` initialized with the value `-9`.
         * This constant can be used where a `NumberLabel` with this specific value is required.
         */
        val OTHER = NumberLabel(-9)
        /**
         * Represents a constant number label with a value of -10.
         *
         * This variable is typically used to denote a specific numerical identifier or label
         * within a range of values. The negative value may indicate a special state or condition
         * in the context of its use.
         */
        val R_I = NumberLabel(-10)
        /**
         * A constant representing the label with a numerical value of -11.
         *
         * This variable is intended for use in scenarios where a predefined
         * numerical label is needed. The value -11 can be significant in
         * various algorithmic or domain-specific contexts.
         *
         * Note: The exact purpose and use case of this variable can vary
         * based on the overall application logic and domain requirements.
         */
        val D_I = NumberLabel(-11)
        /**
         * A constant representing a `NumberLabel` with a value of -12.
         *
         * The `T_I` variable is initialized with a `NumberLabel` instance
         * that holds the integer value -12. This constant is used in
         * scenarios where a specific number label with the value -12
         * is needed.
         */
        val T_I = NumberLabel(-12)

        /**
         * A set of labels that may be used for cryptographic key attributes.
         *
         * The labels included in the set denote various parameters or characteristics
         * associated with cryptographic keys. They are commonly referenced in key
         * management and cryptographic operations.
         *
         * The labels are:
         * - KTY: Key Type
         * - KID: Key ID
         * - ALG: Algorithm
         * - KEY_OPS: Key Operations
         * - BASE_IV: Base Initialization Vector
         * - CRV: Curve
         * - X5_CHAIN: X.509 Certificate Chain
         * - X: X Coordinate
         * - Y: Y Coordinate
         * - D: Private Key
         *
         * TODO: Include support for RSA keys.
         */
        val labels = setOf(KTY, KID, ALG, KEY_OPS, BASE_IV, CRV, X5_CHAIN, X, Y, D) // TODO RSA


        /**
         * Returns a new instance of the Builder class for constructing a CoseKeyCbor object.
         *
         * The Builder provides a fluent interface to set various properties of the CoseKeyCbor
         * object. The properties include kty, kid, alg, key_ops, baseIV, crv, x, y, d, x5chain,
         * and additional fields. Once all desired properties are set, the build() method is called
         * to create an instance of CoseKeyCbor.
         *
         * @return A new Builder instance for constructing a CoseKeyCbor object.
         */
        fun builder() = Builder()

        /**
         * Converts an `ICoseKeyCbor` data transfer object (DTO) into a `CoseKeyCbor` instance.
         *
         * @param dto The `ICoseKeyCbor` instance representing the DTO to be converted.
         * @return A new `CoseKeyCbor` instance populated with the properties of the DTO.
         */
        @JsName("fromDTO")
        fun fromDTO(dto: ICoseKeyCbor) = with(dto) {
            CoseKeyCbor(
                kty = kty,
                kid = kid,
                alg = alg,
                key_ops = key_ops,
                baseIV = baseIV,
                crv = crv,
                x = x,
                y = y,
                d = d,
                x5chain = x5chain,
                additional = additional
            )
        }

        /**
         * Decodes the provided CBOR-encoded byte array into a `CoseKeyCbor` object.
         *
         * @param encodedDeviceEngagement The CBOR-encoded byte array representing the device engagement data.
         * @return The decoded `CoseKeyCbor` object.
         */
        @JsName("cborDecode")
        fun cborDecode(encodedDeviceEngagement: ByteArray): CoseKeyCbor =
            fromCborItem(cborSerializer.decode(encodedDeviceEngagement))

        /**
         * Converts a given CBOR map into a COSE key object.
         *
         * @param m The CBOR map containing key-value pairs representing the COSE key.
         * @return The constructed `CoseKeyCbor` object based on the provided CBOR map.
         * @throws IllegalArgumentException if the key type is RSA, which is not supported.
         */
        @JsName("fromCborItem")
        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>): CoseKeyCbor {
            val kty = KTY.required<CborUInt>(m)
            val keyType = CoseKeyType.Static.fromValue(kty.value.toInt())
            if (keyType === CoseKeyType.RSA) {
                throw IllegalArgumentException("RSA type not supported yet")
            }
            val additional = mutableMapOf(* m.value.entries.filter { labels.contains(it.key) == false }.map { Pair(it.key, it.value) }.toTypedArray())
            return CoseKeyCbor(
                kty = KTY.required(m),
                kid = KID.optional(m),
                alg = ALG.optional(m),
                key_ops = KEY_OPS.optional(m),
                baseIV = BASE_IV.optional(m),
                crv = CRV.optional(m),
                x5chain = X5_CHAIN.optional(m),
                x = X.optional(m),
                y = Y.optional(m),
                d = D.optional(m),
                additional = if (additional.isEmpty()) null else CborMap(additional)
            )
        }
    }
}

/**
 * Type alias for CoseKeyCbor, which represents a COSE key.
 */
typealias COSE_Key = CoseKeyCbor
