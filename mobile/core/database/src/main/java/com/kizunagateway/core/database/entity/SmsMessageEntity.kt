package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.SmsMessage

@Entity(tableName = "sms_message")
data class SmsMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceType: String,
    val sender: String,
    val receiver: String,
    val message: String,
    val receivedAt: String,
    val simSlot: Int,
    val processed: Boolean,
    val createdAt: Long
) {
    fun toDomain() = SmsMessage(id, sourceType, sender, receiver, message, receivedAt, simSlot, processed, createdAt)
    companion object {
        fun fromDomain(sms: SmsMessage) = SmsMessageEntity(
            sms.id, sms.sourceType, sms.sender, sms.receiver, sms.message, sms.receivedAt, sms.simSlot, sms.processed, sms.createdAt
        )
    }
}
