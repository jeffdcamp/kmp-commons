package org.dbtools.kmp.commons.text

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

class EmailUtilTest {
    @Test
    fun testValidEmails() {
        assertThat(EmailUtil.isValidEmailAddress("joe@somedomain.org")).isTrue()
        assertThat(EmailUtil.isValidEmailAddress("joe@somedomain.org")).isTrue()
        assertThat(EmailUtil.isValidEmailAddress("j@s.g")).isTrue()
        assertThat(EmailUtil.isValidEmailAddress("loooooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnnnnnnggggggggggggggggggggggggggggg@somedomain.org")).isTrue()
    }

    @Test
    fun testInvalidValidEmails() {
        assertThat(EmailUtil.isValidEmailAddress("")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("j")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("@somedomain.org")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("joe")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("joe@somedomain")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("joe@.org")).isFalse()
    }

    @Test
    fun testUsernames() {
        assertThat(EmailUtil.getUsername("joe@somedomain.org")).isEqualTo("joe")
        assertThat(EmailUtil.getUsername("joe@somedomain")).isEqualTo("joe")
        assertThat(EmailUtil.getUsername("@somedomain.org")).isEqualTo("")
    }

    @Test
    fun testDomains() {
        assertThat(EmailUtil.getDomain("joe@somedomain.org")).isEqualTo("somedomain.org")
        assertThat(EmailUtil.getDomain("joe@somedomain")).isEqualTo("somedomain")
        assertThat(EmailUtil.getDomain("@somedomain.org")).isEqualTo("somedomain.org")
        assertThat(EmailUtil.getDomain("somedomain.org")).isNull()
    }
}
