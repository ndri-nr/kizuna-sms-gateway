package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.CustomVariable

@Entity(tableName = "custom_variable")
data class CustomVariableEntity(
    @PrimaryKey val key: String,
    val value: String
) {
    fun toDomain() = CustomVariable(key, value)
    companion object {
        fun fromDomain(v: CustomVariable) = CustomVariableEntity(v.key, v.value)
    }
}