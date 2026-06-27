package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.Webhook
import com.kizunagateway.domain.model.WebhookHeader
import com.kizunagateway.domain.model.WebhookTemplate
import kotlinx.coroutines.flow.Flow

interface WebhookRepository {
    suspend fun insertWebhook(webhook: Webhook): Long
    suspend fun updateWebhook(webhook: Webhook)
    suspend fun deleteWebhook(webhookId: Long)
    suspend fun getWebhookById(id: Long): Webhook?
    suspend fun getAllWebhooks(): List<Webhook>
    fun getWebhooksFlow(): Flow<List<Webhook>>
    suspend fun insertHeader(header: WebhookHeader): Long
    suspend fun deleteHeader(headerId: Long)
    suspend fun getHeadersForWebhook(webhookId: Long): List<WebhookHeader>
    suspend fun saveTemplate(template: WebhookTemplate)
    suspend fun getTemplateForWebhook(webhookId: Long): WebhookTemplate?
    suspend fun deleteAllWebhooks()
}
