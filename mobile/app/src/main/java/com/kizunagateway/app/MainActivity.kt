package com.kizunagateway.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
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
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kizunagateway.domain.repository.GatewayConfigRepository
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var gatewayConfigRepository: GatewayConfigRepository

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            notificationService.showMessage(getString(com.kizunagateway.core.ui.R.string.permissions_granted))
        } else {
            notificationService.showMessage(getString(com.kizunagateway.core.ui.R.string.permissions_required))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Sync language preference before setContent to avoid flickering
        runBlocking {
            val config = gatewayConfigRepository.getGatewayConfig()
            val targetLang = config.language
            val currentLocales = AppCompatDelegate.getApplicationLocales()
            val currentLang = if (currentLocales.isEmpty) {
                java.util.Locale.getDefault().language
            } else {
                currentLocales.get(0)?.language
            }
            
            if (currentLang != targetLang) {
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLang))
            }
        }

        enableEdgeToEdge()
        checkAndRequestPermissions()

        setContent {
            KizunaTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val navController = rememberNavController()

                // Observe future language changes
                LaunchedEffect(Unit) {
                    gatewayConfigRepository.getGatewayConfigFlow().collect { config ->
                        val targetLang = config.language
                        val currentLocales = AppCompatDelegate.getApplicationLocales()
                        val currentLang = if (currentLocales.isEmpty) {
                            java.util.Locale.getDefault().language
                        } else {
                            currentLocales.get(0)?.language
                        }
                        
                        if (currentLang != targetLang) {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(targetLang))
                        }
                    }
                }

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
            notificationService.showMessage(getString(com.kizunagateway.core.ui.R.string.could_not_open_file))
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = buildList {
            if (!hasPermission(Manifest.permission.RECEIVE_SMS)) add(Manifest.permission.RECEIVE_SMS)
            if (!hasPermission(Manifest.permission.READ_SMS)) add(Manifest.permission.READ_SMS)
            if (!hasPermission(Manifest.permission.SEND_SMS)) add(Manifest.permission.SEND_SMS)
            if (!hasPermission(Manifest.permission.READ_PHONE_STATE)) add(Manifest.permission.READ_PHONE_STATE)
            if (!hasPermission(Manifest.permission.READ_PHONE_NUMBERS)) add(Manifest.permission.READ_PHONE_NUMBERS)
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}
