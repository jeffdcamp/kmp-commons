package org.dbtools.kmp.commons.download

import okio.FileSystem
import okio.Path

data class DirectDownloadRequest (
    val downloadUrl: String,
    val fileSystem: FileSystem,
    val targetFile: Path,
    val id: String,
    val overwriteExisting: Boolean = true,
    val customHeaders: List<DirectDownloadHeader>? = null
)

data class DirectDownloadHeader(
    val name: String,
    val value: String
)