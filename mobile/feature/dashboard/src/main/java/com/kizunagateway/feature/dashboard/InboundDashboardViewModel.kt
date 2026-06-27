package com.kizunagateway.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.domain.model.DeliveryLog
import com.kizunagateway.domain.model.GatewayConfig
import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.model.Webhook
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.WebhookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val gatewayConfig: GatewayConfig? = null,
    val smsToday: Int = 0,
    val webhookSuccess: Int = 0,
    val webhookFailed: Int = 0,
    val queueSize: Int = 0,
    val lastSms: SmsMessage? = null,
    val lastSuccessLog: DeliveryLog? = null,
    val lastWebhookName: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val logRepository: LogRepository,
    private val webhookRepository: WebhookRepository,
    private val gatewayConfigRepository: GatewayConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var collectionJob: Job? = null

    fun loadDashboardData() {
        if (collectionJob != null) return
        
        collectionJob = viewModelScope.launch {
            // Ensure config exists
            gatewayConfigRepository.getGatewayConfig()

            combine(
                gatewayConfigRepository.getGatewayConfigFlow(),
                smsRepository.getSmsListFlow(50),
                smsRepository.getSmsTodayCountFlow(),
                logRepository.getSuccessCountTodayFlow(),
                logRepository.getFailedCountTodayFlow(),
                logRepository.getLastSuccessfulLogFlow(),
                webhookRepository.getWebhooksFlow()
            ) { args ->
                val config = args[0] as GatewayConfig
                val smsList = args[1] as List<SmsMessage>
                val smsCount = args[2] as Int
                val successCount = args[3] as Int
                val failedCount = args[4] as Int
                val lastLog = args[5] as? DeliveryLog
                val webhooks = args[6] as List<Webhook>

                val lastWebhookName = lastLog?.let { log ->
                    webhooks.find { it.id == log.webhookId }?.name
                }
                
                DashboardUiState(
                    gatewayConfig = config,
                    smsToday = smsCount,
                    webhookSuccess = successCount,
                    webhookFailed = failedCount,
                    queueSize = smsList.count { !it.processed },
                    lastSms = smsList.firstOrNull(),
                    lastSuccessLog = lastLog,
                    lastWebhookName = lastWebhookName
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
