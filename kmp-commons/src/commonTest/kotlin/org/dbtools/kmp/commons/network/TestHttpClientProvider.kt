package org.dbtools.kmp.commons.network

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.dbtools.kmp.commons.log.KtorKermitLogger
import org.dbtools.kmp.commons.network.ktor.ResponseTimePlugin

object TestHttpClientProvider {
    fun getTestClient(
        engine: HttpClientEngine,
    ): HttpClient {
        return HttpClient(engine) {
            install(ResponseTimePlugin)
            install(Logging) {
                logger = KtorKermitLogger
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        useArrayPolymorphism = true
                        allowSpecialFloatingPointValues = true
                        explicitNulls = false
                    }
                )
            }
            install(Resources)
        }
    }
}