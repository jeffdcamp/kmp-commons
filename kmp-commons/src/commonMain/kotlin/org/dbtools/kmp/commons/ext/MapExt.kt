package org.dbtools.kmp.commons.ext

fun Map<*, *>.requireString(key: String): String = getString(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getString(key: String): String? {
    val data = this[key] ?: return null
    return when (data) {
        is String -> data
        else -> error("data for key [$key] is NOT a String.  Data: [${data::class}]")
    }
}

fun Map<*, *>.requireBoolean(key: String): Boolean = getBoolean(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getBoolean(key: String): Boolean? {
    val data = this[key] ?: return null
    return when (data) {
        is Boolean -> data
        else -> error("data for key [$key] is NOT a Boolean.  Data: [${data::class}]")
    }
}

fun Map<*, *>.requireInt(key: String): Int = getInt(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getInt(key: String): Int? {
    val data = this[key] ?: return null
    return when (data) {
        is Int -> data
        is Long -> data.toInt()
        else -> error("data for key [$key] is NOT a Int.  Data: [${data::class}]")
    }
}

fun Map<*, *>.requireFloat(key: String): Float = getFloat(key) ?: error("Value for key [$key] cannot be null/missing")

fun Map<*, *>.getFloat(key: String): Float? {
    val data = this[key] ?: return null
    return when (data) {
        is Float -> data
        is Double -> data.toFloat()
        else -> error("data for key [$key] is NOT a Float.  Data: [${data::class}]")
    }
}

fun <T> Map<*, *>.getType(key: String, map: (Any) -> T): T? {
    val data = this[key] ?: return null
    return map(data)
}
