package com.sphereon.kmp

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable(with = LocalDateTimeIso8601SerializerKMP::class)
class LocalDateTimeKMP(
    val year: Int,
    val monthNumber: Int,
    val dayOfMonth: Int,
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val nanosecond: Int = 0
) : Comparable<LocalDateTimeKMP> {

    val delegate = LocalDateTime(
        year = year,
        monthNumber = monthNumber,
        dayOfMonth = dayOfMonth,
        hour = hour,
        minute = minute,
        second = second,
        nanosecond = nanosecond
    )

    override fun compareTo(other: LocalDateTimeKMP): Int {
        return Static.fromString(this.toString()).compareTo(Static.fromString(other.toString()))
    }

    override fun toString(): String {
        return delegate.toString()
    }

    fun toEpochSeconds(timeZoneId: String? = TimeZone.UTC.id): LongKMP {
        return LongKMP(delegate.toInstant(DateTimeUtils.Static.DEFAULT.timeZone(timeZoneId)).epochSeconds)
    }

    object Static {
        fun fromString(value: String): LocalDateTimeKMP {
            val datetime: LocalDateTime = if (value.lowercase().endsWith('z')) {
                val instant = Instant.parse(value)
                instant.toLocalDateTime(TimeZone.of(DateTimeUtils.Static.DEFAULT.timeZoneId))
            } else {
                LocalDateTime.parse(value)
            }


            return LocalDateTimeKMP(
                datetime.year,
                datetime.monthNumber,
                datetime.dayOfMonth,
                datetime.hour,
                datetime.minute,
                datetime.second,
                datetime.nanosecond
            )
        }
    }

}

//fixme
//fun LocalDateTimeKMP.localDateToCborFullDate() = this.localDateToDateStringISO().toCborFullDate()


typealias InstantIso8601SerializerKMP  = InstantIso8601Serializer


/**
 * A serializer for [LocalDateTime] that uses the ISO-8601 representation.
 *
 * JSON example: `"2007-12-31T23:59:01"`
 *
 * @see LocalDateTime.parse
 * @see LocalDateTime.toString
 */
object LocalDateTimeIso8601SerializerKMP : KSerializer<LocalDateTimeKMP> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.sphereon.kmp.datetime.LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTimeKMP {
        val parsed = LocalDateTime.parse(decoder.decodeString())
        return LocalDateTimeKMP(
            year = parsed.year,
            monthNumber = parsed.monthNumber,
            dayOfMonth = parsed.dayOfMonth,
            hour = parsed.hour,
            minute = parsed.minute,
            second = parsed.second,
            nanosecond = parsed.nanosecond
        )
    }

    override fun serialize(encoder: Encoder, value: LocalDateTimeKMP) {
        encoder.encodeString(value.toString())
    }

}
