package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.ApiKey
import com.kizunagateway.domain.model.OutboundSms
import com.kizunagateway.domain.model.OutboundSmsStatus
import kotlinx.coroutines.flow.Flow

interface OutboundRepository {
    // SMS Management
    fun getAllOutboundSms(): Flow<List<OutboundSms>>
    suspend fun getOutboundSmsById(id: Long): OutboundSms?
    suspend fun getSmsByStatus(status: OutboundSmsStatus): List<OutboundSms>
    suspend fun insertOutboundSms(sms: OutboundSms): Long
    suspend fun updateOutboundSms(sms: OutboundSms)
    suspend fun deleteOutboundSms(id: Long)
    suspend fun deleteAllOutboundSms()
    fun getPendingCount(): Flow<Int>
    suspend fun getSmsCountByKeyInTimeRange(apiKey: String, startTime: java.time.LocalDateTime): Int

    // API Key Management
    fun getAllApiKeys(): Flow<List<ApiKey>>
    suspend fun getActiveKey(key: String): ApiKey?
    suspend fun insertApiKey(apiKey: ApiKey)
    suspend fun updateApiKey(apiKey: ApiKey)
    suspend fun deleteApiKey(id: Long)
}
