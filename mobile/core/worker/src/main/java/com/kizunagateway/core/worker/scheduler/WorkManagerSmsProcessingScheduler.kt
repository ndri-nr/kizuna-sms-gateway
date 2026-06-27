package com.kizunagateway.core.worker.scheduler

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kizunagateway.core.worker.ProcessSmsWorker
import com.kizunagateway.domain.service.SmsProcessingScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSmsProcessingScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsProcessingScheduler {

    override fun scheduleProcessing() {
        val workRequest = OneTimeWorkRequestBuilder<ProcessSmsWorker>()
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.KEEP, // Change REPLACE to KEEP to avoid interrupting active processing
            workRequest
        )
    }

    companion object {
        private const val WORK_NAME = "process_sms_work"
    }
}
