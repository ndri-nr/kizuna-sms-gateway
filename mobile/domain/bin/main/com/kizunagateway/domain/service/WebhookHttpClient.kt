package com.kizunagateway.domain.service

data class HttpResponse(
    val code: Int,
    val body: String,
    val isSuccessful: Boolean
)

interface WebhookHttpClient {
    suspend fun sendRequest(
        url: String,
        method: String,
        headers: Map<String, String>,
        bodyContent: String,
        timeoutSeconds: Int = 30
    ): HttpResponse
}
