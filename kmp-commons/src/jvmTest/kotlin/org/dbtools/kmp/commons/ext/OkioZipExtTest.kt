package org.dbtools.kmp.commons.ext

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import java.io.File
import java.nio.file.Files
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.test.AfterTest
import kotlin.test.Test

class OkioZipExtTest {
    private val fileSystem = FileSystem.SYSTEM
    private val tempDir: Path = Files.createTempDirectory("OkioZipExtTest").toFile().toOkioPath()

    @AfterTest
    fun tearDown() {
        fileSystem.deleteRecursively(tempDir)
    }

    @Test
    fun resolveInside() {
        val dir = "/data/app/files/target".toPath()
        assertThat(dir.resolveInside("a.txt")).isEqualTo("/data/app/files/target/a.txt".toPath())
        assertThat(dir.resolveInside("sub/../a.txt")).isEqualTo("/data/app/files/target/a.txt".toPath())

        listOf("../evil.txt", "../../shared_prefs/auth.xml", "sub/../../evil.txt", "..", "", "/etc/passwd").forEach { entryName ->
            assertFailure { dir.resolveInside(entryName) }.isInstanceOf<IllegalArgumentException>()
        }
    }

    @Test
    fun unzip() {
        val zipFile = createZip("good.zip", "a.txt" to "a", "sub/b.txt" to "b")
        val targetDir = tempDir / "target"

        fileSystem.unzip(zipFile, targetDir)

        assertThat(fileSystem.read(targetDir / "a.txt") { readUtf8() }).isEqualTo("a")
        assertThat(fileSystem.read(targetDir / "sub" / "b.txt") { readUtf8() }).isEqualTo("b")
    }

    @Test
    fun unzipDoesNotWriteOutsideTargetDir() {
        val zipFile = createZip("evil.zip", "../evil.txt" to "evil")
        val targetDir = tempDir / "target"

        // Either the entry is rejected, or it is normalized to a path inside targetDir... it must never escape
        runCatching { fileSystem.unzip(zipFile, targetDir) }

        assertThat(fileSystem.exists(tempDir / "evil.txt")).isFalse()
        assertThat(fileSystem.exists(targetDir)).isTrue()
    }

    @Test
    fun unzipDoesNotFollowASymlinkedFile() {
        val outside = (tempDir / "outside.txt").also { fileSystem.write(it) { writeUtf8("original") } }
        val targetDir = (tempDir / "target").also { fileSystem.createDirectories(it) }
        Files.createSymbolicLink((targetDir / "a.txt").toNioPath(), outside.toNioPath())

        assertFailure { fileSystem.unzip(createZip("evil.zip", "a.txt" to "evil"), targetDir) }.isInstanceOf<IllegalArgumentException>()
        assertThat(fileSystem.read(outside) { readUtf8() }).isEqualTo("original")
    }

    @Test
    fun unzipDoesNotFollowADanglingSymlink() {
        val outside = tempDir / "created-outside.txt"
        val targetDir = (tempDir / "target").also { fileSystem.createDirectories(it) }
        Files.createSymbolicLink((targetDir / "a.txt").toNioPath(), outside.toNioPath())

        assertFailure { fileSystem.unzip(createZip("evil.zip", "a.txt" to "evil"), targetDir) }.isInstanceOf<IllegalArgumentException>()
        assertThat(fileSystem.exists(outside)).isFalse()
    }

    @Test
    fun unzipDoesNotFollowASymlinkedDirectory() {
        val outsideDir = (tempDir / "outside").also { fileSystem.createDirectories(it) }
        val targetDir = (tempDir / "target").also { fileSystem.createDirectories(it) }
        Files.createSymbolicLink((targetDir / "sub").toNioPath(), outsideDir.toNioPath())

        assertFailure { fileSystem.unzip(createZip("evil.zip", "sub/x.txt" to "evil"), targetDir) }.isInstanceOf<IllegalArgumentException>()
        assertThat(fileSystem.exists(outsideDir / "x.txt")).isFalse()
    }

    private fun createZip(name: String, vararg entries: Pair<String, String>): Path {
        val zipFile = File(tempDir.toFile(), name)
        ZipOutputStream(zipFile.outputStream()).use { zip ->
            entries.forEach { (entryName, content) ->
                zip.putNextEntry(ZipEntry(entryName))
                zip.write(content.toByteArray())
                zip.closeEntry()
            }
        }
        return zipFile.toOkioPath()
    }
}
