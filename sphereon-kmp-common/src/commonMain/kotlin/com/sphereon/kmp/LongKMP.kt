package com.sphereon.kmp


import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.JsExport
import kotlin.js.JsName

/**
 * Cross-platform Big Integer support. Unfortunately Longs aren't supported for JS exports, resulting in any's
 *
 * Doubles are supported, however. So we use a wrapper object for a double that gets converted to long internally.
 * Not pretty but does the job, especially with the easy conversion functions
 *
 */
@JsExport
@Serializable(with = LongKMPSerializer::class)
class LongKMP(value: Double) : Number(), Comparable<LongKMP> {
    private val long: Long = value.toLong()
    @JsName("fromString")
    constructor(value: String) : this(value.toDouble())

    @JsName("fromLong")
    constructor(value: Long) : this(value.toDouble())

    @JsName("fromNumber") // maps to number in JS
    constructor(value: Int) : this(value.toDouble())

    override fun toByte(): Byte {
        return long.toByte()
    }

    override fun toDouble(): Double {
        return long.toDouble()
    }

    override fun toFloat(): Float {
        return long.toFloat()
    }

    override fun toInt(): Int {
        return long.toInt()
    }

    override fun toLong(): Long {
        return long
    }

    fun toULong(): ULong {
        return long.toULong()
    }

    fun toUInt(): UInt {
        return long.toUInt()
    }

    override fun toShort(): Short {
        return long.toShort()
    }

    override fun toString() = this.long.toString()

    override fun compareTo(other: LongKMP): Int {
        return long.compareTo(other.long)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LongKMP) return false

        if (long != other.long) return false

        return true
    }

    override fun hashCode(): Int {
        return long.hashCode()
    }



}

object LongKMPSerializer : KSerializer<LongKMP> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LongKMP", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LongKMP) {
        encoder.encodeLong(value.toLong())
    }

    override fun deserialize(decoder: Decoder): LongKMP {
        return LongKMP(decoder.decodeLong())
    }
}

fun Number.toKmpLong() = LongKMP(this.toLong())

@JsExport
fun Number.numberToKmpLong() = this.toKmpLong()
fun UInt.uintToKmpLong() = LongKMP(this.toLong())

@JsExport
fun ULong.ulongToKmpLong() = LongKMP(this.toLong())

@JsExport
fun Byte.byteToKmpLong() = LongKMP(this.toLong())

@JsExport
fun String.stringToKmpLong() = LongKMP(this)
