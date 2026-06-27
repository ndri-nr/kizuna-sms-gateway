package com.kizunagateway.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kizunagateway.core.database.dao.*
import com.kizunagateway.core.database.entity.*

@Database(
    entities = [
        SmsMessageEntity::class,
        WebhookEntity::class,
        WebhookHeaderEntity::class,
        WebhookTemplateEntity::class,
        RuleEntity::class,
        DeliveryLogEntity::class,
        CustomVariableEntity::class,
        OutboundSmsEntity::class,
        ApiKeyEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KizunaDatabase : RoomDatabase() {
    abstract fun smsDao(): SmsDao
    abstract fun webhookDao(): WebhookDao
    abstract fun ruleDao(): RuleDao
    abstract fun logDao(): LogDao
    abstract fun variableDao(): VariableDao
    abstract fun outboundSmsDao(): OutboundSmsDao
    abstract fun apiKeyDao(): ApiKeyDao
}
