package org.dbtools.kmp.commons.ext

import kotlinx.datetime.*
import kotlin.math.floor
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

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
 * Get the [DayOfWeek] for the next [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the future, to find an [DayOfWeek] for
 * @return Instant of the found [DayOfWeek]
 */
fun LocalDate.nextDayOfWeek(
    dayOfWeek: DayOfWeek,
): LocalDate {
    val daysDiff = this.dayOfWeek.isoDayNumber - dayOfWeek.isoDayNumber

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return plus(daysToAdd, DateTimeUnit.DAY)
}

fun DayOfWeek.nextDayOfWeekInstant(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    return asOf.nextDayOfWeek(this, timeZone)
}

fun DayOfWeek.nextDayOfWeekLocalDate(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate {
    return asOf.toLocalDateTime(timeZone).date.nextDayOfWeek(this)
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
 * Get the [LocalDate] for the next [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the future, to find an [LocalDate] for
 * @return Instant of the found [DayOfWeek]
 */
fun LocalDate.nextOrSameDayOfWeek(
    dayOfWeek: DayOfWeek,
): LocalDate {
    val daysDiff = this.dayOfWeek.isoDayNumber - dayOfWeek.isoDayNumber
    if (daysDiff == 0) return this

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return plus(daysToAdd, DateTimeUnit.DAY)
}

fun DayOfWeek.nextOrSameDayOfWeekInstant(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    return asOf.nextOrSameDayOfWeek(this, timeZone)
}

fun DayOfWeek.nextOrSameDayOfWeekLocalDate(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate {
    return asOf.toLocalDateTime(timeZone).date.nextOrSameDayOfWeek(this)
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

fun DayOfWeek.previousDayOfWeekInstant(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    return asOf.previousDayOfWeek(this, timeZone)
}

fun DayOfWeek.previousDayOfWeekLocalDate(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate {
    return asOf.toLocalDateTime(timeZone).date.previousDayOfWeek(this)
}


/**
 * Get the [LocalDate] for the previous [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the past, to find an [LocalDate] for
 * @return Instant of the found [DayOfWeek]
 */
fun LocalDate.previousDayOfWeek(
    dayOfWeek: DayOfWeek,
): LocalDate {
    val daysDiff = dayOfWeek.isoDayNumber - this.dayOfWeek.isoDayNumber

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return minus(daysToAdd, DateTimeUnit.DAY)
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

/**
 * Get the [LocalDate] for the previous [DayOfWeek]
 * Temp workaround for https://github.com/Kotlin/kotlinx-datetime/issues/325
 *
 * @param dayOfWeek [DayOfWeek], in the past, to find an [LocalDate] for
 * @return Instant of the found [DayOfWeek]
 */
fun LocalDate.previousOrSameDayOfWeek(
    dayOfWeek: DayOfWeek,
): LocalDate {
    val daysDiff = dayOfWeek.isoDayNumber - this.dayOfWeek.isoDayNumber
    if (daysDiff == 0) return this

    val daysToAdd = if (daysDiff >= 0) 7 - daysDiff else -daysDiff
    return minus(daysToAdd, DateTimeUnit.DAY)
}

fun DayOfWeek.previousOrSameDayOfWeekInstant(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): Instant {
    return asOf.previousOrSameDayOfWeek(this, timeZone)
}

fun DayOfWeek.previousOrSameDayOfWeekLocalDate(
    asOf: Instant = Clock.System.now(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate {
    return asOf.toLocalDateTime(timeZone).date.previousOrSameDayOfWeek(this)
}

fun LocalDate.Companion.now(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate = clock.todayIn(timeZone)
fun LocalTime.Companion.now(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalTime = clock.now().toLocalDateTime(timeZone).time
fun LocalDateTime.Companion.now(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime = clock.now().toLocalDateTime(timeZone)
fun LocalDateTime.toEpochMilliseconds(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long = this.toInstant(timeZone).toEpochMilliseconds()

fun Instant.isToday(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean = clock.todayIn(timeZone) == toLocalDateTime(timeZone).date
fun Instant.atStartOfDay(clock: Clock = Clock.System, timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant = toLocalDateTime(timeZone).date.atStartOfDayIn(timeZone)
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

fun Instant.Companion.parseNullable(text: String?): Instant? {
    return if (!text.isNullOrEmpty() && text != "null") {
        try {
            Instant.parse(text)
        } catch (expected: Exception) {
            throw IllegalArgumentException("Cannot parse Instant text: $text", expected)
        }
    } else {
        null
    }
}

fun LocalDateTime.Companion.parseNullable(text: String?): LocalDateTime? {
    return if (!text.isNullOrEmpty() && text != "null") {
        try {
            LocalDateTime.parse(text)
        } catch (expected: Exception) {
            throw IllegalArgumentException("Cannot parse datetime text: $text", expected)
        }
    } else {
        null
    }
}

fun LocalDate.Companion.parseNullable(text: String?): LocalDate? {
    return if (!text.isNullOrEmpty() && text != "null") {
        try {
            LocalDate.parse(text)
        } catch (expected: Exception) {
            throw IllegalArgumentException("Cannot parse date text: $text", expected)
        }
    } else {
        null
    }
}

fun LocalTime.Companion.parseNullable(text: String?): LocalTime? {
    return if (!text.isNullOrEmpty() && text != "null") {
        try {
            LocalTime.parse(text)
        } catch (expected: Exception) {
            throw IllegalArgumentException("Cannot parse time text: $text", expected)
        }
    } else {
        null
    }
}
