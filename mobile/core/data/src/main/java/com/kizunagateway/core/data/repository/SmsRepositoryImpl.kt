package com.kizunagateway.core.data.repository

import com.kizunagateway.core.database.dao.SmsDao
import com.kizunagateway.core.database.entity.SmsMessageEntity
import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.repository.SmsRepository
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

class SmsRepositoryImpl(private val smsDao: SmsDao) : SmsRepository {
    override suspend fun insertSms(sms: SmsMessage): Long {
        return smsDao.insertSms(SmsMessageEntity.fromDomain(sms))
    }

    override suspend fun updateSms(sms: SmsMessage) {
        smsDao.updateSms(SmsMessageEntity.fromDomain(sms))
    }

    override suspend fun getSmsById(id: Long): SmsMessage? {
        return smsDao.getSmsById(id)?.toDomain()
    }

    override suspend fun getPendingSms(): List<SmsMessage> {
        return smsDao.getPendingSms().map { it.toDomain() }
    }

    override fun getSmsListFlow(limit: Int): Flow<List<SmsMessage>> {
        return smsDao.getSmsListFlow(limit).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSmsPaged(limit: Int, offset: Int): List<SmsMessage> {
        return smsDao.getSmsPaged(limit, offset).map { it.toDomain() }
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
    override fun getSmsTodayCountFlow(): Flow<Int> {
        return dayFlow.flatMapLatest { startOfDay ->
            smsDao.getSmsTodayCountFlow(startOfDay)
        }
    }

    override suspend fun getAllSms(): List<SmsMessage> {
        return smsDao.getAllSms().map { it.toDomain() }
    }

    override suspend fun deleteAllSms() {
        smsDao.deleteAllSms()
    }

    override suspend fun deleteSmsById(id: Long) {
        smsDao.deleteSmsById(id)
    }
}
