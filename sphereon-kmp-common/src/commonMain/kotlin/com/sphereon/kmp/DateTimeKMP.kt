package com.sphereon.kmp

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
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
        return LocalDateTime.parse(this.toString()).compareTo(LocalDateTime.parse(other.toString()))
    }

    override fun toString(): String {
        return delegate.toString()
    }

    companion object {
        fun fromString(value: String): LocalDateTimeKMP {
            val parsed = LocalDateTime.parse(value)
            return LocalDateTimeKMP(
                parsed.year,
                parsed.monthNumber,
                parsed.dayOfMonth,
                parsed.hour,
                parsed.minute,
                parsed.second,
                parsed.nanosecond
            )
        }
    }

}

//fixme
//fun LocalDateTimeKMP.localDateToCborFullDate() = this.localDateToDateStringISO().toCborFullDate()


object InstantIso8601SerializerKMP : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.sphereon.kmp.datetime.Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

}


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
