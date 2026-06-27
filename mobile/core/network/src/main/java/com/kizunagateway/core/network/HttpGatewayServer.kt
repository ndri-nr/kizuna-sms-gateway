package com.kizunagateway.core.network

import com.kizunagateway.core.network.model.BatchSmsRequest
import com.kizunagateway.core.network.model.SmsRequest
import com.kizunagateway.core.network.model.SmsResponse
import com.kizunagateway.domain.model.OutboundSms
import com.kizunagateway.domain.repository.OutboundRepository
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HttpGatewayServer @Inject constructor(
    private val outboundRepository: OutboundRepository
) {
    private var server: NettyApplicationEngine? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isDraining = false

    fun start(port: Int = 8080) {
        if (server != null) return
        isDraining = false

        server = embeddedServer(Netty, port = port) {
            install(ContentNegotiation) {
                json()
            }
            install(StatusPages) {
                exception<Throwable> { call, cause ->
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (cause.message ?: "Unknown error")))
                }
            }
            routing {
                route("/api/v1/sms") {
                    intercept(ApplicationCallPipeline.Plugins) {
                        if (isDraining) {
                            call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to "Service is shutting down"))
                            finish()
                            return@intercept
                        }
                        
                        val apiKey = call.request.headers["X-API-KEY"]
                        if (apiKey == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Missing API Key"))
                            finish()
                        } else {
                            val keyInfo = outboundRepository.getActiveKey(apiKey)
                            if (keyInfo == null) {
                                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Invalid or inactive API Key"))
                                finish()
                            }
                            // TODO: Implement rate limiting logic here using keyInfo
                        }
                    }

                    post("/send") {
                        val request = call.receive<SmsRequest>()
                        val id = outboundRepository.insertOutboundSms(
                            OutboundSms(
                                phoneNumber = request.phoneNumber,
                                message = request.message,
                                simSlot = request.simSlot ?: 0,
                                webhookUrl = request.webhookUrl
                            )
                        )
                        call.respond(HttpStatusCode.Accepted, SmsResponse(id, "PENDING"))
                    }

                    post("/send-batch") {
                        val batchRequest = call.receive<BatchSmsRequest>()
                        val responses = batchRequest.messages.map { request ->
                            val id = outboundRepository.insertOutboundSms(
                                OutboundSms(
                                    phoneNumber = request.phoneNumber,
                                    message = request.message,
                                    simSlot = request.simSlot ?: 0,
                                    webhookUrl = request.webhookUrl
                                )
                            )
                            SmsResponse(id, "PENDING")
                        }
                        call.respond(HttpStatusCode.Accepted, responses)
                    }

                    get("/{id}") {
                        val id = call.parameters["id"]?.toLongOrNull()
                        if (id == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))
                            return@get
                        }
                        val sms = outboundRepository.getOutboundSmsById(id)
                        if (sms == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "SMS not found"))
                        } else {
                            call.respond(SmsResponse(sms.id, sms.status.name, sms.errorMessage))
                        }
                    }

                    get("/queue") {
                        val pendingCount = outboundRepository.getPendingCount().first()
                        call.respond(mapOf("pendingCount" to pendingCount))
                    }
                }
            }
        }.start(wait = false)
    }

    fun stop(onComplete: () -> Unit) {
        isDraining = true
        scope.launch {
            // Wait for queue to be empty
            while (outboundRepository.getPendingCount().first() > 0) {
                kotlinx.coroutines.delay(1000)
            }
            server?.stop(1000, 5000)
            server = null
            onComplete()
        }
    }
    
    fun isRunning(): Boolean = server != null
}
