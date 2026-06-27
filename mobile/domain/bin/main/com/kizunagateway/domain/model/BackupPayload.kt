package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val gatewayConfig: GatewayConfig,
    val webhooks: List<Webhook>,
    val headers: List<WebhookHeader>,
    val templates: List<WebhookTemplate>,
    val rules: List<Rule>,
    val variables: List<CustomVariable>,
    val smsMessages: List<SmsMessage> = emptyList(),
    val deliveryLogs: List<DeliveryLog> = emptyList(),
    val apiKeys: List<ApiKey> = emptyList(),
    val outboundSms: List<OutboundSms> = emptyList()
)
