package com.kizunagateway.core.network

import com.kizunagateway.domain.service.HttpResponse
import com.kizunagateway.domain.service.WebhookHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebhookClient @Inject constructor(
    private val okHttpClient: OkHttpClient
) : WebhookHttpClient {

    override suspend fun sendRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        bodyContent: String,
        timeoutSeconds: Int
    ): HttpResponse = withContext(Dispatchers.IO) {
        // We still respect timeout per request by creating a shallow copy if it differs from default
        val client = if (okHttpClient.connectTimeoutMillis != timeoutSeconds * 1000) {
            okHttpClient.newBuilder()
                .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
                .build()
        } else {
            okHttpClient
        }

        val requestBody = bodyContent.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val request = okhttp3.Request.Builder()
            .url(url)
            .apply {
                headers.forEach { (k, v) -> addHeader(k, v) }
                method(method.uppercase(), if (method.uppercase() in listOf("GET", "HEAD")) null else requestBody)
            }
            .build()

        try {
            client.newCall(request).execute().use { response ->
                HttpResponse(
                    code = response.code,
                    body = response.body?.string() ?: "",
                    isSuccessful = response.isSuccessful
                )
            }
        } catch (e: Exception) {
            HttpResponse(
                code = 0,
                body = e.message ?: "Unknown error",
                isSuccessful = false
            )
        }
    }
}
