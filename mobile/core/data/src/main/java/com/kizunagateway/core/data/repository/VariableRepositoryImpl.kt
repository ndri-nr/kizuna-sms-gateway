package com.kizunagateway.core.data.repository

import com.kizunagateway.core.database.dao.VariableDao
import com.kizunagateway.core.database.entity.CustomVariableEntity
import com.kizunagateway.domain.model.CustomVariable
import com.kizunagateway.domain.repository.VariableRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class VariableRepositoryImpl(private val variableDao: VariableDao) : VariableRepository {
    override suspend fun insertVariable(variable: CustomVariable) {
        variableDao.insertVariable(CustomVariableEntity.fromDomain(variable))
    }

    override suspend fun deleteVariable(key: String) {
        variableDao.deleteVariable(key)
    }

    override suspend fun getAllVariables(): List<CustomVariable> {
        return variableDao.getAllVariables().map { it.toDomain() }
    }

    override fun getVariablesFlow(): Flow<List<CustomVariable>> {
        return variableDao.getVariablesFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteAllVariables() {
        variableDao.deleteAllVariables()
    }
}
