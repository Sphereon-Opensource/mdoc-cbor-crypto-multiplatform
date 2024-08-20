@file:Suppress("SERIALIZER_TYPE_INCOMPATIBLE")

package com.sphereon.cbor

import com.sphereon.cbor.CDDL.any
import com.sphereon.cbor.CborConst.CDDL_LITERAL
import com.sphereon.cbor.CborConst.KEY_LITERAL
import com.sphereon.cbor.CborTagged.Static.DATE_TIME_STRING

import com.sphereon.kmp.Encoding
import com.sphereon.kmp.Logger
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.decodeFrom
import com.sphereon.kmp.numberToKmpLong
import com.sphereon.kmp.stringToKmpLong
import com.sphereon.kmp.toKmpLong
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.float
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
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
    fun newCborItemFromJson(value: JsonElement?, cddl: CDDLType? = any): CborItem<out Any?>

    fun <T : Any> newCborItem(origValue: T?): CborItem<out Any?>

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

    override fun newCborItemFromJson(element: JsonElement?, cddl: CDDLType?): CborItem<out Any?> {
        val jsonPrimitive: JsonPrimitive? = (element as? JsonPrimitive)?.jsonPrimitive
        val jsonArray: JsonArray? = (element as? JsonArray)?.jsonArray
        val jsonObject: JsonObject? = (element as? JsonObject)?.jsonObject
        val isNull: Boolean = element == JsonNull || element === null
        val isString: Boolean = jsonPrimitive?.isString == true

        if (isNull) {
            return CborNull()
        } else if (isString && jsonPrimitive !== null) {
            return CborString(jsonPrimitive.content)
        } else if (jsonArray !== null) {
            return list.fromJson(jsonArray)
        } else if (jsonObject !== null) {
            if (CborItemJson.Static.isCborItemValueJson(jsonObject)) {

                val key = if (CborItemJson.Static.isCborItemJson(jsonObject)) jsonObject["key"]?.jsonPrimitive?.content else null
                val cddlStr = jsonObject["cddl"]?.jsonPrimitive?.content

                if (cddlStr === null) {
                    Logger.Static.tag("CDDL").warn("cddl key found, but wasn't a primitive, returning a null value")
                    return CborNull()
                }
                val cddlObject = util.fromFormat(cddlStr)
                val cborObject = newCborItemFromJson(jsonObject[jsonObject.keys.find { it != CDDL_LITERAL && it != KEY_LITERAL }], cddlObject)
                println("CBOR ITEM AS JSON ENCOUNTERED: key:${key}, cddl:${cddlStr}, object: ${cborObject}")
                if (key === null) {
                    return cborObject
                }
                return CborMap(mutableMapOf(Pair(CborString(key), cborObject)))
            }
            // number label keys?
            return CborMap(mutableMapOf(* jsonObject.map { Pair(CborString(it.key), newCborItemFromJson(it.value)) }.toTypedArray()))

        } else if (jsonPrimitive !== null) {
            if (cddl == null) {
                return newCborItem(jsonPrimitive)
            }
            return when (cddl) {
                // Needed because we cannot have inheritance with Kotlin to JS, unfortunately. The fromJson would cause clashes if we put it in the interface
                tstr -> tstr.fromJson(jsonPrimitive)
                Null -> CborNull()
                False -> CborSimple.Static.FALSE
                True -> CborSimple.Static.TRUE
                bool -> bool.fromJson(jsonPrimitive)
                bstr -> bstr.fromJson(jsonPrimitive)
                bytes -> bytes.fromJson(jsonPrimitive)
                float -> float.fromJson(jsonPrimitive)
                float16 -> float16.fromJson(jsonPrimitive)
                float32 -> float32.fromJson(jsonPrimitive)
                float64 -> float64.fromJson(jsonPrimitive)
                full_date -> full_date.fromJson(jsonPrimitive)
                int -> int.fromJson(jsonPrimitive)
                nil -> nil.newNil()
                nint -> nint.fromJson(jsonPrimitive)
                tdate -> tdate.fromJson(jsonPrimitive)
                text -> text.fromJson(jsonPrimitive)
                time -> time.fromJson(jsonPrimitive)
                uint -> uint.fromJson(jsonPrimitive)
                undefined -> undefined.newUndefined()
                else -> cddl.newCborItem(jsonPrimitive)
            }
        }
        return newCborItem(element)

    }

    override fun <T : Any> newCborItem(origValue: T?): CborItem<out Any?> {
        // We are not using inheritance for the methods as that would impact JS export.
        // Since this is a sealed class anyway that is not too bad
        var value = origValue
        val jsonElement: JsonElement? = when (value) {
            is JsonPrimitive -> value
            is JsonArray -> value
            is JsonObject -> value
            is JsonNull -> value
            else -> null
        }
        if (value == null) {
            return nil.newNil()
        } else if (value == Unit) {
            return undefined.newUndefined()
        }
        return when (this) {
            tstr -> if (jsonElement === null) tstr.newString(value.toString()) else tstr.fromJson(jsonElement.jsonPrimitive)
            False -> False.newFalse()
            Null -> Null.newNull()
            True -> True.newTrue()
            any -> return when (value) {
                is cddl_tstr -> if (jsonElement === null) tstr.newString(value) else tstr.fromJson(jsonElement.jsonPrimitive)
                is cddl_uint -> if (jsonElement === null) uint.newUint(value) else uint.fromJson(jsonElement.jsonPrimitive)
                is cddl_int -> if (jsonElement === null) int.newLong(value.toLong()) else int.fromJson(jsonElement.jsonPrimitive)
                is cddl_bstr -> if (jsonElement === null) bstr.newByteString(value) else bstr.fromJson(jsonElement.jsonPrimitive)
                is cddl_tdate -> if (jsonElement === null) tdate.newTDate(value) else tdate.fromJson(jsonElement.jsonPrimitive)
                is cddl_full_date -> if (jsonElement === null) full_date.newFullDate(value) else full_date.fromJson(jsonElement.jsonPrimitive)
                is cddl_float64 -> if (jsonElement === null) float64.newFloat64(value) else float64.fromJson(jsonElement.jsonPrimitive)
                is cddl_float -> if (jsonElement === null) float.newFloat(value) else float.fromJson(jsonElement.jsonPrimitive)
                is cddl_bool -> if (jsonElement === null) bool.newBool(value) else bool.fromJson(jsonElement.jsonPrimitive)
                is cddl_list<*> -> if (jsonElement === null) list.newList(value.map {
                    if (it is AnyCborItem) it else any.newCborItem(
                        it
                    )
                }.toMutableList()) else list.fromJson(jsonElement.jsonArray)

                is cddl_map<*, *> -> if (jsonElement === null) map.newMap(mutableMapOf(* value.map {
                    Pair(
                        if (it.key is AnyCborItem) it.key as AnyCborItem else any.newCborItem(it.key),
                        if (it.value is AnyCborItem) it.value as AnyCborItem else any.newCborItem(it.value)
                    )
                }.toTypedArray())) else map.fromJson(jsonElement.jsonObject)

                else -> throw IllegalArgumentException("newCborItem for ${value} Not implemented yet")
            }

            bool -> if (jsonElement === null) bool.newBool(value == true) else bool.fromJson(jsonElement.jsonPrimitive)
            bstr -> if (jsonElement === null) bstr.newByteString(value as ByteArray) else bstr.fromJson(jsonElement.jsonPrimitive)
            bstr_indef_length -> if (jsonElement === null) bstr_indef_length.newByteString(value as List<cddl_bstr>) else TODO("indef cddl from json not implemented yet")
            bytes -> if (jsonElement === null) bytes.newBytes(value as ByteArray) else bytes.fromJson(jsonElement.jsonPrimitive)
            float -> if (jsonElement === null) float.newFloat(value as Float) else float.fromJson(jsonElement.jsonPrimitive)
            float16 -> if (jsonElement === null) float16.newFloat16(value as Float) else float16.fromJson(jsonElement.jsonPrimitive)
            float32 -> if (jsonElement === null) float32.newFloat32(value as Float) else float32.fromJson(jsonElement.jsonPrimitive)
            float64 -> if (jsonElement === null) float64.newFloat64(value as Double) else float64.fromJson(jsonElement.jsonPrimitive)
            full_date -> if (jsonElement === null) full_date.newFullDate(value as cddl_full_date) else full_date.fromJson(jsonElement.jsonPrimitive)
            int -> if (jsonElement === null) int.newInt(value as Int) else int.fromJson(jsonElement.jsonPrimitive)
            list -> if (jsonElement === null) if (value is CborArray<*>) value else list.newList((value as MutableList<*>).map {
                if (it is AnyCborItem) it else any.newCborItem(
                    it
                )
            }.toMutableList()) else list.fromJson(jsonElement.jsonArray)

            map -> if (jsonElement === null) if (value is CborMap<*, *>) value else map.newMap(mutableMapOf(* (value as MutableMap<*, *>).map {
                Pair(
                    if (it.key is AnyCborItem) it.key as AnyCborItem else any.newCborItem(it.key),
                    if (it.value is AnyCborItem) it.value as AnyCborItem else any.newCborItem(it.value)
                )
            }.toTypedArray())) /* fixme. Needs inspection of keys and values and map type*/ else {
                println("===========================================")
                println("JSON ELEMENT: ${jsonElement}")
                return map.fromJson(jsonElement.jsonObject)
            }

            nil -> nil.newNil()
            nint -> if (jsonElement === null) nint.newNInt(if (value is Number) value.toKmpLong() else value as LongKMP) else nint.fromJson(
                jsonElement.jsonPrimitive
            )

            tdate -> if (jsonElement === null) tdate.newTDate(value as cddl_tdate) else tdate.fromJson(jsonElement.jsonPrimitive)
            text -> if (jsonElement === null) text.newText(value as cddl_text) else text.fromJson(jsonElement.jsonPrimitive)
            time -> if (jsonElement === null) time.newTime(value as cddl_time) else time.fromJson(jsonElement.jsonPrimitive)
            tstr_indef_length -> if (jsonElement !== null) tstr_indef_length.newStringIndefLength(value as List<cddl_tstr>) else TODO("tstr indef length from json to cbor not implemented")
            uint -> if (jsonElement === null) uint.newUint(if (value is LongKMP) value else if (value is Number) value.toKmpLong() else value as LongKMP) else uint.fromJson(
                jsonElement.jsonPrimitive
            )

            undefined -> undefined.newUndefined()
        }

    }

    object tstr : CDDL("tstr", MajorType.UNICODE_STRING) {
        fun newString(value: cddl_tstr) = CborString(value)
        fun fromJson(value: JsonPrimitive) = CborString(value.content)

    }

    @Serializable(with = CDDLSerializer::class)
    object uint : CDDL("uint", MajorType.UNSIGNED_INTEGER) {
        fun newUint(value: cddl_uint) = CborUInt(value)
        fun fromJson(value: JsonPrimitive) = CborUInt(value.content.stringToKmpLong())


    }

    @Serializable(with = CDDLSerializer::class)
    object nint : CDDL("nint", MajorType.NEGATIVE_INTEGER) {
        fun newNInt(value: cddl_nint) = CborNInt(value)
        fun fromJson(value: JsonPrimitive) = CborNInt(value.content.stringToKmpLong())
    }

    @Serializable(with = CDDLSerializer::class)
    object int :
        CDDL("int", null, null, aliasFor = arrayOf(uint, nint)) {
        fun newInt(value: Int) =
            if (value < 0) CborNInt(value.numberToKmpLong()) else CborUInt(value.numberToKmpLong())

        fun newLong(value: Long) =
            if (value < 0) CborNInt(value.numberToKmpLong()) else CborUInt(value.numberToKmpLong())

        fun fromJson(value: JsonPrimitive) =
            if (value.long < 0) CborNInt(value.content.stringToKmpLong()) else CborUInt(value.content.stringToKmpLong())

    }

    @Serializable(with = CDDLSerializer::class)
    object bstr : CDDL("bstr", MajorType.BYTE_STRING) {
        fun newByteString(value: cddl_bstr) = CborByteString(value)
        fun fromJson(value: JsonPrimitive, encoding: Encoding = Encoding.BASE64URL) = CborByteString(value.content.decodeFrom(encoding))
    }

    @Serializable(with = CDDLSerializer::class)
    object bstr_indef_length : CDDL("bstr", MajorType.BYTE_STRING) {
        fun newByteString(value: List<cddl_bstr>) = CborByteStringIndefLength(value)
        fun fromJson(value: JsonArray, encoding: Encoding = Encoding.BASE64URL): CborByteStringIndefLength =
            TODO("indef length from json to cbor not implemented yet")
    }

    @Serializable(with = CDDLSerializer::class)
    object bytes : CDDL("bytes", MajorType.BYTE_STRING, aliasFor = arrayOf(bstr)) {
        fun newBytes(value: cddl_bstr) = CborByteString(value)
        fun fromJson(value: JsonPrimitive, encoding: Encoding = Encoding.BASE64URL) = CborByteString(value.content.decodeFrom(encoding))
    }

    @Serializable(with = CDDLSerializer::class)
    object tstr_indef_length : CDDL("tstr", MajorType.UNICODE_STRING) {
        fun newStringIndefLength(value: List<cddl_tstr>) = CborStringIndefLength(value)
        fun fromJson(value: JsonArray, encoding: Encoding = Encoding.BASE64URL): CborStringIndefLength =
            TODO("indef length from json to cbor not implemented yet")
    }

    @Serializable(with = CDDLSerializer::class)
    object text : CDDL("text", MajorType.UNICODE_STRING, aliasFor = arrayOf(tstr)) {
        fun newText(value: cddl_text) = CborString(value)
        fun fromJson(value: JsonPrimitive) = CborString(value.content)
    }


    @Serializable(with = CDDLSerializer::class)
    object tdate : CDDL(
        "tdate", MajorType.TAG, DATE_TIME_STRING
    ) // RFC 7049, section 2.4.1, a tdate data item shall contain a date-time string as specified in RFC 3339
    {
        fun newTDate(value: cddl_tdate) = CborTDate(value)
        fun fromJson(value: JsonPrimitive) = CborTDate(value.content)
    }

    @Serializable(with = CDDLSerializer::class)
    object full_date : CDDL(
        "full-date", MajorType.TAG, CborTagged.Static.FULL_DATE_STRING
    ) // #6.1004(tstr) In accordance with RFC 8943, a full-date data item shall contain a full-datestring as specified in RFC 3339.
    {
        fun newFullDate(value: cddl_full_date) = CborFullDate(value)
        fun fromJson(value: JsonPrimitive) = CborFullDate(value.content)
    }

    @Serializable(with = CDDLSerializer::class)
    object time : CDDL(
        "time", MajorType.TAG, CborTagged.Static.DATE_TIME_NUMBER
    ) // RFC 7049, section 2.4.1, a tdate data item shall contain a date-time number as specified in RFC 3339
    {
        fun newTime(value: cddl_time) = CborTime(value)
        fun fromJson(value: JsonPrimitive) = CborTime(value.content.stringToKmpLong())
    }

    @Serializable(with = CDDLSerializer::class)
    object float16 : CDDL("float16", MajorType.SPECIAL, 25) {
        fun newFloat16(value: cddl_float16) = CborFloat16(value)
        fun fromJson(value: JsonPrimitive) = CborFloat16(value.float)
    }

    @Serializable(with = CDDLSerializer::class)
    object float32 : CDDL("float32", MajorType.SPECIAL, 26) {
        fun newFloat32(value: cddl_float32) = CborFloat32(value)
        fun fromJson(value: JsonPrimitive) = CborFloat32(value.float)
    }

    @Serializable(with = CDDLSerializer::class)
    object float64 : CDDL("float64", MajorType.SPECIAL, 27) {
        fun newFloat64(value: cddl_float64) = CborDouble(value)
        fun fromJson(value: JsonPrimitive) = CborDouble(value.double)
    }

    @Serializable(with = CDDLSerializer::class)
    object float : CDDL("float", MajorType.SPECIAL, aliasFor = arrayOf(float16, float32, float64)) {
        fun newFloat(value: cddl_float) = CborFloat32(value)
        fun fromJson(value: JsonPrimitive) = CborFloat32(value.float)
    }

    @Serializable(with = CDDLSerializer::class)
    object False : CDDL("false", MajorType.SPECIAL, 20) {
        fun newFalse() = CborSimple.Static.FALSE
        fun fromJson(value: JsonPrimitive) = CborSimple.Static.FALSE
    }

    @Serializable(with = CDDLSerializer::class)
    object True : CDDL("true", MajorType.SPECIAL, 21) {
        fun newTrue() = CborSimple.Static.TRUE
        fun fromJson(value: JsonPrimitive) = CborSimple.Static.TRUE
    }

    @Serializable(with = CDDLSerializer::class)
    object bool :
        CDDL("bool", MajorType.SPECIAL, aliasFor = arrayOf(False, True)) {
        fun newBool(value: cddl_bool) = if (value) CborSimple.Static.TRUE else CborSimple.Static.FALSE
        fun fromJson(value: JsonPrimitive) = if (value.boolean) CborSimple.Static.TRUE else CborSimple.Static.FALSE
    }

    @Serializable(with = CDDLSerializer::class)
    object nil : CDDL("nil", MajorType.SPECIAL, 22) {
        fun newNil() = CborSimple.Static.NULL
        fun fromJson(value: JsonElement) = CborSimple.Static.NULL

    }

    @Serializable(with = CDDLSerializer::class)
    object Null : CDDL("null", MajorType.SPECIAL, 22, arrayOf(nil)) {
        fun newNull() = CborSimple.Static.NULL
        fun fromJson(value: JsonElement) = CborSimple.Static.NULL
    }

    @Serializable(with = CDDLSerializer::class)
    object undefined :
        CDDL("undefined", MajorType.SPECIAL, 23) {
        fun newUndefined() = CborSimple.Static.UNDEFINED
        fun fromJson(value: JsonElement) = CborSimple.Static.UNDEFINED
    }

    @Serializable(with = CDDLSerializer::class)
    object map :
        CDDL(
            "map",
            MajorType.MAP
        ) {

        fun newMap(value: MutableMap<AnyCborItem, AnyCborItem>): CborItem<MutableMap<AnyCborItem, AnyCborItem>> =
            CborMap(value)

        /**
         * Please note that this method is not able to map onto the exact Cbor items as the json values have no type information!
         */
        fun fromJson(value: JsonObject): CborMap<CborString, AnyCborItem> = CborMap(value.entries.map {
            Pair(
                CborString(it.key),
                when (it.value) {
                    is JsonPrimitive -> any.fromJson(it.value)
                    is JsonArray -> list.fromJson(it.value as JsonArray)
                    is JsonObject -> fromJson(it.value as JsonObject)
                    else -> throw IllegalArgumentException("Unknown type encountered")
                }
            )
        }.toMap().toMutableMap())
    }


    @Serializable(with = CDDLSerializer::class)
    object list :
        CDDL(
            "list",
            MajorType.ARRAY
        ) {
        fun <T : AnyCborItem> newList(value: cddl_list<T>) = CborArray(value)

        /**
         * Please note that this method is not able to map onto the exact Cbor items as the json values have no type information!
         */
        fun fromJson(value: JsonArray): CborArray<CborItem<*>> = CborArray(value.map { elt ->
            when (elt) {
                is JsonPrimitive -> any.fromJson(elt)
                is JsonArray -> fromJson(elt)
                is JsonObject -> if (CborItemJson.Static.isCborItemValueJson(elt)) newCborItemFromJson(
                    elt,
                    elt[CDDL_LITERAL]?.jsonPrimitive?.content?.let { CDDL.util.fromFormat(it) }) else map.fromJson(elt)

                else -> throw IllegalArgumentException("Unknown type encountered")
            }
        }.toMutableList())
    }

    @Serializable(with = CDDLSerializer::class)
    object any :
        CDDL(
            "any"
        ) {
        fun newAny(value: cddl_any) = CborAny(value)
        fun fromJson(value: JsonElement): CborItem<*> {
            println("any to json: ${value}")
            return newCborItemFromJson(value)
//            TODO("Json any to cbor not implemeted yet")
        }
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
            return fromMajorType(majorVal?.let { MajorType.Static.fromInt(it) }, parts[2].toIntOrNull())
        }

        fun fromBytes(input: Int): CDDL {
            val majorType = input shr 5

            // todo additionalInto
            return fromMajorType(MajorType.Static.fromInt(majorType))
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
