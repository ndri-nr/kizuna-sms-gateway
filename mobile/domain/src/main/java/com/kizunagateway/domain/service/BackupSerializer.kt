package com.kizunagateway.domain.service

import com.kizunagateway.domain.model.BackupPayload

interface BackupSerializer {
    fun serialize(payload: BackupPayload): String
    fun deserialize(json: String): BackupPayload
}
