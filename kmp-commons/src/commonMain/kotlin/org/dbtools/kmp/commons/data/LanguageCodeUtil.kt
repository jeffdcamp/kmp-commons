package org.dbtools.kmp.commons.data

object LanguageCodeUtil {
    private val bcp47ToIso3Cache = mutableMapOf<LanguageCode, LanguageCodeIso3>()
    private val iso3ToBcp47Cache: Map<LanguageCodeIso3, LanguageCode> by lazy { LanguageCodeSource.getIso3ToBcp47Map() }

    fun toLanguageCodeIso3(languageCode: LanguageCode): LanguageCodeIso3? {
        bcp47ToIso3Cache[languageCode]?.let { return it }

        val result: LanguageCodeIso3 = LanguageCodeSource.getIso3For(languageCode) ?: return null

        bcp47ToIso3Cache[languageCode] = result

        return result
    }

    fun toLanguageCode(languageCodeIso3: LanguageCodeIso3): LanguageCode? = iso3ToBcp47Cache[languageCodeIso3]
}

fun LanguageCode.toLanguageCodeIso3(): LanguageCodeIso3? = LanguageCodeUtil.toLanguageCodeIso3(this)
fun LanguageCodeIso3.toLanguageCode(): LanguageCode? = LanguageCodeUtil.toLanguageCode(this)
