package com.kizunagateway.domain.model

data class SimInfo(
    val phoneNumber: String = "Unknown",
    val displayName: String = "Unknown",
    val simSlot: Int = 0,
    val subId: Int = -1
) {
    fun toMap(): Map<String, String> = mapOf(
        "phoneNumber" to phoneNumber,
        "displayName" to displayName,
        "simSlot" to simSlot.toString(),
        "subId" to subId.toString()
    )
}

data class DeviceInfo(
    val deviceId: String = "unknown",
    val deviceModel: String = "unknown",
    val manufacturer: String = "unknown",
    val androidVersion: String = "unknown",
    val appVersion: String = "unknown"
) {
    fun toMap(): Map<String, String> = mapOf(
        "deviceId" to deviceId,
        "deviceModel" to deviceModel,
        "manufacturer" to manufacturer,
        "androidVersion" to androidVersion,
        "appVersion" to appVersion
    )
}
