package com.kizunagateway.feature.rules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.core.ui.R
import com.kizunagateway.domain.model.Rule
import com.kizunagateway.domain.model.Webhook
import com.kizunagateway.domain.service.NotificationService
import com.kizunagateway.domain.repository.RuleRepository
import com.kizunagateway.domain.repository.WebhookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboundRulesViewModel @Inject constructor(
    private val ruleRepository: RuleRepository,
    private val webhookRepository: WebhookRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    private val _webhooks = MutableStateFlow<List<Webhook>>(emptyList())
    val webhooks: StateFlow<List<Webhook>> = _webhooks.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            ruleRepository.getRulesFlow().collect { list ->
                _rules.value = list
            }
        }
        viewModelScope.launch {
            webhookRepository.getWebhooksFlow().collect { list ->
                _webhooks.value = list
            }
        }
    }

    suspend fun saveRule(rule: Rule): Boolean {
        if (rule.enabled) {
            val webhook = webhookRepository.getWebhookById(rule.webhookId)
            if (webhook == null || !webhook.enabled) {
                notificationService.showMessage(R.string.cannot_enable_rule_webhook_error)
                return false
            }
        }

        if (rule.id == 0L) {
            ruleRepository.insertRule(rule)
            notificationService.showMessage(R.string.rule_created)
        } else {
            ruleRepository.updateRule(rule)
            notificationService.showMessage(R.string.rule_updated)
        }
        loadData()
        return true
    }

    fun deleteRule(id: Long) {
        viewModelScope.launch {
            ruleRepository.deleteRule(id)
            loadData()
            notificationService.showMessage(R.string.rule_deleted)
        }
    }

    fun toggleRuleEnabled(rule: Rule) {
        viewModelScope.launch {
            if (!rule.enabled) { // Trying to enable
                val webhook = webhookRepository.getWebhookById(rule.webhookId)
                if (webhook == null || !webhook.enabled) {
                    notificationService.showMessage(R.string.cannot_enable_rule_webhook_error)
                    return@launch
                }
            }
            val newState = !rule.enabled
            ruleRepository.updateRule(rule.copy(enabled = newState))
            loadData()
            notificationService.showMessage(if (newState) R.string.rule_enabled else R.string.rule_disabled)
        }
    }
}
