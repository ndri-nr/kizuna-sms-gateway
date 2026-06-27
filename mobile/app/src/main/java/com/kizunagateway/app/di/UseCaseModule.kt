package com.kizunagateway.app.di

import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.OutboundRepository
import com.kizunagateway.domain.repository.RuleRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.VariableRepository
import com.kizunagateway.domain.repository.WebhookRepository
import com.kizunagateway.domain.service.SmsSender
import com.kizunagateway.domain.service.WebhookHttpClient
import com.kizunagateway.domain.usecase.DeliverWebhookUseCase
import com.kizunagateway.domain.usecase.ExportBackupUseCase
import com.kizunagateway.domain.usecase.ImportBackupUseCase
import com.kizunagateway.domain.usecase.MatchRuleUseCase
import com.kizunagateway.domain.usecase.ReceiveSmsUseCase
import com.kizunagateway.domain.usecase.RenderTemplateUseCase
import com.kizunagateway.domain.usecase.SendAutoReplyUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideReceiveSmsUseCase(smsRepository: SmsRepository): ReceiveSmsUseCase =
        ReceiveSmsUseCase(smsRepository)

    @Provides
    @Singleton
    fun provideMatchRuleUseCase(
        ruleRepository: RuleRepository,
        webhookRepository: WebhookRepository
    ): MatchRuleUseCase =
        MatchRuleUseCase(ruleRepository, webhookRepository)

    @Provides
    @Singleton
    fun provideRenderTemplateUseCase(
        variableRepository: VariableRepository,
        gatewayConfigRepository: GatewayConfigRepository
    ): RenderTemplateUseCase = RenderTemplateUseCase(variableRepository, gatewayConfigRepository)

    @Provides
    @Singleton
    fun provideDeliverWebhookUseCase(
        webhookRepository: WebhookRepository,
        variableRepository: VariableRepository,
        gatewayConfigRepository: GatewayConfigRepository,
        renderTemplateUseCase: RenderTemplateUseCase,
        webhookHttpClient: WebhookHttpClient
    ): DeliverWebhookUseCase = DeliverWebhookUseCase(
        webhookRepository,
        variableRepository,
        gatewayConfigRepository,
        renderTemplateUseCase,
        webhookHttpClient
    )

    @Provides
    @Singleton
    fun provideSendAutoReplyUseCase(
        renderTemplateUseCase: RenderTemplateUseCase,
        smsSender: SmsSender
    ): SendAutoReplyUseCase = SendAutoReplyUseCase(renderTemplateUseCase, smsSender)

    @Provides
    @Singleton
    fun provideExportBackupUseCase(
        gatewayConfigRepository: GatewayConfigRepository,
        webhookRepository: WebhookRepository,
        ruleRepository: RuleRepository,
        variableRepository: VariableRepository,
        smsRepository: SmsRepository,
        logRepository: LogRepository,
        outboundRepository: OutboundRepository
    ): ExportBackupUseCase = ExportBackupUseCase(
        gatewayConfigRepository,
        webhookRepository,
        ruleRepository,
        variableRepository,
        smsRepository,
        logRepository,
        outboundRepository
    )

    @Provides
    @Singleton
    fun provideImportBackupUseCase(
        gatewayConfigRepository: GatewayConfigRepository,
        webhookRepository: WebhookRepository,
        ruleRepository: RuleRepository,
        variableRepository: VariableRepository,
        smsRepository: SmsRepository,
        logRepository: LogRepository,
        outboundRepository: OutboundRepository
    ): ImportBackupUseCase = ImportBackupUseCase(
        gatewayConfigRepository,
        webhookRepository,
        ruleRepository,
        variableRepository,
        smsRepository,
        logRepository,
        outboundRepository
    )
}
