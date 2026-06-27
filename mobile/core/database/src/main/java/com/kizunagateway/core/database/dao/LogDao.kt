package com.kizunagateway.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kizunagateway.core.database.entity.DeliveryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DeliveryLogEntity): Long

    @Query("SELECT * FROM delivery_log ORDER BY createdAt DESC")
    suspend fun getAllLogs(): List<DeliveryLogEntity>

    @Query("SELECT * FROM delivery_log ORDER BY createdAt DESC LIMIT :limit")
    fun getLogsFlow(limit: Int): Flow<List<DeliveryLogEntity>>

    @Query("SELECT * FROM delivery_log ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getLogsPaged(limit: Int, offset: Int): List<DeliveryLogEntity>

    @Query("SELECT COUNT(*) FROM delivery_log WHERE success = 1 AND createdAt >= :startOfDayTimestamp")
    fun getSuccessCountTodayFlow(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM delivery_log WHERE success = 0 AND createdAt >= :startOfDayTimestamp")
    fun getFailedCountTodayFlow(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT * FROM delivery_log WHERE success = 1 ORDER BY createdAt DESC LIMIT 1")
    fun getLastSuccessfulLogFlow(): Flow<DeliveryLogEntity?>

    @Query("DELETE FROM delivery_log WHERE createdAt < :beforeTimestamp")
    suspend fun clearOldLogs(beforeTimestamp: Long)

    @Query("DELETE FROM delivery_log")
    suspend fun deleteAllLogs()

    @Query("DELETE FROM delivery_log WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM delivery_log WHERE smsId = :smsId")
    suspend fun deleteLogsBySmsId(smsId: Long)
}
