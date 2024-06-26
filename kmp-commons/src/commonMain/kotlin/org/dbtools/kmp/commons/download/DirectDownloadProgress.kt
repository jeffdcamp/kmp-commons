@file:Suppress("MemberVisibilityCanBePrivate")

package org.dbtools.kmp.commons.download

sealed class DirectDownloadProgress {
    data object Enqueued : DirectDownloadProgress()
    class Downloading(val totalBytesRead: Long, val contentLength: Long) : DirectDownloadProgress() {
        fun calculateProgress(): Long = (100 * totalBytesRead) / contentLength
    }
    class DownloadComplete(val success: Boolean, val message: String? = null) : DirectDownloadProgress()

    /**
     * Progress based on a range of 0-100
     */
    val progress: Int
        get() {
            return when (this) {
                is Enqueued -> 0
                is Downloading -> calculateProgress().toInt()
                is DownloadComplete -> MAX_PROGRESS
            }
        }

    val viewProgress: Float
        get() {
        return when (this) {
            is Enqueued -> 0f
            is Downloading -> calculateProgress() / 100f
            is DownloadComplete -> MAX_COMPOSE_PROGRESS
        }
    }

    fun toDebugString(): String {
        return when (this) {
            is Enqueued -> "Enqueued"
            is Downloading -> "${this.calculateProgress()}%"
            is DownloadComplete -> "Download Complete (success: ${this.success}${if (this.message.isNullOrBlank()) "" else "  message: " + this.message})"
        }
    }

    companion object {
        const val MAX_PROGRESS = 100
        const val MAX_COMPOSE_PROGRESS = 1f
    }
}
