package com.kizunagateway.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SmsRequest(
    val phoneNumber: String,
    val message: String,
    val simSlot: Int? = null,
    val webhookUrl: String? = null
)

@Serializable
data class BatchSmsRequest(
    val messages: List<SmsRequest>
)
