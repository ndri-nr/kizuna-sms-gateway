package com.kizunagateway.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kizunagateway.domain.model.DeliveryLog
import com.kizunagateway.domain.model.WebhookDeliveryInput
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.LogRepository
import com.kizunagateway.domain.repository.SmsRepository
import com.kizunagateway.domain.repository.WebhookRepository
import com.kizunagateway.domain.service.DeviceInfoProvider
import com.kizunagateway.domain.usecase.DeliverWebhookUseCase
import com.kizunagateway.domain.usecase.MatchRuleUseCase
import com.kizunagateway.domain.usecase.SendAutoReplyUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ProcessSmsWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val smsRepository: SmsRepository,
    private val webhookRepository: WebhookRepository,
    private val logRepository: LogRepository,
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val matchRuleUseCase: MatchRuleUseCase,
    private val deliverWebhookUseCase: DeliverWebhookUseCase,
    private val sendAutoReplyUseCase: SendAutoReplyUseCase,
    private val deviceInfoProvider: DeviceInfoProvider
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ProcessSmsWorker"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting ProcessSmsWorker... Attempt: ${runAttemptCount + 1}")
        val pendingSmsList = smsRepository.getPendingSms()
        Log.d(TAG, "Found ${pendingSmsList.size} pending SMS messages")

        if (pendingSmsList.isEmpty()) return Result.success()

        val config = gatewayConfigRepository.getGatewayConfig()
        val deviceInfo = deviceInfoProvider.getDeviceInfo()

        var shouldRetryWorker = false

        for (sms in pendingSmsList) {
            val simInfo = deviceInfoProvider.getSimInfo(sms.simSlot)
            Log.d(TAG, "Processing SMS ID: ${sms.id} from ${sms.sender}")
            try {
                var success: Boolean
                var isRetryableError: Boolean
                val matchedWebhookId = try {
                    matchRuleUseCase(sms.sender, sms.message)
                } catch (e: Exception) {
                    Log.e(TAG, "Error matching rules", e)
                    null
                }

                Log.d(TAG, "Matched Webhook ID: $matchedWebhookId")

                if (matchedWebhookId != null) {
                    val webhook = webhookRepository.getWebhookById(matchedWebhookId)
                    if (webhook != null) {
                        Log.d(TAG, "Sending webhook: ${webhook.name} to ${webhook.url}")

                        val deliveryResult = deliverWebhookUseCase(
                            WebhookDeliveryInput(
                                sms = sms,
                                webhook = webhook,
                                deviceInfo = deviceInfo,
                                simInfo = simInfo
                            )
                        )

                        success = deliveryResult.success
                        isRetryableError = deliveryResult.isRetryableError
                        
                        logRepository.insertLog(
                            DeliveryLog(
                                smsId = sms.id,
                                webhookId = webhook.id,
                                requestHeaders = deliveryResult.renderedHeaders.toString(),
                                requestBody = deliveryResult.renderedBody,
                                responseBody = deliveryResult.responseBody,
                                responseCode = deliveryResult.responseCode,
                                success = success,
                                retryCount = runAttemptCount,
                                errorMessage = deliveryResult.errorMessage
                            )
                        )

                        if (isRetryableError) {
                            shouldRetryWorker = true
                        }

                        if (deliveryResult.responseCode == 200) {
                            sendAutoReplyUseCase(webhook, sms, deviceInfo, simInfo)
                        }

                        if (success || !isRetryableError || runAttemptCount >= 3) {
                            smsRepository.updateSms(sms.copy(processed = true))
                            Log.d(TAG, "Marked SMS ID ${sms.id} as processed")
                        }
                    } else {
                        Log.w(TAG, "Webhook not found or disabled for ID: $matchedWebhookId")
                        // Rule matched but webhook is gone/disabled. Mark as processed to clear queue.
                        smsRepository.updateSms(sms.copy(processed = true))
                    }
                } else {
                    Log.d(TAG, "No rule matched for SMS ID: ${sms.id}")
                    if (config.deleteUntrackedSms) {
                        Log.d(TAG, "Deleting untracked message.")
                        smsRepository.deleteSmsById(sms.id)
                    } else {
                        Log.d(TAG, "Keeping untracked message. Marking as processed.")
                        smsRepository.updateSms(sms.copy(processed = true))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing SMS ID: ${sms.id}", e)
                shouldRetryWorker = true
            }
        }

        return if (shouldRetryWorker && runAttemptCount < 3) {
            Log.d(TAG, "Retrying worker... attempt ${runAttemptCount + 1}")
            Result.retry()
        } else {
            Result.success()
        }
    }
}
