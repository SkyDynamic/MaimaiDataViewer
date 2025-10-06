package io.github.skydynamic.maidataviewer.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AppHttpClient {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15000
            connectTimeoutMillis = 15000
        }
        expectSuccess = false
        HttpResponseValidator {
            handleResponseExceptionWithRequest { cause, _ ->
                Log.e("AppHttpClient", "请求失败: ${cause.message}")
            }
        }
    }

    suspend fun <T> request(block: suspend (client: HttpClient) -> T): T? {
        return try {
            block(client)
        } catch (e: Exception) {
            null
        }
    }
}