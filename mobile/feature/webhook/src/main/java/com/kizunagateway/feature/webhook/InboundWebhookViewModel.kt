package com.kizunagateway.feature.webhook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.domain.model.*
import com.kizunagateway.domain.repository.WebhookRepository
import com.kizunagateway.domain.service.DeviceInfoProvider
import com.kizunagateway.domain.service.NotificationService
import com.kizunagateway.domain.usecase.DeliverWebhookUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WebhookTestResult(
    val success: Boolean,
    val responseCode: Int,
    val responseBody: String,
    val renderedBody: String,
    val sentHeaders: Map<String, String>
)

@HiltViewModel
class WebhookViewModel @Inject constructor(
    private val webhookRepository: WebhookRepository,
    private val ruleRepository: com.kizunagateway.domain.repository.RuleRepository,
    private val variableRepository: com.kizunagateway.domain.repository.VariableRepository,
    private val deliverWebhookUseCase: DeliverWebhookUseCase,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _webhooks = MutableStateFlow<List<Webhook>>(emptyList())
    val webhooks: StateFlow<List<Webhook>> = _webhooks.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    private val _testResult = MutableStateFlow<WebhookTestResult?>(null)
    val testResult: StateFlow<WebhookTestResult?> = _testResult.asStateFlow()

    private val _globalHeaders = MutableStateFlow<List<CustomVariable>>(emptyList())
    val globalHeaders: StateFlow<List<CustomVariable>> = _globalHeaders.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadWebhooks()
        loadGlobalHeaders()
        loadRules()
    }

    fun loadWebhooks() {
        viewModelScope.launch {
            webhookRepository.getWebhooksFlow().collect { list ->
                _webhooks.value = list
            }
        }
    }

    fun loadRules() {
        viewModelScope.launch {
            ruleRepository.getRulesFlow().collect { list ->
                _rules.value = list
            }
        }
    }

    fun loadGlobalHeaders() {
        viewModelScope.launch {
            variableRepository.getVariablesFlow().collect { list ->
                _globalHeaders.value = list
            }
        }
    }

    fun saveGlobalHeader(key: String, value: String) {
        viewModelScope.launch {
            variableRepository.insertVariable(CustomVariable(key, value))
            loadGlobalHeaders()
            notificationService.showMessage("Global header saved")
        }
    }

    fun deleteGlobalHeader(key: String) {
        viewModelScope.launch {
            variableRepository.deleteVariable(key)
            loadGlobalHeaders()
            notificationService.showMessage("Global header deleted")
        }
    }

    suspend fun saveWebhook(
        webhook: Webhook,
        headers: List<WebhookHeader>,
        bodyTemplate: String
    ): Boolean {
        if (webhook.id != 0L && !webhook.enabled) {
            // Trying to disable an existing webhook
            val rules = ruleRepository.getAllRules()
            val usedInRules = rules.filter { it.webhookId == webhook.id && it.enabled }
            if (usedInRules.isNotEmpty()) {
                val ruleNames = usedInRules.joinToString { it.name }
                notificationService.showMessage("Cannot disable webhook. It is used in active rules: $ruleNames")
                return false
            }
        }

        val id = if (webhook.id == 0L) {
            webhookRepository.insertWebhook(webhook)
        } else {
            webhookRepository.updateWebhook(webhook)
            webhook.id
        }

        // Save template
        webhookRepository.saveTemplate(WebhookTemplate(id, bodyTemplate))

        // Update headers (remove old and insert new, or simple overwrite)
        // For simplicity in this CRUD, we just delete all existing headers and insert current ones
        val existing = webhookRepository.getHeadersForWebhook(id)
        existing.forEach { webhookRepository.deleteHeader(it.id) }
        headers.forEach {
            webhookRepository.insertHeader(it.copy(webhookId = id))
        }
        loadWebhooks()
        notificationService.showMessage(if (webhook.id == 0L) "Webhook created" else "Webhook updated")
        return true
    }

    fun deleteWebhook(id: Long) {
        viewModelScope.launch {
            val rules = ruleRepository.getAllRules()
            val usedInRules = rules.filter { it.webhookId == id }
            
            if (usedInRules.isNotEmpty()) {
                val ruleNames = usedInRules.joinToString { it.name }
                notificationService.showMessage("Cannot delete webhook. It is used in rules: $ruleNames")
                return@launch
            }

            webhookRepository.deleteWebhook(id)
            loadWebhooks()
            notificationService.showMessage("Webhook deleted")
        }
    }

    fun toggleWebhookEnabled(webhook: Webhook) {
        viewModelScope.launch {
            if (webhook.enabled) {
                // Trying to disable
                val rules = ruleRepository.getAllRules()
                val usedInRules = rules.filter { it.webhookId == webhook.id && it.enabled }
                if (usedInRules.isNotEmpty()) {
                    val ruleNames = usedInRules.joinToString { it.name }
                    notificationService.showMessage("Cannot disable webhook. It is used in active rules: $ruleNames")
                    return@launch
                }
            }
            
            val newState = !webhook.enabled
            webhookRepository.updateWebhook(webhook.copy(enabled = newState))
            loadWebhooks()
            notificationService.showMessage(if (newState) "Webhook enabled" else "Webhook disabled")
        }
    }

    fun testWebhook(
        webhook: Webhook,
        headers: List<WebhookHeader>,
        bodyTemplate: String,
        sampleSender: String,
        sampleMessage: String
    ) {
        viewModelScope.launch {
            val deviceInfo = deviceInfoProvider.getDeviceInfo()
            val simInfo = deviceInfoProvider.getSimInfo(0)

            val sms = SmsMessage(
                sender = sampleSender,
                receiver = "Self",
                message = sampleMessage,
                receivedAt = java.time.Instant.now().toString()
            )

            val result = deliverWebhookUseCase(
                WebhookDeliveryInput(
                    sms = sms,
                    webhook = webhook,
                    deviceInfo = deviceInfo,
                    simInfo = simInfo,
                    webhookHeaders = headers,
                    bodyTemplate = bodyTemplate
                )
            )

            _testResult.value = WebhookTestResult(
                success = result.success,
                responseCode = result.responseCode,
                responseBody = result.responseBody,
                renderedBody = result.renderedBody,
                sentHeaders = result.renderedHeaders
            )
        }
    }

    fun clearTestResult() {
        _testResult.value = null
    }

    fun formatJson(json: String): String {
        return try {
            val obj = org.json.JSONObject(json)
            obj.toString(4)
        } catch (_: Exception) {
            try {
                val array = org.json.JSONArray(json)
                array.toString(4)
            } catch (e2: Exception) {
                json
            }
        }
    }

    suspend fun getHeaders(webhookId: Long) = webhookRepository.getHeadersForWebhook(webhookId)
    suspend fun getTemplate(webhookId: Long) = webhookRepository.getTemplateForWebhook(webhookId)
}
