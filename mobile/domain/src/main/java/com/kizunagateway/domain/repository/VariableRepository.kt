package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.CustomVariable
import kotlinx.coroutines.flow.Flow

interface VariableRepository {
    suspend fun insertVariable(variable: CustomVariable)
    suspend fun deleteVariable(key: String)
    suspend fun getAllVariables(): List<CustomVariable>
    fun getVariablesFlow(): Flow<List<CustomVariable>>
    suspend fun deleteAllVariables()
}
