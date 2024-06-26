@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.kmp.commons.download

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.prepareGet
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import okio.buffer
import okio.use
import kotlin.time.TimeSource.Monotonic.markNow

class DirectDownloader {
    var inProgress = false // replace with https://github.com/Kotlin/kotlinx-atomicfu
    private var cancelRequested = false

    private val _progressStateFlow = MutableStateFlow<DirectDownloadProgress>(DirectDownloadProgress.Enqueued)
    val progressStateFlow: StateFlow<DirectDownloadProgress> = _progressStateFlow

    suspend fun download(httpClient: HttpClient, directDownloadRequest: DirectDownloadRequest, dispatcher: CoroutineDispatcher = Dispatchers.IO): DirectDownloadResult = withContext(dispatcher) {
        if (inProgress) {
            return@withContext DirectDownloadResult(false, "Download already in progress")
        }
        inProgress = true

        val directDownloadResult: DirectDownloadResult = downloadFile(httpClient, directDownloadRequest) { totalBytesRead, contentLength ->
            _progressStateFlow.value = DirectDownloadProgress.Downloading(totalBytesRead, contentLength)
        }

        _progressStateFlow.value = DirectDownloadProgress.DownloadComplete(directDownloadResult.success, directDownloadResult.message)

        return@withContext directDownloadResult
    }

    private suspend fun downloadFile(
        httpClient: HttpClient,
        directDownloadRequest: DirectDownloadRequest,
        updateProgress: (totalBytesRead: Long, contentLength: Long) -> Unit
    ): DirectDownloadResult {
        val mark = markNow()

        // make sure target file doesn't already exist
        val prepareTargetFileResult = prepareTargetFile(directDownloadRequest)
        if (prepareTargetFileResult != null) {
            return prepareTargetFileResult
        }

        val directDownloadResult: DirectDownloadResult = try {
            httpClient.prepareGet(directDownloadRequest.downloadUrl).execute { httpResponse ->
                // Parse Content-Length header value.
                val contentLength = httpResponse.contentLength() ?: 0L

                directDownloadRequest.fileSystem.sink(directDownloadRequest.targetFile).buffer().use { outputFileBufferSink ->
                    var totalBytesRead = 0L
                    val channel: ByteReadChannel = httpResponse.body()
                    while (!channel.isClosedForRead) {
                        if (cancelRequested) {
                            return@execute DirectDownloadResult(false, "Download canceled")
                        }

                        val packet: ByteReadPacket = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!packet.isEmpty) {
                            val bytes = packet.readBytes()

                            outputFileBufferSink.write(bytes)

                            // update totalBytesRead
                            totalBytesRead += bytes.size

                            // update progress
                            updateProgress(totalBytesRead, contentLength)
                        }
                    }
                }
                println("A file saved to ${directDownloadRequest.targetFile}")
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


    /*
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
        const val DEFAULT_BUFFER_SIZE: Int = 8 * 1024
    }
}
