package com.kizunagateway.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kizunagateway.domain.model.Rule

@Entity(tableName = "rule")
data class RuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val enabled: Boolean,
    val priority: Int,
    val senderRegex: String?,
    val containsText: String?,
    val webhookId: Long
) {
    fun toDomain() = Rule(id, name, enabled, priority, senderRegex, containsText, webhookId)
    companion object {
        fun fromDomain(r: Rule) = RuleEntity(
            r.id, r.name, r.enabled, r.priority, r.senderRegex, r.containsText, r.webhookId
        )
    }
}
