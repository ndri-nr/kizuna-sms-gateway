package com.kizunagateway.domain.service

import com.kizunagateway.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationService {
    val notifications: Flow<AppNotification>
    fun showNotification(notification: AppNotification)
    fun showMessage(message: String)
}
