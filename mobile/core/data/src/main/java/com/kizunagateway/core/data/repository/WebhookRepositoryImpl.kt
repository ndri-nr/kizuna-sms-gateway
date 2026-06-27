package com.kizunagateway.core.data.repository

import com.kizunagateway.core.database.dao.WebhookDao
import com.kizunagateway.core.database.entity.WebhookEntity
import com.kizunagateway.core.database.entity.WebhookHeaderEntity
import com.kizunagateway.core.database.entity.WebhookTemplateEntity
import com.kizunagateway.domain.model.Webhook
import com.kizunagateway.domain.model.WebhookHeader
import com.kizunagateway.domain.model.WebhookTemplate
import com.kizunagateway.domain.repository.WebhookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WebhookRepositoryImpl(private val webhookDao: WebhookDao) : WebhookRepository {
    override suspend fun insertWebhook(webhook: Webhook): Long {
        return webhookDao.insertWebhook(WebhookEntity.fromDomain(webhook))
    }

    override suspend fun updateWebhook(webhook: Webhook) {
        webhookDao.updateWebhook(WebhookEntity.fromDomain(webhook))
    }

    override suspend fun deleteWebhook(webhookId: Long) {
        webhookDao.deleteWebhook(webhookId)
    }

    override suspend fun getWebhookById(id: Long): Webhook? {
        return webhookDao.getWebhookById(id)?.toDomain()
    }

    override suspend fun getAllWebhooks(): List<Webhook> {
        return webhookDao.getAllWebhooks().map { it.toDomain() }
    }

    override fun getWebhooksFlow(): Flow<List<Webhook>> {
        return webhookDao.getWebhooksFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertHeader(header: WebhookHeader): Long {
        return webhookDao.insertHeader(WebhookHeaderEntity.fromDomain(header))
    }

    override suspend fun deleteHeader(headerId: Long) {
        webhookDao.deleteHeader(headerId)
    }

    override suspend fun getHeadersForWebhook(webhookId: Long): List<WebhookHeader> {
        return webhookDao.getHeadersForWebhook(webhookId).map { it.toDomain() }
    }

    override suspend fun saveTemplate(template: WebhookTemplate) {
        webhookDao.saveTemplate(WebhookTemplateEntity.fromDomain(template))
    }

    override suspend fun getTemplateForWebhook(webhookId: Long): WebhookTemplate? {
        return webhookDao.getTemplateForWebhook(webhookId)?.toDomain()
    }

    override suspend fun deleteAllWebhooks() {
        webhookDao.deleteAllWebhooks()
        webhookDao.deleteAllHeaders()
        webhookDao.deleteAllTemplates()
    }
}
