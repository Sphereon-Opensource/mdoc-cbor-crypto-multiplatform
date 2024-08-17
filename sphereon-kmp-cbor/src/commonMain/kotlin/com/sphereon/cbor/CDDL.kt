@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")

package com.sphereon.cbor

import com.sphereon.cbor.CborTagged.Companion.DATE_TIME_STRING
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.numberToKmpLong
import com.sphereon.kmp.toKmpLong
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

/**
 * CDDL type mappings.
 * We prefix them to not have them mistaken with KMP or other programming language types
 */

typealias cddl_int = LongKMP
typealias cddl_uint = LongKMP // We do have a specific Cbor item class and validation
typealias cddl_nint = LongKMP
typealias cddl_bstr = ByteArray
typealias cddl_tstr = String
typealias cddl_text = String
typealias cddl_float = Float
typealias cddl_float16 = cddl_float
typealias cddl_float32 = cddl_float
typealias cddl_float64 = Double
typealias cddl_bool = Boolean
typealias cddl_false = cddl_bool
typealias cddl_true = cddl_bool
typealias cddl_nil = Unit?
typealias cddl_null = cddl_nil
typealias cddl_undefined = Unit
typealias cddl_map<K, V> = MutableMap<K, V>
typealias cddl_list<V> = MutableList<V>
typealias cddl_tdate = String
typealias cddl_time = LongKMP
typealias cddl_full_date = String
typealias cddl_any = Any

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = CDDLSerializer::class)
sealed interface CDDLType {
    val format: String
    val majorType: MajorType?
    val info: Int?
    val aliasFor: Array<CDDLType>
    fun toTag(additionalInfo: Int? = null): String

//    fun newCborItem(value: Any?): CborItem<out Any?>

}


/**
 *  The initial byte of each data item contains both information about
 *    the major type (the high-order 3 bits, described in Section 2.1) and
 *    additional information (the low-order 5 bits).  When the value of the
 *    additional information is less than 24, it is directly used as a
 *    small unsigned integer.  When it is 24 to 27, the additional bytes
 *    for a variable-length integer immediately follow; the values 24 to 27
 *    of the additional information specify that its length is a 1-, 2-,
 *    4-, or 8-byte unsigned integer, respectively.  Additional information
 *    value 31 is used for indefinite-length items, described in
 *    Section 2.2.  Additional information values 28 to 30 are reserved for
 *    future expansion.
 */
@Suppress("UNCHECKED_CAST")
@JsExport
@Serializable(with = CDDLSerializer::class)
sealed class CDDL(
    override val format: String,
    override val majorType: MajorType? = null,
    override val info: Int? = null,
    override val aliasFor: Array<CDDLType> = arrayOf(),
    ) : CDDLType {

    /*fun <T : Any?> anyToType(value: T): CDDLType {
        if (value == null) {
            return nil
        } else if (value == Unit) {
            return undefined
        }
        return when (value) {
            is cddl_tstr -> tstr
            is LongKMP -> if (value.toLong() < 0L) nint else uint
            is cddl_bstr -> bstr
            else -> throw IllegalArgumentException("could not determine type for $value")
        }
    }
*/
    fun <T : Any> newCborItem(value: T?): CborItem<out Any?> {
        // We are not using inheritance for the methods as that would impact JS export.
        // Since this is a sealed class anyway that is not too bad
        if (value == null) {
            return nil.newNil()
        } else if (value == Unit) {
            return undefined.newUndefined()
        }
        return when (this) {
            tstr -> tstr.newString(value.toString())
            False -> False.newFalse()
            Null -> Null.newNull()
            True -> True.newTrue()
            any -> return when (value) {
                is cddl_tstr -> tstr.newString(value)
                is cddl_uint -> uint.newUint(value)
                is cddl_int -> int.newLong(value.toLong())
                is cddl_bstr -> bstr.newByteString(value)
                is cddl_tdate -> tdate.newTDate(value)
                is cddl_full_date -> full_date.newFullDate(value)
                is cddl_float64 -> float64.newFloat64(value)
                is cddl_float -> float.newFloat(value)
                is cddl_bool -> bool.newBool(value)
                is cddl_list<*> -> list.newList(value.map {
                    if (it is AnyCborItem) it else any.newCborItem(
                        it
                    )
                }.toMutableList())

                is cddl_map<*, *> -> map.newMap(mutableMapOf(* value.map {
                    Pair(
                        if (it.key is AnyCborItem) it.key as AnyCborItem else any.newCborItem(it.key),
                        if (it.value is AnyCborItem) it.value as AnyCborItem else any.newCborItem(it.value)
                    )
                }.toTypedArray()))

                else -> throw IllegalArgumentException("newCborItem for ${value} Not implemented yet")
            }

            bool -> bool.newBool(value == true)
            bstr -> bstr.newByteString(value as ByteArray)
            bstr_indef_length -> bstr_indef_length.newByteString(value as List<cddl_bstr>)
            bytes -> bytes.newBytes(value as ByteArray)
            float -> float.newFloat(value as Float)
            float16 -> float16.newFloat16(value as Float)
            float32 -> float32.newFloat32(value as Float)
            float64 -> float64.newFloat64(value as Double)
            full_date -> full_date.newFullDate(value as cddl_full_date)
            int -> int.newInt(value as Int)
            list -> if (value is CborArray<*>) value else list.newList((value as MutableList<*>).map {
                if (it is AnyCborItem) it else any.newCborItem(
                    it
                )
            }.toMutableList())

            map -> if (value is CborMap<*, *>) value else map.newMap(mutableMapOf(* (value as MutableMap<*, *>).map {
                Pair(
                    if (it.key is AnyCborItem) it.key as AnyCborItem else any.newCborItem(it.key),
                    if (it.value is AnyCborItem) it.value as AnyCborItem else any.newCborItem(it.value)
                )
            }.toTypedArray())) // fixme. Needs inspection of keys and values and map type
            nil -> nil.newNil()
            nint -> nint.newNInt(if (value is LongKMP) value else if (value is Number) value.toKmpLong() else value as LongKMP)
            tdate -> tdate.newTDate(value as cddl_tdate)
            text -> text.newText(value as cddl_text)
            time -> time.newTime(value as cddl_time)
            tstr_indef_length -> tstr_indef_length.newStringIndefLength(value as List<cddl_tstr>)
            uint -> uint.newUint(if (value is LongKMP) value else if (value is Number) value.toKmpLong() else value as LongKMP)
            undefined -> undefined.newUndefined()
        }

    }

    object tstr : CDDL("tstr", MajorType.UNICODE_STRING) {
        fun newString(value: cddl_tstr) = CborString(value)

    }

    @Serializable(with = CDDLSerializer::class)
    object uint : CDDL("uint", MajorType.UNSIGNED_INTEGER) {
        fun newUint(value: cddl_uint) = CborUInt(value)


    }

    @Serializable(with = CDDLSerializer::class)
    object nint : CDDL("nint", MajorType.NEGATIVE_INTEGER) {
        fun newNInt(value: cddl_nint) = CborNInt(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object int :
        CDDL("int", null, null, aliasFor = arrayOf(uint, nint)) {
        fun newInt(value: Int) =
            if (value < 0) CborNInt(value.numberToKmpLong()) else CborUInt(value.numberToKmpLong())

        fun newLong(value: Long) =
            if (value < 0) CborNInt(value.numberToKmpLong()) else CborUInt(value.numberToKmpLong())


    }

    @Serializable(with = CDDLSerializer::class)
    object bstr : CDDL("bstr", MajorType.BYTE_STRING) {
        fun newByteString(value: cddl_bstr) = CborByteString(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object bstr_indef_length : CDDL("bstr", MajorType.BYTE_STRING) {
        fun newByteString(value: List<cddl_bstr>) = CborByteStringIndefLength(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object bytes : CDDL("bytes", MajorType.BYTE_STRING, aliasFor = arrayOf(bstr)) {
        fun newBytes(value: cddl_bstr) = CborByteString(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object tstr_indef_length : CDDL("tstr", MajorType.UNICODE_STRING) {
        fun newStringIndefLength(value: List<cddl_tstr>) = CborStringIndefLength(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object text : CDDL("text", MajorType.UNICODE_STRING, aliasFor = arrayOf(tstr)) {
        fun newText(value: cddl_text) = CborString(value)
    }


    @Serializable(with = CDDLSerializer::class)
    object tdate : CDDL(
        "tdate", MajorType.TAG, DATE_TIME_STRING
    ) // RFC 7049, section 2.4.1, a tdate data item shall contain a date-time string as specified in RFC 3339
    {
        fun newTDate(value: cddl_tdate) = CborTDate(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object full_date : CDDL(
        "full-date", MajorType.TAG, CborTagged.FULL_DATE_STRING
    ) // #6.1004(tstr) In accordance with RFC 8943, a full-date data item shall contain a full-datestring as specified in RFC 3339.
    {
        fun newFullDate(value: cddl_full_date) = CborFullDate(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object time : CDDL(
        "time", MajorType.TAG, CborTagged.DATE_TIME_NUMBER
    ) // RFC 7049, section 2.4.1, a tdate data item shall contain a date-time number as specified in RFC 3339
    {
        fun newTime(value: cddl_time) = CborTime(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object float16 : CDDL("float16", MajorType.SPECIAL, 25) {
        fun newFloat16(value: cddl_float16) = CborFloat16(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object float32 : CDDL("float32", MajorType.SPECIAL, 26) {
        fun newFloat32(value: cddl_float32) = CborFloat32(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object float64 : CDDL("float64", MajorType.SPECIAL, 27) {
        fun newFloat64(value: cddl_float64) = CborDouble(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object float : CDDL("float", MajorType.SPECIAL, aliasFor = arrayOf(float16, float32, float64)) {
        fun newFloat(value: cddl_float) = CborFloat32(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object False : CDDL("false", MajorType.SPECIAL, 20) {
        fun newFalse() = CborSimple.FALSE
    }

    @Serializable(with = CDDLSerializer::class)
    object True : CDDL("true", MajorType.SPECIAL, 21) {
        fun newTrue() = CborSimple.TRUE
    }

    @Serializable(with = CDDLSerializer::class)
    object bool :
        CDDL("bool", MajorType.SPECIAL, aliasFor = arrayOf(False, True)) {
        fun newBool(value: cddl_bool) = if (value) CborSimple.TRUE else CborSimple.FALSE
    }

    @Serializable(with = CDDLSerializer::class)
    object nil : CDDL("nil", MajorType.SPECIAL, 22) {
        fun newNil() = CborSimple.NULL

    }

    @Serializable(with = CDDLSerializer::class)
    object Null : CDDL("null", MajorType.SPECIAL, 22, arrayOf(nil)) {
        fun newNull() = CborSimple.NULL
    }

    @Serializable(with = CDDLSerializer::class)
    object undefined :
        CDDL("undefined", MajorType.SPECIAL, 23) {
        fun newUndefined() = CborSimple.UNDEFINED
    }

    @Serializable(with = CDDLSerializer::class)
    object map :
        CDDL(
            "map",
            MajorType.MAP
        ) {

        fun newMap(value: MutableMap<AnyCborItem, AnyCborItem>): CborItem<MutableMap<AnyCborItem, AnyCborItem>> =
            CborMap(value)
    }


    @Serializable(with = CDDLSerializer::class)
    object list :
        CDDL(
            "list",
            MajorType.ARRAY
        ) {
        fun <T : AnyCborItem> newList(value: cddl_list<T>) = CborArray(value)
    }

    @Serializable(with = CDDLSerializer::class)
    object any :
        CDDL(
            "any"
        ) {
        fun newAny(value: cddl_any) = CborAny(value)
    }


    override fun toTag(additionalInfo: Int?): String {
        if (aliasFor.isNotEmpty()) {
            if (additionalInfo == null) {
                return aliasFor[0].toTag(additionalInfo)
            }

            // fixme: this is not correct. We first need to traverse the aliases, as an alias could go without major and additional info
            return util.entries.first { it.majorType == majorType && it.info == additionalInfo }
                .toTag(additionalInfo)
        }

        var tag = "#"
        if (majorType != null) {
            tag += majorType
        }
        if (additionalInfo != null) {
            tag += ".${additionalInfo}"
        }
        return tag
    }


    override fun toString(): String {
        return "CDDL(format='$format', majorType=$majorType, info=$info, aliasFor=${aliasFor.contentToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CDDL) return false

        if (format != other.format) return false
        if (majorType != other.majorType) return false
        if (info != other.info) return false
        if (!aliasFor.contentEquals(other.aliasFor)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = format.hashCode()
        result = 31 * result + (majorType?.hashCode() ?: 0)
        result = 31 * result + (info ?: 0)
        result = 31 * result + aliasFor.contentHashCode()
        return result
    }


    object util {
        // Lazy for serialization as this class is used as object in the above CDDL class
        val entries by lazy {
            arrayOf(
                any,
                uint,
                int,
                nint,
                bstr,
                bytes,
                tstr,
                tdate,
                time,
                float,
                False,
                True,
                bool,
                nil,
                Null,
                undefined,
                float,
                float16,
                float32,
                float64
            )
        }

        fun fromFormat(format: String) = entries.first { it.format == format }

        fun fromTag(tag: String): CDDL {
            if (!tag.startsWith("#")) {
                throw IllegalArgumentException("Invalid tag supplied ${tag}")
            }
            val parts = tag.split("#", ".")
            if (parts.size == 1) {
                return any
            }
            val majorVal = parts[1].toIntOrNull()
            return fromMajorType(majorVal?.let { MajorType.fromInt(it) }, parts[2].toIntOrNull())
        }

        fun fromBytes(input: Int): CDDL {
            val majorType = input shr 5

            // todo additionalInto
            return fromMajorType(MajorType.fromInt(majorType))
        }

        fun fromMajorType(majorType: MajorType? = null, additionalInfo: Int? = null) = entries.first {
            it.majorType == majorType && it.info == additionalInfo
        }
    }

}



internal object CDDLSerializer : KSerializer<CDDL> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CDDL", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CDDL) {
        encoder.encodeString(value.format)
    }

    override fun deserialize(decoder: Decoder): CDDL {
        return CDDL.util.fromFormat(decoder.decodeString())
    }
}
