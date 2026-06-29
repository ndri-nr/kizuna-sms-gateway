package com.kizunagateway.core.service

import android.util.Log
import com.kizunagateway.domain.service.NotificationService
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
@InternalSerializationApi
data class TunnelRequest(
    val id: String,
    val method: String,
    val path: String,
    val headers: Map<String, String>,
    val body: String? = null
)

@Serializable
@InternalSerializationApi
data class TunnelResponse(
    val id: String,
    val status: Int,
    val headers: Map<String, String>,
    val body: String? = null
)

@Singleton
class WebSocketTunnelClient @Inject constructor(
    private val notificationService: NotificationService
) {
    private val client = HttpClient {
        install(WebSockets) {
            pingInterval = 20_000
        }
    }
    private val localClient = HttpClient()
    private var tunnelJob: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    fun start(
        tunnelUrl: String, 
        gatewayId: String, 
        deviceSecret: String, 
        scope: CoroutineScope,
        onError: () -> Unit = {}
    ) {
        if (tunnelJob?.isActive == true) return
        
        tunnelJob = scope.launch {
            var backoff = 1000L
            while (isActive) {
                try {
                    Log.i("Tunnel", "Connecting to tunnel server: $tunnelUrl")
                    client.webSocket(
                        method = HttpMethod.Get,
                        host = tunnelUrl,
                        path = "/ws/${gatewayId.lowercase()}",
                        request = {
                            header("Authorization", deviceSecret)
                        }
                    ) {
                        Log.i("Tunnel", "Connected to tunnel server")
                        backoff = 1000L // Reset backoff on successful connection
                        
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                val text = frame.readText()
                                handleRequest(text, this)
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Log.e("Tunnel", "Tunnel error: ${e.message}")
                    notificationService.showMessage("Websocket service unavailable")
                    onError() // Notify service to stop

                    delay(backoff)
                    backoff = (backoff * 2).coerceAtMost(60_000L)
                }
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    private suspend fun handleRequest(text: String, session: DefaultWebSocketSession) {
        try {
            val request = json.decodeFromString<TunnelRequest>(text)
            Log.d("Tunnel", "Handling request: ${request.method} ${request.path}")
            
            // Proxy to local Ktor server
            val localResponse = localClient.request("http://localhost:8080${request.path}") {
                method = HttpMethod.parse(request.method)
                request.headers.forEach { (k, v) -> 
                    if (k.lowercase() != "content-length" && k.lowercase() != "host") {
                        header(k, v)
                    }
                }
                request.body?.let { setBody(it) }
            }
            
            val response = TunnelResponse(
                id = request.id,
                status = localResponse.status.value,
                headers = localResponse.headers.entries().associate { it.key to it.value.joinToString(",") },
                body = localResponse.bodyAsText()
            )
            
            session.send(Frame.Text(json.encodeToString(response)))
        } catch (e: Exception) {
            Log.e("Tunnel", "Error handling proxied request: ${e.message}")
        }
    }

    fun stop() {
        tunnelJob?.cancel()
        tunnelJob = null
    }
}
