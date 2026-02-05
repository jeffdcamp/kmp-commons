@file:Suppress("unused")

package org.dbtools.kmp.commons.network.ktor

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request

val ResponseTimePlugin = createClientPlugin("ResponseTimePlugin") {
    onResponse { response ->
        Logger.i {
            "<-- ${response.request.method.value} ${response.request.url} : ${response.responseTime.timestamp - response.requestTime.timestamp} ms"
        }
    }
}