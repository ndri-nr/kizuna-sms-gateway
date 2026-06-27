package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Webhook (
    val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val url: String,
    val method: String = "POST",
    val timeoutSeconds: Int = 30,
    val retryCount: Int = 5,
    val autoReplyMessage: String? = null
)

@Serializable
data class WebhookHeader (
    val id: Long = 0,
    val webhookId: Long,
    val key: String,
    val value: String
)

@Serializable
data class WebhookTemplate (
    val webhookId: Long,
    val bodyTemplate: String
)
