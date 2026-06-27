package com.kizunagateway.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.kizunagateway.app.navigation.KizunaNavGraph
import com.kizunagateway.core.ui.theme.KizunaTheme
import com.kizunagateway.domain.service.NotificationService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var notificationService: NotificationService

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            notificationService.showMessage("All required permissions granted")
        } else {
            notificationService.showMessage("Permissions are required for full functionality")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            KizunaTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()

                LaunchedEffect(Unit) {
                    notificationService.notifications.collect { notification ->
                        val result = snackbarHostState.showSnackbar(
                            message = notification.message,
                            actionLabel = notification.actionLabel,
                            duration = SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed && notification.actionUri != null) {
                            handleNotificationAction(notification.actionUri!!)
                        }
                    }
                }

                KizunaNavGraph(
                    navController = navController,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }

    private fun handleNotificationAction(uriString: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uriString.toUri(), "application/json")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(intent)
        } catch (_: Exception) {
            notificationService.showMessage("Could not open file")
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = buildList {
            if (!hasPermission(Manifest.permission.RECEIVE_SMS)) add(Manifest.permission.RECEIVE_SMS)
            if (!hasPermission(Manifest.permission.READ_SMS)) add(Manifest.permission.READ_SMS)
            if (!hasPermission(Manifest.permission.SEND_SMS)) add(Manifest.permission.SEND_SMS)
            if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) add(Manifest.permission.READ_PHONE_STATE)
            if (!hasPermission(Manifest.permission.READ_PHONE_NUMBERS)) add(Manifest.permission.READ_PHONE_NUMBERS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
