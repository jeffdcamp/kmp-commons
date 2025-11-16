package org.dbtools.kmp.commons.data

import kotlinx.serialization.Serializable
import org.dbtools.kmp.commons.serialization.DataClassStringSerializer
import org.dbtools.kmp.commons.serialization.DataValueStringClass

@Serializable(with = LanguageCodeSerializer::class)
data class LanguageCode(override val value: String): DataValueStringClass {
    init {
        require(value.isNotBlank()) { "LanguageCode must not be empty" }
    }

    // Split the BCP 47 code into its component parts
    private val parts: List<String> by lazy { value.split('-') }

    // ISO 639-1,3: The primary language is always the first part (e.g., "en", "zh")
    val primaryLanguage: String by lazy { parts.first() }

    // ISO 15924: The script is an optional 4-letter code (e.g., "Latn", "Hans")
    val script: String? by lazy { parts.drop(1).find { it.length == 4 && it.all(Char::isLetter) } }

    // ISO 3166-1 or UN M.49: The region is an optional 2-letter or 3-digit code (e.g., "US", "419")
    val region: String? by lazy {
        parts.drop(1).find {
            (it.length == 2 && it.all(Char::isLetter)) ||
                    (it.length == 3 && it.all(Char::isDigit))
        }
    }

//    companion object {
//        val BCP47_REGEX = "^[a-zA-Z]{2}(-[a-zA-Z0-9]{1,8})*$".toRegex()
//    }
}
object LanguageCodeSerializer : DataClassStringSerializer<LanguageCode>("LanguageCodeSerializer", { LanguageCode(it) })

@Serializable(with = LanguageCodeIso3Serializer::class)
data class LanguageCodeIso3(override val value: String): DataValueStringClass {
    init {
        require(value.isNotBlank()) { "LanguageCodeIso3 must not be empty" }
        require(value.length == 3) { "LanguageCodeIso3 must be exactly 3 characters long" }
    }
}
object LanguageCodeIso3Serializer : DataClassStringSerializer<LanguageCodeIso3>("LanguageCodeIso3Serializer", { LanguageCodeIso3(it) })
