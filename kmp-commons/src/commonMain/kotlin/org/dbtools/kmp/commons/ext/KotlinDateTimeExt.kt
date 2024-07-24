package org.dbtools.kmp.commons.ext

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds

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