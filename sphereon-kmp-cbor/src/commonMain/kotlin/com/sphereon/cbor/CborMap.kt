package com.sphereon.cbor

import com.sphereon.cbor.CborArray
import com.sphereon.cbor.CborConst.CDDL_LITERAL
import com.sphereon.cbor.CborConst.VALUE_LITERAL
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.js.JsExport


@Suppress("UNCHECKED_CAST")
@JsExport
open class CborMap<K : AnyCborItem, V : AnyCborItem?>(
    value: MutableMap<K, V> = mutableMapOf(), val indefiniteLength: Boolean = false
) : CborCollectionItem<MutableMap<K, V>>(value, CDDL.map) {
    operator fun <T> get(key: K): T {
        return value[key] as T
    }


    override fun toJsonSimple(): JsonObject {
        println("jsonSimple Map:")
        return JsonObject(value.entries.map {
            val key = it.key.toJsonSimple().jsonPrimitive.content
            println(" =simple= key: ${key}")
            val value =
                if (it.value is CborItem<*>) (it.value as CborItem<Any>).toJsonSimple() else throw IllegalArgumentException("Map must contain cbor values")
            println("      =simple= key: ${key}, value: ${value}")
            Pair(
                key,
                value
            )
        }.toMap())
    }


    override fun toJsonWithCDDL(): JsonArray {
        println("==Array:")
        return JsonArray(
            value.entries.map {
                val json =
                    if (it.value is CborItem<*>) (it.value as CborItem<Any>).toJsonWithCDDL() else throw IllegalArgumentException("Map must contain cbor values")
                val arrayElement = json as? JsonArray
                val isArray = arrayElement !== null
                val objectElement = json as? JsonObject
                val isObject = objectElement !== null
                val primitiveElement = json as? JsonPrimitive
                val isPrimitive = primitiveElement !== null
                val key = it.key.toJsonSimple().jsonPrimitive
                val cddl = if (isArray) JsonPrimitive(CDDL.list.format) else if (isPrimitive) JsonPrimitive(
                    it.value?.cddl?.format ?: CDDL.nil.format
                ) else json.jsonObject[CDDL_LITERAL]!!

                val value = primitiveElement ?: arrayElement ?: json.jsonObject[VALUE_LITERAL]!!
                println("   ==Object {cddl(${cddl}), value($value)}")
                JsonObject(
                    mapOf(
                        Pair(
                            "key",
                            key
                        ),
                        Pair(
                            CDDL_LITERAL,
                            cddl
                        ),
                        Pair(VALUE_LITERAL, value),
                    )
                )

            }
        )
    }

    fun toJsonWithCDDLObject(): JsonObject {
        println("==Object:")
        return JsonObject(
            mapOf(*value.entries.map {
                val json =
                    if (it.value is CborItem<*>) (it.value as CborItem<Any>).toJsonWithCDDL() else throw IllegalArgumentException("Map must contain cbor values")
                val arrayElement = json as? JsonArray
                val isArray = arrayElement !== null
                val objectElement = json as? JsonObject
                val isObject = objectElement !== null
                val primitiveElement = json as? JsonPrimitive
                val isPrimitive = primitiveElement !== null


                val key = it.key.toJsonSimple().jsonPrimitive.content
                val cddl = if (isArray) JsonPrimitive(CDDL.list.format) else if (isPrimitive) JsonPrimitive(
                    it.value?.cddl?.format ?: CDDL.nil.format
                ) else json.jsonObject[CDDL_LITERAL]!!

                val value = primitiveElement ?: arrayElement ?: json.jsonObject[VALUE_LITERAL]!!
                println("key: ${it.key.toJsonSimple().jsonPrimitive.content}\r\n           => OBJECT {cddl(${cddl}), value($value)}")
                Pair(
                    key,
                    JsonObject(
                        mapOf(
                            Pair(
                                CDDL_LITERAL,
                                cddl
                            ),
//                            Pair("key", it.key.toJsonSimple().jsonPrimitive),
                            Pair(VALUE_LITERAL, value),
                        )
                    )
                )
            }.toTypedArray())
        )
    }

    override fun toJsonCborItem(): ICborItemValueJson {
        return object : ICborItemValueJson {
            override val cddl = CDDL.map
            override val value = this@CborMap.toJsonWithCDDL()
        }
    }

    fun <T> getStringLabel(key: cddl_tstr, required: Boolean? = false): T {
        val value = value[StringLabel(key) as K]
        if (required == true && value == null) {
            throw IllegalArgumentException("Value is null for required label $key")
        }
        return value as T
    }

    fun <T> getNumberLabel(key: cddl_uint, required: Boolean? = false): T {
        val value = value[NumberLabel(key.toInt()) as K]
        if (required == true && value == null) {
            throw IllegalArgumentException("Value is null for required label $key")
        }
        return value as T
    }


    override fun encode(builder: ByteStringBuilder) {
        if (indefiniteLength) {
            val majorTypeShifted = (majorType!!.type shl 5)
            builder.append((majorTypeShifted + 31).toByte())
            for ((keyItem, valueItem) in value) {
                keyItem.encode(builder)
                valueItem?.encode(builder)
            }
            builder.append(0xff.toByte())
        } else {
            Cbor.encodeLength(builder, majorType!!, value.size)
            for ((keyItem, valueItem) in value) {
                keyItem.encode(builder)
                valueItem?.encode(builder)
            }
        }
    }

    fun cborEncode() = Cbor.encode(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CborMap<*, *>) return false
        if (!super.equals(other)) return false

        if (indefiniteLength != other.indefiniteLength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + indefiniteLength.hashCode()
        return result
    }


    companion object {
        /**
         * Creates a new builder.
         *
         * @return a [MapBuilder], call [MapBuilder.end] when done adding items to get a
         * [CborBuilder].
         */
        fun <T> builder(subject: T): MapBuilder<CborBuilder<T>> {
            val dataItem = CborMap(mutableMapOf())
            return MapBuilder(CborBuilder(dataItem, subject), dataItem)
        }

        internal fun decode(
            encodedCbor: ByteArray, offset: Int
        ): Pair<Int, CborMap<AnyCborItem, AnyCborItem>> {
            val lowBits = encodedCbor[offset].toInt().and(0x1f)
            if (lowBits == 31) {
                // indefinite length
                var cursor = offset + 1
                val items = mutableMapOf<AnyCborItem, AnyCborItem>()
                while (true) {
                    if (encodedCbor[cursor].toInt().and(0xff) == 0xff) {
                        // BREAK code, we're done
                        cursor += 1
                        break
                    }
                    val (nextItemOffset, keyItem) = Cbor.decodeWithOffset(encodedCbor, cursor)
                    val (nextItemOffset2, valueItem) = Cbor.decodeWithOffset(encodedCbor, nextItemOffset)
                    items.put(keyItem, valueItem)
                    check(nextItemOffset2 > cursor)
                    cursor = nextItemOffset2
                }
                return Pair(cursor, CborMap(items, true))
            } else {
                var (cursor, numItems) = Cbor.decodeLength(encodedCbor, offset)
                val items = mutableMapOf<AnyCborItem, AnyCborItem>()
                if (numItems == 0UL) {
                    return Pair(cursor, CborMap(mutableMapOf()))
                }
                for (n in IntRange(0, numItems.toInt() - 1)) {
                    val (nextItemOffset, keyItem) = Cbor.decodeWithOffset(encodedCbor, cursor)
                    val (nextItemOffset2, valueItem) = Cbor.decodeWithOffset(encodedCbor, nextItemOffset)
                    items.put(keyItem, valueItem)
                    check(nextItemOffset2 > cursor)
                    cursor = nextItemOffset2
                }
                return Pair(cursor, CborMap(items))
            }
        }
    }
}


