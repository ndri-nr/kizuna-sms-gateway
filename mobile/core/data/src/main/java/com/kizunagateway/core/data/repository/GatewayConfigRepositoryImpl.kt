package com.kizunagateway.core.data.repository

import android.content.Context
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kizunagateway.domain.model.GatewayConfig
import com.kizunagateway.domain.repository.GatewayConfigRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "gateway_config")

class GatewayConfigRepositoryImpl(private val context: Context) : GatewayConfigRepository {

    private object PreferencesKeys {
        val GATEWAY_ID = stringPreferencesKey("gateway_id")
        val GATEWAY_NAME = stringPreferencesKey("gateway_name")
        val DEVICE_SECRET = stringPreferencesKey("device_secret")
        val DELETE_UNTRACKED_SMS = booleanPreferencesKey("delete_untracked_sms")
        val OUTBOUND_WEBHOOK_URL = stringPreferencesKey("outbound_webhook_url")
        val TUNNEL_SERVER_URL = stringPreferencesKey("tunnel_server_url")
    }

    override suspend fun getGatewayConfig(): GatewayConfig {
        val prefs = context.dataStore.data.first()
        val gatewayId = prefs[PreferencesKeys.GATEWAY_ID]
        val deviceSecret = prefs[PreferencesKeys.DEVICE_SECRET]

        if (gatewayId == null || deviceSecret == null) {
            val generatedId = "KGW-${UUID.randomUUID().toString().take(8).uppercase()}"
            val generatedSecret = UUID.randomUUID().toString()
            val generatedName = "Gateway-${Build.MODEL}"

            val newConfig = GatewayConfig(generatedId, generatedName, generatedSecret, false)
            saveGatewayConfig(newConfig)
            return newConfig
        }
        return getGatewayConfigFlow().first()
    }

    override fun getGatewayConfigFlow(): Flow<GatewayConfig> {
        return context.dataStore.data.map { prefs ->
            val gatewayId = prefs[PreferencesKeys.GATEWAY_ID]
            val gatewayName = prefs[PreferencesKeys.GATEWAY_NAME]
            val deviceSecret = prefs[PreferencesKeys.DEVICE_SECRET]
            val deleteUntracked = prefs[PreferencesKeys.DELETE_UNTRACKED_SMS] ?: false
            val outboundWebhookUrl = prefs[PreferencesKeys.OUTBOUND_WEBHOOK_URL]
            val tunnelServerUrl = prefs[PreferencesKeys.TUNNEL_SERVER_URL] ?: "sms-gateway.artivy.id"

            if (gatewayId == null || deviceSecret == null) {
                // Return a temporary config or handle initialization separately.
                // For safety in flow, we can't easily save from within map.
                // But getGatewayConfig() is usually called first to ensure init.
                GatewayConfig(
                    gatewayId ?: "Initializing...",
                    gatewayName ?: "Gateway-${Build.MODEL}",
                    deviceSecret ?: "",
                    deleteUntracked,
                    outboundWebhookUrl,
                    tunnelServerUrl
                )
            } else {
                GatewayConfig(gatewayId, gatewayName ?: "Gateway-${Build.MODEL}", deviceSecret, deleteUntracked, outboundWebhookUrl, tunnelServerUrl)
            }
        }
    }

    override suspend fun saveGatewayConfig(config: GatewayConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.GATEWAY_ID] = config.gatewayId
            preferences[PreferencesKeys.GATEWAY_NAME] = config.gatewayName
            preferences[PreferencesKeys.DEVICE_SECRET] = config.deviceSecret
            preferences[PreferencesKeys.DELETE_UNTRACKED_SMS] = config.deleteUntrackedSms
            config.outboundWebhookUrl?.let { preferences[PreferencesKeys.OUTBOUND_WEBHOOK_URL] = it }
            preferences[PreferencesKeys.TUNNEL_SERVER_URL] = config.tunnelServerUrl
        }
    }
}
