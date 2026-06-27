package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.ApiKey
import java.time.LocalDateTime

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val key: String,
    val name: String,
    val smsPerMinute: Int,
    val smsPerHour: Int,
    val createdAt: LocalDateTime,
    val isActive: Boolean
)

fun ApiKeyEntity.toDomain() = ApiKey(
    id = id,
    key = key,
    name = name,
    smsPerMinute = smsPerMinute,
    smsPerHour = smsPerHour,
    createdAt = createdAt,
    isActive = isActive
)

fun ApiKey.toEntity() = ApiKeyEntity(
    id = id,
    key = key,
    name = name,
    smsPerMinute = smsPerMinute,
    smsPerHour = smsPerHour,
    createdAt = createdAt,
    isActive = isActive
)
