package org.dbtools.kmp.commons

import androidx.core.net.toUri
import org.dbtools.kmp.commons.network.ktor.Uri

@Suppress("UnnecessaryFullyQualifiedName")
fun Uri.toAndroidUri(): android.net.Uri {
    return toString().toUri()
}

@Suppress("UnnecessaryFullyQualifiedName")
fun android.net.Uri.toKmpUri(): Uri {
    return Uri.parse(toString())
}
