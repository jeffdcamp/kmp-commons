@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * JVM implementation of NetworkUtil.
 * Uses standard Java networking to check connectivity.
 */
actual class NetworkUtil {

    /**
     * Checks if the device is connected to the internet by attempting to reach a well-known host.
     *
     * @param allowMobileNetwork Ignored on JVM as there's no concept of mobile network
     * @return true if connected to the internet
     */
    actual fun isConnected(allowMobileNetwork: Boolean): Boolean {
        return try {
            // Try to resolve a well-known DNS
            val address = InetAddress.getByName("dns.google")

            // Additionally verify we can establish a TCP connection
            Socket().use { socket ->
                socket.connect(InetSocketAddress(address, 53), 3000)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * On JVM, networks are typically not metered.
     *
     * @return false (JVM networks are assumed to be unmetered)
     */
    actual fun isActiveNetworkMetered(): Boolean = false

    /**
     * Returns a Flow that periodically checks network connectivity and emits ConnectionInfo.
     * Since JVM doesn't have native network change callbacks, this implementation polls.
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
