package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.SmsMessage
import kotlinx.coroutines.flow.Flow

interface SmsRepository {
    suspend fun insertSms(sms: SmsMessage): Long
    suspend fun updateSms(sms: SmsMessage)
    suspend fun getSmsById(id: Long): SmsMessage?
    suspend fun getPendingSms(): List<SmsMessage>
    fun getSmsListFlow(limit: Int = 100): Flow<List<SmsMessage>>
    fun getSmsTodayCountFlow(): Flow<Int>
    suspend fun getSmsPaged(limit: Int, offset: Int): List<SmsMessage>
    suspend fun getAllSms(): List<SmsMessage>
    suspend fun deleteAllSms()
    suspend fun deleteSmsById(id: Long)
}
