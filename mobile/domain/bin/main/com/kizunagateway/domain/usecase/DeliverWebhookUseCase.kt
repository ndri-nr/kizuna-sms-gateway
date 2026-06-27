package com.kizunagateway.domain.usecase

import com.kizunagateway.domain.model.DeviceInfo
import com.kizunagateway.domain.model.SimInfo
import com.kizunagateway.domain.model.SmsMessage
import com.kizunagateway.domain.model.WebhookDeliveryInput
import com.kizunagateway.domain.model.WebhookDeliveryResult
import com.kizunagateway.domain.model.WebhookHeader
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.VariableRepository
import com.kizunagateway.domain.repository.WebhookRepository
import com.kizunagateway.domain.service.WebhookHttpClient

class DeliverWebhookUseCase(
    private val webhookRepository: WebhookRepository,
    private val variableRepository: VariableRepository,
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val renderTemplateUseCase: RenderTemplateUseCase,
    private val webhookHttpClient: WebhookHttpClient
) {
    suspend operator fun invoke(input: WebhookDeliveryInput): WebhookDeliveryResult {
        val sms = input.sms
        val webhook = input.webhook
        val deviceInfo = input.deviceInfo
        val simInfo = input.simInfo

        val renderedUrl = renderTemplateUseCase(webhook.url, sms, deviceInfo, simInfo)

        val headers = input.webhookHeaders
            ?: webhookRepository.getHeadersForWebhook(webhook.id)

        val renderedHeaders = buildRenderedHeaders(
            sms = sms,
            deviceInfo = deviceInfo,
            simInfo = simInfo,
            headers = headers
        )

        val bodyTemplate = input.bodyTemplate
            ?: webhookRepository.getTemplateForWebhook(webhook.id)?.bodyTemplate
            ?: DEFAULT_BODY_TEMPLATE

        val renderedBody = renderTemplateUseCase(bodyTemplate, sms, deviceInfo, simInfo)

        return try {
            val response = webhookHttpClient.sendRequest(
                url = renderedUrl,
                method = webhook.method,
                headers = renderedHeaders,
                bodyContent = renderedBody,
                timeoutSeconds = webhook.timeoutSeconds
            )
            val isRetryableError = !response.isSuccessful &&
                (response.code == 502 || response.code == 503 || response.code == 504)

            WebhookDeliveryResult(
                success = response.isSuccessful,
                responseCode = response.code,
                responseBody = response.body,
                renderedUrl = renderedUrl,
                renderedBody = renderedBody,
                renderedHeaders = renderedHeaders,
                isRetryableError = isRetryableError
            )
        } catch (e: Exception) {
            WebhookDeliveryResult(
                success = false,
                responseCode = 500,
                responseBody = "",
                renderedUrl = renderedUrl,
                renderedBody = renderedBody,
                renderedHeaders = renderedHeaders,
                isRetryableError = true,
                errorMessage = e.localizedMessage ?: e.message ?: "Unknown Network Error"
            )
        }
    }

    private suspend fun buildRenderedHeaders(
        sms: SmsMessage,
        deviceInfo: DeviceInfo,
        simInfo: SimInfo,
        headers: List<WebhookHeader>
    ): Map<String, String> {
        val gatewayConfig = gatewayConfigRepository.getGatewayConfig()
        val renderedHeaders = mutableMapOf<String, String>()

        variableRepository.getAllVariables().forEach { variable ->
            renderedHeaders[variable.key] = variable.value
        }

        headers.forEach { header ->
            val renderedKey = renderTemplateUseCase(header.key, sms, deviceInfo, simInfo)
            val renderedVal = renderTemplateUseCase(header.value, sms, deviceInfo, simInfo)
            renderedHeaders[renderedKey] = renderedVal
        }

        renderedHeaders["X-DEVICE-TOKEN"] = gatewayConfig.deviceSecret
        return renderedHeaders
    }

    companion object {
        const val DEFAULT_BODY_TEMPLATE =
            "{\"sender\":\"{{sender}}\",\"receiver\":\"{{receiver}}\",\"message\":\"{{message}}\",\"receivedAt\":\"{{receivedAt}}\"}"
    }
}
