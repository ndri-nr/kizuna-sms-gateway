package com.kizunagateway.domain.model

data class WebhookDeliveryInput(
    val sms: SmsMessage,
    val webhook: Webhook,
    val deviceInfo: DeviceInfo,
    val simInfo: SimInfo,
    val webhookHeaders: List<WebhookHeader>? = null,
    val bodyTemplate: String? = null
)

data class WebhookDeliveryResult(
    val success: Boolean,
    val responseCode: Int,
    val responseBody: String,
    val renderedUrl: String,
    val renderedBody: String,
    val renderedHeaders: Map<String, String>,
    val isRetryableError: Boolean,
    val errorMessage: String? = null
)
