package com.kizunagateway.feature.dashboard
import com.kizunagateway.core.ui.theme.KizunaColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    showHeaderOnly: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    DashboardContent(uiState = uiState, showHeaderOnly = showHeaderOnly)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    uiState: DashboardUiState,
    showHeaderOnly: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KizunaColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (showHeaderOnly) {
            Text(
                text = stringResource(R.string.dashboard),
                color = KizunaColors.OnSurface,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = uiState.gatewayConfig?.gatewayName ?: stringResource(R.string.app_name),
                        color = KizunaColors.OnSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ID: ${uiState.gatewayConfig?.gatewayId ?: stringResource(R.string.pending)}",
                        color = KizunaColors.Primary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Stats Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.sms_today),
                value = uiState.smsToday.toString(),
                color = KizunaColors.Primary, // Replaced hardcoded
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.queue_size),
                value = uiState.queueSize.toString(),
                color = Color(0xFFFBBF24), // Keep some functional colors but maybe move to theme later
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = stringResource(R.string.success),
                value = uiState.webhookSuccess.toString(),
                color = Color(0xFF25D366),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.failed),
                value = uiState.webhookFailed.toString(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        // Last activity log
        Card(
            colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.last_sms_received),
                    color = KizunaColors.OnSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.lastSms != null) {
                    Text(
                        text = "${stringResource(R.string.from)}: ${uiState.lastSms.sender}",
                        color = KizunaColors.Primary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.lastSms.message,
                        color = KizunaColors.OnSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                } else {
                    Text(
                        text = stringResource(R.string.no_messages_received),
                        color = KizunaColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.last_successful_webhook),
                    color = KizunaColors.OnSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.lastSuccessLog != null) {
                    val webhookDisplayName = uiState.lastWebhookName ?: "ID: ${uiState.lastSuccessLog.webhookId}"
                    Text(
                        text = "${stringResource(R.string.to)}: $webhookDisplayName",
                        color = Color(0xFF25D366),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stringResource(R.string.response_code)}: ${uiState.lastSuccessLog.responseCode}",
                        color = KizunaColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        text = stringResource(R.string.no_webhook_delivered),
                        color = KizunaColors.Muted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, color = KizunaColors.Muted, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}
