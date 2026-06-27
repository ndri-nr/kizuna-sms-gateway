package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable
import com.kizunagateway.domain.util.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class OutboundSms(
    val id: Long = 0,
    val phoneNumber: String,
    val message: String,
    val simSlot: Int = 0,
    val status: OutboundSmsStatus = OutboundSmsStatus.PENDING,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = LocalDateTimeSerializer::class)
    val sentAt: LocalDateTime? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val deliveredAt: LocalDateTime? = null,
    val errorMessage: String? = null,
    val messageId: String? = null, // External ID or Android SMS ID
    val webhookUrl: String? = null
)

@Serializable
enum class OutboundSmsStatus {
    PENDING,
    SENDING,
    SENT,
    FAILED,
    DELIVERED
}
