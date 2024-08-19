package com.sphereon.cbor

import com.sphereon.kmp.LongKMP
import kotlinx.io.bytestring.ByteStringBuilder
import kotlinx.serialization.json.JsonElement
import kotlin.js.JsExport


@JsExport
open class CborTagged<T>(val tagNumber: Int, val taggedItem: CborItem<T>) :
    CborItem<T>(taggedItem.value, taggedItem.cddl) {
    override fun encode(builder: ByteStringBuilder) {
        Cbor.encodeLength(builder, MajorType.TAG, tagNumber)
        taggedItem.encode(builder)
    }

    override fun toJsonSimple(): JsonElement {
        return taggedItem.toJsonSimple()
    }

    companion object {
        /**
         * Standard date/time string.
         *
         * Tag number 0 contains a text string in the standard format described by the date-time
         * production in [RFC3339], as refined by Section 3.3 of [RFC4287], representing the point
         * in time described there. A nested item of another type or a text string that doesn't
         * match the format described in [RFC4287] is invalid.
         */
        const val DATE_TIME_STRING = 0

        /**
         * Epoch-based date/time.
         *
         * Tag number 1 contains a numerical value counting the number of seconds from
         * 1970-01-01T00:00Z in UTC time to the represented point in civil time.
         *
         * The tag content MUST be an unsigned or negative integer (major types 0 and 1) or a
         * floating-point number (major type 7 with additional information 25, 26, or 27). Other
         * contained types are invalid.
         *
         * Nonnegative values (major type 0 and nonnegative floating-point numbers) stand for time
         * values on or after 1970-01-01T00:00Z UTC and are interpreted according to POSIX [TIME_T].
         * (POSIX time is also known as "UNIX Epoch time".) Leap seconds are handled specially by
         * POSIX time, and this results in a 1-second discontinuity several times per decade. Note
         * that applications that require the expression of times beyond early 2106 cannot leave out
         * support of 64-bit integers for the tag content.
         *
         * Negative values (major type 1 and negative floating-point numbers) are interpreted as
         * determined by the application requirements as there is no universal standard for UTC
         * count-of-seconds time before 1970-01-01T00:00Z (this is particularly true for points in
         * time that precede discontinuities in national calendars). The same applies to non-finite
         * values.
         *
         * To indicate fractional seconds, floating-point values can be used within tag number 1
         * instead of integer values. Note that this generally requires binary64 support, as
         * binary16 and binary32 provide nonzero fractions of seconds only for a short period of
         * time around early 1970. An application that requires tag number 1 support may restrict
         * the tag content to be an integer (or a floating-point value) only.
         *
         * Note that platform types for date/time may include null or undefined values, which may
         * also be desirable at an application protocol level. While emitting tag number 1
         * values with non-finite tag content values (e.g., with NaN for undefined date/time values
         * or with Infinity for an expiry date that is not set) may seem an obvious way to handle
         * this, using untagged null or undefined avoids the use of non-finites and results in a
         * shorter encoding. Application protocol designers are encouraged to consider these cases
         * and include clear guidelines for handling them.
         */
        const val DATE_TIME_NUMBER = 1


        /**
         * Encoded CBOR data item.
         *
         * Sometimes it is beneficial to carry an embedded CBOR data item that is not meant to be
         * decoded immediately at the time the enclosing data item is being decoded. Tag number
         * 24 (CBOR data item) can be used to tag the embedded byte string as a single data item
         * encoded in CBOR format. Contained items that aren't byte strings are invalid. A contained
         * byte string is valid if it encodes a well-formed CBOR data item; validity checking of the
         * decoded CBOR item is not required for tag validity (but could be offered by a generic
         * decoder as a special option).
         */
        const val ENCODED_CBOR = 24

        /**
         * CBOR tag for a text string representing a date without a time.
         *
         * See https://datatracker.ietf.org/doc/html/rfc8943.
         */
        const val FULL_DATE_STRING = 1004

        internal fun decode(encodedCbor: ByteArray, offset: Int): Pair<Int, AnyCborItem> {
            val (itemOffset, tagNumber) = Cbor.decodeLength(encodedCbor, offset)
            val (newItemOffset, taggedItem) = Cbor.decodeWithOffset(encodedCbor, itemOffset)
            return when (val tag = tagNumber.toInt()) {
                FULL_DATE_STRING -> {
                    Pair(newItemOffset, CborFullDate(taggedItem.value as String))
                }

                DATE_TIME_STRING -> {
                    Pair(newItemOffset, CborTDate(taggedItem.value as String))
                }

                DATE_TIME_NUMBER -> {
                    Pair(newItemOffset, CborTime(taggedItem.value as LongKMP))
                }

                ENCODED_CBOR -> {
                    require(taggedItem is CborByteString)
                    Pair(
                        newItemOffset,
                        CborEncodedItem(
                            taggedItem.cborDecode() as AnyCborItem,
                            taggedItem,
                        )
                    )
                }

                /*COSE_SIGN1 -> {
                    Pair(newItemOffset, CoseSign1(taggedItem))
                }*/

                else -> {
                    CborConst.LOG.info("##### Generic TAG encountered: $tag")
                    return Pair(newItemOffset, CborTagged(tagNumber.toInt(), taggedItem))
                }
            }

        }
    }
}
