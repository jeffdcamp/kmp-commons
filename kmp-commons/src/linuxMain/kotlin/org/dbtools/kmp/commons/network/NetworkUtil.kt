@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.SOCK_STREAM
import platform.posix.close
import platform.posix.connect
import platform.posix.htons
import platform.posix.sockaddr_in
import platform.posix.socket

/**
 * Linux implementation of NetworkUtil.
 * Uses POSIX sockets to check connectivity.
 */
actual class NetworkUtil {

    /**
     * Checks if the device is connected to the internet by attempting to connect to a well-known host.
     *
     * @param allowMobileNetwork Ignored on Linux as there's no concept of mobile network
     * @return true if connected to the internet
     */
    @OptIn(ExperimentalForeignApi::class)
    actual fun isConnected(allowMobileNetwork: Boolean): Boolean {
        return memScoped {
            val socketFd = socket(AF_INET, SOCK_STREAM, 0)
            if (socketFd < 0) return@memScoped false

            try {
                val serverAddr = alloc<sockaddr_in>()
                serverAddr.sin_family = AF_INET.convert()
                serverAddr.sin_port = htons(53u) // DNS port
                serverAddr.sin_addr.s_addr = inet_addr("8.8.8.8") // Google DNS

                val result = connect(socketFd, serverAddr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                result == 0
            } finally {
                close(socketFd)
            }
        }
    }

    /**
     * On Linux, networks are typically not metered.
     *
     * @return false (Linux networks are assumed to be unmetered)
     */
    actual fun isActiveNetworkMetered(): Boolean = false

    /**
     * Returns a Flow that periodically checks network connectivity and emits ConnectionInfo.
     * Since Linux doesn't have native network change callbacks, this implementation polls.
     */
    actual fun connectionInfoFlow(): Flow<ConnectionInfo> = flow {
        while (true) {
            emit(ConnectionInfo(isConnected(), isActiveNetworkMetered()))
            delay(POLL_INTERVAL_MS)
        }
    }.distinctUntilChanged()

    companion object {
        private const val POLL_INTERVAL_MS = 5000L
    }
}
