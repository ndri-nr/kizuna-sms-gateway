package com.kizunagateway.feature.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kizunagateway.domain.model.DeliveryLog
import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.model.WebhookDeliveryInput
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.WebhookRepository
import com.kizunagateway.domain.service.DeviceInfoProvider
import com.kizunagateway.domain.usecase.DeliverWebhookUseCase
import com.kizunagateway.domain.usecase.MatchRuleUseCase
import com.kizunagateway.domain.usecase.SendAutoReplyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

data class GroupedDeliveryLog(
    val smsId: Long,
    val logs: List<DeliveryLog>
) {
    val latestLog: DeliveryLog get() = logs.first()
    val success: Boolean get() = logs.any { it.success }
}

@HiltViewModel
class LogsViewModel @Inject constructor(
    private val smsRepository: SmsRepository,
    private val logRepository: LogRepository,
    private val webhookRepository: WebhookRepository,
    private val deliverWebhookUseCase: DeliverWebhookUseCase,
    private val sendAutoReplyUseCase: SendAutoReplyUseCase,
    private val matchRuleUseCase: MatchRuleUseCase,
    private val deviceInfoProvider: DeviceInfoProvider
) : ViewModel() {

    private val _logsLimit = MutableStateFlow(50)
    private val _smsLimit = MutableStateFlow(50)

    private val _logs = MutableStateFlow<List<DeliveryLog>>(emptyList())
    val logs: StateFlow<List<DeliveryLog>> = _logs.asStateFlow()

    private val _groupedLogs = MutableStateFlow<List<GroupedDeliveryLog>>(emptyList())
    val groupedLogs: StateFlow<List<GroupedDeliveryLog>> = _groupedLogs.asStateFlow()

    private val _smsList = MutableStateFlow<List<SmsMessage>>(emptyList())
    val smsList: StateFlow<List<SmsMessage>> = _smsList.asStateFlow()

    private val _webhookNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val webhookNames: StateFlow<Map<Long, String>> = _webhookNames.asStateFlow()

    private val _hasMoreLogs = MutableStateFlow(true)
    val hasMoreLogs: StateFlow<Boolean> = _hasMoreLogs.asStateFlow()

    private val _hasMoreSms = MutableStateFlow(true)
    val hasMoreSms: StateFlow<Boolean> = _hasMoreSms.asStateFlow()

    init {
        loadLogs()
        loadWebhooks()
    }

    private fun loadWebhooks() {
        viewModelScope.launch {
            webhookRepository.getWebhooksFlow().collect { list ->
                _webhookNames.value = list.associate { it.id to it.name }
            }
        }
    }

    fun loadLogs() {
        viewModelScope.launch {
            _logsLimit.collect { limit ->
                logRepository.getLogsFlow(limit).collect { list ->
                    _logs.value = list
                    _groupedLogs.value = list.groupBy { it.smsId }
                        .map { (smsId, logs) ->
                            GroupedDeliveryLog(
                                smsId = smsId,
                                logs = logs.sortedByDescending { it.createdAt }
                            )
                        }.sortedByDescending { it.latestLog.createdAt }
                    
                    // Simple pagination check: if we got less than requested, no more data
                    _hasMoreLogs.value = list.size >= limit
                }
            }
        }
        viewModelScope.launch {
            _smsLimit.collect { limit ->
                smsRepository.getSmsListFlow(limit).collect { list ->
                    _smsList.value = list
                    _hasMoreSms.value = list.size >= limit
                }
            }
        }
    }

    fun loadMoreLogs() {
        _logsLimit.value += 50
    }

    fun loadMoreSms() {
        _smsLimit.value += 50
    }

    fun formatJson(json: String): String {
        return try {
            val obj = JSONObject(json)
            obj.toString(4)
        } catch (e: Exception) {
            try {
                val array = JSONArray(json)
                array.toString(4)
            } catch (e2: Exception) {
                json
            }
        }
    }

    fun formatHeaders(json: String): String {
        return try {
            val type = object : TypeToken<Map<String, String>>() {}.type
            val map: Map<String, String> = Gson().fromJson(json, type)
            map.entries.joinToString("\n") { "${it.key}: ${it.value}" }
        } catch (e: Exception) {
            json
        }
    }

    fun retryDelivery(log: DeliveryLog) {
        viewModelScope.launch {
            val sms = smsRepository.getSmsById(log.smsId) ?: return@launch
            
            var targetWebhookId = log.webhookId
            if (targetWebhookId == 0L) {
                targetWebhookId = matchRuleUseCase(sms.sender, sms.message) ?: 0L
            }

            if (targetWebhookId == 0L) {
                // Still no match
                logRepository.insertLog(
                    DeliveryLog(
                        smsId = log.smsId,
                        webhookId = 0,
                        requestBody = "No matching webhook rules found (Retry)",
                        responseBody = "Rules re-checked, still no match.",
                        responseCode = 0,
                        success = false,
                        retryCount = log.retryCount + 1
                    )
                )
                return@launch
            }

            val webhook = webhookRepository.getWebhookById(targetWebhookId) ?: return@launch
            if (!webhook.enabled) {
                logRepository.insertLog(
                    DeliveryLog(
                        smsId = log.smsId,
                        webhookId = webhook.id,
                        requestBody = "Retry failed: Webhook '${webhook.name}' is disabled.",
                        responseBody = "Enable the webhook to retry delivery.",
                        responseCode = 0,
                        success = false,
                        retryCount = log.retryCount + 1
                    )
                )
                return@launch
            }

            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val simInfo = deviceInfoProvider.getSimInfo(sms.simSlot)

            val result = deliverWebhookUseCase(
                WebhookDeliveryInput(
                    sms = sms,
                    webhook = webhook,
                    deviceInfo = deviceInfo,
                    simInfo = simInfo
                )
            )

            if (result.responseCode == 200) {
                sendAutoReplyUseCase(webhook, sms, deviceInfo, simInfo)
            }

            logRepository.insertLog(
                DeliveryLog(
                    smsId = log.smsId,
                    webhookId = targetWebhookId,
                    requestHeaders = Gson().toJson(result.renderedHeaders),
                    requestBody = result.renderedBody,
                    responseBody = result.responseBody,
                    responseCode = result.responseCode,
                    success = result.success,
                    retryCount = log.retryCount + 1
                )
            )
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            // Clear all logs before current time (clear all)
            logRepository.clearOldLogs(System.currentTimeMillis() + 1000)
            loadLogs()
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            logRepository.deleteLogById(id)
        }
    }

    fun deleteGroupedLogs(smsId: Long) {
        viewModelScope.launch {
            logRepository.deleteLogsBySmsId(smsId)
        }
    }

    fun deleteSms(id: Long) {
        viewModelScope.launch {
            smsRepository.deleteSmsById(id)
        }
    }

    fun clearAllSms() {
        viewModelScope.launch {
            smsRepository.deleteAllSms()
        }
    }
}
