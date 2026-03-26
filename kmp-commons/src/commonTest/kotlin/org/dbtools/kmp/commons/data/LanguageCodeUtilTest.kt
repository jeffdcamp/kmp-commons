package org.dbtools.kmp.commons.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test

class LanguageCodeUtilTest {
    @Test
    fun testBcp47ToIso3() {
        assertThat(LanguageCode("en").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("eng"))
        assertThat(LanguageCode("fr").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("fra"))
        assertThat(LanguageCode("de").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("deu"))
        assertThat(LanguageCode("es").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("spa"))
        assertThat(LanguageCode("ja").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("jpn"))
        assertThat(LanguageCode("zh").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("zho"))
        assertThat(LanguageCode("pt").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("por"))
        assertThat(LanguageCode("ru").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("rus"))
        assertThat(LanguageCode("ko").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("kor"))
        assertThat(LanguageCode("it").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("ita"))
    }

    @Test
    fun testBcp47WithRegionToIso3() {
        assertThat(LanguageCode("en-US").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("eng"))
        assertThat(LanguageCode("zh-Hans").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("zho"))
        assertThat(LanguageCode("pt-BR").toLanguageCodeIso3()).isNotNull().isEqualTo(LanguageCodeIso3("por"))
    }

    @Test
    fun testIso3ToBcp47() {
        assertThat(LanguageCodeIso3("eng").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("en"))
        assertThat(LanguageCodeIso3("fra").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("fr"))
        assertThat(LanguageCodeIso3("deu").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("de"))
        assertThat(LanguageCodeIso3("spa").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("es"))
        assertThat(LanguageCodeIso3("jpn").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("ja"))
        assertThat(LanguageCodeIso3("zho").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("zh"))
        assertThat(LanguageCodeIso3("por").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("pt"))
        assertThat(LanguageCodeIso3("rus").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("ru"))
        assertThat(LanguageCodeIso3("kor").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("ko"))
        assertThat(LanguageCodeIso3("ita").toLanguageCode()).isNotNull().isEqualTo(LanguageCode("it"))
    }

    @Test
    fun testUnknownIso3ReturnsNull() {
        assertThat(LanguageCodeIso3("zzz").toLanguageCode()).isNull()
    }
}
