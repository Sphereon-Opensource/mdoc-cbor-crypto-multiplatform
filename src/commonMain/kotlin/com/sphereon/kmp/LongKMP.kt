package com.sphereon.kmp


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
class LongKMP(value: Double) : Number(), Comparable<LongKMP> {
    private val long: Long = value.toLong()
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
        return long.toLong()
    }

    fun toULong(): ULong {
        return long.toLong().toULong()
    }

    fun toUInt(): UInt {
        return long.toLong().toUInt()
    }

    override fun toShort(): Short {
        return long.toShort()
    }

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

    override fun toString(): String {
        return "LongKMP($long)"
    }


}

@JsExport
fun Number.toBigInt() = LongKMP(this.toLong())

@JsExport
fun Number.bigIntFromNumber() = this.toBigInt()
fun UInt.bigIntFromUInt() = LongKMP(this.toLong())

@JsExport
fun ULong.bigIntFromULong() = LongKMP(this.toLong())

@JsExport
fun Byte.bigIntFromByte() = LongKMP(this.toLong())

@JsExport
fun String.bigIntFromString() = LongKMP(this.toLong())
