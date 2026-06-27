package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.model.BackupPayload
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.OutboundRepository
import com.kizunagateway.domain.repository.RuleRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.VariableRepository
import com.kizunagateway.domain.repository.WebhookRepository
import kotlinx.coroutines.flow.first

class ImportBackupUseCase(
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val webhookRepository: WebhookRepository,
    private val ruleRepository: RuleRepository,
    private val variableRepository: VariableRepository,
    private val smsRepository: SmsRepository,
    private val logRepository: LogRepository,
    private val outboundRepository: OutboundRepository
) {
    suspend operator fun invoke(payload: BackupPayload) {
        webhookRepository.deleteAllWebhooks()
        ruleRepository.deleteAllRules()
        variableRepository.deleteAllVariables()
        smsRepository.deleteAllSms()
        logRepository.deleteAllLogs()
        outboundRepository.deleteAllOutboundSms()
        
        // API Keys don't have a deleteAll, we can iterate or skip delete if we want to merge.
        outboundRepository.getAllApiKeys().first().forEach { apiKey ->
            outboundRepository.deleteApiKey(apiKey.id)
        }

        gatewayConfigRepository.saveGatewayConfig(payload.gatewayConfig)

        payload.variables.forEach { variableRepository.insertVariable(it) }

        val oldToNewWebhookId = mutableMapOf<Long, Long>()
        payload.webhooks.forEach { webhook ->
            val oldId = webhook.id
            val newId = webhookRepository.insertWebhook(webhook.copy(id = 0))
            oldToNewWebhookId[oldId] = newId

            payload.headers.filter { it.webhookId == oldId }.forEach { header ->
                webhookRepository.insertHeader(header.copy(id = 0, webhookId = newId))
            }
            payload.templates.find { it.webhookId == oldId }?.let { template ->
                webhookRepository.saveTemplate(template.copy(webhookId = newId))
            }
        }

        payload.rules.forEach { rule ->
            val newWebhookId = oldToNewWebhookId[rule.webhookId] ?: 0L
            ruleRepository.insertRule(rule.copy(id = 0, webhookId = newWebhookId))
        }

        val oldToNewSmsId = mutableMapOf<Long, Long>()
        payload.smsMessages.forEach { sms ->
            val oldId = sms.id
            val newId = smsRepository.insertSms(sms.copy(id = 0))
            oldToNewSmsId[oldId] = newId
        }

        payload.deliveryLogs.forEach { log ->
            val newSmsId = oldToNewSmsId[log.smsId] ?: return@forEach
            val newWebhookId = oldToNewWebhookId[log.webhookId] ?: 0L
            logRepository.insertLog(
                log.copy(
                    id = 0,
                    smsId = newSmsId,
                    webhookId = newWebhookId
                )
            )
        }

        payload.apiKeys.forEach {
            outboundRepository.insertApiKey(it.copy(id = 0))
        }

        payload.outboundSms.forEach {
            outboundRepository.insertOutboundSms(it.copy(id = 0))
        }
    }
}
