package com.sphereon.cbor

import com.sphereon.kmp.DefaultLogger
import kotlin.math.pow
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.experimental.or
import kotlin.js.JsExport
import kotlin.jvm.JvmStatic

/**
 * CBOR support routines. Shamelessly copied and inspired by Google as we got fed up with having to jump through hoops with Kotlinx-serialization
 *
 * This package includes support for CBOR as specified in
 * [RFC 8849](https://www.rfc-editor.org/rfc/rfc8949.html).
 */
@JsExport
object Cbor {
    private val HEX_DIGITS = "0123456789abcdef".toCharArray()

    internal fun encodeLength(
        builder: ByteStringBuilder,
        majorType: MajorType,
        length: Int
    ) = encodeLength(builder, majorType, length.toULong())

    internal fun encodeLength(
        builder: ByteStringBuilder,
        majorType: MajorType,
        length: ULong
    ) {
        val majorTypeShifted = (majorType.type shl 5).toByte()
        builder.apply {
            if (length < 24U) {
                append(majorTypeShifted.or(length.toByte()))
            } else if (length < (1U shl 8)) {
                append(majorTypeShifted.or(24))
                append(length.toByte())
            } else if (length < (1U shl 16)) {
                append(majorTypeShifted.or(25))
                append((length shr 8).and(0xffU).toByte())
                append((length shr 0).and(0xffU).toByte())
            } else if (length < (1U shl 32)) {
                append(majorTypeShifted.or(26))
                append((length shr 24).and(0xffU).toByte())
                append((length shr 16).and(0xffU).toByte())
                append((length shr 8).and(0xffU).toByte())
                append((length shr 0).and(0xffU).toByte())
            } else {
                append(majorTypeShifted.or(27))
                append((length shr 56).and(0xffU).toByte())
                append((length shr 48).and(0xffU).toByte())
                append((length shr 40).and(0xffU).toByte())
                append((length shr 32).and(0xffU).toByte())
                append((length shr 24).and(0xffU).toByte())
                append((length shr 16).and(0xffU).toByte())
                append((length shr 8).and(0xffU).toByte())
                append((length shr 0).and(0xffU).toByte())
            }
        }
    }

    /**
     * Encodes a data item to CBOR.
     *
     * @param item the [DataItem] to encode.
     * @returns the bytes of the item.
     */
    @JvmStatic
    fun encode(item: AnyCborItem?): ByteArray {
        val input = item ?: CborNull()
        val builder = ByteStringBuilder()
        input.encode(builder)
        return builder.toByteString().toByteArray()
    }

    // returns the new offset, then the length/value encoded in the decoded content
    //
    // throws IllegalArgumentException if not enough data or if additionalInformation
    // field is invalid
    //
    internal fun decodeLength(encodedCbor: ByteArray, offset: Int): Pair<Int, ULong> {
        val additionalInformation: Int
        try {
            additionalInformation = encodedCbor[offset].toInt().and(0x1f)
            if (additionalInformation < 24) {
                return Pair(offset + 1, additionalInformation.toULong())
            }
            when (additionalInformation) {
                24 -> return Pair(offset + 2, encodedCbor[offset + 1].toULong().and(0xffUL))
                25 -> {
                    val length = (encodedCbor[offset + 1].toULong().and(0xffUL) shl 8) +
                            encodedCbor[offset + 2].toULong().and(0xffUL)
                    return Pair(offset + 3, length)
                }

                26 -> {
                    val length = (encodedCbor[offset + 1].toULong().and(0xffUL) shl 24) +
                            (encodedCbor[offset + 2].toULong().and(0xffUL) shl 16) +
                            (encodedCbor[offset + 3].toULong().and(0xffUL) shl 8) +
                            encodedCbor[offset + 4].toULong().and(0xffUL)
                    return Pair(offset + 5, length)
                }

                27 -> {
                    val length = (encodedCbor[offset + 1].toULong().and(0xffUL) shl 56) +
                            (encodedCbor[offset + 2].toULong().and(0xffUL) shl 48) +
                            (encodedCbor[offset + 3].toULong().and(0xffUL) shl 40) +
                            (encodedCbor[offset + 4].toULong().and(0xffUL) shl 32) +
                            (encodedCbor[offset + 5].toULong().and(0xffUL) shl 24) +
                            (encodedCbor[offset + 6].toULong().and(0xffUL) shl 16) +
                            (encodedCbor[offset + 7].toULong().and(0xffUL) shl 8) +
                            encodedCbor[offset + 8].toULong().and(0xffUL)
                    return Pair(offset + 9, length)
                }

                31 ->
                    return Pair(offset + 1, 0UL)  // indefinite length
                else -> {}
            }
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Out of data at offset $offset", e)
        }
        throw IllegalArgumentException(
            "Illegal additional information value $additionalInformation at offset $offset"
        )
    }

    // This is based on the C code in https://www.rfc-editor.org/rfc/rfc8949.html#section-appendix.d
    private fun fromRawHalfFloat(raw: Int): Float {
        val exp = (raw shr 10) and 0x1f
        val mant = raw and 0x3ff
        val sign = (raw and 0x8000) != 0
        val value: Float
        if (exp == 0) value = mant * 2f.pow(-24)
        else if (exp != 31) value = (mant + 1024) * 2f.pow(exp - 25)
        else value = (if (mant == 0) Float.POSITIVE_INFINITY else Float.NaN)
        return if (sign) -value else value
    }

    /**
     * Low-level function to decode CBOR.
     *
     * This decodes a single CBOR data item and also returns the offset of the given byte array
     * of the data that was consumed.
     *
     * @param encodedCbor the bytes of the CBOR to decode.
     * @param offset the offset into the byte array to start decoding.
     * @return a pair where the first value is the ending offset and the second value is the
     * decoded data item.
     * @throws IllegalArgumentException if the data isn't valid CBOR.
     */
    @JvmStatic
    fun decodeWithOffset(encodedCbor: ByteArray, offset: Int): Pair<Int, AnyCborItem> {
        try {
            val first = encodedCbor[offset]
            val majorType = MajorType.Static.fromInt(first.toInt().and(0xff) ushr 5)
            val additionalInformation = first.toInt().and(0x1f)
            val (newOffset, item) = when (majorType) {
                MajorType.UNSIGNED_INTEGER -> {
                    if (additionalInformation == 31) {
                        throw IllegalArgumentException(
                            "Additional information 31 not allowed for majorType $majorType"
                        )
                    }
                    CborUInt.Static.decode(encodedCbor, offset)
                }

                MajorType.NEGATIVE_INTEGER -> {
                    if (additionalInformation == 31) {
                        throw IllegalArgumentException(
                            "Additional information 31 not allowed for majorType $majorType"
                        )
                    }
                    CborNInt.Static.decode(encodedCbor, offset)
                }

                MajorType.BYTE_STRING -> {
                    if (additionalInformation == 31) {
                        CborByteStringIndefLength.Static.decode(encodedCbor, offset)
                    } else {
                        CborByteString.Static.decode(encodedCbor, offset)
                    }
                }

                MajorType.UNICODE_STRING -> {
                    if (additionalInformation == 31) {
                        CborStringIndefLength.Static.decode(encodedCbor, offset)
                    } else {
                        CborString.Static.decode(encodedCbor, offset)
                    }
                }

                MajorType.ARRAY -> CborArray.Static.decode(encodedCbor, offset)
                MajorType.MAP -> CborMap.Static.decode(encodedCbor, offset)
                MajorType.TAG -> {
                    if (additionalInformation == 31) {
                        throw IllegalArgumentException(
                            "Additional information 31 not allowed for majorType 6"
                        )
                    }
                    CborTagged.Static.decode(encodedCbor, offset)
                }

                MajorType.SPECIAL -> {
                    if (additionalInformation < 24) {
                        CborSimple.Static.decode(encodedCbor, offset)
                    } else if (additionalInformation == 25) {
                        val raw = (encodedCbor[offset + 1].toInt().and(0xff) shl 8) +
                                encodedCbor[offset + 2].toInt().and(0xff)
                        Pair(offset + 3, CborFloat16(fromRawHalfFloat(raw)))
                    } else if (additionalInformation == 26) {
                        CborFloat.Static.decode(encodedCbor, offset)
                    } else if (additionalInformation == 27) {
                        CborDouble.Static.decode(encodedCbor, offset)
                    } else if (additionalInformation == 31) {
                        throw IllegalArgumentException("BREAK outside indefinite-length item")
                    } else {
                        CborConst.LOG.warn("fixme: decoding ${MajorType.SPECIAL} probably currently doesn't work")
                        CborSimple.Static.decode(encodedCbor, offset)
                    }
                }
            }
            check(newOffset > offset)
            return Pair(newOffset, item)
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalArgumentException("Out of bounds decoding data", e)
        } catch (e: Throwable) {
            throw IllegalArgumentException("Error occurred when decoding CBOR", e)
        }
    }

    /**
     * Decodes CBOR.
     *
     * The given [ByteArray] should contain the bytes of a single CBOR data item.
     *
     * @param encodedCbor the bytes of the CBOR to decode.
     * @return a [DataItem] with the decoded data.
     * @throws IllegalArgumentException if bytes are left over or the data isn't valid CBOR.
     */
    @JvmStatic
    fun <T: AnyCborItem> decode(encodedCbor: ByteArray): T {
        val (newOffset, item) = decodeWithOffset(encodedCbor, 0)
        if (newOffset != encodedCbor.size) {
            throw IllegalArgumentException(
                "${newOffset - encodedCbor.size} bytes leftover after decoding"
            )
        }
        return item as T
    }

    // Returns true iff all elements in |items| are not compound (e.g. an array or a map).
    private fun allDataItemsNonCompound(
        items: List<AnyCborItem>,
        options: Set<DiagnosticOption>
    ): Boolean {
        for (item in items) {
            if (options.contains(DiagnosticOption.EMBEDDED_CBOR) &&
                item is CborTagged && item.tagNumber == CborTagged.Static.ENCODED_CBOR
            ) {
                return false
            }
            when (item.majorType) {
                MajorType.ARRAY, MajorType.MAP -> return false
                else -> {}
            }
        }
        return true
    }

    private fun fitsInASingleLine(
        items: List<AnyCborItem>,
        options: Set<DiagnosticOption>
    ): Boolean =
        // For now just use this heuristic.
        allDataItemsNonCompound(items, options) && items.size < 8

    private fun toDiagnostics(
        sb: StringBuilder,
        indent: Int,
        item: AnyCborItem,
        tagNumberOfParent: Int?,
        options: Set<DiagnosticOption>
    ) {
        val pretty = options.contains(DiagnosticOption.PRETTY_PRINT)
        val indentString = if (!pretty) {
            ""
        } else {
            val indentBuilder = StringBuilder()
            for (n in 0 until indent) {
                indentBuilder.append(' ')
            }
            indentBuilder.toString()
        }

        if (item is RawCbor) {
            toDiagnostics(sb, indent, decode(item.value), tagNumberOfParent, options)
            return
        }

        when (item.majorType) {
            MajorType.UNSIGNED_INTEGER -> sb.append((item as CborUInt).value)

            MajorType.NEGATIVE_INTEGER -> {
                sb.append('-')
                sb.append((item as CborNInt).value)
            }

            MajorType.BYTE_STRING -> {
                when (item) {
                    is CborByteStringIndefLength -> {
                        if (DiagnosticOption.BSTR_PRINT_LENGTH in options) {
                            sb.append("indefinite-size byte-string")
                        } else {
                            sb.append("(_")
                            var count = 0
                            for (chunk in item.value) {
                                if (count++ == 0) {
                                    sb.append(" h'")
                                } else {
                                    sb.append(", h'")
                                }
                                for (b in chunk) {
                                    sb.append(HEX_DIGITS[b.toInt().and(0xff) shr 4])
                                    sb.append(HEX_DIGITS[b.toInt().and(0x0f)])
                                }
                                sb.append('\'')
                            }
                            sb.append(')')
                        }
                    }

                    is CborByteString -> {
                        if (tagNumberOfParent != null && tagNumberOfParent == CborTagged.Static.ENCODED_CBOR) {
                            sb.append("<< ")
                            try {
                                val embeddedItem: AnyCborItem = decode(item.value)
                                toDiagnostics(sb, indent, embeddedItem, null, options)
                            } catch (e: Exception) {
                                // Never throw an exception
                                sb.append("Error Decoding CBOR")
                            }
                            sb.append(" >>")
                        } else {
                            if (DiagnosticOption.BSTR_PRINT_LENGTH in options) {
                                when (item.value.size) {
                                    1 -> sb.append("${item.value.size} byte")
                                    else -> sb.append("${item.value.size} bytes")
                                }
                            } else {
                                sb.append("h'")
                                for (b in item.value) {
                                    sb.append(HEX_DIGITS[b.toInt().and(0xff) shr 4])
                                    sb.append(HEX_DIGITS[b.toInt().and(0x0f)])
                                }
                                sb.append("'")
                            }
                        }
                    }

                    else -> throw IllegalStateException("Unexpected item type $item")
                }
            }

            MajorType.UNICODE_STRING -> {
                when (item) {
                    is CborStringIndefLength -> {
                        sb.append("(_")
                        var count = 0
                        for (chunk in item.value) {
                            if (count++ == 0) {
                                sb.append(" \"")
                            } else {
                                sb.append(", \"")
                            }
                            val escapedChunkValue = chunk
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                            sb.append("$escapedChunkValue\"")
                        }
                        sb.append(')')
                    }

                    is CborString -> {
                        val escapedTstrValue = item.value
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                        sb.append("\"$escapedTstrValue\"")
                    }

                    else -> throw IllegalStateException("Unexpected item type")
                }
            }

            MajorType.ARRAY -> {
                val items = (item as CborArray<out AnyCborItem>).value
                if (!pretty || fitsInASingleLine(items, options)) {
                    sb.append(if (item.indefiniteLength) "[_ " else "[")
                    var count = 0
                    for (elementItem in items) {
                        toDiagnostics(sb, indent, elementItem, null, options)
                        if (++count < items.size) {
                            sb.append(", ")
                        }
                    }
                    sb.append("]")
                } else {
                    sb.append("[\n").append(indentString)
                    var count = 0
                    for (elementItem in items) {
                        sb.append("  ")
                        toDiagnostics(sb, indent + 2, elementItem, null, options)
                        if (++count < items.size) {
                            sb.append(",")
                        }
                        sb.append("\n").append(indentString)
                    }
                    sb.append("]")
                }
            }

            MajorType.MAP -> {
                val items = (item as CborMap<*, *>).value
                if (!pretty || items.isEmpty()) {
                    sb.append(if (item.indefiniteLength) "{_ " else "{")
                    var count = 0
                    for ((key, value) in items) {
                        toDiagnostics(sb, indent, key, null, options)
                        sb.append(": ")
                        toDiagnostics(sb, indent + 2, value!!, null, options)
                        if (++count < items.size) {
                            sb.append(", ")
                        }
                    }
                    sb.append("}")
                } else {
                    sb.append(if (item.indefiniteLength) "{_\n" else "{\n")
                    sb.append(indentString)
                    var count = 0
                    for ((key, value) in items) {
                        sb.append("  ")
                        toDiagnostics(sb, indent + 2, key, null, options)
                        sb.append(": ")
                        toDiagnostics(sb, indent + 2, value!!, null, options)
                        if (++count < items.size) {
                            sb.append(",")
                        }
                        sb.append("\n").append(indentString)
                    }
                    sb.append("}")
                }
            }

            MajorType.TAG -> {
                val tagNumber = (item as CborTagged).tagNumber
                sb.append("$tagNumber(")
                toDiagnostics(sb, indent, item.taggedItem, tagNumber, options)
                sb.append(")")
            }

            MajorType.SPECIAL -> {
                when (item) {
                    is CborSimple -> {
                        when (item) {
                            CborSimple.Static.FALSE -> sb.append("false")
                            CborSimple.Static.TRUE -> sb.append("true")
                            CborSimple.Static.NULL -> sb.append("null")
                            CborSimple.Static.UNDEFINED -> sb.append("undefined")
                            else -> sb.append("simple(${item.value})")
                        }
                    }

                    is CborFloat -> {
                        sb.append(item.value)
                    }

                    is CborDouble -> {
                        sb.append(item.value)
                    }

                    else -> {
                        throw IllegalArgumentException("Unexpected instance for MajorType.SPECIAL")
                    }
                }
            }

            null -> TODO()
        }

    }

    /**
     * Returns the diagnostics notation for a data item.
     *
     * @param item the CBOR data item.
     * @param options zero or more [DiagnosticOption].
     */
    @JvmStatic
    fun toDiagnostics(
        item: AnyCborItem,
        options: Set<DiagnosticOption> = emptySet()
    ): String {
        val sb = StringBuilder()
        toDiagnostics(sb, 0, item, null, options)
        return sb.toString()
    }

    /**
     * Returns the diagnostics notation for an encoded data item.
     *
     * @param encodedItem the encoded CBOR data item.
     * @param options zero or more [DiagnosticOption].
     */
    @JvmStatic
    fun toDiagnosticsEncoded(
        encodedItem: ByteArray,
        options: Set<DiagnosticOption> = emptySet()
    ): String {
        val sb = StringBuilder()
        toDiagnostics(sb, 0, decode(encodedItem), null, options)
        return sb.toString()
    }

}
