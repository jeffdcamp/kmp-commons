@file:Suppress("MemberVisibilityCanBePrivate")
@file:OptIn(ExperimentalAtomicApi::class)

package org.dbtools.kmp.commons.download

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.http.headers
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.readByteArray
import okio.buffer
import okio.use
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource.Monotonic.markNow

/**
 * DirectDownloader
 *
 * Provides ability to download a file directly to a target path using a DownloadRequest
 */
class DirectDownloader {
    val inProgress = AtomicBoolean(false)
    // atomic (like [inProgress]) because cancel() is called from a different thread than the download loop
    // that reads it
    private val cancelRequested = AtomicBoolean(false)

    private val _progressStateFlow = MutableStateFlow<DirectDownloadProgress>(DirectDownloadProgress.Enqueued)
    val progressStateFlow: StateFlow<DirectDownloadProgress> = _progressStateFlow

    /**
     * Download File
     *
     * Retried up to [DirectDownloadRequest.maxAttempts] times (DEFAULT 1... no retry).
     *
     * @param httpClient Ktor HttpClient
     * @param directDownloadRequest Request info for downloader
     * @param dispatcher Coroutine Dispatcher to be used for the download
     *
     * @return DirectDownloadResult containing success flag and possible messages
     */
    suspend fun download(
        httpClient: HttpClient,
        directDownloadRequest: DirectDownloadRequest,
        dispatcher: CoroutineDispatcher = Dispatchers.IO
    ): DirectDownloadResult = withContext(dispatcher) {
        if (!inProgress.compareAndSet(expectedValue = false, newValue = true)) {
            return@withContext DirectDownloadResult(false, "Download already in progress")
        }

        try {
            val directDownloadResult: DirectDownloadResult = downloadFileWithRetry(
                httpClient = httpClient,
                directDownloadRequest = directDownloadRequest
            )

            _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(directDownloadResult.success, directDownloadResult.message)

            return@withContext directDownloadResult
        } finally {
            // release the guard so this instance can be used for another download
            inProgress.store(false)

            // a cancel() applies to the download it canceled, and is consumed by it. Without this reset an
            // instance that was canceled once would report "Download canceled" for every later download.
            // Reset here rather than on the way in, so that canceling *before* a download still cancels it.
            cancelRequested.store(false)
        }
    }

    /**
     * Attempt [downloadFile] up to [DirectDownloadRequest.maxAttempts] times.
     *
     * A download is NOT resumable, so a retry starts over from byte 0 (progress resets to
     * [DirectDownloadProgress.Enqueued]). A retry is therefore only worth spending on a connection that is
     * DEAD (reset, network handoff, CDN edge drop) — that fails immediately, and no timeout value can help
     * it. A connection that is merely SLOW is the HttpClient socket timeout's problem, not this loop's.
     *
     * Only failures [shouldRetry] considers transient are attempted again.
     */
    private suspend fun downloadFileWithRetry(
        httpClient: HttpClient,
        directDownloadRequest: DirectDownloadRequest,
    ): DirectDownloadResult {
        val maxAttempts = directDownloadRequest.maxAttempts.coerceAtLeast(1)
        val updateProgress: (totalBytesRead: Long, contentLength: Long) -> Unit = { totalBytesRead, contentLength ->
            _progressStateFlow.value = DirectDownloadProgress.Downloading(totalBytesRead, contentLength)
        }

        var directDownloadResult = downloadFile(httpClient, directDownloadRequest, updateProgress)
        var attempt = 1

        while (attempt < maxAttempts && shouldRetry(directDownloadResult)) {
            Logger.w {
                "Retrying download [${directDownloadRequest.downloadUrl}] (attempt [${attempt + 1}] of [$maxAttempts])  " +
                    "code: [${directDownloadResult.code}]  message: [${directDownloadResult.message}]"
            }
            delay(directDownloadRequest.retryDelay)

            // shouldRetry() was evaluated BEFORE the delay, so re-check here: a cancel() that arrived during
            // the backoff must not spend another request (expensive on a metered network)
            if (cancelRequested.load()) {
                directDownloadResult = DirectDownloadResult(false, CANCELED_MESSAGE)
                break
            }

            attempt++
            _progressStateFlow.value = DirectDownloadProgress.Enqueued
            directDownloadResult = downloadFile(httpClient, directDownloadRequest, updateProgress)
        }

        if (!directDownloadResult.success) {
            Logger.e {
                "Download failed [${directDownloadRequest.downloadUrl}] after [$attempt] attempt(s)  " +
                    "code: [${directDownloadResult.code}]  message: [${directDownloadResult.message}]"
            }
        }

        return directDownloadResult
    }

    private suspend fun downloadFile(
        httpClient: HttpClient,
        directDownloadRequest: DirectDownloadRequest,
        updateProgress: (totalBytesRead: Long, contentLength: Long) -> Unit,
    ): DirectDownloadResult {
        val mark = markNow()

        // make sure target file doesn't already exist
        val prepareTargetFileResult = prepareTargetFile(directDownloadRequest)
        if (prepareTargetFileResult != null) {
            return prepareTargetFileResult
        }

        val directDownloadResult: DirectDownloadResult = try {
            val httpStatement = httpClient.prepareGet(directDownloadRequest.downloadUrl) {
                // add any custom headers
                headers {
                    directDownloadRequest.customHeaders?.forEach { directDownloadHeader ->
                        append(directDownloadHeader.name, directDownloadHeader.value)
                    }
                }
            }

            // execute and download
            httpStatement.execute { httpResponse ->
                if (!httpResponse.status.isSuccess()) {
                    return@execute DirectDownloadResult(false, "Failed to download file: ${httpResponse.status}", code = httpResponse.status.value)
                }

                // Parse Content-Length header value.
                val contentLength = httpResponse.contentLength() ?: 0L

                directDownloadRequest.fileSystem.sink(directDownloadRequest.targetFile).buffer().use { outputFileBufferSink ->
                    @Suppress("UNUSED_VARIABLE") // used to provide sum to "updateProgress(...)
                    var totalBytesRead = 0L
                    val channel: ByteReadChannel = httpResponse.body()
                    while (!channel.isClosedForRead) {
                        if (cancelRequested.load()) {
                            return@execute DirectDownloadResult(false, CANCELED_MESSAGE)
                        }

                        val source: Source = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!source.exhausted()) {
                            val bytes = source.readByteArray()

                            outputFileBufferSink.write(bytes)

                            // update totalBytesRead
                            totalBytesRead += bytes.size

                            // update progress
                            if (directDownloadRequest.trackProgress && totalBytesRead % directDownloadRequest.trackProgressUpdateIntervalSize == 0L) {
                                updateProgress(totalBytesRead, contentLength)
                            }
                        }
                    }
                }
                Logger.i { "A file saved to ${directDownloadRequest.targetFile}" }
                DirectDownloadResult(success = true)
            }
        } catch (expected: Exception) {
            DirectDownloadResult(success = false, expected.message)
        }

        // verify result
        if (directDownloadResult.success && !directDownloadRequest.fileSystem.exists(directDownloadRequest.targetFile)) {
            val message = "Download was successful, but the target file does not exist (${directDownloadRequest.targetFile})"
            return DirectDownloadResult(false, message)
        }

        Logger.i { "Download complete for: ${directDownloadRequest.targetFile} (${mark.elapsedNow()})" }

        return directDownloadResult
    }

    /**
     * Whether a failed download is worth another attempt.
     *
     * A retry re-downloads the whole file, so it is only spent on a *transient* failure. Everything else is
     * the server's definitive answer and will not change within a [DirectDownloadRequest.retryDelay]:
     * - a 404 — the asset does not exist (for callers that probe for an optional file or an update diff,
     *   this is the *expected* answer, and retrying it only delays their fallback),
     * - a 401/403 — the same credentials will be rejected again,
     * - a 3xx — a deterministic refusal (an HTTPS->HTTP downgrade, a loop, a 304). Ktor's `HttpRedirect`
     *   plugin normally consumes redirects before they reach here, but that is the caller's HttpClient to
     *   configure: if redirects are expected, leave redirect handling enabled on it, because a 3xx arriving
     *   here is treated as final and is NOT followed or retried,
     * - a [DirectDownloadResult.LOCAL_ERROR_CODE] — a local misconfiguration that never even reached the
     *   network, so re-running it produces the identical failure.
     */
    private fun shouldRetry(directDownloadResult: DirectDownloadResult): Boolean = when {
        directDownloadResult.success -> false
        cancelRequested.load() -> false // a canceled download is never retried
        // a deterministic local failure: an unusable path, or overwriteExisting == false
        directDownloadResult.code == DirectDownloadResult.LOCAL_ERROR_CODE -> false
        // no HTTP response at all: connection reset, network handoff, DNS, TLS, socket timeout
        directDownloadResult.code == DirectDownloadResult.NO_RESPONSE_CODE -> true
        // a server / CDN edge failure, most often 502, 503 or 504
        directDownloadResult.code in SERVER_ERROR_CODES -> true
        // transient by definition
        directDownloadResult.code == HttpStatusCode.RequestTimeout.value -> true
        directDownloadResult.code == HttpStatusCode.TooManyRequests.value -> true
        else -> false
    }

    /**
     * Make sure target directory exists, and target file does NOT yet exist
     */
    @Suppress("ReturnCount") // all return points are valid and needed
    private fun prepareTargetFile(directDownloadRequest: DirectDownloadRequest): DirectDownloadResult? {
        val fileSystem = directDownloadRequest.fileSystem
        val targetFile = directDownloadRequest.targetFile

        // make sure target directory exists
        try {
            val targetDirectory = targetFile.parent
                ?: return DirectDownloadResult(false, "Failed to prepareTargetFile target directory == null", DirectDownloadResult.LOCAL_ERROR_CODE)

            if (!fileSystem.exists(targetDirectory)) {
                fileSystem.createDirectories(targetDirectory)
                if (!fileSystem.exists(targetDirectory)) {
                    // createDirectories() did not throw, yet the directory still is not there. That is a
                    // deterministic local condition (read-only / broken volume), not a transient one.
                    return DirectDownloadResult(
                        false,
                        "Failed to create target directory: [${targetDirectory}]",
                        DirectDownloadResult.LOCAL_ERROR_CODE
                    )
                }
            }
        } catch (expected: Exception) {
            val message = "Failed to create target directory: [${targetFile.parent}]  message: [${expected.message}]"
            Logger.e(expected) { message }
            return DirectDownloadResult(false, message)
        }

        // check to see if target file exists
        if (fileSystem.exists(targetFile)) {
            if (directDownloadRequest.overwriteExisting) {
                try {
                    fileSystem.delete(targetFile)
                } catch (expected: Exception) {
                    val message = "Failed to delete existing target file: [${targetFile}]  message: [${expected.message}]"
                    Logger.e(expected) { message }
                    return DirectDownloadResult(false, message)
                }
            } else {
                return DirectDownloadResult(
                    false,
                    "Failed download to target file...  target file already exists: [${targetFile}]  (overwriteExisting == false)",
                    DirectDownloadResult.LOCAL_ERROR_CODE
                )
            }
        }

        // if we get to this point... all is well! (target directory exists, and target file does NOT yet exist)
        return null
    }

    fun cancel() {
        cancelRequested.store(true)
    }

    companion object {
        const val DEFAULT_BUFFER_SIZE = 8 * 1024

        private const val CANCELED_MESSAGE = "Download canceled"
        const val DEFAULT_PROGRESS_UPDATE_BYTE_SIZE = 1000L

        /** No retry by default... opt in per request with [DirectDownloadRequest.maxAttempts]. */
        const val DEFAULT_MAX_ATTEMPTS = 1
        val DEFAULT_RETRY_DELAY = 2.seconds

        private val SERVER_ERROR_CODES = 500..599
    }
}
