package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.WebhookHeader

@Entity(tableName = "webhook_header")
data class WebhookHeaderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val webhookId: Long,
    val key: String,
    val value: String
) {
    fun toDomain() = WebhookHeader(id, webhookId, key, value)
    companion object {
        fun fromDomain(h: WebhookHeader) = WebhookHeaderEntity(
            h.id, h.webhookId, h.key, h.value
        )
    }
}
