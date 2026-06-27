package com.kizunagateway.core.worker.di

import com.kizunagateway.core.worker.scheduler.WorkManagerSmsProcessingScheduler
import com.kizunagateway.domain.service.SmsProcessingScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerModule {

    @Binds
    @Singleton
    abstract fun bindSmsProcessingScheduler(
        impl: WorkManagerSmsProcessingScheduler
    ): SmsProcessingScheduler
}
