package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.repository.RuleRepository
import com.kizunagateway.domain.repository.WebhookRepository
import java.util.regex.Pattern

class MatchRuleUseCase(
    private val ruleRepository: RuleRepository,
    private val webhookRepository: WebhookRepository
) {
    suspend operator fun invoke(sender: String, message: String): Long? {
        val rules = ruleRepository.getAllRules()
            .filter { it.enabled }
            .sortedByDescending { it.priority }

        val webhooks = webhookRepository.getAllWebhooks().associateBy { it.id }

        for (rule in rules) {
            val webhook = webhooks[rule.webhookId]
            if (webhook == null || !webhook.enabled) continue

            val matchesRegex = if (!rule.senderRegex.isNullOrBlank()) {
                try {
                    Pattern.compile(rule.senderRegex).matcher(sender).matches()
                } catch (e: Exception) {
                    false
                }
            } else {
                null
            }

            val matchesContains = if (!rule.containsText.isNullOrBlank()) {
                message.contains(rule.containsText, ignoreCase = true)
            } else {
                null
            }

            // A rule matches if:
            // 1. Regex is specified and matches
            // 2. Contains is specified and matches
            // 3. Both are specified and both match (or if either matches based on policy; let's say if both are specified, both must match. If only one is specified, it must match. If none, it is catch-all)
            val isMatch = when {
                matchesRegex != null && matchesContains != null -> matchesRegex && matchesContains
                matchesRegex != null -> matchesRegex
                matchesContains != null -> matchesContains
                else -> true // Catch-all rule
            }

            if (isMatch) {
                return rule.webhookId
            }
        }
        return null
    }
}
