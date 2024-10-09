@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.kmp.commons.download

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.http.headers
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.io.Source
import kotlinx.io.readByteArray
import love.forte.plugin.suspendtrans.annotation.JsPromise
import okio.buffer
import okio.use
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlin.time.TimeSource.Monotonic.markNow

@OptIn(ExperimentalJsExport::class)
@JsExport
data class Greeting(val title: String, val row: Int) {

    fun helloWorld() {
        print("TEST Hello World")
    }

}

/**
 * DirectDownloader
 *
 * Provides ability to download a file directly to a target path using a DownloadRequest
 */
@OptIn(ExperimentalJsExport::class)
@JsExport
class DirectDownloader {
    val inProgress = atomic(false)
    private var cancelRequested = false

    private val _progressStateFlow = MutableStateFlow<DirectDownloadProgress>(DirectDownloadProgress.Enqueued)
    val progressStateFlow: StateFlow<DirectDownloadProgress> = _progressStateFlow

    /**
     * Download File
     * @param httpClient Ktor HttpClient
     * @param directDownloadRequest Request info for downloader
     * @param dispatcher Coroutine Dispatcher to be used for the download
     *
     * @return DirectDownloadResult containing success flag and possible messages
     */
    @JsPromise
    @JsExport.Ignore
    suspend fun download(
        httpClient: HttpClient,
        directDownloadRequest: DirectDownloadRequest,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ): DirectDownloadResult = withContext(dispatcher) {
        if (!inProgress.compareAndSet(expect = false, update = true)) {
            return@withContext DirectDownloadResult(false, "Download already in progress")
        }

        val directDownloadResult: DirectDownloadResult = downloadFile(
            httpClient = httpClient,
            directDownloadRequest = directDownloadRequest
        ) { totalBytesRead, contentLength ->
            _progressStateFlow.value = DirectDownloadProgress.Downloading(totalBytesRead, contentLength)
        }

        _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(directDownloadResult.success, directDownloadResult.message)

        return@withContext directDownloadResult
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
                // Parse Content-Length header value.
                val contentLength = httpResponse.contentLength() ?: 0L

                directDownloadRequest.fileSystem.sink(directDownloadRequest.targetFile).buffer().use { outputFileBufferSink ->
                    @Suppress("UNUSED_VARIABLE") // used to provide sum to "updateProgress(...)
                    var totalBytesRead = 0L
                    val channel: ByteReadChannel = httpResponse.body()
                    while (!channel.isClosedForRead) {
                        if (cancelRequested) {
                            return@execute DirectDownloadResult(false, "Download canceled")
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
     * Make sure target directory exists, and target file does NOT yet exist
     */
    @Suppress("ReturnCount") // all return points are valid and needed
    private fun prepareTargetFile(directDownloadRequest: DirectDownloadRequest): DirectDownloadResult? {
        val fileSystem = directDownloadRequest.fileSystem
        val targetFile = directDownloadRequest.targetFile

        // make sure target directory exists
        try {
            val targetDirectory = targetFile.parent ?: return DirectDownloadResult(false, "Failed to prepareTargetFile target directory == null")

            if (!fileSystem.exists(targetDirectory)) {
                fileSystem.createDirectories(targetDirectory)
                if (!fileSystem.exists(targetDirectory)) {
                    return DirectDownloadResult(false, "Failed to create target directory: [${targetDirectory}]")
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
                return DirectDownloadResult(false, "Failed download to target file...  target file already exists: [${targetFile}]  (overwriteExisting == false)")
            }
        }

        // if we get to this point... all is well! (target directory exists, and target file does NOT yet exist)
        return null
    }

    fun cancel() {
        cancelRequested = true
    }

    companion object {
        const val DEFAULT_BUFFER_SIZE = 8 * 1024
        const val DEFAULT_PROGRESS_UPDATE_BYTE_SIZE = 1000L
    }
}