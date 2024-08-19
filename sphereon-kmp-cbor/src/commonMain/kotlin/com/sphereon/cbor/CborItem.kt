@file:JsExport

package com.sphereon.cbor


import com.sphereon.cbor.CborConst.CDDL_LITERAL
import com.sphereon.cbor.CborConst.KEY_LITERAL
import com.sphereon.cbor.CborConst.VALUE_LITERAL
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.JsExport


/**
 * We use closed polymorphism here (sealed)
 */
@JsExport
sealed class CborItem<Type>(
    val value: Type,
    cddl: CDDLType
) : HasCborJsonRepresentation, CborBaseItem(cddl) {
    protected val HEX_DIGITS = "0123456789abcdef".toCharArray()

    internal abstract fun encode(builder: ByteStringBuilder)

    val majorType = cddl.majorType
    val info = cddl.info


    val asStr: cddl_tstr
        get() {
            require(this is CborString)
            return value
        }

    val asBool: cddl_bool
        get() {
            require(this is CborBool)
            return value
        }

    val asBstr: cddl_bstr
        get() {
            require(this is CborByteString)
            return value
        }

    val asLong: Long
        get() {
            return when (this) {
                is CborNInt -> -value.toLong()
                is CborUInt -> value.toLong()
                else -> throw IllegalArgumentException("Value $value ($cddl) is not long")
            }
        }

    val asInt: Int
        get() {
            return when (this) {
                is CborNInt -> if (value.toLong() > Int.MAX_VALUE) throw IllegalArgumentException("Unsigned value does not fit in Int field") else -value.toInt()
                is CborUInt -> if (value.toLong() > Int.MAX_VALUE) throw IllegalArgumentException("Unsigned value does not fit in Int field") else value.toInt()
                else -> throw IllegalArgumentException("Value $value ($cddl) is not int")
            }
        }

    fun toValue(): Type {
        return value
    }

    override fun toJsonSimple(): JsonElement {
        throw IllegalArgumentException("Because of boxing this method must be implemented in subclasses")
    }

    override fun toJson(includeCDDL: Boolean): JsonElement {
        return if (includeCDDL) toJsonWithCDDL() else toJsonSimple()
    }

    override fun toJsonWithCDDL(): JsonElement {
        val cddl = JsonPrimitive(this.cddl.format)
        println("-CborItem(JSONObject(CDDL:$cddl, value:${toJsonSimple()} (jsonsimple))")
        return JsonObject(mapOf(Pair(CDDL_LITERAL, cddl), Pair(VALUE_LITERAL, toJsonSimple())))
    }

    override fun toJsonCborItem(): ICborItemValueJson {
        return object : ICborItemValueJson {
            override val cddl: CDDLType
                get() = this@CborItem.cddl
            override val value: JsonElement
                get() = this@CborItem.toJsonWithCDDL()
        }
    }

    @Suppress("UNCHECKED_CAST")
    val asMap: cddl_map<Any, Any>
        get() {
            require(this is CborMap<*, *>)
            return this.value as cddl_map<Any, Any>
        }

    @Suppress("UNCHECKED_CAST")
    val asList: cddl_list<Any>
        get() {
            require(this is CborArray<*>)
            return this.value as cddl_list<Any>
        }


    /**
     * The value of a [Tagged] data item.
     *
     * @throws IllegalArgumentException if not the data item isn't of type [Tagged].
     */
    val asTagged: CborItem<Type>
        get() {
            require(this is CborTagged<Type>)
            return this.taggedItem
        }

    /**
     * The decoded CBOR from a bstr with tag [Tagged.ENCODED_CBOR].
     *
     * @throws IllegalArgumentException if the data item isn't a tag with tag [Tagged.ENCODED_CBOR]
     * containing a bstr with valid CBOR.
     */
    val asTaggedEncodedCbor: CborItem<Type>
        get() {
            require(this is CborTagged)
            require(this.tagNumber == CborTagged.ENCODED_CBOR)
            val child = this.taggedItem
            require(child is CborByteString)
            return Cbor.decode(child.value)
        }

    /**
     * Allows subclasses to perform validations on the value
     */
    protected open fun validate() {
    }

    override fun toString(): String {
        return "CborItem($VALUE_LITERAL=$value, $CDDL_LITERAL=$cddl)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AnyCborItem) return false

        if (value != other.value) return false
        if (majorType != other.majorType) return false
        if (info != other.info) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value?.hashCode() ?: 0
        result = 31 * result + (majorType?.hashCode() ?: 0)
        result = 31 * result + (info ?: 0)
        return result
    }


    init {
        this.validate()
    }

}

@JsExport
abstract class CborCollectionItem<Type>(value: Type, cddl: CDDLType) : CborItem<Type>(value, cddl)


typealias AnyCborItem = CborItem<*>

/**
 * Json representation
 */
interface ICborItemValueJson {
    val cddl: CDDLType
    val value: JsonElement
}

/**
 * Json representation
 */
interface ICborItemJson : ICborItemValueJson {
    val key: String
}

interface HasCborJsonRepresentation {
    fun toJsonWithCDDL(): JsonElement // Array or Object
    fun toJson(includeCDDL: Boolean = false): JsonElement
    fun toJsonSimple(): JsonElement
    fun toJsonCborItem(): ICborItemValueJson

}

@Serializable
data class CborItemJson(override val key: String, override val value: JsonElement, override val cddl: CDDLType) : HasCborJsonRepresentation,
    ICborItemJson {
    override fun toJsonWithCDDL() =
        JsonObject(
            mapOf(
                Pair(
                    JsonPrimitive(key).content,
                    JsonObject(mapOf(Pair(VALUE_LITERAL, value), Pair(CDDL_LITERAL, JsonPrimitive(cddl.format))))
                ),
            )
        )

    override fun toJsonSimple() = JsonObject(mapOf(Pair(JsonPrimitive(key).content, value)))
    override fun toJson(includeCDDL: Boolean) = if (includeCDDL) toJsonWithCDDL() else toJsonSimple()
    override fun toJsonCborItem(): ICborItemValueJson = this


    object Static {
        fun isCborItemValueJson(jsonElement: JsonElement): Boolean {
            if (jsonElement !is JsonObject) {
                return false
            }
            if (jsonElement.size == 2 || jsonElement.size == 3) {
                return jsonElement.containsKey(CDDL_LITERAL) && jsonElement.containsKey(VALUE_LITERAL)
            }
            return false
        }

        fun isCborItemJson(jsonElement: JsonElement): Boolean {
            if (jsonElement !is JsonObject) {
                return false
            }
            if (jsonElement.size == 3) {
                return jsonElement.containsKey(CDDL_LITERAL) && jsonElement.containsKey(VALUE_LITERAL) && jsonElement.containsKey(KEY_LITERAL)
            }
            return false
        }


        fun fromJsonPrimitive(jsonPrimitive: JsonPrimitive, cddl: CDDLType, key: String? = null): ICborItemValueJson {
            if (key !== null) {
                return CborItemJson(key, jsonPrimitive, cddl)
            }

            return object : ICborItemValueJson {
                override val cddl: CDDLType
                    get() = cddl
                override val value: JsonElement
                    get() = jsonPrimitive
            }
        }

        fun fromJsonArray(jsonArray: JsonArray): Array<ICborItemValueJson> {
            return jsonArray.map {
                if (it as? JsonObject !== null) {
                    fromJsonObjectAsValueJson(it.jsonObject)
                } else if (it as? JsonPrimitive !== null) {
                    if (it.isString) {
                        fromJsonPrimitive(it.jsonPrimitive, CDDL.tstr)
                    } else {
                        fromJsonPrimitive(it.jsonPrimitive, CDDL.any)
                    }
                } else if (it as? JsonArray !== null) {
                    fromJsonArray(it.jsonArray)
                }
                throw IllegalStateException("JsonObject does not contain 3 elements from a Cbor Json Item")

            }.toTypedArray()
        }

        fun fromJsonObjectAsCborItemJson(jsonObject: JsonObject): ICborItemJson {
            if (!isCborItemJson(jsonObject)) {
                throw IllegalStateException("JsonObject does not contain 3 elements from a Cbor Json Item")
            }
            return fromJsonObjectAsValueJson(jsonObject) as ICborItemJson
        }

        fun fromJsonObjectAsValueJson(jsonObject: JsonObject): ICborItemValueJson {
            if (!isCborItemValueJson(jsonObject) && !isCborItemValueJson(jsonObject)) {
                throw IllegalStateException("JsonObject does not contain 2 or 3 elements from a Cbor Json Item")
            }
            val value = jsonObject[VALUE_LITERAL] ?: throw IllegalStateException("value not available")
            val cddl = CDDL.util.fromFormat(
                jsonObject[CDDL_LITERAL]?.jsonPrimitive?.content ?: throw IllegalStateException("cddl key not available")
            )
            if (jsonObject.containsKey(KEY_LITERAL)) {
                return object : ICborItemJson {
                    override val cddl = cddl
                    override val key = jsonObject[KEY_LITERAL]?.jsonPrimitive?.content ?: throw IllegalStateException("'key' key not available")
                    override val value = value
                }
            }
            return object : ICborItemValueJson {
                override val cddl = cddl
                override val value = value
            }
        }

        fun fromDTO(cborItemJson: ICborItemJson): CborItemJson {
            return CborItemJson(cborItemJson.key, cborItemJson.value, cborItemJson.cddl)
        }
    }
}

fun JsonObject.jsonObjectToCborJsonItem() = CborItemJson.Static.fromJsonObjectAsValueJson(this)
