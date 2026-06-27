package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GatewayConfig (
    val gatewayId: String,
    val gatewayName: String,
    val deviceSecret: String,
    val deleteUntrackedSms: Boolean = false,
    val outboundWebhookUrl: String? = null,
    val tunnelServerUrl: String = "sms-gateway.artivy.id"
)
