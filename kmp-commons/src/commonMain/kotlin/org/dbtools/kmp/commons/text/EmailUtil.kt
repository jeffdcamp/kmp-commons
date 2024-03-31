package org.dbtools.kmp.commons.text

object EmailUtil {
    fun isValidEmailAddress(email: String): Boolean = EMAIL_ADDRESS.matches(email)
    fun getUsername(email: String): String? = "^[^@]*".toRegex().find(email)?.value
    fun getDomain(email: String): String? = "@(.*$)".toRegex().find(email)?.groupValues?.get(1)

    private val EMAIL_ADDRESS: Regex = """[a-zA-Z0-9+._%\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+""".toRegex()
}
