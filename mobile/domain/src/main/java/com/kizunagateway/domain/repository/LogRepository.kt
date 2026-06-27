package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.DeliveryLog
import kotlinx.coroutines.flow.Flow

interface LogRepository {
    suspend fun insertLog(log: DeliveryLog): Long
    suspend fun getAllLogs(): List<DeliveryLog>
    fun getLogsFlow(limit: Int = 100): Flow<List<DeliveryLog>>
    suspend fun getLogsPaged(limit: Int, offset: Int): List<DeliveryLog>
    fun getSuccessCountTodayFlow(): Flow<Int>
    fun getFailedCountTodayFlow(): Flow<Int>
    fun getLastSuccessfulLogFlow(): Flow<DeliveryLog?>
    suspend fun clearOldLogs(beforeTimestamp: Long)
    suspend fun deleteAllLogs()
    suspend fun deleteLogById(id: Long)
    suspend fun deleteLogsBySmsId(smsId: Long)
}
