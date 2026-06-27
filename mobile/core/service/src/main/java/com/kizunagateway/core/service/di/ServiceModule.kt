package com.kizunagateway.core.service.di

import android.content.Context
import com.kizunagateway.core.service.SmsSenderImpl
import com.kizunagateway.core.service.NotificationServiceImpl
import com.kizunagateway.domain.service.SmsSender
import com.kizunagateway.domain.service.NotificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideNotificationService(@ApplicationContext context: Context): NotificationService = 
        NotificationServiceImpl(context)

    @Provides
    @Singleton
    fun provideSmsSenderService(@ApplicationContext context: Context): SmsSender =
        SmsSenderImpl(context)
}
