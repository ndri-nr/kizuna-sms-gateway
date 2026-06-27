package com.kizunagateway.core.data.repository

import com.kizunagateway.core.database.dao.ApiKeyDao
import com.kizunagateway.core.database.dao.OutboundSmsDao
import com.kizunagateway.core.database.entity.toDomain
import com.kizunagateway.core.database.entity.toEntity
import com.kizunagateway.domain.model.ApiKey
import com.kizunagateway.domain.model.OutboundSms
import com.kizunagateway.domain.model.OutboundSmsStatus
import com.kizunagateway.domain.repository.OutboundRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OutboundRepositoryImpl @Inject constructor(
    private val outboundSmsDao: OutboundSmsDao,
    private val apiKeyDao: ApiKeyDao
) : OutboundRepository {

    override fun getAllOutboundSms(): Flow<List<OutboundSms>> {
        return outboundSmsDao.getAllOutboundSms().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getOutboundSmsById(id: Long): OutboundSms? {
        return outboundSmsDao.getOutboundSmsById(id)?.toDomain()
    }

    override suspend fun getSmsByStatus(status: OutboundSmsStatus): List<OutboundSms> {
        return outboundSmsDao.getSmsByStatus(status).map { it.toDomain() }
    }

    override suspend fun insertOutboundSms(sms: OutboundSms): Long {
        return outboundSmsDao.insertOutboundSms(sms.toEntity())
    }

    override suspend fun updateOutboundSms(sms: OutboundSms) {
        outboundSmsDao.updateOutboundSms(sms.toEntity())
    }

    override suspend fun deleteOutboundSms(id: Long) {
        outboundSmsDao.deleteOutboundSms(id)
    }

    override suspend fun deleteAllOutboundSms() {
        outboundSmsDao.deleteAllOutboundSms()
    }

    override fun getPendingCount(): Flow<Int> {
        return outboundSmsDao.getPendingCount()
    }

    override fun getAllApiKeys(): Flow<List<ApiKey>> {
        return apiKeyDao.getAllApiKeys().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getActiveKey(key: String): ApiKey? {
        return apiKeyDao.getActiveKey(key)?.toDomain()
    }

    override suspend fun insertApiKey(apiKey: ApiKey) {
        apiKeyDao.insertApiKey(apiKey.toEntity())
    }

    override suspend fun updateApiKey(apiKey: ApiKey) {
        apiKeyDao.insertApiKey(apiKey.toEntity())
    }

    override suspend fun deleteApiKey(id: Long) {
        apiKeyDao.deleteApiKey(id)
    }
}
