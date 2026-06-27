package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.WebhookTemplate

@Entity(tableName = "webhook_template")
data class WebhookTemplateEntity(
    @PrimaryKey val webhookId: Long,
    val bodyTemplate: String
) {
    fun toDomain() = WebhookTemplate(webhookId, bodyTemplate)
    companion object {
        fun fromDomain(t: WebhookTemplate) = WebhookTemplateEntity(
            t.webhookId, t.bodyTemplate
        )
    }
}
