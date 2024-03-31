package org.dbtools.kmp.commons.ext

import androidx.lifecycle.SavedStateHandle

fun createSaveStateErrorMessage(key: String) = "Missing SavedState value for Key: $key"

fun SavedStateHandle.requireString(key: String): String = requireNotNull(get<String>(key)) { createSaveStateErrorMessage(key) }
fun SavedStateHandle.requireInt(key: String): Int = requireNotNull(get<Int>(key)) { createSaveStateErrorMessage(key) }
fun SavedStateHandle.requireLong(key: String): Long = requireNotNull(get<Long>(key)) { createSaveStateErrorMessage(key) }
fun SavedStateHandle.requireFloat(key: String): Float = requireNotNull(get<Float>(key)) { createSaveStateErrorMessage(key) }
fun SavedStateHandle.requireBoolean(key: String): Boolean = requireNotNull(get<Boolean>(key)) { createSaveStateErrorMessage(key) }

fun SavedStateHandle.getString(key: String): String? = get<String>(key)
fun SavedStateHandle.getInt(key: String): Int? = get<Int>(key)
fun SavedStateHandle.getLong(key: String): Long? = get<Long>(key)
fun SavedStateHandle.getFloat(key: String): Float? = get<Float>(key)
fun SavedStateHandle.getBoolean(key: String): Boolean? = get<Boolean>(key)
