package org.dbtools.kmp.commons.ext

import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.nanoseconds

fun Instant.trimToSeconds(): Instant {
    return minus(nanosecondsOfSecond.nanoseconds)
}
