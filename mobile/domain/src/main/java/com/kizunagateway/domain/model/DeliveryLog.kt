package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryLog (
    val id: Long = 0,
    val smsId: Long,
    val webhookId: Long,
    val requestHeaders: String = "",
    val requestBody: String,
    val responseBody: String,
    val responseCode: Int,
    val success: Boolean,
    val retryCount: Int,
    val errorMessage: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
