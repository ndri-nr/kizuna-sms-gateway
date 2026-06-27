package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable
import com.kizunagateway.domain.util.LocalDateTimeSerializer
import java.time.LocalDateTime

@Serializable
data class ApiKey(
    val id: Long = 0,
    val key: String,
    val name: String,
    val smsPerMinute: Int = 10,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val isActive: Boolean = true
)
