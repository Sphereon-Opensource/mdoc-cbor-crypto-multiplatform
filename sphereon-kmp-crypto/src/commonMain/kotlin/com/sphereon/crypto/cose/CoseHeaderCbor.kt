package com.sphereon.crypto.cose

import com.sphereon.cbor.AbstractCborInt
import com.sphereon.cbor.AnyCborItem
import com.sphereon.cbor.CDDL
import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborBuilder
import com.sphereon.cbor.CborByteString
import com.sphereon.cbor.CborMap
import com.sphereon.cbor.CborString
import com.sphereon.cbor.CborView
import com.sphereon.json.JsonView
import com.sphereon.cbor.NumberLabel
import com.sphereon.cbor.cborSerializer
import com.sphereon.cbor.encodeToBase64Array
import com.sphereon.cbor.encodeToCborByteArray
import com.sphereon.cbor.toCborByteString
import com.sphereon.cbor.toCborString
import com.sphereon.cbor.toCborStringArray
import com.sphereon.cbor.toNumberLabel
import com.sphereon.cbor.toStringArray
import com.sphereon.json.cryptoJsonSerializer
import com.sphereon.kmp.Encoding
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.numberToKmpLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.js.JsExport

/**
 * Represents a COSE (CBOR Object Signing and Encryption) header in JSON format.
 *
 * This data class provides various common COSE header parameters as specified in RFC 8152.
 * The parameters are defined to describe the cryptographic algorithm, critical fields,
 * content type, key identifier, initialization vector, partial initialization vector,
 * and x.509 certificate chain.
 *
 * @property alg The COSE algorithm parameter.
 * @property crit An array of critical headers.
 * @property contentType The content type of the payload.
 * @property kid The key identifier for the key used.
 * @property iv Initialization vector for encryption.
 * @property partialIv Partial initialization vector.
 * @property x5chain An array representing the x.509 certificate chain.
 */
@JsExport
@Serializable
data class CoseHeaderJson(

    /**
     * 3.1.  Common COSE Headers Parameters are listed below
     *
     * See https://www.rfc-editor.org/rfc/rfc8152 Table 2
     *
     */
    val alg: CoseAlgorithm? = null,
    val crit: Array<String>? = null,
    val contentType: String? = null,
    val kid: String? = null,
    val iv: String? = null,
    val partialIv: String? = null,
    val x5chain: Array<String>? = null,
) : JsonView() {
    /**
     * Converts the current instance of `CoseHeaderJson` to a JSON string.
     *
     * This method uses `cryptoJsonSerializer` to serialize the current
     * instance to its JSON representation.
     *
     * @return A JSON string representation of the current `CoseHeaderJson` object.
     */
    override fun toJsonString() = cryptoJsonSerializer.encodeToString(this)

    /**
     * Converts the current CoseHeader object to its CBOR (Concise Binary Object Representation) format.
     *
     * @return An instance of CoseHeaderCbor that represents the CBOR-encoded header information.
     */
    override fun toCbor(): CoseHeaderCbor = CoseHeaderCbor(
        alg = alg,
        crit = crit?.toCborStringArray(),
        contentType = contentType?.toCborString(),
        kid = kid?.toCborByteString(Encoding.UTF8),
        partialIv = partialIv?.toCborByteString(Encoding.UTF8),
        iv = iv?.toCborByteString(Encoding.UTF8),
        x5chain = x5chain?.encodeToCborByteArray(Encoding.BASE64) // base64 not url
    )

    // TODO: To JOSE
}


/**
 * Represents a COSE header encoded in CBOR format.
 *
 * @property alg The algorithm identifier.
 * @property crit Critical headers that must be understood.
 * @property contentType A string describing the content type.
 * @property kid The key identifier.
 * @property iv Initialization vector for cipher operations.
 * @property partialIv Partial initialization vector for cipher operations.
 * @property x5chain Certificate chain.
 */
@JsExport
data class CoseHeaderCbor(

    /**
     * 3.1.  Common COSE Headers Parameters are listed below
     *
     * See https://www.rfc-editor.org/rfc/rfc8152 Table 2
     *
     */

    val alg: CoseAlgorithm? = null,
    val crit: CborArray<CborString>? = null,
    val contentType: CborString? = null,
    var kid: CborByteString? = null,
    val iv: CborByteString? = null,
    val partialIv: CborByteString? = null,
    var x5chain: CborArray<CborByteString>? = null,
) : CborView<CoseHeaderCbor, CoseHeaderJson, CborMap<NumberLabel, AnyCborItem>>(CDDL.map) {
    /**
     * Utility object containing static properties and methods for handling COSE headers.
     */
    object Static {
        val ALG = NumberLabel(1)
        val CRIT = NumberLabel(2)
        val CONTENT_TYPE = NumberLabel(3)
        val KID = NumberLabel(4)
        val IV = NumberLabel(5)
        val PARTIAL_IV = NumberLabel(6)
        val X5CHAIN = NumberLabel(33)

        /**
         * Copies the given `CoseHeaderCbor` object or initializes a new one if the provided object is null.
         *
         * @param other The `CoseHeaderCbor` object to copy. If null, a new `CoseHeaderCbor` object is initialized.
         * @return A new `CoseHeaderCbor` object, either a copy of the provided object or a newly created one.
         */
        fun copyOrInit(other: CoseHeaderCbor?) = if (other === null) CoseHeaderCbor() else other.copy()

        /**
         * Converts a CBOR map to a CoseHeaderCbor object.
         *
         * @param m The CBOR map containing NumberLabel keys and AnyCborItem values.
         * @return A CoseHeaderCbor object populated with the converted values from the provided CBOR map.
         */
        fun fromCborItem(m: CborMap<NumberLabel, AnyCborItem>): CoseHeaderCbor {
            val algValue = ALG.optional<AbstractCborInt<LongKMP>>(m)
                ?.let { if (it.cddl == CDDL.nint) -it.value.toInt() else it.value.toInt() }

            /**
             * If a single certificate is conveyed, it is placed in a CBOR byte string.
             * If multiple certificates are conveyed, a CBOR array of byte strings is used, with each certificate being in its own byte string.
             */
            var x5Chain = X5CHAIN.optional<AnyCborItem>(m)
            if (x5Chain is CborByteString) {
                x5Chain = CborArray(mutableListOf(x5Chain))
            }
            @Suppress("UNCHECKED_CAST") return CoseHeaderCbor(
                alg = CoseAlgorithm.Static.fromValue(algValue),
                crit = CRIT.optional(m),
                contentType = CONTENT_TYPE.optional(m),
                kid = KID.optional(m),
                iv = IV.optional(m),
                partialIv = PARTIAL_IV.optional(m),
                x5chain = if (x5Chain != null) x5Chain as CborArray<CborByteString> else null
            )
        }

        /**
         * Decodes a CBOR encoded byte array into its corresponding object.
         *
         * @param encoded The byte array containing CBOR encoded data.
         * @return The decoded object.
         */
        fun cborDecode(encoded: ByteArray) = fromCborItem(cborSerializer.decode(encoded))
    }

    /**
     * Builds a CBOR representation of the CoseHeaderCbor.
     *
     * The method constructs a CBOR map using the fields of the class:
     * - ALG: The algorithm identifier.
     * - CRIT: Critical headers.
     * - CONTENT_TYPE: The content type.
     * - KID: Key ID.
     * - IV: Initialization Vector.
     * - PARTIAL_IV: Partial Initialization Vector.
     * - X5CHAIN: The X.509 certificate chain.
     *
     * If a single certificate is conveyed, it is placed in a CBOR byte string.
     * If multiple certificates are conveyed, a CBOR array of byte strings is used, with each certificate being in its own byte string.
     *
     * @return A CborBuilder initialized with the various fields of the CoseHeaderCbor.
     */
    override fun cborBuilder(): CborBuilder<CoseHeaderCbor> {
        return CborMap.Static.builder(this).put(Static.ALG, alg?.value?.numberToKmpLong()?.toInt()?.toNumberLabel(), true)
            .put(Static.CRIT, crit, true).put(Static.CONTENT_TYPE, contentType, true).put(Static.KID, kid, true).put(Static.IV, iv, true)
            .put(Static.PARTIAL_IV, partialIv, true)
            /**
             * If a single certificate is conveyed, it is placed in a CBOR byte string.
             * If multiple certificates are conveyed, a CBOR array of byte strings is used, with each certificate being in its own byte string.
             *
             */
            .put(Static.X5CHAIN, if (x5chain?.value?.size == 1) x5chain!!.value[0] else x5chain, true).end()

    }


    /**
     * Converts the COSE header to a JSON representation.
     *
     * @return A CoseHeaderJson object containing the JSON representation of the COSE header.
     */
    @OptIn(ExperimentalStdlibApi::class)
    override fun toJson(): CoseHeaderJson = CoseHeaderJson(
        alg = alg,
        crit = crit?.toStringArray(),
        contentType = contentType?.toString(),
        kid = kid?.encodeTo(Encoding.UTF8),
        iv = iv?.encodeTo(Encoding.UTF8),
        partialIv = partialIv?.encodeTo(Encoding.UTF8),
        x5chain = x5chain?.encodeToBase64Array()
    )

    /**
     * Checks if all the properties (x5chain, alg, partialIv, kid, iv, crit, contentType) are null.
     *
     * @return true if all properties are null; false otherwise.
     */
    fun isEmpty(): Boolean {
        return this.x5chain == null && this.alg == null && this.partialIv == null && this.kid == null && this.iv == null && this.crit == null && this.contentType == null
    }


    /**
     * Checks if this CoseHeaderCbor object is equal to another object.
     * Two CoseHeaderCbor objects are considered equal if all their
     * corresponding fields (alg, crit, contentType, kid, iv, partialIv, x5chain) are equal.
     *
     * @param other the object to compare with this CoseHeaderCbor object.
     * @return true if the specified object is equal to this CoseHeaderCbor object, false otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CoseHeaderCbor) return false

        if (alg != other.alg) return false
        if (crit != other.crit) return false
        if (contentType != other.contentType) return false
        if (kid != other.kid) return false
        if (iv != other.iv) return false
        if (partialIv != other.partialIv) return false
        if (x5chain != other.x5chain) return false

        return true
    }

    /**
     * Generates a hash code value for this object based on its fields.
     *
     * @return an integer hash code value representing this object.
     */
    override fun hashCode(): Int {
        var result = alg?.hashCode() ?: 0
        result = 31 * result + (crit?.hashCode() ?: 0)
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (kid?.hashCode() ?: 0)
        result = 31 * result + (iv?.hashCode() ?: 0)
        result = 31 * result + (partialIv?.hashCode() ?: 0)
        result = 31 * result + (x5chain?.hashCode() ?: 0)
        return result
    }

    /**
     * Returns a string representation of the `CoseHeaderCbor` instance.
     *
     * @return a string that includes the values of `alg`, `crit`, `contentType`, `kid`, `iv`, `partialIv`, and `x5chain`
     */
    override fun toString(): String {
        return "CoseHeaderCbor(alg=$alg, crit=$crit, contentType=$contentType, kid=$kid, iv=$iv, partialIv=$partialIv, x5chain=$x5chain)"
    }


}
