package com.kizunagateway.feature.about

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.domain.model.AppNotification
import com.kizunagateway.domain.model.GatewayConfig
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.service.BackupSerializer
import com.kizunagateway.domain.service.NotificationService
import com.kizunagateway.domain.usecase.ExportBackupUseCase
import com.kizunagateway.domain.usecase.ImportBackupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class AboutViewModel @Inject constructor(
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val backupSerializer: BackupSerializer,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _gatewayConfig = MutableStateFlow<GatewayConfig?>(null)
    val gatewayConfig: StateFlow<GatewayConfig?> = _gatewayConfig.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _gatewayConfig.value = gatewayConfigRepository.getGatewayConfig()
        }
    }

    fun updateGatewayName(name: String) {
        viewModelScope.launch {
            val current = gatewayConfigRepository.getGatewayConfig()
            val updated = current.copy(gatewayName = name)
            gatewayConfigRepository.saveGatewayConfig(updated)
            _gatewayConfig.value = updated
            notificationService.showMessage("Gateway name updated")
        }
    }

    fun getBackupFileName(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        return "kizuna_gateway_backup_$timestamp.json"
    }

    fun exportToFile(uri: android.net.Uri, contentResolver: android.content.ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = exportBackupUseCase()
                val json = backupSerializer.serialize(payload)

                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    OutputStreamWriter(outputStream).use { writer ->
                        writer.write(json)
                    }
                }

                notificationService.showNotification(
                    AppNotification(
                        message = "Backup saved successfully",
                        actionLabel = "VIEW",
                        actionUri = uri.toString()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                notificationService.showMessage("Failed to export backup")
            }
        }
    }

    fun importFromFile(uri: android.net.Uri, contentResolver: android.content.ContentResolver) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = contentResolver.openInputStream(uri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        reader.readText()
                    }
                } ?: throw Exception("Could not read file")

                val payload = backupSerializer.deserialize(json)
                importBackupUseCase(payload)

                loadData()
                notificationService.showMessage("Data and configuration restored successfully")
            } catch (e: Exception) {
                e.printStackTrace()
                notificationService.showMessage("Failed to import backup")
            }
        }
    }
}
