package com.kizunagateway.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.core.ui.R
import com.kizunagateway.domain.model.AppNotification
import com.kizunagateway.domain.model.CustomVariable
import com.kizunagateway.domain.model.GatewayConfig
import com.kizunagateway.domain.repository.GatewayConfigRepository
import com.kizunagateway.domain.repository.VariableRepository
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
class InboundSettingsViewModel @Inject constructor(
    private val gatewayConfigRepository: GatewayConfigRepository,
    private val variableRepository: VariableRepository,
    private val exportBackupUseCase: ExportBackupUseCase,
    private val importBackupUseCase: ImportBackupUseCase,
    private val backupSerializer: BackupSerializer,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _gatewayConfig = MutableStateFlow<GatewayConfig?>(null)
    val gatewayConfig: StateFlow<GatewayConfig?> = _gatewayConfig.asStateFlow()

    private val _variables = MutableStateFlow<List<CustomVariable>>(emptyList())
    val variables: StateFlow<List<CustomVariable>> = _variables.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _gatewayConfig.value = gatewayConfigRepository.getGatewayConfig()
        }
        viewModelScope.launch {
            variableRepository.getVariablesFlow().collect { list ->
                _variables.value = list
            }
        }
    }

    fun updateGatewayName(name: String) {
        viewModelScope.launch {
            val current = gatewayConfigRepository.getGatewayConfig()
            val updated = current.copy(gatewayName = name)
            gatewayConfigRepository.saveGatewayConfig(updated)
            _gatewayConfig.value = updated
            notificationService.showMessage(R.string.gateway_name_updated)
        }
    }

    fun updateDeleteUntrackedSms(delete: Boolean) {
        viewModelScope.launch {
            val current = gatewayConfigRepository.getGatewayConfig()
            val updated = current.copy(deleteUntrackedSms = delete)
            gatewayConfigRepository.saveGatewayConfig(updated)
            _gatewayConfig.value = updated
            notificationService.showMessage(if (delete) R.string.untracked_sms_deletion_enabled else R.string.untracked_sms_deletion_disabled)
        }
    }

    fun saveVariable(variable: CustomVariable) {
        viewModelScope.launch {
            variableRepository.insertVariable(variable)
            loadData()
            notificationService.showMessage(R.string.variable_saved)
        }
    }

    fun deleteVariable(key: String) {
        viewModelScope.launch {
            variableRepository.deleteVariable(key)
            loadData()
        }
    }

    fun showToast(message: String) {
        notificationService.showMessage(message)
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
                notificationService.showMessage(R.string.export_failed)
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
                notificationService.showMessage(R.string.import_success)
            } catch (e: Exception) {
                e.printStackTrace()
                notificationService.showMessage(R.string.import_failed)
            }
        }
    }
}
