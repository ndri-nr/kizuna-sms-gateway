package com.kizunagateway.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CustomVariable (
    val key: String,
    val value: String
)
