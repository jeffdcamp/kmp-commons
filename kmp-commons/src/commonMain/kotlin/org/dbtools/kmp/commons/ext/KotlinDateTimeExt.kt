package org.dbtools.kmp.commons.ext

import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

fun Instant.trimToSeconds(): Instant {
    return minus(nanosecondsOfSecond.nanoseconds)
}

/**
 * Get the [DayOfWeek] for this Instant
 *
 * @param timeZone [TimeZone] that should be used consideration when determining [DayOfWeek]. Default = currentSystemDefault()
 * @return [DayOfWeek] for this Instant
 */
fun Instant.dayOfWeek(timeZone: TimeZone = TimeZone.currentSystemDefault()): DayOfWeek = toLocalDateTime(timeZone).dayOfWeek

/**
 * Get the [Instant] for the next [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the future, to find an [Instant] for
 * @param timeZone [TimeZone] that should be used consideration when searching for [DayOfWeek]. Default = currentSystemDefault()
 * @return Instant of the found [DayOfWeek]
 */
fun Instant.nextDayOfWeek(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val daysDiff = dayOfWeek(timeZone).isoDayNumber - dayOfWeek.isoDayNumber

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return plus(daysToAdd.days)
}

/**
 * Get the [Instant] for the next [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the future, to find an [Instant] for
 * @param timeZone [TimeZone] that should be used consideration when searching for [DayOfWeek]. Default = currentSystemDefault()
 * @return Instant of the found [DayOfWeek]
 */
fun Instant.nextOrSameDayOfWeek(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val daysDiff = dayOfWeek(timeZone).isoDayNumber - dayOfWeek.isoDayNumber
    if (daysDiff == 0) return this

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return plus(daysToAdd.days)
}

/**
 * Get the [Instant] for the previous [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the past, to find an [Instant] for
 * @param timeZone [TimeZone] that should be used consideration when searching for [DayOfWeek]. Default = currentSystemDefault()
 * @return Instant of the found [DayOfWeek]
 */
fun Instant.previousDayOfWeek(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val daysDiff = dayOfWeek.isoDayNumber - dayOfWeek(timeZone).isoDayNumber

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return minus(daysToAdd.days)
}

/**
 * Get the [Instant] for the previous [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the past, to find an [Instant] for
 * @param timeZone [TimeZone] that should be used consideration when searching for [DayOfWeek]. Default = currentSystemDefault()
 * @return Instant of the found [DayOfWeek]
 */
fun Instant.previousOrSameDayOfWeek(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    val daysDiff = dayOfWeek.isoDayNumber - dayOfWeek(timeZone).isoDayNumber
    if (daysDiff == 0) return this

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return minus(daysToAdd.days)
}

fun LocalDate.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = Clock.System.todayIn(timeZone)
fun LocalTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalTime = Clock.System.now().toLocalDateTime(timeZone).time
fun LocalDateTime.Companion.now(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
fun LocalDateTime.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = this.toInstant(timeZone).toEpochMilliseconds()

fun LocalDate.atStartOfDay(): LocalDateTime = atTime(0, 0)
fun LocalDate.atEndOfDay(): LocalDateTime = atTime(23, 59, 59, 59)
fun LocalDate.atEndOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime = plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone).minus(1.seconds).toLocalDateTime(timeZone)
