package com.kizunagateway.feature.home

import android.content.Context
import android.os.PowerManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizunagateway.domain.model.GatewayConfig
import com.kizunagateway.domain.repository.GatewayConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val gatewayConfig: GatewayConfig? = null,
    val isBatteryOptimized: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gatewayConfigRepository: GatewayConfigRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun checkBatteryOptimization() {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.packageName)
        _uiState.value = _uiState.value.copy(isBatteryOptimized = isIgnoring)
    }

    fun loadData() {
        checkBatteryOptimization()
        viewModelScope.launch {
            gatewayConfigRepository.getGatewayConfigFlow().collect { config ->
                _uiState.value = _uiState.value.copy(gatewayConfig = config)
            }
        }
    }
}
