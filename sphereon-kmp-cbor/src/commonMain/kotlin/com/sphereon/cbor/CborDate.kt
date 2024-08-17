package com.sphereon.cbor

import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.DefaultDateTimeUtils
import com.sphereon.kmp.LocalDateTimeKMP
import com.sphereon.kmp.LongKMP
import com.sphereon.kmp.toKotlin
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlin.js.JsExport

@JsExport
class CborTDate(value: cddl_tdate) : CborTagged<cddl_tdate>(CDDL.tdate.info!!, CborString(value)) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(toValue())
    }
}

@JsExport
class CborFullDate(
    value: cddl_full_date
) : CborTagged<cddl_full_date>(CDDL.full_date.info!!, CborString(value)) {
    override fun toJson(): JsonElement {
        return JsonPrimitive(toValue())
    }
}


@JsExport
fun CborTDate.cborTDateToEpochSeconds(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    LongKMP(this.cborTDateToLocalDateTime(utils, timeZoneId).toInstant(utils.timeZone(timeZoneId)).epochSeconds)


@JsExport
fun CborFullDate.cborFullDateToEpochSeconds(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    LongKMP(this.cborFullDateToLocalDateTime(utils, timeZoneId).toInstant(utils.timeZone(timeZoneId)).epochSeconds)

fun CborTDate.cborTDateToLocalDateTime(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    this.value.toLocalDateTime(utils, timeZoneId)

fun CborFullDate.cborFullDateToLocalDateTime(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    this.value.toLocalDateTime(utils, timeZoneId)

fun cddl_full_date.toLocalDateTime(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    utils.dateTime(timeZoneId, LocalDateTimeKMP.fromString(this).toEpochSeconds(timeZoneId).toInt()).toKotlin()

@JsExport
fun cddl_full_date.toCborFullDate(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    CborFullDate(this.toLocalDateTime(utils, timeZoneId).date.toString())

fun Instant.instantToDateStringISO(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    utils.dateISO(timeZoneId, epochSeconds = this.epochSeconds.toInt())

@JsExport
fun LocalDateTimeKMP.localDateToDateStringISO(
    utils: DateTimeUtils = DateTimeUtils.DEFAULT,
    timeZoneId: String? = null
) =
    utils.dateISO(timeZoneId, this.delegate.toInstant(TimeZone.of(timeZoneId ?: utils.timeZoneId)).epochSeconds.toInt())

fun LocalDateTime.localDateTimeToDateStringISO(
    utils: DateTimeUtils = DefaultDateTimeUtils.INSTANCE,
    timeZoneId: String? = null
) =
    utils.dateISO(
        timeZoneId,
        epochSeconds = this.toInstant(TimeZone.of(timeZoneId ?: utils.timeZoneId)).epochSeconds.toInt()
    )

fun LocalDate.localDateToDateStringISO(
    utils: DateTimeUtils = DefaultDateTimeUtils.INSTANCE,
    timeZoneId: String? = null
) =
    utils.dateISO(
        timeZoneId,
        epochSeconds = this.atTime(0, 0).toInstant(TimeZone.of(timeZoneId ?: utils.timeZoneId)).epochSeconds.toInt()
    )

@JsExport
fun LocalDateTimeKMP.localDateToCborFullDate() = this.localDateToDateStringISO().toCborFullDate()
