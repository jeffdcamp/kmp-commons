package org.dbtools.kmp.commons.ext

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.openZip

/**
 * Unzip file from zip file and output to file Path
 * @param sourceZipFile Zip file containing file
 * @param fileInZip file Path of file in zip file
 * @param outFile target file Path to copy the file to
 */
fun FileSystem.unzipFile(sourceZipFile: Path, fileInZip: Path, outFile: Path) {
    require(exists(sourceZipFile)) { "sourceZipFile ($sourceZipFile) does not exist" }
    val zipFilesystem = openZip(sourceZipFile)
    zipFilesystem.copyFileToFileSystem(fileInZip, this, outFile)
}

/**
 * Unzip the full content of a zip file to a target directory
 * @param sourceZipFile Source zip file
 * @param targetDir Directory that will be created and contents will be unzipped to
 * @param mustCreate Fail if the directory already exists. default = false
 */
fun FileSystem.unzip(sourceZipFile: Path, targetDir: Path, mustCreate: Boolean = false) {
    require(!isDirectory(sourceZipFile)) { "sourceZipFile must be a file" }
    require(exists(sourceZipFile)) { "sourceZipFile ($sourceZipFile) does not exist" }

    if (exists(targetDir) && !mustCreate) {
        require(isDirectory(targetDir)) { "existing targetDir ($targetDir) is not a directory" }
    }

    // create targetDir
    createDirectories(targetDir, mustCreate = mustCreate)
    require(exists(targetDir)) { "targetDir could not be created" }
    val targetDirFull = canonicalize(targetDir)

    val zipFilesystem = openZip(sourceZipFile)
    zipFilesystem.listRecursively("".toPath()).forEach { path ->
        val pathName = path.toString().removePrefix("/")
        val outPath = targetDirFull.resolveInside(pathName)
        if (zipFilesystem.isDirectory(path)) {
            requireResolvedWithin(outPath, targetDirFull, pathName)
            createDirectories(outPath)
        } else {
            outPath.parent?.let { requireResolvedWithin(it, targetDirFull, pathName) }
            // writing to a symbolic link would write to wherever it points (even if the link target does not exist yet)
            require(metadataOrNull(outPath)?.symlinkTarget == null) { "Zip entry ($pathName) target is a symbolic link" }
            zipFilesystem.copyFileToFileSystem(path, this, outPath)
        }
    }
}

/**
 * Make sure [path] (with symbolic links resolved) is inside [dir]... a symbolic link already in the target directory (or in a parent of
 * [path]) could otherwise redirect a write outside of [dir]. [path] may not exist yet, so its nearest existing ancestor is checked.
 */
private fun FileSystem.requireResolvedWithin(path: Path, dir: Path, entryName: String) {
    var existing: Path? = path
    while (existing != null && !exists(existing)) {
        existing = existing.parent
    }
    require(existing != null && canonicalize(existing).isWithin(dir)) { "Zip entry ($entryName) resolves outside of the target directory ($dir)" }
}

/**
 * Resolve [child] against this directory and make sure the result stays inside this directory (prevents "Zip Slip" path traversal
 * where a zip entry such as "../../shared_prefs/auth.xml" would be written outside of the target directory)
 */
internal fun Path.resolveInside(child: String): Path {
    val resolved = resolve(child, normalize = true)
    require(resolved != this && resolved.isWithin(this)) { "Zip entry ($child) is outside of the target directory ($this)" }
    return resolved
}

private fun Path.isWithin(dir: Path): Boolean {
    var current: Path? = this
    while (current != null) {
        if (current == dir) return true
        current = current.parent
    }
    return false
}
