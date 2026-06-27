package com.kizunagateway.core.template.di

import android.content.Context
import com.kizunagateway.core.template.AndroidInfoResolver
import com.kizunagateway.domain.service.DeviceInfoProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TemplateModule {

    @Provides
    @Singleton
    fun provideDeviceInfoProvider(@ApplicationContext context: Context): DeviceInfoProvider =
        AndroidInfoResolver(context)
}
