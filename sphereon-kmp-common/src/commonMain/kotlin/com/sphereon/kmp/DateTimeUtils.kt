package com.sphereon.kmp

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate.Formats.ISO
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
//@KustomExport
@JsExport
//@JsName("DateTimeUtilsJS")
class DateTimeUtils(
    var clock: Clock = Clock.System,
    var timeZoneId: String = TimeZone.currentSystemDefault().id
) {
    // TODO: Let's hope we can properly use Longs in a KMP project in 2038, when the epoch would run out of max int
    fun epochSeconds() = now().epochSeconds.toInt()
    fun dateTimeUTC(epochSeconds: Int? = epochSeconds()) = dateTime(TimeZone.UTC.id, epochSeconds)
    fun dateTimeLocal(epochSeconds: Int? = epochSeconds()) = dateTime(timeZoneId, epochSeconds)
    fun dateTime(timeZoneId: String? = null, epochSeconds: Int? = epochSeconds()) =
        Instant.fromEpochSeconds(epochSeconds?.toLong() ?: epochSeconds().toLong())
            .toLocalDateTime(timeZone(timeZoneId)).toKMP()

    fun dateLocalISO(epochSeconds: Int? = epochSeconds()) = dateISO(timeZoneId, epochSeconds)
    fun dateISO(timeZoneId: String?, epochSeconds: Int? = epochSeconds()) =
        dateTime(timeZoneId ?: this.timeZoneId, epochSeconds).toKotlin().date.format(ISO)

    object Static {
        val DEFAULT: DateTimeUtils = DateTimeUtils()
    }


    private fun now() = clock.now()
    @JsExport.Ignore
    fun timeZone(timeZoneId: String?): TimeZone = timeZoneId?.let { TimeZone.of(it) } ?: defaultTimeZone.value
    private val defaultTimeZone = lazy { TimeZone.of(this.timeZoneId) }
}

@OptIn(ExperimentalJsExport::class)
@JsExport
object DefaultDateTimeUtils {
    val INSTANCE = DateTimeUtils()
}

fun LocalDateTimeKMP.toKotlin(): LocalDateTime = LocalDateTime.parse(this.toString())
fun LocalDateTime.toKMP(): LocalDateTimeKMP = LocalDateTimeKMP.Static.fromString(this.toString())

fun Long.toLocalDateTimeKMP(dateTimeUtils: DateTimeUtils? = null): LocalDateTimeKMP =
    getDateTime(dateTimeUtils).dateTimeLocal(this.toInt())

fun getDateTime(dateTimeUtils: DateTimeUtils? = null) = dateTimeUtils ?: DefaultDateTimeUtils.INSTANCE

