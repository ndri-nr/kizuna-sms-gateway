package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.repository.SmsRepository

class ReceiveSmsUseCase(private val smsRepository: SmsRepository) {
    suspend operator fun invoke(sender: String, receiver: String?, message: String, receivedAt: String, simSlot: Int): Long {
        val sms = SmsMessage(
            sender = sender,
            receiver = receiver ?: ("Sim slot $simSlot"),
            message = message,
            receivedAt = receivedAt,
            simSlot = simSlot
        )
        return smsRepository.insertSms(sms)
    }
}
