@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")

package com.sphereon.cbor


import com.sphereon.kmp.decodeFromHex
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport


@JsExport
class CborHexEncodedItem(hex: String) : CborByteString(
    hex.decodeFromHex()
)

/**
 * CBOR Tag 24 support (CBOR data item)
 *
 * From https://datatracker.ietf.org/doc/html/rfc7049#section-2.4.4.1 Encoded CBOR Data Item
 *
 * Sometimes it is beneficial to carry an embedded CBOR data item that
 *    is not meant to be decoded immediately at the time the enclosing data
 *    item is being parsed.  Tag 24 (CBOR data item) can be used to tag the
 *    embedded byte string as a data item encoded in CBOR format.
 *
 */
@JsExport
open class CborEncodedItem<Type>(
    val decodedValue: Type,
    value: CborByteString = cborSerializeViewOrItem(decodedValue)
) : CborItem<CborTagged<cddl_bstr>>(CborTagged(24, value), CDDL.bstr) {

    override fun toJson(): JsonElement {
        // TODO. Do we want to use the decoded value?
        return value.toJson()
    }

    fun <Type : AnyCborItem> cborDecode(): Type {
        return when (decodedValue) {
            is CborView<*,*,*> -> decodedValue.toCbor() as Type
            is AnyCborItem -> decodedValue as Type
            else -> throw IllegalArgumentException("A CborEncoded item or CborView is required")
        }
    }

    companion object {
        fun cborSerializeViewOrItem(deserializedValue: Any?): CborByteString {
            return when (deserializedValue) {
                is AnyCborItem -> CborByteString(cborSerializer.encode(deserializedValue))
                is CborView<*, *, *> -> CborByteString(deserializedValue.cborEncode())
                else -> throw IllegalArgumentException("A CborEncoded item or CborView is required")
            }
        }

        fun <Type : AnyCborItem> fromDecodedValue(
            decodedValue: Type
        ): CborEncodedItem<Type> {
            return CborEncodedItem(decodedValue, CborByteString(cborSerializer.encode(decodedValue)))
        }

        fun <Type : AnyCborItem> toDecodedValue(encoded: CborEncodedItem<Type>): Type {
            return encoded.decodedValue
        }


    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CborEncodedItem<*>

        return decodedValue == other.decodedValue
    }

    override fun hashCode(): Int {
        return decodedValue?.hashCode() ?: 0
    }

    override fun encode(builder: ByteStringBuilder) {
        value.encode(builder)
        /*if (decodedValue is CborView<*, *, *>) {
            return CborTagged(
                CborTagged.ENCODED_CBOR,
                (decodedValue as CborView<*, *, *>).toCborItem() as AnyCborItem
            ).encode(builder)
        }

        return CborTagged(CborTagged.ENCODED_CBOR, cborSerializer.decode(value.value) as AnyCborItem).encode(builder)*/
    }


    override fun toString(): String {
        return "CborEncodedItem(deserializedValue=$decodedValue, value=$value)"
    }

}

