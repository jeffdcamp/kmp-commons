package org.dbtools.kmp.commons.ext

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.time.Instant
import kotlin.test.Test

class KotlinDateTimeExtKtTest {

    @Test
    fun trimToSeconds() {
        val original = Instant.parse("2023-09-23T17:30:32.954178279Z")
        val expected = "2023-09-23T17:30:32Z"
        val output = original.trimToSeconds()

        assertThat(output.toString()).isEqualTo(expected)
    }

    @Test
    fun trimToSecondsWithTimeZone() {
        val original = Instant.parse("2023-09-23T11:30:32.954178279-06:00")
        val expected = "2023-09-23T17:30:32Z"
        val output = original.trimToSeconds()

        assertThat(output.toString()).isEqualTo(expected)
    }
}
