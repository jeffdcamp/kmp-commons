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
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import kotlin.math.floor
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Trim the nanoseconds off of an Instant
 *
 * @return Instant with nanoseconds set to 0
 */
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

fun LocalDate.Companion.now(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = clock.todayIn(timeZone)
fun LocalTime.Companion.now(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalTime = clock.now().toLocalDateTime(timeZone).time
fun LocalDateTime.Companion.now(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime = clock.now().toLocalDateTime(timeZone)
fun LocalDateTime.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = this.toInstant(timeZone).toEpochMilliseconds()

fun Instant.isToday(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean = clock.todayIn(timeZone) == toLocalDateTime(timeZone).date
fun LocalDate.atStartOfDay(): LocalDateTime = atTime(0, 0)
fun LocalDate.atEndOfDay(): LocalDateTime = atTime(23, 59, 59, 59)
fun LocalDate.atEndOfDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime = plus(DatePeriod(days = 1)).atStartOfDayIn(timeZone).minus(1.seconds).toLocalDateTime(timeZone)

fun Instant.weeksUntil(other: Instant, timeZone: TimeZone): Int = floor(daysUntil(other, timeZone).toDouble() / 7).toInt()
fun LocalDate.weeksUntil(other: LocalDate): Int = floor(daysUntil(other).toDouble() / 7).toInt()

operator fun DayOfWeek.plus(days: Long): DayOfWeek {
    val amount = (days % 7).toInt()
    return DayOfWeek.entries[(ordinal + (amount + 7)) % 7]
}

operator fun DayOfWeek.minus(days: Long): DayOfWeek = plus(-(days % 7))
