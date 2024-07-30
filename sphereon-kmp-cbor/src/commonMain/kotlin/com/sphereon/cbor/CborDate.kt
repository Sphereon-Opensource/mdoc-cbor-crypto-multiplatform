package com.sphereon.cbor

import com.sphereon.kmp.DateTimeUtils
import com.sphereon.kmp.DefaultDateTimeUtils
import com.sphereon.kmp.LocalDateTimeKMP
import com.sphereon.kmp.toKotlin
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.io.bytestring.ByteStringBuilder
import kotlin.js.JsExport

@JsExport
class CborTDate(value: cddl_tdate) : CborTagged<cddl_tdate>(CDDL.tdate.info!!, CborString(value))

@JsExport
class CborFullDate(
    value: cddl_full_date
) : CborTagged<cddl_full_date>(CDDL.full_date.info!!, CborString(value))


fun cddl_full_date.toFullDate(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    utils.dateTime(timeZoneId).toKotlin().date

fun cddl_full_date.toCborFullDate(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    CborFullDate(this.toFullDate(utils, timeZoneId).toString())

fun Instant.instantToDateStringISO(utils: DateTimeUtils = DateTimeUtils.DEFAULT, timeZoneId: String? = null) =
    utils.dateISO(timeZoneId, epochSeconds = this.epochSeconds.toInt())

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

fun LocalDateTimeKMP.localDateToCborFullDate() = this.localDateToDateStringISO().toCborFullDate()
