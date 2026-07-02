package com.kizunagateway.core.network.di

import com.kizunagateway.core.network.NetworkInfoProviderImpl
import com.kizunagateway.core.network.WebhookClient
import com.kizunagateway.domain.service.NetworkInfoProvider
import com.kizunagateway.domain.service.WebhookHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideWebhookHttpClient(okHttpClient: OkHttpClient): WebhookHttpClient =
        WebhookClient(okHttpClient)

    @Provides
    @Singleton
    fun provideNetworkInfoProvider(okHttpClient: OkHttpClient): NetworkInfoProvider =
        NetworkInfoProviderImpl(okHttpClient)
}
