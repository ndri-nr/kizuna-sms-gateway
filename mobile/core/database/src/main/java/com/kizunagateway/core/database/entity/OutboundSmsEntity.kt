package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.OutboundSms
import com.kizunagateway.domain.model.OutboundSmsStatus
import java.time.LocalDateTime

@Entity(tableName = "outbound_sms")
data class OutboundSmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val message: String,
    val simSlot: Int,
    val status: OutboundSmsStatus,
    val createdAt: LocalDateTime,
    val sentAt: LocalDateTime?,
    val deliveredAt: LocalDateTime?,
    val errorMessage: String?,
    val messageId: String?,
    val webhookUrl: String?
)

fun OutboundSmsEntity.toDomain() = OutboundSms(
    id = id,
    phoneNumber = phoneNumber,
    message = message,
    simSlot = simSlot,
    status = status,
    createdAt = createdAt,
    sentAt = sentAt,
    deliveredAt = deliveredAt,
    errorMessage = errorMessage,
    messageId = messageId,
    webhookUrl = webhookUrl
)

fun OutboundSms.toEntity() = OutboundSmsEntity(
    id = id,
    phoneNumber = phoneNumber,
    message = message,
    simSlot = simSlot,
    status = status,
    createdAt = createdAt,
    sentAt = sentAt,
    deliveredAt = deliveredAt,
    errorMessage = errorMessage,
    messageId = messageId,
    webhookUrl = webhookUrl
)
