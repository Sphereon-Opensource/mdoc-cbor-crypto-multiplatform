package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport


@Suppress("UNCHECKED_CAST")
@JsExport
open class CborMap<K : AnyCborItem, V : AnyCborItem?>(
    value: MutableMap<K, V> = mutableMapOf(),
    val indefiniteLength: Boolean = false
) : CborCollectionItem<MutableMap<K, V>>(value, CDDL.map) {
    operator fun <T> get(key: K): T {
        return value[key] as T
    }


    override fun <T> toJson(): T {
        return mutableMapOf(* value.map {
            Pair(
                it.key.toJson<Any>(),
                if (it.value is CborItem<*>) (it.value as CborItem<Any>).toJson<Any>() else it.value
            )
        }.toTypedArray()) as T
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
            encodedCbor: ByteArray,
            offset: Int
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


