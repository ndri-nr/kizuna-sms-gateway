package com.kizunagateway.core.data.repository

import com.kizunagateway.core.database.dao.LogDao
import com.kizunagateway.core.database.entity.DeliveryLogEntity
import com.kizunagateway.domain.model.DeliveryLog
import com.kizunagateway.domain.repository.LogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.time.Duration.Companion.milliseconds

class LogRepositoryImpl(private val logDao: LogDao) : LogRepository {
    override suspend fun insertLog(log: DeliveryLog): Long {
        return logDao.insertLog(DeliveryLogEntity.fromDomain(log))
    }

    override suspend fun getAllLogs(): List<DeliveryLog> {
        return logDao.getAllLogs().map { it.toDomain() }
    }

    override fun getLogsFlow(limit: Int): Flow<List<DeliveryLog>> {
        return logDao.getLogsFlow(limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLogsPaged(limit: Int, offset: Int): List<DeliveryLog> {
        return logDao.getLogsPaged(limit, offset).map { it.toDomain() }
    }

    private val dayFlow = flow {
        while (true) {
            emit(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
            val now = LocalDateTime.now()
            val nextMidnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0)
            delay(Duration.between(now, nextMidnight).toMillis().milliseconds)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSuccessCountTodayFlow(): Flow<Int> {
        return dayFlow.flatMapLatest { startOfDay ->
            logDao.getSuccessCountTodayFlow(startOfDay)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFailedCountTodayFlow(): Flow<Int> {
        return dayFlow.flatMapLatest { startOfDay ->
            logDao.getFailedCountTodayFlow(startOfDay)
        }
    }

    override fun getLastSuccessfulLogFlow(): Flow<DeliveryLog?> {
        return logDao.getLastSuccessfulLogFlow().map { it?.toDomain() }
    }

    override suspend fun clearOldLogs(beforeTimestamp: Long) {
        logDao.clearOldLogs(beforeTimestamp)
    }

    override suspend fun deleteAllLogs() {
        logDao.deleteAllLogs()
    }

    override suspend fun deleteLogById(id: Long) {
        logDao.deleteLogById(id)
    }

    override suspend fun deleteLogsBySmsId(smsId: Long) {
        logDao.deleteLogsBySmsId(smsId)
    }
}
