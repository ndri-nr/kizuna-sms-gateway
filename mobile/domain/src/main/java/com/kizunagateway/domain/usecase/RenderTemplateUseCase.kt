package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.model.DeviceInfo
import com.kizunagateway.domain.model.SimInfo
import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.VariableRepository
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class RenderTemplateUseCase(
    private val variableRepository: VariableRepository,
    private val gatewayConfigRepository: GatewayConfigRepository
) {
    private val placeholderRegex = Regex("""\{\{(\w+)\}\}""")

    suspend operator fun invoke(
        template: String,
        sms: SmsMessage,
        deviceInfo: DeviceInfo,
        simInfo: SimInfo
    ): String {
        val gatewayConfig = gatewayConfigRepository.getGatewayConfig()
        val customVars = variableRepository.getAllVariables()

        val variables = mutableMapOf<String, String>().apply {
            // 1. SMS Variables
            put("sender", sms.sender)
            put("receiver", sms.receiver)
            put("message", sms.message)
            put("receivedAt", sms.receivedAt)
            put("messageId", sms.id.toString())

            // 2. Gateway Variables
            put("gatewayId", gatewayConfig.gatewayId)
            put("gatewayName", gatewayConfig.gatewayName)

            // 3. Device Variables
            put("deviceId", deviceInfo.deviceId)
            put("deviceModel", deviceInfo.deviceModel)
            put("manufacturer", deviceInfo.manufacturer)
            put("androidVersion", deviceInfo.androidVersion)
            put("appVersion", deviceInfo.appVersion)

            // 4. SIM Variables
            put("phoneNumber", simInfo.phoneNumber)
            put("displayName", simInfo.displayName)
            put("simSlot", simInfo.simSlot.toString())
            put("subId", simInfo.subId.toString())

            // 5. Date Variables
            val now = LocalDate.now()
            val timeNow = LocalTime.now()
            put("currentDate", now.toString())
            put("currentTime", timeNow.format(DateTimeFormatter.ofPattern("HH:mm:ss")))
            put("currentTimestamp", System.currentTimeMillis().toString())

            // 6. Custom Variables
            customVars.forEach { put(it.key, it.value) }
        }

        return placeholderRegex.replace(template) { matchResult ->
            val key = matchResult.groupValues[1]
            variables[key] ?: matchResult.value
        }
    }
}
