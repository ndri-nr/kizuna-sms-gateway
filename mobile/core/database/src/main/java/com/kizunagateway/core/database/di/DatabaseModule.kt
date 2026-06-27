package com.kizunagateway.core.database.di

import android.content.Context
import androidx.room.Room
import com.kizunagateway.core.database.KizunaDatabase
import com.kizunagateway.core.database.dao.ApiKeyDao
import com.kizunagateway.core.database.dao.LogDao
import com.kizunagateway.core.database.dao.OutboundSmsDao
import com.kizunagateway.core.database.dao.RuleDao
import com.kizunagateway.core.database.dao.SmsDao
import com.kizunagateway.core.database.dao.VariableDao
import com.kizunagateway.core.database.dao.WebhookDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KizunaDatabase {
        return Room.databaseBuilder(
            context,
            KizunaDatabase::class.java,
            "kizuna_gateway.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSmsDao(db: KizunaDatabase): SmsDao = db.smsDao()

    @Provides
    fun provideWebhookDao(db: KizunaDatabase): WebhookDao = db.webhookDao()

    @Provides
    fun provideRuleDao(db: KizunaDatabase): RuleDao = db.ruleDao()

    @Provides
    fun provideLogDao(db: KizunaDatabase): LogDao = db.logDao()

    @Provides
    fun provideVariableDao(db: KizunaDatabase): VariableDao = db.variableDao()

    @Provides
    fun provideOutboundSmsDao(db: KizunaDatabase): OutboundSmsDao = db.outboundSmsDao()

    @Provides
    fun provideApiKeyDao(db: KizunaDatabase): ApiKeyDao = db.apiKeyDao()
}
