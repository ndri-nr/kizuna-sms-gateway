package com.kizunagateway.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class SmsResponse(
    val id: Long,
    val status: String,
    val message: String? = null
)

@Serializable
data class QueueResponse(
    val pendingCount: Int,
    val messages: List<SmsResponse>
)
