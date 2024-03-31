package org.dbtools.kmp.commons.text

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test

class NumberFormatExtKtTest {
    @Test
    fun toPercentageText() {
        assertThat(0.toDouble().toPercentageText()).isEqualTo("0%")
        assertThat(.01.toPercentageText()).isEqualTo("1%")
        assertThat(.02.toPercentageText()).isEqualTo("2%")
        assertThat(.20.toPercentageText()).isEqualTo("20%")

        assertThat(.01.toPercentageText()).isEqualTo("1%")
        assertThat(.011.toPercentageText()).isEqualTo("1%")
        assertThat(.012.toPercentageText()).isEqualTo("1%")
        assertThat(.025.toPercentageText()).isEqualTo("3%")

        assertThat(1.toDouble().toPercentageText()).isEqualTo("100%")
        assertThat(1.0.toPercentageText()).isEqualTo("100%")
        assertThat(1.00.toPercentageText()).isEqualTo("100%")
        assertThat(0.10.toPercentageText()).isEqualTo("10%")
        assertThat(0.5.toPercentageText()).isEqualTo("50%")
        assertThat(0.50.toPercentageText()).isEqualTo("50%")
    }

    @Test
    fun toStringTrimTrailingZero() {
        assertThat(0.toDouble().toStringTrimTrailingZero()).isEqualTo("0")
        assertThat(1.toDouble().toStringTrimTrailingZero()).isEqualTo("1")
        assertThat(2.toDouble().toStringTrimTrailingZero()).isEqualTo("2")
        assertThat(20.toDouble().toStringTrimTrailingZero()).isEqualTo("20")

        assertThat(1.0.toStringTrimTrailingZero()).isEqualTo("1")
        assertThat(1.1.toStringTrimTrailingZero()).isEqualTo("1.1")
        assertThat(1.2.toStringTrimTrailingZero()).isEqualTo("1.2")
        assertThat(20.5.toStringTrimTrailingZero()).isEqualTo("20.5")

        assertThat(1.00.toStringTrimTrailingZero()).isEqualTo("1")
        assertThat(1.10.toStringTrimTrailingZero()).isEqualTo("1.1")
        assertThat(0.5.toStringTrimTrailingZero()).isEqualTo("0.5")
        assertThat(0.50.toStringTrimTrailingZero()).isEqualTo("0.5")
    }

    @Test
    fun isNumeric() {
        assertThat("0".isNumeric()).isTrue()
        assertThat("1".isNumeric()).isTrue()
        assertThat("2".isNumeric()).isTrue()
        assertThat("1.1".isNumeric()).isTrue()
        assertThat("1.2".isNumeric()).isTrue()
        assertThat("1.25".isNumeric()).isTrue()
        assertThat(".5".isNumeric()).isTrue()
        assertThat(".25".isNumeric()).isTrue()

        assertThat("".isNumeric()).isFalse()
        assertThat(".".isNumeric()).isFalse()
        assertThat("1.".isNumeric()).isFalse()
        assertThat("a".isNumeric()).isFalse()
        assertThat("abc".isNumeric()).isFalse()
    }

    @Test
    fun isNumericAllowHangingDecimal() {
        assertThat("0".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat("1".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat("2".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat("1.1".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat("1.2".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat("1.25".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat(".5".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat(".25".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat(".".isNumeric(allowHangingDecimal = true)).isTrue()
        assertThat("1.".isNumeric(allowHangingDecimal = true)).isTrue()

        assertThat("".isNumeric(allowHangingDecimal = true)).isFalse()
        assertThat("a".isNumeric(allowHangingDecimal = true)).isFalse()
        assertThat("abc".isNumeric(allowHangingDecimal = true)).isFalse()
    }
}
