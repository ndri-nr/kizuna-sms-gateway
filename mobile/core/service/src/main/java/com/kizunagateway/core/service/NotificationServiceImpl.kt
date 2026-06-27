package com.kizunagateway.core.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.kizunagateway.domain.model.AppNotification
import com.kizunagateway.domain.service.NotificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationService {
    private val _notifications = MutableSharedFlow<AppNotification>(extraBufferCapacity = 1)
    override val notifications: SharedFlow<AppNotification> = _notifications.asSharedFlow()

    override fun showNotification(notification: AppNotification) {
        _notifications.tryEmit(notification)
    }

    override fun showMessage(message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}
