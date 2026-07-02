package com.kizunagateway.feature.outbound

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
    val localAddress by viewModel.localAddress.collectAsState()
    val publicAddress by viewModel.publicAddress.collectAsState()
    val tunnelUrl by viewModel.baseUrl.collectAsState()

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

            if (isRunning) {
                Spacer(modifier = Modifier.height(24.dp))
                TestEndpointsCard(
                    localAddress = localAddress,
                    publicAddress = publicAddress,
                    tunnelUrl = tunnelUrl
                )
            }
        }
    }
}

@Composable
private fun TestEndpointsCard(
    localAddress: String,
    publicAddress: String,
    tunnelUrl: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.test_endpoints),
                color = KizunaColors.OnSurface,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            if (localAddress.isNotBlank()) {
                AddressRow(label = stringResource(R.string.local_address), value = localAddress)
            }

            if (publicAddress.isNotBlank()) {
                AddressRow(
                    label = stringResource(R.string.public_address),
                    value = publicAddress,
                    hint = stringResource(R.string.public_address_hint)
                )
            }

            if (tunnelUrl.isNotBlank()) {
                AddressRow(label = stringResource(R.string.tunnel_url), value = tunnelUrl)
            }
        }
    }
}

@Composable
private fun AddressRow(
    label: String,
    value: String,
    hint: String? = null
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            color = KizunaColors.Muted,
            fontSize = 12.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = KizunaColors.Primary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                clipboardManager.setText(AnnotatedString(value))
                Toast.makeText(context, context.getString(R.string.address_copied), Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.copy_address),
                    tint = KizunaColors.Muted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (hint != null) {
            Text(
                text = hint,
                color = KizunaColors.Muted,
                fontSize = 11.sp
            )
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
