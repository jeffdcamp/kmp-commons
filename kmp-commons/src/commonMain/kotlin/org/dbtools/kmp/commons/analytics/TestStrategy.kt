@file:Suppress("unused")

package org.dbtools.kmp.commons.analytics

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class TestStrategy(
    private val logBlock: (message: String) -> Unit
) : AppAnalytics.Strategy {
    private var logLevel = AppAnalytics.LogLevel.NONE
    private var providerLoggingEnabled = false

    var eventScopeLevel = AppAnalytics.DEFAULT_EVENT_SCOPE_LEVEL
    var screenScopeLevel = AppAnalytics.DEFAULT_SCREEN_SCOPE_LEVEL
    var errorScopeLevel = AppAnalytics.DEFAULT_ERROR_SCOPE_LEVEL

    private val analyticsList = mutableListOf<Analytic>()
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        allowSpecialFloatingPointValues = true
        useArrayPolymorphism = true
        prettyPrint = true
    }

    override fun setLogLevel(logLevel: AppAnalytics.LogLevel, enableProviderLogging: Boolean) {
        this.logLevel = logLevel
        this.providerLoggingEnabled = enableProviderLogging
    }

    override fun logError(errorMessage: String, errorClass: String, scopeLevel: AppAnalytics.ScopeLevel) {
        if (scopeLevel.ordinal > errorScopeLevel.ordinal) {
            return
        }

        consoleLogMessage(AppAnalytics.LogLevel.EVENT, "logError($errorMessage)")
    }

    override fun logEvent(eventId: String, parameterMap: Map<String, String>, scopeLevel: AppAnalytics.ScopeLevel) {
        if (scopeLevel.ordinal > eventScopeLevel.ordinal) {
            return
        }

        consoleLogMessage(AppAnalytics.LogLevel.EVENT, "logEvent($eventId)")
        consoleLogParameterMap(parameterMap)
    }

    override fun logScreen(screenTitle: String, parameterMap: Map<String, String>, scopeLevel: AppAnalytics.ScopeLevel) {
        if (scopeLevel.ordinal > screenScopeLevel.ordinal) {
            return
        }

        consoleLogMessage(AppAnalytics.LogLevel.EVENT, "logScreen($screenTitle)")
        consoleLogParameterMap(parameterMap)
    }

    private fun consoleLogMessage(level: AppAnalytics.LogLevel, message: String) {
        if (level.ordinal <= logLevel.ordinal) {
            logBlock(message)
        }
    }

    private fun consoleLogParameterMap(parameterMap: Map<String, String>) {
        if (logLevel.ordinal >= AppAnalytics.LogLevel.VERBOSE.ordinal) {
            parameterMap.keys.forEach {
                consoleLogMessage(AppAnalytics.LogLevel.VERBOSE, "  $it:${parameterMap[it]}")
            }
        }
    }

    @Serializable
    private data class Analytic(val type: AnalyticType, val name: String, val properties: Map<String, String> = emptyMap())
    private enum class AnalyticType {
        EVENT, SCREEN, ERROR
    }
}
