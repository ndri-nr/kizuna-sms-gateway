package com.kizunagateway.domain.service

interface SmsSender {
    fun sendSms(phoneNumber: String, message: String, simSlot: Int)
}
