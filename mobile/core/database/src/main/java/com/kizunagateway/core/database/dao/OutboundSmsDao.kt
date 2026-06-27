package com.kizunagateway.core.database.dao

import androidx.room.*
import com.kizunagateway.core.database.entity.OutboundSmsEntity
import com.kizunagateway.domain.model.OutboundSmsStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboundSmsDao {
    @Query("SELECT * FROM outbound_sms ORDER BY createdAt DESC")
    fun getAllOutboundSms(): Flow<List<OutboundSmsEntity>>

    @Query("SELECT * FROM outbound_sms WHERE id = :id")
    suspend fun getOutboundSmsById(id: Long): OutboundSmsEntity?

    @Query("SELECT * FROM outbound_sms WHERE status = :status ORDER BY createdAt ASC")
    suspend fun getSmsByStatus(status: OutboundSmsStatus): List<OutboundSmsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOutboundSms(sms: OutboundSmsEntity): Long

    @Update
    suspend fun updateOutboundSms(sms: OutboundSmsEntity)

    @Query("DELETE FROM outbound_sms WHERE id = :id")
    suspend fun deleteOutboundSms(id: Long)

    @Query("DELETE FROM outbound_sms")
    suspend fun deleteAllOutboundSms()

    @Query("SELECT COUNT(*) FROM outbound_sms WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM outbound_sms WHERE apiKey = :apiKey AND createdAt >= :startTime")
    suspend fun getSmsCountByKeyInTimeRange(apiKey: String, startTime: java.time.LocalDateTime): Int
}
