package com.kizunagateway.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kizunagateway.core.database.entity.CustomVariableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VariableDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariable(variable: CustomVariableEntity)

    @Query("DELETE FROM custom_variable WHERE `key` = :key")
    suspend fun deleteVariable(key: String)

    @Query("SELECT * FROM custom_variable")
    suspend fun getAllVariables(): List<CustomVariableEntity>

    @Query("SELECT * FROM custom_variable")
    fun getVariablesFlow(): Flow<List<CustomVariableEntity>>

    @Query("DELETE FROM custom_variable")
    suspend fun deleteAllVariables()
}