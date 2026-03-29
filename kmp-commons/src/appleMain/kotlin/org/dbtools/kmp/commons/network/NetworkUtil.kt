@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_path_get_status
import platform.Network.nw_path_is_constrained
import platform.Network.nw_path_is_expensive
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_t
import platform.Network.nw_path_uses_interface_type
import platform.Network.nw_interface_type_cellular
import platform.darwin.dispatch_get_main_queue
import kotlin.concurrent.Volatile

/**
 * Apple (iOS/macOS) implementation of NetworkUtil.
 * Uses the Network framework's NWPathMonitor for connectivity status.
 */
@OptIn(ExperimentalForeignApi::class)
actual class NetworkUtil {

    private val pathMonitor = nw_path_monitor_create()

    @Volatile
    private var currentPath: nw_path_t = null

    init {
        nw_path_monitor_set_queue(pathMonitor, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(pathMonitor) { path ->
            currentPath = path
        }
        nw_path_monitor_start(pathMonitor)
    }

    /**
     * Checks if the device is connected to the internet.
     *
     * @param allowMobileNetwork If false, returns false when only cellular data is available
     * @return true if connected to the internet
     */
    actual fun isConnected(allowMobileNetwork: Boolean): Boolean {
        val path = currentPath ?: return false
        val status = nw_path_get_status(path)

        if (status != nw_path_status_satisfied) {
            return false
        }

        if (!allowMobileNetwork && nw_path_uses_interface_type(path, nw_interface_type_cellular)) {
            return false
        }

        return true
    }

    /**
     * Checks if the active network is metered/expensive.
     * On Apple platforms, this corresponds to "expensive" paths (cellular, personal hotspot).
     *
     * @return true if the network is expensive/constrained
     */
    actual fun isActiveNetworkMetered(): Boolean {
        val path = currentPath ?: return false
        return nw_path_is_expensive(path) || nw_path_is_constrained(path)
    }

    /**
     * Returns a Flow that emits ConnectionInfo whenever network connectivity changes.
     */
    actual fun connectionInfoFlow(): Flow<ConnectionInfo> = callbackFlow {
        val monitor = nw_path_monitor_create()

        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
        nw_path_monitor_set_update_handler(monitor) { path ->
            val status = nw_path_get_status(path)
            val isConnected = status == nw_path_status_satisfied
            val isMetered = nw_path_is_expensive(path) || nw_path_is_constrained(path)
            trySendBlocking(ConnectionInfo(isConnected, isMetered))
        }
        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }.buffer(Channel.CONFLATED)
}
