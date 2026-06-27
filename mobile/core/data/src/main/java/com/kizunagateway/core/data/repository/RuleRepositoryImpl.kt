package com.kizunagateway.core.data.repository

import com.kizunagateway.core.database.dao.RuleDao
import com.kizunagateway.core.database.entity.RuleEntity
import com.kizunagateway.domain.model.Rule
import com.kizunagateway.domain.repository.RuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RuleRepositoryImpl(private val ruleDao: RuleDao) : RuleRepository {
    override suspend fun insertRule(rule: Rule): Long {
        return ruleDao.insertRule(RuleEntity.fromDomain(rule))
    }

    override suspend fun updateRule(rule: Rule) {
        ruleDao.updateRule(RuleEntity.fromDomain(rule))
    }

    override suspend fun deleteRule(ruleId: Long) {
        ruleDao.deleteRule(ruleId)
    }

    override suspend fun getAllRules(): List<Rule> {
        return ruleDao.getAllRules().map { it.toDomain() }
    }

    override fun getRulesFlow(): Flow<List<Rule>> {
        return ruleDao.getRulesFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun deleteAllRules() {
        ruleDao.deleteAllRules()
    }
}
