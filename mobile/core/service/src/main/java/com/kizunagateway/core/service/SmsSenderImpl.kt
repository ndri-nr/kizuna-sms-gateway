package com.kizunagateway.core.service

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.util.Log
import com.kizunagateway.domain.service.SmsSender
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsSenderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsSender {

    @SuppressLint("MissingPermission")
    override fun sendSms(phoneNumber: String, message: String, simSlot: Int) {
        try {
            val subId = getSubscriptionIdForSlot(simSlot)
            Log.d("SmsSender", "Sending to $phoneNumber via SIM slot=$simSlot, resolved subId=$subId")

            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(subId)
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(phoneNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            }
        } catch (e: Exception) {
            Log.e("SmsSender", "Failed to send SMS to $phoneNumber", e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getSubscriptionIdForSlot(simSlot: Int): Int {  // ← non-nullable Int
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)

        // Method 1: Direct slot query (most reliable, works API 22+)
        try {
            val subInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlot)
            if (subInfo != null) {
                Log.d("SmsSender", "Method 1 success: subId=${subInfo.subscriptionId}")
                return subInfo.subscriptionId
            }
        } catch (e: Exception) {
            Log.w("SmsSender", "Method 1 failed: ${e.message}")
        }

        // Method 2: getSubscriptionIds (API 30+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val subIds = subscriptionManager.getSubscriptionIds(simSlot)
                if (subIds != null && subIds.isNotEmpty()) {
                    Log.d("SmsSender", "Method 2 success: subId=${subIds[0]}")
                    return subIds[0]
                }
            } catch (e: Exception) {
                Log.w("SmsSender", "Method 2 failed: ${e.message}")
            }
        }

        // Method 3: Scan activeSubscriptionInfoList manually
        try {
            val activeList = subscriptionManager.activeSubscriptionInfoList
            Log.d("SmsSender", "Active subscriptions: ${
                activeList?.map { "slot=${it.simSlotIndex}, subId=${it.subscriptionId}, name=${it.displayName}" }
            }")
            val subInfo = activeList?.find { it.simSlotIndex == simSlot }
            if (subInfo != null) {
                Log.d("SmsSender", "Method 3 success: subId=${subInfo.subscriptionId}")
                return subInfo.subscriptionId
            }
        } catch (e: Exception) {
            Log.w("SmsSender", "Method 3 failed: ${e.message}")
        }

        // Fallback: use default SMS subscription
        val default = SubscriptionManager.getDefaultSmsSubscriptionId()
        Log.w("SmsSender", "All methods failed for slot=$simSlot, falling back to default subId=$default")
        return default
    }
}