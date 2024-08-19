package com.sphereon.cbor

import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlin.js.JsExport

class CborStringIndefLength(value: List<cddl_tstr>) : CborItem<List<cddl_tstr>>(value, CDDL.tstr_indef_length) {
    override fun encode(builder: ByteStringBuilder) {

        val majorTypeShifted = (majorType!!.type shl 5)
        builder.append((majorTypeShifted + 31).toByte())
        value.forEach {
            val encodedStr = it.encodeToByteArray()
            Cbor.encodeLength(builder, majorType, encodedStr.size)
            builder.append(encodedStr)
        }
        builder.append(0xff.toByte())
    }

    companion object {
        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, CborStringIndefLength> {
            val majorTypeShifted = (MajorType.UNICODE_STRING.type shl 5)
            val marker = (majorTypeShifted + 31).toByte()
            check(encodedCbor[offset] == marker)
            val chunks = mutableListOf<String>()
            var cursor = offset + 1
            while (true) {
                if (encodedCbor[cursor].toInt().and(0xff) == 0xff) {
                    // BREAK code, we're done
                    cursor += 1
                    break
                }
                val (chunkEndOffset, chunk) = Cbor.decodeWithOffset(encodedCbor, cursor)
                check(chunk is CborString)
                chunks.add(chunk.value)
                check(chunkEndOffset > cursor)
                cursor = chunkEndOffset
            }
            return Pair(cursor, CborStringIndefLength(chunks))
        }
    }

        override fun toJsonSimple(): JsonArray {
            TODO("Indef lengt to json not implemented yet")
        }

}

@JsExport
class CborNil : CborSimple<cddl_nil>(null, CDDL.nil) {
    init {
        if (value != null) {
            throw IllegalArgumentException("Nil requires value ${null}")
        }
    }

    override fun toJsonSimple(): JsonElement {
        return JsonNull
    }
}

@JsExport
class CborNull : CborSimple<cddl_null>(null, CDDL.Null) {

    init {
        if (value != null) {
            throw IllegalArgumentException("Nil requires value ${null}")
        }
    }

    override fun toJsonSimple(): JsonElement {
        return JsonNull
    }
}
