package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SmsMessage (
    val id: Long = 0,
    val sourceType: String = "SMS",
    val sender: String,
    val receiver: String,
    val message: String,
    val receivedAt: String,
    val simSlot: Int = 0,
    val processed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
