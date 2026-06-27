package com.kizunagateway.feature.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SubscriptionManager
import android.util.Log
import com.kizunagateway.domain.service.DeviceInfoProvider
import com.kizunagateway.domain.service.SmsProcessingScheduler
import com.kizunagateway.domain.usecase.MatchRuleUseCase
import com.kizunagateway.domain.usecase.ReceiveSmsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class InboundSmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var receiveSmsUseCase: ReceiveSmsUseCase

    @Inject
    lateinit var matchRuleUseCase: MatchRuleUseCase

    @Inject
    lateinit var deviceInfoProvider: DeviceInfoProvider

    @Inject
    lateinit var smsProcessingScheduler: SmsProcessingScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (smsMessages.isEmpty()) return

        val firstMessage = smsMessages[0]
        val sender = firstMessage.displayOriginatingAddress ?: "Unknown"
        val body = smsMessages.joinToString("") { it.displayMessageBody ?: "" }
        val receivedAt = Instant.ofEpochMilli(firstMessage.timestampMillis).toString()
        
        // Use subscription ID from intent extras if available, otherwise fallback to slot guessing
        val subscriptionId = intent.getIntExtra("subscription", -1)
        val simSlot = detectSimSlot(context, intent, subscriptionId)
        
        val receiver = deviceInfoProvider.getSimInfo(simSlot).phoneNumber

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                receiveSmsUseCase(
                    sender = sender,
                    receiver = receiver,
                    message = body,
                    receivedAt = receivedAt,
                    simSlot = simSlot
                )
                smsProcessingScheduler.scheduleProcessing()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to process incoming SMS", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun detectSimSlot(context: Context, intent: Intent, subId: Int): Int {
        if (subId != -1) {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            try {
                @Suppress("MissingPermission")
                val info = subscriptionManager?.getActiveSubscriptionInfo(subId)
                if (info != null) return info.simSlotIndex
            } catch (e: Exception) {
                Log.e(TAG, "Error resolving subId $subId to slot", e)
            }
        }

        // Fallback to searching common intent extras
        val bundle = intent.extras
        val slotKeys = arrayOf(
            "slot", "simSlot", "simId", "sim_id", "phone", "com.android.phone.extra.slot", "subscription"
        )

        for (key in slotKeys) {
            val value = bundle?.get(key) ?: continue
            val intValue = value.toString().toIntOrNull() ?: -1
            if (intValue in 0..5) return intValue
        }

        return 0
    }

    companion object {
        private const val TAG = "SmsReceiver"
    }
}
