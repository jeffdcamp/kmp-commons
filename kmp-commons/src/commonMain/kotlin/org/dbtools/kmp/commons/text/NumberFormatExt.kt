@file:Suppress("ImplicitDefaultLocale")
package org.dbtools.kmp.commons.text

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.toPercentageText(): String {
    val rounded = (this * 100).toRoundedText(0)
    return "${rounded}%"
}

fun Double.toRoundedText(decimalPlaces: Int = 1, trimTrailingZero: Boolean = true): String {
    val multiplier = 10.0.pow(decimalPlaces)
    val text = ((this * multiplier).roundToInt() / multiplier).toString()

    return if (trimTrailingZero) {
        text.trimTrailingZero()
    } else {
        text
    }
}

fun String.trimTrailingZero(): String {
    return if (isNotBlank()) {
        if (indexOf(".") < 0) {
            this
        } else {
            replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
        }
    } else {
        this
    }
}

fun Double.toStringTrimTrailingZero(): String {
    return toString().trimTrailingZero()
}

fun String.isNumeric(allowHangingDecimal: Boolean = false): Boolean {
    if (allowHangingDecimal && equals(".")) {
        return true
    }

    if (allowHangingDecimal && endsWith(".")) {
        val beforeLastDecimal = take(this.length - 1)
        return beforeLastDecimal.isNumeric()
    }

    if (startsWith(".")) {
        return takeLast(this.length - 1).isNumeric()
    }

    val regex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
    return matches(regex)
}
