package com.kizunagateway.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kizunagateway.core.database.entity.WebhookEntity
import com.kizunagateway.core.database.entity.WebhookHeaderEntity
import com.kizunagateway.core.database.entity.WebhookTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebhookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWebhook(webhook: WebhookEntity): Long

    @Update
    suspend fun updateWebhook(webhook: WebhookEntity)

    @Query("DELETE FROM webhook WHERE id = :webhookId")
    suspend fun deleteWebhook(webhookId: Long)

    @Query("SELECT * FROM webhook WHERE id = :id")
    suspend fun getWebhookById(id: Long): WebhookEntity?

    @Query("SELECT * FROM webhook")
    suspend fun getAllWebhooks(): List<WebhookEntity>

    @Query("SELECT * FROM webhook")
    fun getWebhooksFlow(): Flow<List<WebhookEntity>>

    // Headers
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeader(header: WebhookHeaderEntity): Long

    @Query("DELETE FROM webhook_header WHERE id = :headerId")
    suspend fun deleteHeader(headerId: Long)

    @Query("SELECT * FROM webhook_header WHERE webhookId = :webhookId")
    suspend fun getHeadersForWebhook(webhookId: Long): List<WebhookHeaderEntity>

    // Templates
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTemplate(template: WebhookTemplateEntity)

    @Query("SELECT * FROM webhook_template WHERE webhookId = :webhookId")
    suspend fun getTemplateForWebhook(webhookId: Long): WebhookTemplateEntity?

    @Query("DELETE FROM webhook")
    suspend fun deleteAllWebhooks()

    @Query("DELETE FROM webhook_header")
    suspend fun deleteAllHeaders()

    @Query("DELETE FROM webhook_template")
    suspend fun deleteAllTemplates()
}
