@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import kotlinx.coroutines.flow.Flow

/**
 * Utility class for checking network connectivity status.
 *
 * Platform-specific implementations provide the actual network checking logic:
 * - Android: Uses ConnectivityManager
 * - JVM: Uses standard Java networking
 * - iOS/macOS: Uses NWPathMonitor
 */
expect class NetworkUtil {
    /**
     * Checks if the device is connected to the internet.
     *
     * @param allowMobileNetwork If false, returns false when only cellular data is available
     * @return true if connected to the internet
     */
    fun isConnected(allowMobileNetwork: Boolean = true): Boolean

    /**
     * Checks if the active network is metered (e.g., cellular data with usage limits).
     *
     * @return true if the active network is metered
     */
    fun isActiveNetworkMetered(): Boolean

    /**
     * Returns a Flow that emits ConnectionInfo whenever network connectivity changes.
     */
    fun connectionInfoFlow(): Flow<ConnectionInfo>
}
