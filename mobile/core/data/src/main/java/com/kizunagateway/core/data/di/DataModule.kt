package com.kizunagateway.core.data.di

import android.content.Context
import com.kizunagateway.core.data.repository.GatewayConfigRepositoryImpl
import com.kizunagateway.core.data.repository.LogRepositoryImpl
import com.kizunagateway.core.data.repository.OutboundRepositoryImpl
import com.kizunagateway.core.data.repository.RuleRepositoryImpl
import com.kizunagateway.core.data.repository.SmsRepositoryImpl
import com.kizunagateway.core.data.repository.VariableRepositoryImpl
import com.kizunagateway.core.data.repository.WebhookRepositoryImpl
import com.kizunagateway.core.data.serialization.KotlinXBackupSerializer
import com.kizunagateway.core.database.dao.ApiKeyDao
import com.kizunagateway.core.database.dao.LogDao
import com.kizunagateway.core.database.dao.OutboundSmsDao
import com.kizunagateway.core.database.dao.RuleDao
import com.kizunagateway.core.database.dao.SmsDao
import com.kizunagateway.core.database.dao.VariableDao
import com.kizunagateway.core.database.dao.WebhookDao
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.OutboundRepository
import com.kizunagateway.domain.repository.RuleRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.VariableRepository
import com.kizunagateway.domain.repository.WebhookRepository
import com.kizunagateway.domain.service.BackupSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSmsRepository(smsDao: SmsDao): SmsRepository = SmsRepositoryImpl(smsDao)

    @Provides
    @Singleton
    fun provideWebhookRepository(webhookDao: WebhookDao): WebhookRepository = WebhookRepositoryImpl(webhookDao)

    @Provides
    @Singleton
    fun provideRuleRepository(ruleDao: RuleDao): RuleRepository = RuleRepositoryImpl(ruleDao)

    @Provides
    @Singleton
    fun provideLogRepository(logDao: LogDao): LogRepository = LogRepositoryImpl(logDao)

    @Provides
    @Singleton
    fun provideVariableRepository(variableDao: VariableDao): VariableRepository =
        VariableRepositoryImpl(variableDao)

    @Provides
    @Singleton
    fun provideGatewayConfigRepository(@ApplicationContext context: Context): GatewayConfigRepository =
        GatewayConfigRepositoryImpl(context)

    @Provides
    @Singleton
    fun provideOutboundRepository(
        outboundSmsDao: OutboundSmsDao,
        apiKeyDao: ApiKeyDao
    ): OutboundRepository = OutboundRepositoryImpl(outboundSmsDao, apiKeyDao)

    @Provides
    @Singleton
    fun provideBackupSerializer(serializer: KotlinXBackupSerializer): BackupSerializer = serializer
}
