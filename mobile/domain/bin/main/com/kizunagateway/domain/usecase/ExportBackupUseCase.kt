package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.model.BackupPayload
import com.kizunagateway.domain.model.WebhookHeader
import com.kizunagateway.domain.model.WebhookTemplate
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.OutboundRepository
import com.kizunagateway.domain.repository.RuleRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.VariableRepository
import com.kizunagateway.domain.repository.WebhookRepository
import kotlinx.coroutines.flow.first

class ExportBackupUseCase(
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val webhookRepository: WebhookRepository,
    private val ruleRepository: RuleRepository,
    private val variableRepository: VariableRepository,
    private val smsRepository: SmsRepository,
    private val logRepository: LogRepository,
    private val outboundRepository: OutboundRepository
) {
    suspend operator fun invoke(): BackupPayload {
        val config = gatewayConfigRepository.getGatewayConfig()
        val webhooks = webhookRepository.getAllWebhooks()
        val rules = ruleRepository.getAllRules()
        val variables = variableRepository.getAllVariables()
        val smsMessages = smsRepository.getAllSms()
        val deliveryLogs = logRepository.getAllLogs()

        // For simplicity, we convert Flow to List for backup.
        // In a real app, you might want to use a more efficient way if there are many logs.
        // But for backup, this is usually okay.
        
        // Note: OutboundRepository uses Flows for list queries.
        // We'll use a helper to get current values if possible, or use first()
        val currentApiKeys = outboundRepository.getAllApiKeys().first()
        val currentOutboundLogs = outboundRepository.getAllOutboundSms().first()

        val allHeaders = mutableListOf<WebhookHeader>()
        val allTemplates = mutableListOf<WebhookTemplate>()

        webhooks.forEach { webhook ->
            allHeaders.addAll(webhookRepository.getHeadersForWebhook(webhook.id))
            webhookRepository.getTemplateForWebhook(webhook.id)?.let { allTemplates.add(it) }
        }

        return BackupPayload(
            gatewayConfig = config,
            webhooks = webhooks,
            headers = allHeaders,
            templates = allTemplates,
            rules = rules,
            variables = variables,
            smsMessages = smsMessages,
            deliveryLogs = deliveryLogs,
            apiKeys = currentApiKeys,
            outboundSms = currentOutboundLogs
        )
    }
}
