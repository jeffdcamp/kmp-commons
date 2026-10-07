@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.flow
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * JVM implementation of NetworkUtil.
 * Uses standard Java networking to check connectivity.
 *
 * @param probeHost Host used to probe for connectivity. Override when the default is blocked (e.g. corporate networks that block dns.google:53)
 * @param probePort TCP port on [probeHost] used to probe for connectivity
 */
actual class NetworkUtil(
    private val probeHost: String = DEFAULT_PROBE_HOST,
    private val probePort: Int = DEFAULT_PROBE_PORT,
) {

    /**
     * Checks if the device is connected to the internet by attempting to reach a well-known host.
     *
     * @param allowMobileNetwork Ignored on JVM as there's no concept of mobile network
     * @return true if connected to the internet
     */
    actual fun isConnected(allowMobileNetwork: Boolean): Boolean {
        return try {
            // Try to resolve the probe host
            val address = InetAddress.getByName(probeHost)

            // Additionally verify we can establish a TCP connection
            Socket().use { socket ->
                socket.connect(InetSocketAddress(address, probePort), PROBE_CONNECT_TIMEOUT_MS)
                true
            }
        } catch (ignore: IOException) {
            // A failed probe simply means "not reachable"; the polling flow calls this every few
            // seconds, so logging here would spam the log while offline.
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
    }
        // isConnected() performs a blocking socket connect; keep it off the collector's dispatcher.
        .flowOn(Dispatchers.IO)
        .distinctUntilChanged()

    // No long-lived resources to release; connectivity is probed on demand.
    actual fun close() = Unit

    companion object {
        const val DEFAULT_PROBE_HOST = "dns.google"
        const val DEFAULT_PROBE_PORT = 53
        private const val PROBE_CONNECT_TIMEOUT_MS = 3000
        private const val POLL_INTERVAL_MS = 5000L
    }
}
