package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonArray
import kotlin.js.JsExport

@Suppress("UNCHECKED_CAST")
@JsExport
class CborArray<V : CborItem<*>>(value: cddl_list<V> = mutableListOf(), val indefiniteLength: Boolean = false) :
    CborCollectionItem<cddl_list<V>>(value, CDDL.list) {
    fun <T> required(idx: Int): T {
        if (idx > this.value.size) {
            throw IllegalArgumentException("Index $idx out of bounds")
        }
        return value[idx] as T
    }

    fun <T> optional(idx: Int): T? {
        return value.getOrNull(idx) as T
    }

    override fun toJsonSimple(): JsonArray {
        return JsonArray(value.map { it.toJsonSimple() })
    }

    override fun toJsonWithCDDL(): JsonArray {
        return JsonArray(value.map { it.toJsonWithCDDL() })
    }

    override fun toJsonCborItem(): ICborItemValueJson {
        return object : ICborItemValueJson {
            override val cddl = this@CborArray.cddl
            override val value = this@CborArray.toJsonWithCDDL()
        }
    }

    override fun encode(builder: ByteStringBuilder) {
        if (indefiniteLength) {
            val majorTypeShifted = (majorType!!.type shl 5)
            builder.append((majorTypeShifted + 31).toByte())
            value.forEach { (it as CborItem<V>).encode(builder) }
            builder.append(0xff.toByte())
        } else {
            Cbor.encodeLength(builder, majorType!!, value.size)
            value.forEach { (it as CborItem<V>).encode(builder) }
        }
    }

    fun cborEncode() = Cbor.encode(this)

    companion object {
        /**
         * Creates a new builder.
         *
         * @return an [ArrayBuilder], call [ArrayBuilder.end] when done adding items to get a
         * [CborBuilder].
         */
        fun <T> builder(subject: T): ArrayBuilder<CborBuilder<T>> {
            val dataItem = CborArray(mutableListOf())
            return ArrayBuilder(CborBuilder(dataItem, subject), dataItem)
        }

        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborArray<AnyCborItem>> {
            val lowBits = encodedCbor[offset].toInt().and(0x1f)
            if (lowBits == 31) {
                // indefinite length
                var cursor = offset + 1
                val items = mutableListOf<AnyCborItem>()
                while (true) {
                    if (encodedCbor[cursor].toInt().and(0xff) == 0xff) {
                        // BREAK code, we're done
                        cursor += 1
                        break
                    }
                    val (nextItemOffset, item) = Cbor.decodeWithOffset(encodedCbor, cursor)
                    items.add(item)
                    check(nextItemOffset > cursor)
                    cursor = nextItemOffset
                }
                return Pair(cursor, CborArray(items, true))
            } else {
                var (cursor, numItems) = Cbor.decodeLength(encodedCbor, offset)
                val items = mutableListOf<AnyCborItem>()
                if (numItems == 0UL) {
                    return Pair(cursor, CborArray(mutableListOf()))
                }
                for (n in IntRange(0, numItems.toInt() - 1)) {
                    val (nextItemOffset, item) = Cbor.decodeWithOffset(encodedCbor, cursor)
                    items.add(item)
                    check(nextItemOffset > cursor)
                    cursor = nextItemOffset
                }
                return Pair(cursor, CborArray(items))
            }
        }
    }
}

fun Array<out CborView<*, *, *>>.cborViewArrayToCborItem() =
    if (this.isEmpty()) null else CborArray(this.map { it.toCbor() as AnyCborItem }.toMutableList())

fun List<CborView<*, *, *>>.cborViewListToCborItem() =
    if (this.isEmpty()) null else CborArray(this.map { it.toCbor() as AnyCborItem }.toMutableList())
