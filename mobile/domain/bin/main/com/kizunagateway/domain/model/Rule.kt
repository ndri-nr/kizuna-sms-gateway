package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Rule (
    val id: Long = 0,
    val name: String,
    val enabled: Boolean = true,
    val priority: Int = 0,
    val senderRegex: String? = null,
    val containsText: String? = null,
    val webhookId: Long
)
