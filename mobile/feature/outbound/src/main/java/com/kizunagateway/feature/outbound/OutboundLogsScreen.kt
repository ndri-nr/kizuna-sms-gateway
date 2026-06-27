package com.kizunagateway.feature.outbound

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.theme.KizunaColors
import com.kizunagateway.domain.model.OutboundSms
import com.kizunagateway.domain.model.OutboundSmsStatus
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutboundLogsScreen(viewModel: OutboundViewModel) {
    val logs by viewModel.outboundLogs.collectAsState()
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(KizunaColors.Background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SMS Logs",
                        color = KizunaColors.OnSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = Color(0xFFF87171))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (logs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No outbound messages logged", color = KizunaColors.Muted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(logs) { sms ->
                            OutboundSmsCard(
                                sms = sms,
                                onDelete = { logToDelete = sms.id }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = KizunaColors.Surface,
            title = { Text("Clear Outbound Logs", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to delete all outbound SMS logs? This action cannot be undone.", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAllLogs()
                        showClearConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = KizunaColors.Muted)
                }
            }
        )
    }

    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text("Delete Log", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to delete this log entry?", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteLog(logToDelete!!)
                        logToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text("Cancel", color = KizunaColors.Muted)
                }
            }
        )
    }
}

@Composable
fun OutboundSmsCard(sms: OutboundSms, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val statusColor = when (sms.status) {
        OutboundSmsStatus.SENT, OutboundSmsStatus.DELIVERED -> Color(0xFF25D366)
        OutboundSmsStatus.FAILED -> Color(0xFFF87171)
        OutboundSmsStatus.SENDING -> KizunaColors.Primary
        else -> KizunaColors.Muted
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sms.phoneNumber,
                    color = KizunaColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = sms.createdAt.format(formatter),
                    color = KizunaColors.Muted,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sms.message,
                color = KizunaColors.OnSurface,
                fontSize = 14.sp
            )
            
            sms.errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: $it",
                    color = Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = sms.status.name,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFF87171).copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
