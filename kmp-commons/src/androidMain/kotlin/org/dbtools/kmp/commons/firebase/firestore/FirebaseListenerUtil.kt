package org.dbtools.kmp.commons.firebase.firestore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dbtools.kmp.commons.coroutine.ProcessScope

class FirebaseListenerUtil {
    private var lastRestartDelay: Long = MINIMUM_DELAY
    private var lastErrorTime: Long = 0

    /**
     * When a listener gets an error it calls this to manage when to restart. This uses an exponential fall off time between [MINIMUM_DELAY] and [MAXIMUM_DELAY]
     * and resets if [AMPLE_TIME] has passed since the last error.
     */
    fun restartAfterError(restart: () -> Unit) = ProcessScope.launch(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastError = currentTime - lastErrorTime
        lastErrorTime = currentTime
        lastRestartDelay = if (timeSinceLastError >= AMPLE_TIME) {
            MINIMUM_DELAY
        } else {
            (lastRestartDelay * 2).coerceAtMost(MAXIMUM_DELAY)
        }
        delay(lastRestartDelay)
        restart()
    }

    companion object {
        private const val AMPLE_TIME = 60 * 60 * 1000L // one hour
        private const val MINIMUM_DELAY = 10 * 1000L // 10 seconds
        private const val MAXIMUM_DELAY = 10 * 60 * 1000L // 10 minutes
    }
}
