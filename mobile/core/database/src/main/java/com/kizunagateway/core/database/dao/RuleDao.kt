package com.kizunagateway.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kizunagateway.core.database.entity.RuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: RuleEntity): Long

    @Update
    suspend fun updateRule(rule: RuleEntity)

    @Query("DELETE FROM rule WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: Long)

    @Query("SELECT * FROM rule")
    suspend fun getAllRules(): List<RuleEntity>

    @Query("SELECT * FROM rule ORDER BY priority DESC")
    fun getRulesFlow(): Flow<List<RuleEntity>>

    @Query("DELETE FROM rule")
    suspend fun deleteAllRules()
}