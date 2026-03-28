package org.dbtools.kmp.commons.data

import org.dbtools.kmp.commons.data.LanguageCodeUtil.ISO_639_1_TO_ISO_639_3


/**
 * Converts between BCP 47 language codes ([LanguageCode]) and ISO 639-3 language codes ([LanguageCodeIso3]).
 *
 * This uses a static lookup table of ISO 639-1 (2-letter) to ISO 639-3 (3-letter) mappings rather than
 * platform-specific APIs (e.g., java.util.Locale, NSLocale) so that behavior is identical across all
 * Kotlin Multiplatform targets (Android, JVM, iOS, macOS, Linux, etc.).
 *
 * The ISO 639-1/639-3 standard is extremely stable and rarely changes, making a static map a reliable approach.
 * The map also includes deprecated BCP 47 codes ("in", "iw", "ji") as aliases for compatibility.
 *
 * ## Validation
 *
 * A JVM test ([LanguageCodeNativeMapValidationTest]) cross-references this map against the JVM's
 * [java.util.Locale.getISOLanguages] data to detect missing codes, mismatched values, or stale entries.
 * Run `./gradlew :kmp-commons:jvmTest` to verify the map is still current.
 *
 * ## Updating the map
 *
 * If the validation test fails (e.g., after a JDK update adds new ISO 639 codes):
 *
 * 1. Read the test failure output — it lists exactly which codes are missing or mismatched.
 * 2. Add or update the entries in [ISO_639_1_TO_ISO_639_3] below, keeping entries in alphabetical order.
 * 3. Re-run `./gradlew :kmp-commons:jvmTest` to confirm all tests pass.
 *
 * AI prompt: "The LanguageCodeNativeMapValidationTest is failing. Read the test output, then update
 * ISO_639_1_TO_ISO_639_3 in LanguageCodeUtil.kt to fix the failures. Keep entries in alphabetical order."
 */
object LanguageCodeUtil {
    /**
     * ISO 639-1 (2-letter) to ISO 639-3 (3-letter) language code mapping.
     *
     * Includes deprecated BCP 47 codes as aliases:
     * - "in" -> "ind" (deprecated alias for "id", Indonesian)
     * - "iw" -> "heb" (deprecated alias for "he", Hebrew)
     * - "ji" -> "yid" (deprecated alias for "yi", Yiddish)
     * - "mo" -> "mol" (deprecated alias for "ro", Moldavian/Romanian)
     */
    internal val ISO_639_1_TO_ISO_639_3: Map<String, String> by lazy { mapOf(
        "aa" to "aar", "ab" to "abk", "ae" to "ave", "af" to "afr", "ak" to "aka",
        "am" to "amh", "an" to "arg", "ar" to "ara", "as" to "asm", "av" to "ava",
        "ay" to "aym", "az" to "aze",
        "ba" to "bak", "be" to "bel", "bg" to "bul", "bh" to "bih", "bi" to "bis",
        "bm" to "bam", "bn" to "ben", "bo" to "bod", "br" to "bre", "bs" to "bos",
        "ca" to "cat", "ce" to "che", "ch" to "cha", "co" to "cos", "cr" to "cre",
        "cs" to "ces", "cu" to "chu", "cv" to "chv", "cy" to "cym",
        "da" to "dan", "de" to "deu", "dv" to "div", "dz" to "dzo",
        "ee" to "ewe", "el" to "ell", "en" to "eng", "eo" to "epo", "es" to "spa",
        "et" to "est", "eu" to "eus",
        "fa" to "fas", "ff" to "ful", "fi" to "fin", "fj" to "fij", "fo" to "fao",
        "fr" to "fra", "fy" to "fry",
        "ga" to "gle", "gd" to "gla", "gl" to "glg", "gn" to "grn", "gu" to "guj",
        "gv" to "glv",
        "ha" to "hau", "he" to "heb", "hi" to "hin", "ho" to "hmo", "hr" to "hrv",
        "ht" to "hat", "hu" to "hun", "hy" to "hye", "hz" to "her",
        "ia" to "ina", "id" to "ind", "ie" to "ile", "ig" to "ibo", "ii" to "iii",
        "ik" to "ipk", "in" to "ind", "io" to "ido", "is" to "isl", "it" to "ita",
        "iu" to "iku", "iw" to "heb",
        "ja" to "jpn", "ji" to "yid", "jv" to "jav",
        "ka" to "kat", "kg" to "kon", "ki" to "kik", "kj" to "kua", "kk" to "kaz",
        "kl" to "kal", "km" to "khm", "kn" to "kan", "ko" to "kor", "kr" to "kau",
        "ks" to "kas", "ku" to "kur", "kv" to "kom", "kw" to "cor", "ky" to "kir",
        "la" to "lat", "lb" to "ltz", "lg" to "lug", "li" to "lim", "ln" to "lin",
        "lo" to "lao", "lt" to "lit", "lu" to "lub", "lv" to "lav",
        "mg" to "mlg", "mh" to "mah", "mi" to "mri", "mk" to "mkd", "ml" to "mal",
        "mn" to "mon", "mo" to "mol", "mr" to "mar", "ms" to "msa", "mt" to "mlt",
        "my" to "mya",
        "na" to "nau", "nb" to "nob", "nd" to "nde", "ne" to "nep", "ng" to "ndo",
        "nl" to "nld", "nn" to "nno", "no" to "nor", "nr" to "nbl", "nv" to "nav",
        "ny" to "nya",
        "oc" to "oci", "oj" to "oji", "om" to "orm", "or" to "ori", "os" to "oss",
        "pa" to "pan", "pi" to "pli", "pl" to "pol", "ps" to "pus", "pt" to "por",
        "qu" to "que",
        "rm" to "roh", "rn" to "run", "ro" to "ron", "ru" to "rus", "rw" to "kin",
        "sa" to "san", "sc" to "srd", "sd" to "snd", "se" to "sme", "sg" to "sag",
        "si" to "sin", "sk" to "slk", "sl" to "slv", "sm" to "smo", "sn" to "sna",
        "so" to "som", "sq" to "sqi", "sr" to "srp", "ss" to "ssw", "st" to "sot",
        "su" to "sun", "sv" to "swe", "sw" to "swa",
        "ta" to "tam", "te" to "tel", "tg" to "tgk", "th" to "tha", "ti" to "tir",
        "tk" to "tuk", "tl" to "tgl", "tn" to "tsn", "to" to "ton", "tr" to "tur",
        "ts" to "tso", "tt" to "tat", "tw" to "twi", "ty" to "tah",
        "ug" to "uig", "uk" to "ukr", "ur" to "urd", "uz" to "uzb",
        "ve" to "ven", "vi" to "vie", "vo" to "vol",
        "wa" to "wln", "wo" to "wol",
        "xh" to "xho",
        "yi" to "yid", "yo" to "yor",
        "za" to "zha", "zh" to "zho", "zu" to "zul",
    ) }

    /**
     * ISO 639-3 (3-letter) to ISO 639-1 (2-letter) language code mapping.
     */
    private val ISO_639_3_TO_ISO_639_1: Map<String, String> by lazy {
        buildMap {
            ISO_639_1_TO_ISO_639_3.forEach { (iso1, iso3) ->
                // Only keep the first mapping for each iso3 code (avoids deprecated codes like "in"/"iw"/"ji" overwriting)
                if (!containsKey(iso3)) {
                    put(iso3, iso1)
                }
            }
        }
    }

    fun toLanguageCodeIso3(languageCode: LanguageCode): LanguageCodeIso3? {
        val primaryLanguage = languageCode.primaryLanguage.lowercase()

        // If the primary language is already 3 letters, it may already be ISO 639-3
        val iso3Value = if (primaryLanguage.length == 3) {
            primaryLanguage
        } else {
            ISO_639_1_TO_ISO_639_3[primaryLanguage] ?: return null
        }

        return LanguageCodeIso3(iso3Value)
    }

    fun toLanguageCode(languageCodeIso3: LanguageCodeIso3): LanguageCode? {
        val iso1 = ISO_639_3_TO_ISO_639_1[languageCodeIso3.value] ?: return null
        return LanguageCode(iso1)
    }
}

fun LanguageCode.toLanguageCodeIso3(): LanguageCodeIso3? = LanguageCodeUtil.toLanguageCodeIso3(this)
fun LanguageCodeIso3.toLanguageCode(): LanguageCode? = LanguageCodeUtil.toLanguageCode(this)
