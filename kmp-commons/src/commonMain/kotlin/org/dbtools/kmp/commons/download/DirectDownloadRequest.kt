package org.dbtools.kmp.commons.download

import okio.FileSystem
import okio.Path
import kotlin.time.Duration
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
 * @property maxAttempts Total number of times the download may be attempted, including the first one
 * (DEFAULT 1... no retry). A dropped connection (reset, WiFi/cellular handoff, CDN edge drop) fails
 * immediately and no timeout value can help it, so a small number of attempts is what recovers it.
 * Downloads are NOT resumable, so every retry re-downloads the file from byte 0... keep this small, and
 * prefer a generous [io.ktor.client.plugins.HttpTimeout] `socketTimeoutMillis` on the HttpClient for a
 * connection that is merely slow (riding out a stall is free). Only *transient* failures are retried
 * (no HTTP response, 5xx, 408, 429) — a 404, 401/403 or 3xx is taken as final, so raising this costs
 * nothing on a call that probes for an asset that may not exist.
 * @property retryDelay How long to wait before each retry (DEFAULT 2 seconds). Ignored when
 * [maxAttempts] is 1.
 */
data class DirectDownloadRequest(
    val downloadUrl: String,
    val fileSystem: FileSystem,
    val targetFile: Path,
    val id: String = Uuid.random().toString(),
    val overwriteExisting: Boolean = true,
    val customHeaders: List<DirectDownloadHeader>? = null,
    val trackProgress: Boolean = false,
    val trackProgressUpdateIntervalSize: Long = DirectDownloader.DEFAULT_PROGRESS_UPDATE_BYTE_SIZE,
    val maxAttempts: Int = DirectDownloader.DEFAULT_MAX_ATTEMPTS,
    val retryDelay: Duration = DirectDownloader.DEFAULT_RETRY_DELAY
)

data class DirectDownloadHeader(
    val name: String,
    val value: String
)