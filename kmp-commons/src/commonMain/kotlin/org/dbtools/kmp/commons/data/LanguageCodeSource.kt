package org.dbtools.kmp.commons.data

expect object LanguageCodeSource {
    fun getIso3For(languageCode: LanguageCode): LanguageCodeIso3?
    fun getIso3ToBcp47Map(): Map<LanguageCodeIso3, LanguageCode>
}
