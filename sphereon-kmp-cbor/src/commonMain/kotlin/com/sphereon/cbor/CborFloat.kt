package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.experimental.or
import kotlin.js.JsExport

@JsExport
open class CborFloat(value: Float, cddl: CDDLType) : CborNumber<Float>(value, cddl) {
    override fun encode(builder: ByteStringBuilder) {
        builder.run {
            val majorTypeShifted = (majorType!!.type shl 5).toByte()
            append(majorTypeShifted.or(26))

            val raw = value.toRawBits()
            append((raw shr 24).and(0xff).toByte())
            append((raw shr 16).and(0xff).toByte())
            append((raw shr 8).and(0xff).toByte())
            append((raw shr 0).and(0xff).toByte())
        }

    }

    object Static {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborFloat> {
            val raw = (encodedCbor[offset + 1].toInt().and(0xff) shl 24) +
                    (encodedCbor[offset + 2].toInt().and(0xff) shl 16) +
                    (encodedCbor[offset + 3].toInt().and(0xff) shl 8) +
                    encodedCbor[offset + 4].toInt().and(0xff)
            return Pair(offset + 5, CborFloat(Float.fromBits(raw), CDDL.float))
        }
    }
}

@JsExport
class CborFloat16(value: cddl_float16) : CborFloat(value, CDDL.float16)

@JsExport
class CborFloat32(value: cddl_float32) : CborFloat(value, CDDL.float32)

@JsExport
class CborDouble(value: cddl_float64) : CborItem<cddl_float64>(value, CDDL.float64) {
    override fun toJsonSimple(): JsonElement {
        return JsonPrimitive(toValue())
    }

    override fun encode(builder: ByteStringBuilder) =
        builder.run {
            val majorTypeShifted = (majorType!!.type shl 5).toByte()
            append(majorTypeShifted.or(27))

            val raw = value.toRawBits()
            append((raw shr 56).and(0xff).toByte())
            append((raw shr 48).and(0xff).toByte())
            append((raw shr 40).and(0xff).toByte())
            append((raw shr 32).and(0xff).toByte())
            append((raw shr 24).and(0xff).toByte())
            append((raw shr 16).and(0xff).toByte())
            append((raw shr 8).and(0xff).toByte())
            append((raw shr 0).and(0xff).toByte())
        }


    object Static {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborDouble> {
            val raw = (encodedCbor[offset + 1].toLong().and(0xffL) shl 56) +
                    (encodedCbor[offset + 2].toLong().and(0xffL) shl 48) +
                    (encodedCbor[offset + 3].toLong().and(0xffL) shl 40) +
                    (encodedCbor[offset + 4].toLong().and(0xffL) shl 32) +
                    (encodedCbor[offset + 5].toLong().and(0xffL) shl 24) +
                    (encodedCbor[offset + 6].toLong().and(0xffL) shl 16) +
                    (encodedCbor[offset + 7].toLong().and(0xffL) shl 8) +
                    encodedCbor[offset + 8].toLong().and(0xffL)
            return Pair(offset + 9, CborDouble(Double.fromBits(raw)))
        }
    }

}

fun cddl_float.toCborFloat() = CborFloat32(this)
fun cddl_float64.toCborFloat64() = CborDouble(this)
