package org.dbtools.kmp.commons.security

import java.security.MessageDigest

/**
 * Create sha256 string hash
 */
fun String.sha256(): String {
    return this.hashString("SHA-256")
}

/**
 * Create string hash
 * @param algorithm Algorithm used with MessageDigest (Example: "SHA-256")
 */
fun String.hashString(algorithm: String): String {
    return MessageDigest
        .getInstance(algorithm)
        .digest(this.toByteArray())
        .fold("") { str, text -> str + "%02x".format(text) }
}