package com.kizunagateway.core.template

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import com.kizunagateway.domain.model.DeviceInfo
import com.kizunagateway.domain.model.SimInfo

import com.kizunagateway.domain.service.DeviceInfoProvider

class AndroidInfoResolver(private val context: Context) : DeviceInfoProvider {

    @SuppressLint("HardwareIds")
    override fun getDeviceInfo(): DeviceInfo {
        val deviceId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } catch (_: Exception) {
            "unknown-device-id"
        }
        val appVersion = try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0.0"
        } catch (_: Exception) {
            "1.0.0"
        }

        return DeviceInfo(
            deviceId = deviceId ?: "unknown",
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            appVersion = appVersion
        )
    }

    @SuppressLint("MissingPermission")
    override fun getSimInfo(simSlot: Int): SimInfo {
        val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)

        return try {
            val subInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlot)

            if (subInfo != null) {
                val phoneNumber = subInfo.number
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() && it != "Unknown" }
                    ?: getPhoneNumberFromTelephonyManager(simSlot)
                    ?: "Unknown"

                SimInfo(
                    phoneNumber = phoneNumber,
                    displayName = subInfo.displayName.toString(),
                    simSlot = simSlot,
                    subId = subInfo.subscriptionId
                )
            } else {
                SimInfo(
                    phoneNumber = "Unknown",
                    displayName = "SIM ${simSlot + 1}",
                    simSlot = simSlot,
                    subId = -1
                )
            }
        } catch (e: Exception) {
            Log.e("AndroidInfoResolver", "Error getting SIM info for slot $simSlot", e)
            SimInfo(simSlot = simSlot)
        }
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    private fun getPhoneNumberFromTelephonyManager(simSlot: Int): String? {
        return try {
            val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)
            val subInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(simSlot)
                ?: return null

            val telephonyManager = context.getSystemService(TelephonyManager::class.java)
            telephonyManager
                .createForSubscriptionId(subInfo.subscriptionId)
                .line1Number
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.w("AndroidInfoResolver", "TelephonyManager fallback failed for slot $simSlot: ${e.message}")
            null
        }
    }
}
