@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flow
import platform.linux.inet_addr
import platform.posix.AF_INET
import platform.posix.EINPROGRESS
import platform.posix.F_GETFL
import platform.posix.F_SETFL
import platform.posix.O_NONBLOCK
import platform.posix.POLLOUT
import platform.posix.SOCK_STREAM
import platform.posix.SOL_SOCKET
import platform.posix.SO_ERROR
import platform.posix.close
import platform.posix.connect
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.getsockopt
import platform.posix.htons
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.sockaddr_in
import platform.posix.socket
import platform.posix.socklen_tVar

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
                // Put the socket in non-blocking mode so connect() can be bounded by poll();
                // a blocking connect() has no timeout and can hang for the OS default on an
                // offline/firewalled host.
                val flags = fcntl(socketFd, F_GETFL, 0)
                if (flags < 0 || fcntl(socketFd, F_SETFL, flags or O_NONBLOCK) < 0) {
                    return@memScoped false
                }

                val serverAddr = alloc<sockaddr_in>()
                serverAddr.sin_family = AF_INET.convert()
                serverAddr.sin_port = htons(53u) // DNS port
                serverAddr.sin_addr.s_addr = inet_addr("8.8.8.8") // Google DNS

                val result = connect(socketFd, serverAddr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                if (result == 0) return@memScoped true // connected immediately
                if (errno != EINPROGRESS) return@memScoped false // failed for some other reason

                // Wait (bounded) for the socket to become writable, which signals connect completion.
                val pollFd = alloc<pollfd>()
                pollFd.fd = socketFd
                pollFd.events = POLLOUT.convert()
                val pollResult = poll(pollFd.ptr, 1u, CONNECT_TIMEOUT_MS)
                if (pollResult <= 0) return@memScoped false // timed out or poll error

                // poll() reports writability even on connect failure; check SO_ERROR to confirm success.
                val soError = alloc<IntVar>()
                val soErrorLen = alloc<socklen_tVar>()
                soErrorLen.value = sizeOf<IntVar>().convert()
                if (getsockopt(socketFd, SOL_SOCKET, SO_ERROR, soError.ptr, soErrorLen.ptr) < 0) {
                    return@memScoped false
                }
                soError.value == 0
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
    }
        // isConnected() performs a socket connect; keep it off the collector's dispatcher.
        // Kotlin/Native has no Dispatchers.IO, so use Default.
        .flowOn(Dispatchers.Default)
        .distinctUntilChanged()

    // No long-lived resources to release; connectivity is probed on demand.
    actual fun close() = Unit

    companion object {
        private const val POLL_INTERVAL_MS = 5000L
        private const val CONNECT_TIMEOUT_MS = 3000
    }
}
