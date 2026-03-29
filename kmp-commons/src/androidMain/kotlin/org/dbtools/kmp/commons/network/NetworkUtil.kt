@file:Suppress("unused")

package org.dbtools.kmp.commons.network

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

actual class NetworkUtil(private val connectivityManager: ConnectivityManager) {

    @RequiresPermission(ACCESS_NETWORK_STATE)
    actual fun isConnected(allowMobileNetwork: Boolean): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false

        val hasInternet = hasInternetCapabilityInAnyNetwork()

        // check to see if mobile network is usable
        return when {
            !hasInternet -> false
            allowMobileNetwork -> {
                // mobile network is allowed... so just check if we have Internet
                true
            }
            else -> {
                // mobile network is NOT allowed...

                // check the default network for cellular connection
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return false
                }

                // check to see if vpn is using cellular connection
                if (activeNetworkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) && !hasInternetCapableWiFiInAnyNetwork()) {
                    // vpn network AND there was NO WiFi is in the list of transport networks
                    return false
                }

                // cellular network is NOT being used... at this point we MUST have a good connection
                true
            }
        }

    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    actual fun isActiveNetworkMetered() = connectivityManager.isActiveNetworkMetered

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private fun hasInternetCapabilityInAnyNetwork(): Boolean {
        return connectivityManager.allNetworks.any { network ->
            connectivityManager.getNetworkCapabilities(network)?.let {
                it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            } == true
        }
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    private fun hasInternetCapableWiFiInAnyNetwork(): Boolean {
        return connectivityManager.allNetworks.any { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun activeNetworkHasCaptivePortal(): Boolean {
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork) ?: return false
        return activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getAllNetworkInfo(allowMobileNetwork: Boolean = true): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "No Active Network"
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return "No Network Capabilities"

        var info: String = "App can use Internet: ${isConnected(allowMobileNetwork)}\n" +
                "Is Internet Available: ${activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)}\n" +
                "Is Internet Validated: ${activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)}\n" +
                "Is Captive Portal: ${activeNetworkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL)}\n" +
                "\n" +
                "=== Active Network ===\n" +
                "$activeNetworkCapabilities" +
                "\n\n" +
                "=== All Networks ===\n"

        connectivityManager.allNetworks.forEach { network ->
            connectivityManager.getNetworkCapabilities(network)?.let {
                info += "$it\n\n"
            }
        }

        return info
    }

    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun getActiveNetworkInfo(): String {
        val activeNetwork = connectivityManager.activeNetwork ?: return "No Active Network"
        val activeNetworkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return "No Network Capabilities"
        return activeNetworkCapabilities.toString()
    }

    /**
     * Registers a [ConnectivityManager.NetworkCallback] that can react to network connectivity changes. NOTE:
     * this is only supported on devices running API 21 or higher.
     */
    @RequiresPermission(ACCESS_NETWORK_STATE)
    fun registerNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
    }

    /**
     * Unregisters [ConnectivityManager.NetworkCallback] registered in [registerNetworkCallback]
     */
    fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback) = connectivityManager.unregisterNetworkCallback(networkCallback)

    @RequiresPermission(ACCESS_NETWORK_STATE)
    actual fun connectionInfoFlow(): Flow<ConnectionInfo> = callbackFlow {

        // Send initial info in case device isn't connected at all (Airplane Mode).
        trySendBlocking(ConnectionInfo(isConnected(), isActiveNetworkMetered()))

        val callback = object : ConnectivityManager.NetworkCallback() {
            @RequiresPermission(ACCESS_NETWORK_STATE)
            override fun onLost(network: Network) = setConnectionInfo()

            @RequiresPermission(ACCESS_NETWORK_STATE)
            override fun onAvailable(network: Network) = setConnectionInfo()

            @RequiresPermission(ACCESS_NETWORK_STATE)
            private fun setConnectionInfo() {
                trySendBlocking(ConnectionInfo(isConnected(), isActiveNetworkMetered()))
            }
        }
        registerNetworkCallback(callback)
        awaitClose { unregisterNetworkCallback(callback) }
    }.buffer(Channel.CONFLATED)
}
