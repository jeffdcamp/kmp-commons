package org.dbtools.kmp.commons.data

import co.touchlab.kermit.Logger
import java.util.Locale

actual object LanguageCodeSource {
    actual fun getIso3For(languageCode: LanguageCode): LanguageCodeIso3? {
        return try {
            val locale = Locale.forLanguageTag(languageCode.value)

            if (locale.language.isEmpty()) return null

            LanguageCodeIso3(locale.isO3Language)
        } catch (expected: Exception) {
            Logger.e(expected) { "Can't convert from $languageCode to LanguageCodeIso3" }
            null
        }
    }

    actual fun getIso3ToBcp47Map(): Map<LanguageCodeIso3, LanguageCode> {
        return buildMap {
            Locale.getISOLanguages().forEach { bcp47Code ->
                val locale = try {
                    Locale.of(bcp47Code)
                } catch (expected: Exception) {
                    Logger.e(expected) { "Can't convert from $bcp47Code to LanguageCodeIso3 (Locale.of($bcp47Code) failed)" }
                    null
                }

                try {
                    locale?.let { put(LanguageCodeIso3(it.isO3Language), LanguageCode(bcp47Code)) }
                } catch (expected: Exception) {
                    Logger.e(expected) { "Can't convert from $bcp47Code to LanguageCodeIso3 (locale.isO3Language failed)" }
                }
            }
        }
    }
}
