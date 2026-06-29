package com.kizunagateway.domain.service

import com.kizunagateway.domain.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationService {
    val notifications: Flow<AppNotification>
    fun showNotification(notification: AppNotification)
    fun showMessage(message: String)
    fun showMessage(resId: Int, vararg formatArgs: Any)
    fun getString(resId: Int, vararg formatArgs: Any): String
}
