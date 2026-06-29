package com.kizunagateway.core.service

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kizunagateway.core.network.HttpGatewayServer
import com.kizunagateway.domain.model.OutboundSmsStatus
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.OutboundRepository
import com.kizunagateway.domain.service.SmsSender
import com.kizunagateway.domain.service.WebhookHttpClient
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class OutboundSmsService : Service() {

    @Inject lateinit var httpGatewayServer: HttpGatewayServer
    @Inject lateinit var tunnelClient: WebSocketTunnelClient
    @Inject lateinit var outboundRepository: OutboundRepository
    @Inject lateinit var gatewayConfigRepository: GatewayConfigRepository
    @Inject lateinit var smsSender: SmsSender
    @Inject lateinit var webhookHttpClient: WebhookHttpClient

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var processingJob: Job? = null

    companion object {
        private const val CHANNEL_ID = "outbound_sms_service"
        private const val NOTIFICATION_ID = 1001
        
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        _isRunning.value = true
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }

    private fun startService() {
        _isRunning.value = true
        val notification = createNotification(getString(R.string.outbound_service_running))
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        if (!httpGatewayServer.isRunning()) {
            httpGatewayServer.start()
        }

        serviceScope.launch {
            val config = gatewayConfigRepository.getGatewayConfig()
            tunnelClient.start(
                tunnelUrl = config.tunnelServerUrl,
                gatewayId = config.gatewayId,
                deviceSecret = config.deviceSecret,
                scope = serviceScope,
                onError = {
                    // Force disconnect/stop service on error
                    stopService()
                }
            )
        }
        
        startQueueProcessor()
    }

    private fun stopService() {
        _isRunning.value = false
        updateNotification(getString(R.string.stopping_outbound_service))
        
        tunnelClient.stop()
        httpGatewayServer.stop {
            serviceScope.launch {
                processingJob?.cancelAndJoin()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun startQueueProcessor() {
        if (processingJob?.isActive == true) return
        
        processingJob = serviceScope.launch {
            while (isActive) {
                val pendingSms = outboundRepository.getSmsByStatus(OutboundSmsStatus.PENDING)
                if (pendingSms.isEmpty()) {
                    delay(5000.milliseconds)
                    continue
                }

                for (sms in pendingSms) {
                    if (!isActive) break
                    
                    try {
                        outboundRepository.updateOutboundSms(sms.copy(status = OutboundSmsStatus.SENDING))
                        
                        // Actually send SMS
                        smsSender.sendSms(sms.phoneNumber, sms.message, sms.simSlot)
                        
                        val updatedSms = sms.copy(
                            status = OutboundSmsStatus.SENT,
                            sentAt = LocalDateTime.now()
                        )
                        outboundRepository.updateOutboundSms(updatedSms)
                        triggerWebhook(updatedSms)
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        val failedSms = sms.copy(
                            status = OutboundSmsStatus.FAILED,
                            errorMessage = e.message
                        )
                        outboundRepository.updateOutboundSms(failedSms)
                        triggerWebhook(failedSms)
                    }
                    delay(1000.milliseconds)
                }
            }
        }
    }

    private suspend fun triggerWebhook(sms: com.kizunagateway.domain.model.OutboundSms) {
        val config = gatewayConfigRepository.getGatewayConfig()
        val url = sms.webhookUrl ?: config.outboundWebhookUrl
        if (url.isNullOrBlank()) return

        val payload = mapOf(
            "id" to sms.id,
            "phoneNumber" to sms.phoneNumber,
            "status" to sms.status.name,
            "errorMessage" to sms.errorMessage,
            "sentAt" to sms.sentAt?.toString(),
            "messageId" to sms.messageId
        )

        try {
            webhookHttpClient.sendRequest(
                url = url,
                method = "POST",
                headers = mapOf("Content-Type" to "application/json"),
                bodyContent = Json.encodeToString(payload)
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e("OutboundSmsService", "Failed to trigger webhook: ${e.message}")
        }
    }

    private fun createNotification(content: String): Notification {
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(appName)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification(content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.outbound_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        _isRunning.value = false
        serviceScope.cancel()
        super.onDestroy()
    }
}
