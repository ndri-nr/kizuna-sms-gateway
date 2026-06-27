package com.kizunagateway.domain.model

data class AppNotification (
    val message: String,
    val actionLabel: String? = null,
    val actionUri: String? = null
)
