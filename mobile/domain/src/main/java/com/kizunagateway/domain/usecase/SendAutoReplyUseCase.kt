package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.model.DeviceInfo
import com.kizunagateway.domain.model.SimInfo
import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.model.Webhook
import com.kizunagateway.domain.service.SmsSender

class SendAutoReplyUseCase(
    private val renderTemplateUseCase: RenderTemplateUseCase,
    private val smsSender: SmsSender
) {
    suspend operator fun invoke(
        webhook: Webhook,
        sms: SmsMessage,
        deviceInfo: DeviceInfo,
        simInfo: SimInfo
    ) {
        val replyTemplate = webhook.autoReplyMessage ?: return
        val renderedReply = renderTemplateUseCase(replyTemplate, sms, deviceInfo, simInfo)
        smsSender.sendSms(sms.sender, renderedReply, sms.simSlot)
    }
}
