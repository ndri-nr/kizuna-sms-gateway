package com.kizunagateway.core.database.dao

import androidx.room.*
import com.kizunagateway.core.database.entity.ApiKeyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY createdAt DESC")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    @Query("SELECT * FROM api_keys WHERE `key` = :key AND isActive = 1 LIMIT 1")
    suspend fun getActiveKey(key: String): ApiKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(apiKey: ApiKeyEntity)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun deleteApiKey(id: Long)
}
