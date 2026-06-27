package com.kizunagateway.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kizunagateway.core.database.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSms(sms: SmsMessageEntity): Long

    @Update
    suspend fun updateSms(sms: SmsMessageEntity)

    @Query("SELECT * FROM sms_message WHERE id = :id")
    suspend fun getSmsById(id: Long): SmsMessageEntity?

    @Query("SELECT * FROM sms_message WHERE processed = 0 ORDER BY createdAt ASC")
    suspend fun getPendingSms(): List<SmsMessageEntity>

    @Query("SELECT * FROM sms_message ORDER BY createdAt DESC LIMIT :limit")
    fun getSmsListFlow(limit: Int): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_message ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getSmsPaged(limit: Int, offset: Int): List<SmsMessageEntity>

    @Query("SELECT COUNT(*) FROM sms_message WHERE createdAt >= :startOfDayTimestamp")
    fun getSmsTodayCountFlow(startOfDayTimestamp: Long): Flow<Int>

    @Query("SELECT * FROM sms_message")
    suspend fun getAllSms(): List<SmsMessageEntity>

    @Query("DELETE FROM sms_message")
    suspend fun deleteAllSms()

    @Query("DELETE FROM sms_message WHERE id = :id")
    suspend fun deleteSmsById(id: Long)
}