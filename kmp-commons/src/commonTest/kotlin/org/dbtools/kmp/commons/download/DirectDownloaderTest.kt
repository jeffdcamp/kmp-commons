package org.dbtools.kmp.commons.download

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okio.FileMetadata
import okio.FileSystem
import okio.ForwardingFileSystem
import okio.IOException
import okio.Path
import okio.Path.Companion.toPath
import okio.SYSTEM
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DirectDownloaderTest {

    // @Test
    fun download() = runTest {
        val fileSystem = FileSystem.SYSTEM
        val downloadDir = "build/test-download".toPath()
        val downloadFile = downloadDir / "README.md"
        val downloadUrl = "https://raw.githubusercontent.com/jeffdcamp/android-commons/master/README.md"

        // create downloader
        val directDownloader = DirectDownloader()

        // create download location
        fileSystem.deleteRecursively(downloadDir)
        fileSystem.createDirectories(downloadDir)

        // create request
        val downloadRequest = DirectDownloadRequest(downloadUrl, fileSystem, downloadFile)

        val httpClient = HttpClient(CIO)

        // download
        directDownloader.download(httpClient, downloadRequest)

        assertThat(fileSystem.exists(downloadFile)).isTrue()
    }

    // ===== Retry =====
    // Retry is opt-in via DirectDownloadRequest.maxAttempts so existing callers are unaffected, and only
    // *transient* failures are retried. Every retry re-downloads from byte 0, so these count the requests
    // the engine actually received.

    @Test
    fun noRetryByDefault() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respondError(HttpStatusCode.InternalServerError)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem()))

        assertThat(result.success).isFalse()
        assertThat(requestCount).isEqualTo(1)
    }

    @Test
    fun retriesUntilSuccess() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            if (requestCount == 1) respondError(HttpStatusCode.InternalServerError) else respond(FILE_CONTENT)
        }
        val fileSystem = FakeFileSystem()

        val result = DirectDownloader().download(httpClient, retryRequest(fileSystem, maxAttempts = 2))

        assertThat(result.success).isTrue()
        assertThat(requestCount).isEqualTo(2)
        assertThat(fileSystem.read(TARGET_FILE) { readByteArray() }.decodeToString()).isEqualTo(FILE_CONTENT.decodeToString())
    }

    @Test
    fun retriesADeadConnection() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            if (requestCount == 1) throw IOException("Connection reset by peer")
            respond(FILE_CONTENT)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 2))

        assertThat(result.success).isTrue()
        assertThat(requestCount).isEqualTo(2)
    }

    @Test
    fun stopsAfterMaxAttempts() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respondError(HttpStatusCode.BadGateway)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 3))

        assertThat(result.success).isFalse()
        assertThat(result.code).isEqualTo(HttpStatusCode.BadGateway.value)
        assertThat(requestCount).isEqualTo(3)
    }

    /**
     * A 404 is the server's definitive answer — for a caller probing for an optional asset or an update
     * diff it is the *expected* answer, so retrying it would only delay their fallback.
     */
    @Test
    fun doesNotRetryANotFound() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respondError(HttpStatusCode.NotFound)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 3))

        assertThat(result.success).isFalse()
        assertThat(requestCount).isEqualTo(1)
    }

    /** The same credentials would just be rejected again. */
    @Test
    fun doesNotRetryAnAuthFailure() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respondError(HttpStatusCode.Forbidden)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 3))

        assertThat(result.success).isFalse()
        assertThat(requestCount).isEqualTo(1)
    }

    /**
     * A 3xx is a deterministic refusal. 304 Not Modified is used here precisely because it is a 3xx that
     * actually reaches the downloader — a true redirect (301/302) is consumed by Ktor's `HttpRedirect`
     * plugin and never surfaces as a failure.
     */
    @Test
    fun doesNotRetryA3xxStatus() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respondError(HttpStatusCode.NotModified)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 3))

        assertThat(result.success).isFalse()
        assertThat(requestCount).isEqualTo(1)
    }

    @Test
    fun retriesARequestTimeout() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            if (requestCount == 1) respondError(HttpStatusCode.RequestTimeout) else respond(FILE_CONTENT)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 2))

        assertThat(result.success).isTrue()
        assertThat(requestCount).isEqualTo(2)
    }

    @Test
    fun retriesTooManyRequests() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            if (requestCount == 1) respondError(HttpStatusCode.TooManyRequests) else respond(FILE_CONTENT)
        }

        val result = DirectDownloader().download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 2))

        assertThat(result.success).isTrue()
        assertThat(requestCount).isEqualTo(2)
    }

    /** A canceled download must not burn its remaining attempts. */
    @Test
    fun aCanceledDownloadIsNotRetried() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respond(FILE_CONTENT)
        }
        val directDownloader = DirectDownloader()
        directDownloader.cancel()

        val result = directDownloader.download(httpClient, retryRequest(FakeFileSystem(), maxAttempts = 3))

        assertThat(result.success).isFalse()
        assertThat(requestCount).isEqualTo(1)
    }

    /**
     * `shouldRetry` is evaluated before the backoff, so a `cancel()` that lands *during* `retryDelay` has to
     * be re-checked after it — otherwise the loop spends another request that is immediately thrown away,
     * which is expensive on a metered network.
     *
     * Uses a real dispatcher and a real delay, because the cancel has to interleave with the backoff.
     */
    @Test
    fun aCancelDuringTheRetryDelayDoesNotSpendAnotherRequest() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respondError(HttpStatusCode.BadGateway)
        }
        val directDownloader = DirectDownloader()

        val download = async(Dispatchers.Default) {
            directDownloader.download(
                httpClient,
                retryRequest(FakeFileSystem(), maxAttempts = 3).copy(retryDelay = RETRY_BACKOFF)
            )
        }

        // let attempt 1 fail, then cancel while the loop is sitting in delay(retryDelay)
        withContext(Dispatchers.Default) { delay(RETRY_BACKOFF / 3) }
        assertThat(requestCount).isEqualTo(1) // attempt 1 done, loop is in the backoff
        directDownloader.cancel()

        val result = download.await()

        assertThat(result.success).isFalse()
        assertThat(result.message).isEqualTo("Download canceled")
        assertThat(requestCount).isEqualTo(1) // the canceled attempt was never sent
    }

    /**
     * `createDirectories` returning without throwing, yet leaving the directory absent, is a deterministic
     * local condition (a read-only or broken volume) — not something a `retryDelay` can fix.
     */
    @Test
    fun doesNotRetryAnUncreatableTargetDirectory() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respond(FILE_CONTENT)
        }
        val fileSystem = SilentlyUncreatableDirFileSystem(FakeFileSystem())

        val result = DirectDownloader().download(httpClient, retryRequest(fileSystem, maxAttempts = 3))

        assertThat(result.success).isFalse()
        assertThat(result.code).isEqualTo(DirectDownloadResult.LOCAL_ERROR_CODE)
        assertThat(fileSystem.createDirectoryAttempts).isEqualTo(1) // attempted once, not maxAttempts times
        assertThat(requestCount).isEqualTo(0)
    }

    /** Accepts `createDirectory` without doing anything, so `exists()` stays false and nothing is thrown. */
    private class SilentlyUncreatableDirFileSystem(delegate: FileSystem) : ForwardingFileSystem(delegate) {
        var createDirectoryAttempts = 0

        override fun createDirectory(dir: Path, mustCreate: Boolean) {
            createDirectoryAttempts++
        }
    }

    /**
     * A `cancel()` applies only to the download it interrupted. Without resetting the flag, an instance
     * that was canceled once would report "Download canceled" forever — and would still spend the request
     * to do it.
     */
    @Test
    fun aCanceledInstanceCanDownloadAgain() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respond(FILE_CONTENT)
        }
        val fileSystem = FakeFileSystem()
        val directDownloader = DirectDownloader()

        directDownloader.cancel()
        assertThat(directDownloader.download(httpClient, retryRequest(fileSystem)).success).isFalse()

        val result = directDownloader.download(httpClient, retryRequest(fileSystem))

        assertThat(result.success).isTrue()
        assertThat(fileSystem.read(TARGET_FILE) { readByteArray() }.decodeToString()).isEqualTo(FILE_CONTENT.decodeToString())
    }

    /**
     * A deterministic *local* failure never reached the network, so re-running it produces the identical
     * failure. It must not burn the remaining attempts (nor their [DirectDownloadRequest.retryDelay]s).
     */
    @Test
    fun doesNotRetryADeterministicLocalFailure() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respond(FILE_CONTENT)
        }
        // the failure never reaches the network, so requestCount cannot see the retries... count the
        // attempts at the filesystem instead (each attempt checks the target file exactly once)
        val fileSystem = AttemptCountingFileSystem(FakeFileSystem())
        fileSystem.createDirectories(TARGET_FILE.parent!!)
        fileSystem.write(TARGET_FILE) { write("existing".encodeToByteArray()) }
        fileSystem.targetFileChecks = 0 // ignore the checks made by the setup above

        val result = DirectDownloader().download(
            httpClient,
            retryRequest(fileSystem, maxAttempts = 3).copy(overwriteExisting = false)
        )

        assertThat(result.success).isFalse()
        assertThat(result.code).isEqualTo(DirectDownloadResult.LOCAL_ERROR_CODE)
        assertThat(requestCount).isEqualTo(0) // never even reached the network
        assertThat(fileSystem.targetFileChecks).isEqualTo(1) // attempted once, not maxAttempts times
    }

    /** Counts how many times the downloader inspected the target file, which is once per attempt. */
    private class AttemptCountingFileSystem(delegate: FileSystem) : ForwardingFileSystem(delegate) {
        var targetFileChecks = 0

        override fun metadataOrNull(path: Path): FileMetadata? {
            if (path == TARGET_FILE) targetFileChecks++
            return super.metadataOrNull(path)
        }
    }

    /** The `inProgress` guard is released when a download ends, so one instance can be reused. */
    @Test
    fun theSameInstanceCanDownloadTwice() = runTest {
        var requestCount = 0
        val httpClient = mockClient {
            requestCount++
            respond(FILE_CONTENT)
        }
        val directDownloader = DirectDownloader()
        val fileSystem = FakeFileSystem()

        assertThat(directDownloader.download(httpClient, retryRequest(fileSystem)).success).isTrue()
        assertThat(directDownloader.download(httpClient, retryRequest(fileSystem)).success).isTrue()
        assertThat(requestCount).isEqualTo(2)
    }

    private fun mockClient(handler: suspend MockRequestHandleScope.() -> HttpResponseData): HttpClient =
        HttpClient(MockEngine { handler() })

    private fun retryRequest(fileSystem: FileSystem, maxAttempts: Int = DirectDownloader.DEFAULT_MAX_ATTEMPTS): DirectDownloadRequest =
        DirectDownloadRequest(
            downloadUrl = DOWNLOAD_URL,
            fileSystem = fileSystem,
            targetFile = TARGET_FILE,
            maxAttempts = maxAttempts,
            retryDelay = Duration.ZERO, // keep the test fast... the delay itself is not what is under test
        )

    companion object {
        private const val DOWNLOAD_URL = "https://example.com/data.zip"
        private val TARGET_FILE: Path = "/downloads/data.zip".toPath()
        private val FILE_CONTENT = "downloaded-file-bytes".encodeToByteArray()

        /** Long enough to reliably cancel partway into it on a loaded CI machine. */
        private val RETRY_BACKOFF = 3.seconds
    }
}