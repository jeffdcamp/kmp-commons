package org.dbtools.kmp.commons.download

import assertk.assertThat
import assertk.assertions.isTrue
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test

class DirectDownloaderTest {

    @Test
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

        val httpClient = HttpClient(OkHttp.create())

        // download
        directDownloader.download(httpClient, downloadRequest)

        assertThat(fileSystem.exists(downloadFile)).isTrue()
    }
}