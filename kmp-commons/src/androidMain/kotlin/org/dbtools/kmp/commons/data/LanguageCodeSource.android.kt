package org.dbtools.kmp.commons.data

import android.os.Build
import co.touchlab.kermit.Logger
import java.util.Locale
import java.util.MissingResourceException

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                        Locale.of(bcp47Code)
                    } else {
                        @Suppress("DEPRECATION")
                        Locale(bcp47Code)
                    }
                } catch (expected: Exception) {
                    Logger.e(expected) { "Can't convert from $bcp47Code to LanguageCodeIso3 (Locale($bcp47Code) failed)" }
                    null
                }

                try {
                    locale?.let { put(LanguageCodeIso3(it.isO3Language), LanguageCode(bcp47Code)) }
                } catch (expected: MissingResourceException) {
                    Logger.e(expected) { "Can't convert from $bcp47Code to LanguageCodeIso3" }
                }
            }
        }
    }
}
