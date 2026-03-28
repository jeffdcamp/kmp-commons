package org.dbtools.kmp.commons.data

import java.util.Locale
import kotlin.test.Test
import kotlin.test.fail

/**
 * Validates that [LanguageCodeUtil.ISO_639_1_TO_ISO_639_3] stays in sync with the JVM's
 * [Locale] data (backed by ICU). The JVM is treated as the source of truth.
 *
 * If this test fails after a JDK update:
 * 1. Read the failure output — it lists exactly which codes are missing or mismatched.
 * 2. Update [LanguageCodeUtil.ISO_639_1_TO_ISO_639_3] (keep alphabetical order).
 * 3. Re-run: `./gradlew :kmp-commons:jvmTest`
 */
class LanguageCodeNativeMapValidationTest {

    private fun getJvmIso1ToIso3Map(): Map<String, String> {
        return buildMap {
            Locale.getISOLanguages().forEach { iso1Code ->
                try {
                    val locale = Locale.of(iso1Code)
                    val iso3 = locale.isO3Language
                    if (iso3.isNotEmpty()) {
                        put(iso1Code, iso3)
                    }
                } catch (_: Exception) {
                    // skip codes that fail
                }
            }
        }
    }

    @Test
    fun `static map should contain all JVM ISO 639 codes`() {
        val jvmMap = getJvmIso1ToIso3Map()
        val missing = jvmMap.filter { (iso1, _) -> iso1 !in LanguageCodeUtil.ISO_639_1_TO_ISO_639_3 }

        if (missing.isNotEmpty()) {
            fail(
                "The following ISO 639-1 codes exist in the JVM but are missing from LanguageCodeUtil.ISO_639_1_TO_ISO_639_3:\n" +
                        missing.entries.joinToString("\n") { "  \"${it.key}\" to \"${it.value}\"" }
            )
        }
    }

    @Test
    fun `static map values should match JVM ISO 639-3 codes`() {
        val jvmMap = getJvmIso1ToIso3Map()
        val mismatches = LanguageCodeUtil.ISO_639_1_TO_ISO_639_3.filter { (iso1, staticIso3) ->
            val jvmIso3 = jvmMap[iso1]
            jvmIso3 != null && jvmIso3 != staticIso3
        }

        if (mismatches.isNotEmpty()) {
            fail(
                "The following ISO 639-3 codes in LanguageCodeUtil.ISO_639_1_TO_ISO_639_3 do not match the JVM values:\n" +
                        mismatches.entries.joinToString("\n") { "  \"${it.key}\": static=\"${it.value}\" vs jvm=\"${jvmMap[it.key]}\"" }
            )
        }
    }

    @Test
    fun `static map should not contain codes absent from JVM`() {
        val jvmMap = getJvmIso1ToIso3Map()
        // Deprecated codes ("in", "iw", "ji", "mo") are aliases kept intentionally — exclude from this check
        val deprecatedAliases = setOf("in", "iw", "ji", "mo")
        val extra = LanguageCodeUtil.ISO_639_1_TO_ISO_639_3.filter { (iso1, _) -> iso1 !in jvmMap && iso1 !in deprecatedAliases }

        if (extra.isNotEmpty()) {
            fail(
                "The following ISO 639-1 codes are in LanguageCodeUtil.ISO_639_1_TO_ISO_639_3 but not in the JVM:\n" +
                        extra.entries.joinToString("\n") { "  \"${it.key}\" to \"${it.value}\"" }
            )
        }
    }
}
