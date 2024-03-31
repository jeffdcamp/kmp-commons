package org.dbtools.kmp.commons.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow

fun NetworkUtil.connectionInfoFlow(context: Context): Flow<ConnectionInfo> = callbackFlow {

    // Send initial info in case device isn't connected at all (Airplane Mode).
    trySendBlocking(ConnectionInfo(isConnected(context), isActiveNetworkMetered(context)))

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) = setConnectionInfo()

        override fun onAvailable(network: Network) = setConnectionInfo()

        private fun setConnectionInfo() {
            trySendBlocking(ConnectionInfo(isConnected(context), isActiveNetworkMetered(context)))
        }
    }
    registerNetworkCallback(context, callback)
    awaitClose { unregisterNetworkCallback(context, callback) }
}.buffer(Channel.CONFLATED)
