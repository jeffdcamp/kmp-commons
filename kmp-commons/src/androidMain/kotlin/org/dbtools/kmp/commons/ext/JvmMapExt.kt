package org.dbtools.kmp.commons.ext

import com.google.firebase.Timestamp
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date

fun Map<*, *>.requireInstant(key: String): Timestamp = getTimestamp(key) ?: error("Value for key [$key] cannot be null/missing")
fun Map<*, *>.getTimestamp(key: String): Timestamp? {
    val data = this[key] ?: return null
    return when (data) {
        is Timestamp -> data
        else -> error("data for key [$key] is NOT a Timestamp.  Data: [${data::class.java}]")
    }
}

fun Map<*, *>.requireDate(key: String): Date = getDate(key) ?: error("Value for key [$key] cannot be null/missing")
fun Map<*, *>.getDate(key: String): Date? = getTimestamp(key)?.toDate()

fun Map<*, *>.requireOffsetDateTime(key: String): OffsetDateTime = getOffsetDateTime(key) ?: error("Value for key [$key] cannot be null/missing")
fun Map<*, *>.getOffsetDateTime(key: String): OffsetDateTime? = getTimestamp(key)?.toDate()?.toInstant()?.atOffset(ZoneOffset.UTC)
