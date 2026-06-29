package com.kizunagateway.feature.outbound

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R
import com.kizunagateway.core.ui.components.PowerSwitch
import com.kizunagateway.core.ui.theme.KizunaColors
import com.kizunagateway.domain.model.OutboundSmsStatus

@Composable
fun OutboundDashboardScreen(viewModel: OutboundViewModel) {
    val logs by viewModel.outboundLogs.collectAsState()
    val isRunning by viewModel.isServiceRunning.collectAsState()
    
    val pendingCount = logs.count { it.status == OutboundSmsStatus.PENDING }
    val sentCount = logs.count { it.status == OutboundSmsStatus.SENT || it.status == OutboundSmsStatus.DELIVERED }
    val failedCount = logs.count { it.status == OutboundSmsStatus.FAILED }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KizunaColors.Background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            PowerSwitch(
                checked = isRunning,
                onCheckedChange = { viewModel.toggleService(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isRunning) stringResource(R.string.connected) else stringResource(R.string.disconnected),
                color = if (isRunning) Color(0xFF25D366) else KizunaColors.Muted,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(stringResource(R.string.pending), pendingCount.toString(), KizunaColors.Muted, Modifier.weight(1f))
                StatCard(stringResource(R.string.sent), sentCount.toString(), KizunaColors.Primary, Modifier.weight(1f))
                StatCard(stringResource(R.string.failed), failedCount.toString(), KizunaColors.Error, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = KizunaColors.Muted)
            Text(value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
