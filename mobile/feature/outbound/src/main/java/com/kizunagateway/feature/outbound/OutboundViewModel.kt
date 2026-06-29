package com.kizunagateway.feature.outbound

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.core.service.OutboundSmsService
import com.kizunagateway.domain.model.ApiKey
import com.kizunagateway.domain.model.OutboundSms
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.OutboundRepository
import com.kizunagateway.domain.service.NotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class OutboundViewModel @Inject constructor(
    application: Application,
    private val outboundRepository: OutboundRepository,
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val notificationService: NotificationService
) : AndroidViewModel(application) {

    private val _isServiceRunning = MutableStateFlow(OutboundSmsService.isRunning.value)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _apiKeys = MutableStateFlow<List<ApiKey>>(emptyList())
    val apiKeys: StateFlow<List<ApiKey>> = _apiKeys.asStateFlow()

    private val _outboundLogs = MutableStateFlow<List<OutboundSms>>(emptyList())
    val outboundLogs: StateFlow<List<OutboundSms>> = _outboundLogs.asStateFlow()

    private val _deviceSecret = MutableStateFlow("")
    val deviceSecret: StateFlow<String> = _deviceSecret.asStateFlow()

    private val _webhookUrl = MutableStateFlow("")
    val webhookUrl: StateFlow<String> = _webhookUrl.asStateFlow()

    private val _tunnelServerUrl = MutableStateFlow("")
    val tunnelServerUrl: StateFlow<String> = _tunnelServerUrl.asStateFlow()

    private val _gatewayId = MutableStateFlow("")
    val gatewayId: StateFlow<String> = _gatewayId.asStateFlow()

    val baseUrl: StateFlow<String> = combine(_tunnelServerUrl, _gatewayId) { tunnel, id ->
        if (tunnel.isBlank() || id.isBlank()) "" else "https://$tunnel/${id.lowercase()}"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        loadData()
        viewModelScope.launch {
            OutboundSmsService.isRunning.collect { running ->
                _isServiceRunning.value = running
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            outboundRepository.getAllApiKeys().collect { _apiKeys.value = it }
        }
        viewModelScope.launch {
            outboundRepository.getAllOutboundSms().collect { _outboundLogs.value = it }
        }
        viewModelScope.launch {
            val config = gatewayConfigRepository.getGatewayConfig()
            _deviceSecret.value = config.deviceSecret
            _webhookUrl.value = config.outboundWebhookUrl ?: ""
            _tunnelServerUrl.value = config.tunnelServerUrl
            _gatewayId.value = config.gatewayId
        }
    }

    fun toggleService(enabled: Boolean) {
        val intent = Intent(getApplication(), OutboundSmsService::class.java).apply {
            action = if (enabled) OutboundSmsService.ACTION_START else OutboundSmsService.ACTION_STOP
        }
        if (enabled) {
            getApplication<Application>().startForegroundService(intent)
        } else {
            getApplication<Application>().startService(intent)
        }
        notificationService.showMessage(if (enabled) "Service started" else "Service stopped")
    }

    fun generateApiKey(name: String) {
        viewModelScope.launch {
            val key = ApiKey(
                key = UUID.randomUUID().toString().replace("-", ""),
                name = name
            )
            outboundRepository.insertApiKey(key)
            notificationService.showMessage("API Key generated")
        }
    }

    fun toggleApiKeyEnabled(apiKey: ApiKey) {
        viewModelScope.launch {
            val newState = !apiKey.isActive
            outboundRepository.updateApiKey(apiKey.copy(isActive = newState))
            notificationService.showMessage(if (newState) "API Key enabled" else "API Key disabled")
        }
    }

    fun deleteApiKey(id: Long) {
        viewModelScope.launch {
            outboundRepository.deleteApiKey(id)
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            outboundRepository.deleteOutboundSms(id)
        }
    }

    fun updateApiKeyRateLimit(apiKey: ApiKey, perMinute: Int) {
        viewModelScope.launch {
            outboundRepository.updateApiKey(
                apiKey.copy(
                    smsPerMinute = perMinute
                )
            )
            notificationService.showMessage("Rate limit updated for ${apiKey.name}")
        }
    }

    fun deleteAllLogs() {
        viewModelScope.launch {
            outboundRepository.deleteAllOutboundSms()
        }
    }

    fun getBaseUrl(): String {
        return "https://${_tunnelServerUrl.value}/${_gatewayId.value}"
    }

    private var saveWebhookJob: Job? = null
    private var saveTunnelUrlJob: Job? = null

    fun updateWebhookUrl(url: String) {
        _webhookUrl.value = url
        saveWebhookJob?.cancel()
        saveWebhookJob = viewModelScope.launch {
            delay(500.milliseconds)
            val current = gatewayConfigRepository.getGatewayConfig()
            gatewayConfigRepository.saveGatewayConfig(current.copy(outboundWebhookUrl = url))
        }
    }

    fun updateTunnelServerUrl(url: String) {
        _tunnelServerUrl.value = url
        saveTunnelUrlJob?.cancel()
        saveTunnelUrlJob = viewModelScope.launch {
            delay(500.milliseconds)
            val current = gatewayConfigRepository.getGatewayConfig()
            gatewayConfigRepository.saveGatewayConfig(current.copy(tunnelServerUrl = url))
        }
    }
}
