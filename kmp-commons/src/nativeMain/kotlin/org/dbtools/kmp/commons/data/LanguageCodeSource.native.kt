package org.dbtools.kmp.commons.data

import co.touchlab.kermit.Logger

actual object LanguageCodeSource {
    // ISO 639-1 (2-letter) to ISO 639-3 (3-letter) mapping
    private val iso1ToIso3Map: Map<String, String> by lazy {
        mapOf(
            "aa" to "aar", "ab" to "abk", "af" to "afr", "ak" to "aka", "am" to "amh",
            "an" to "arg", "ar" to "ara", "as" to "asm", "av" to "ava", "ay" to "aym",
            "az" to "aze", "ba" to "bak", "be" to "bel", "bg" to "bul", "bh" to "bih",
            "bi" to "bis", "bm" to "bam", "bn" to "ben", "bo" to "bod", "br" to "bre",
            "bs" to "bos", "ca" to "cat", "ce" to "che", "ch" to "cha", "co" to "cos",
            "cr" to "cre", "cs" to "ces", "cu" to "chu", "cv" to "chv", "cy" to "cym",
            "da" to "dan", "de" to "deu", "dv" to "div", "dz" to "dzo", "ee" to "ewe",
            "el" to "ell", "en" to "eng", "eo" to "epo", "es" to "spa", "et" to "est",
            "eu" to "eus", "fa" to "fas", "ff" to "ful", "fi" to "fin", "fj" to "fij",
            "fo" to "fao", "fr" to "fra", "fy" to "fry", "ga" to "gle", "gd" to "gla",
            "gl" to "glg", "gn" to "grn", "gu" to "guj", "gv" to "glv", "ha" to "hau",
            "he" to "heb", "hi" to "hin", "ho" to "hmo", "hr" to "hrv", "ht" to "hat",
            "hu" to "hun", "hy" to "hye", "hz" to "her", "ia" to "ina", "id" to "ind",
            "ie" to "ile", "ig" to "ibo", "ii" to "iii", "ik" to "ipk", "in" to "ind",
            "io" to "ido", "is" to "isl", "it" to "ita", "iu" to "iku", "iw" to "heb",
            "ja" to "jpn", "ji" to "yid", "jv" to "jav", "ka" to "kat", "kg" to "kon",
            "ki" to "kik", "kj" to "kua", "kk" to "kaz", "kl" to "kal", "km" to "khm",
            "kn" to "kan", "ko" to "kor", "kr" to "kau", "ks" to "kas", "ku" to "kur",
            "kv" to "kom", "kw" to "cor", "ky" to "kir", "la" to "lat", "lb" to "ltz",
            "lg" to "lug", "li" to "lim", "ln" to "lin", "lo" to "lao", "lt" to "lit",
            "lu" to "lub", "lv" to "lav", "mg" to "mlg", "mh" to "mah", "mi" to "mri",
            "mk" to "mkd", "ml" to "mal", "mn" to "mon", "mr" to "mar", "ms" to "msa",
            "mt" to "mlt", "my" to "mya", "na" to "nau", "nb" to "nob", "nd" to "nde",
            "ne" to "nep", "ng" to "ndo", "nl" to "nld", "nn" to "nno", "no" to "nor",
            "nr" to "nbl", "nv" to "nav", "ny" to "nya", "oc" to "oci", "oj" to "oji",
            "om" to "orm", "or" to "ori", "os" to "oss", "pa" to "pan", "pi" to "pli",
            "pl" to "pol", "ps" to "pus", "pt" to "por", "qu" to "que", "rm" to "roh",
            "rn" to "run", "ro" to "ron", "ru" to "rus", "rw" to "kin", "sa" to "san",
            "sc" to "srd", "sd" to "snd", "se" to "sme", "sg" to "sag", "si" to "sin",
            "sk" to "slk", "sl" to "slv", "sm" to "smo", "sn" to "sna", "so" to "som",
            "sq" to "sqi", "sr" to "srp", "ss" to "ssw", "st" to "sot", "su" to "sun",
            "sv" to "swe", "sw" to "swa", "ta" to "tam", "te" to "tel", "tg" to "tgk",
            "th" to "tha", "ti" to "tir", "tk" to "tuk", "tl" to "tgl", "tn" to "tsn",
            "to" to "ton", "tr" to "tur", "ts" to "tso", "tt" to "tat", "tw" to "twi",
            "ty" to "tah", "ug" to "uig", "uk" to "ukr", "ur" to "urd", "uz" to "uzb",
            "ve" to "ven", "vi" to "vie", "vo" to "vol", "wa" to "wln", "wo" to "wol",
            "xh" to "xho", "yi" to "yid", "yo" to "yor", "za" to "zha", "zh" to "zho",
            "zu" to "zul",
        )
    }

    private val iso3ToIso1Map: Map<String, String> by lazy {
        buildMap {
            iso1ToIso3Map.forEach { (iso1, iso3) ->
                // Only keep the first mapping for each iso3 code (avoids deprecated codes like "in"/"iw"/"ji" overwriting)
                if (!containsKey(iso3)) {
                    put(iso3, iso1)
                }
            }
        }
    }

    actual fun getIso3For(languageCode: LanguageCode): LanguageCodeIso3? {
        return try {
            val primaryLanguage = languageCode.primaryLanguage.lowercase()

            // If the primary language is already 3 letters, it may already be ISO 639-3
            if (primaryLanguage.length == 3) {
                return LanguageCodeIso3(primaryLanguage)
            }

            val iso3 = iso1ToIso3Map[primaryLanguage] ?: return null
            LanguageCodeIso3(iso3)
        } catch (expected: Exception) {
            Logger.e(expected) { "Can't convert from $languageCode to LanguageCodeIso3" }
            null
        }
    }

    actual fun getIso3ToBcp47Map(): Map<LanguageCodeIso3, LanguageCode> {
        return buildMap {
            iso3ToIso1Map.forEach { (iso3, iso1) ->
                try {
                    put(LanguageCodeIso3(iso3), LanguageCode(iso1))
                } catch (expected: Exception) {
                    Logger.e(expected) { "Can't convert from $iso3 to LanguageCode" }
                }
            }
        }
    }
}
