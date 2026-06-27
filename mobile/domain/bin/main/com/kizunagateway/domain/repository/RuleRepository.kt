package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.Rule
import kotlinx.coroutines.flow.Flow

interface RuleRepository {
    suspend fun insertRule(rule: Rule): Long
    suspend fun updateRule(rule: Rule)
    suspend fun deleteRule(ruleId: Long)
    suspend fun getAllRules(): List<Rule>
    fun getRulesFlow(): Flow<List<Rule>>
    suspend fun deleteAllRules()
}
