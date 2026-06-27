package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.Webhook

@Entity(tableName = "webhook")
data class WebhookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val enabled: Boolean,
    val url: String,
    val method: String,
    val timeoutSeconds: Int,
    val retryCount: Int,
    val autoReplyMessage: String? = null
) {
    fun toDomain() = Webhook(id, name, enabled, url, method, timeoutSeconds, retryCount, autoReplyMessage)
    companion object {
        fun fromDomain(w: Webhook) = WebhookEntity(
            w.id, w.name, w.enabled, w.url, w.method, w.timeoutSeconds, w.retryCount, w.autoReplyMessage
        )
    }
}
