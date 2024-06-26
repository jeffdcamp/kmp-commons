@file:Suppress("unused")

package org.dbtools.kmp.commons.network.ktor

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request

val ResponseTimePlugin = createClientPlugin("ResponseTimePlugin") {
    onResponse { response ->
        co.touchlab.kermit.Logger.i {
            "<-- ${response.request.method.value} ${response.request.url} : ${response.responseTime.timestamp - response.requestTime.timestamp} ms"
        }
    }
}