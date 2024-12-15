package vk.service

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

object VkHttpClient {
    private val client = HttpClient(CIO)

    suspend fun get(
        endpoint: String,
        params: Map<String, String>,
        accessToken: String,
        apiVersion: String,
    ): String {
        val baseUrl = "https://api.vk.com/method/$endpoint"

        val response: HttpResponse = client.get(baseUrl) {
            params.forEach { (key, value) ->
                parameter(key, value)
            }
            parameter("access_token", accessToken)
            parameter("v", apiVersion)
        }
        return response.bodyAsText()
    }

    fun close() {
        client.close()
    }
}