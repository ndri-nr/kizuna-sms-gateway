package com.kizunagateway.core.data.serialization

import com.kizunagateway.domain.model.BackupPayload
import com.kizunagateway.domain.service.BackupSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KotlinXBackupSerializer @Inject constructor() : BackupSerializer {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    override fun serialize(payload: BackupPayload): String = json.encodeToString(payload)

    override fun deserialize(jsonString: String): BackupPayload = json.decodeFromString(jsonString)
}
