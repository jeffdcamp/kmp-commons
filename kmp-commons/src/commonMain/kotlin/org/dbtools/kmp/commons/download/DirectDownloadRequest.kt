package org.dbtools.kmp.commons.download

import okio.FileSystem
import okio.Path
import kotlin.uuid.Uuid

/**
 * @property downloadUrl Url of file to download
 * @property fileSystem Okio Filesystem that should be used for target file path
 * @property targetFile Okio file Path to target file
 * @property id Unique identifier to help distinguish this DirectDownloadRequest among others
 * @property overwriteExisting If target file exists, allow overwrite? (DEFAULT true)
 * @property customHeaders Extra headers that may need to added to the request (for auth, etc)
 * @property trackProgress Flag for identifying if progress should be tracked (DEFAULT false).  Downloads may be faster
 * @property trackProgressUpdateIntervalSize bytes download interval for progress update (DEFAULT 1000)
 */
data class DirectDownloadRequest(
    val downloadUrl: String,
    val fileSystem: FileSystem,
    val targetFile: Path,
    val id: String = Uuid.random().toString(),
    val overwriteExisting: Boolean = true,
    val customHeaders: List<DirectDownloadHeader>? = null,
    val trackProgress: Boolean = false,
    val trackProgressUpdateIntervalSize: Long = DirectDownloader.DEFAULT_PROGRESS_UPDATE_BYTE_SIZE
)

data class DirectDownloadHeader(
    val name: String,
    val value: String
)