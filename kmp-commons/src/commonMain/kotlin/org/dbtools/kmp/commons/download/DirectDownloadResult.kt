package org.dbtools.kmp.commons.download

data class DirectDownloadResult(
    val success: Boolean,
    val message: String? = null,
    val code: Int = NO_RESPONSE_CODE
) {
    companion object {
        /**
         * [code] when the download failed in a way that MIGHT succeed on another attempt: no HTTP response
         * (connection reset, network handoff, DNS, TLS, socket timeout), or the filesystem throwing while
         * preparing the target — external storage can be transiently unavailable. Treated as retryable.
         *
         * A local failure that is *deterministic* uses [LOCAL_ERROR_CODE] instead, so not every
         * target-directory problem lands here: one where `createDirectories` throws does, while one where it
         * returns yet leaves the directory absent does not.
         */
        const val NO_RESPONSE_CODE = -1

        /**
         * [code] when the download failed locally for a DETERMINISTIC reason that a retry cannot change — an
         * unusable target path, a target directory that cannot be created, or a target file that already
         * exists while `overwriteExisting == false`. Never retried, so the caller gets its answer immediately
         * instead of after [DirectDownloadRequest.maxAttempts] pointless delays.
         */
        const val LOCAL_ERROR_CODE = -2
    }
}
