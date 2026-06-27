package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.DeliveryLog

@Entity(tableName = "delivery_log")
data class DeliveryLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val smsId: Long,
    val webhookId: Long,
    val requestHeaders: String,
    val requestBody: String,
    val responseBody: String,
    val responseCode: Int,
    val success: Boolean,
    val retryCount: Int,
    val errorMessage: String?,
    val createdAt: Long
) {
    fun toDomain() = DeliveryLog(id, smsId, webhookId, requestHeaders, requestBody, responseBody, responseCode, success, retryCount, errorMessage, createdAt)
    companion object {
        fun fromDomain(l: DeliveryLog) = DeliveryLogEntity(
            l.id, l.smsId, l.webhookId, l.requestHeaders, l.requestBody, l.responseBody, l.responseCode, l.success, l.retryCount, l.errorMessage, l.createdAt
        )
    }
}
