package com.kizunagateway.domain.repository

import com.kizunagateway.domain.model.*

import kotlinx.coroutines.flow.Flow

interface GatewayConfigRepository {
    suspend fun getGatewayConfig(): GatewayConfig
    fun getGatewayConfigFlow(): Flow<GatewayConfig>
    suspend fun saveGatewayConfig(config: GatewayConfig)
}
