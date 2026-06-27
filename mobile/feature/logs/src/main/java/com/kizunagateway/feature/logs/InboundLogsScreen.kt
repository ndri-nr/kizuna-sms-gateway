package com.kizunagateway.feature.logs
import com.kizunagateway.core.ui.theme.KizunaColors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.domain.model.DeliveryLog
import com.kizunagateway.domain.model.SmsMessage
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(viewModel: LogsViewModel) {
    val groupedLogs by viewModel.groupedLogs.collectAsState()
    val smsList by viewModel.smsList.collectAsState()
    val webhookNames by viewModel.webhookNames.collectAsState()
    val hasMoreLogs by viewModel.hasMoreLogs.collectAsState()
    val hasMoreSms by viewModel.hasMoreSms.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showClearSmsConfirmDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<Long?>(null) }
    var groupToDelete by remember { mutableStateOf<Long?>(null) }
    var smsToDelete by remember { mutableStateOf<Long?>(null) }
    val tabs = listOf("Delivery Logs", "Received SMS")

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(KizunaColors.Surface)
                    .padding(top = 8.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = KizunaColors.Primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = KizunaColors.Primary
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { 
                                Text(
                                    title, 
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == index) KizunaColors.Primary else KizunaColors.Muted
                                ) 
                            }
                        )
                    }
                }
            }
        },
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
                        text = tabs[selectedTab],
                        color = KizunaColors.OnSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedTab == 0 && groupedLogs.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear Logs", tint = Color(0xFFF87171))
                        }
                    } else if (selectedTab == 1 && smsList.isNotEmpty()) {
                        IconButton(onClick = { showClearSmsConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear SMS", tint = Color(0xFFF87171))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (selectedTab == 0) {
                    if (groupedLogs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No delivery logs", color = KizunaColors.Muted)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(groupedLogs) { groupedLog ->
                                GroupedDeliveryLogCard(
                                    groupedLog = groupedLog,
                                    webhookNames = webhookNames,
                                    onRetry = { log -> viewModel.retryDelivery(log) },
                                    onDeleteGroup = { groupToDelete = groupedLog.smsId },
                                    onDeleteLog = { logId -> logToDelete = logId },
                                    formatHeaders = { viewModel.formatHeaders(it) },
                                    formatJson = { viewModel.formatJson(it) }
                                )
                            }
                            if (hasMoreLogs) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        TextButton(onClick = { viewModel.loadMoreLogs() }) {
                                            Text("Load More Logs", color = KizunaColors.Primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (smsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No SMS messages received", color = KizunaColors.Muted)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(smsList) { sms ->
                                SmsLogCard(sms = sms, onDelete = { smsToDelete = sms.id })
                            }
                            if (hasMoreSms) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        TextButton(onClick = { viewModel.loadMoreSms() }) {
                                            Text("Load More SMS", color = KizunaColors.Primary)
                                        }
                                    }
                                }
                            }
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
            title = { Text("Clear Logs", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to delete all delivery logs? This action cannot be undone.", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearLogs()
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

    if (showClearSmsConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearSmsConfirmDialog = false },
            containerColor = KizunaColors.Surface,
            title = { Text("Clear All SMS", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to delete all received SMS messages? This action cannot be undone.", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllSms()
                        showClearSmsConfirmDialog = false
                    }
                ) {
                    Text("Clear All", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearSmsConfirmDialog = false }) {
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
            text = { Text("Are you sure you want to delete this delivery log entry?", color = KizunaColors.Muted) },
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

    if (groupToDelete != null) {
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text("Delete Log Group", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to delete all logs for this SMS? This action cannot be undone.", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGroupedLogs(groupToDelete!!)
                        groupToDelete = null
                    }
                ) {
                    Text("Delete All", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) {
                    Text("Cancel", color = KizunaColors.Muted)
                }
            }
        )
    }

    if (smsToDelete != null) {
        AlertDialog(
            onDismissRequest = { smsToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text("Delete SMS", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to delete this received SMS message?", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSms(smsToDelete!!)
                        smsToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { smsToDelete = null }) {
                    Text("Cancel", color = KizunaColors.Muted)
                }
            }
        )
    }
}

@Composable
fun GroupedDeliveryLogCard(
    groupedLog: GroupedDeliveryLog,
    webhookNames: Map<Long, String>,
    onRetry: (DeliveryLog) -> Unit,
    onDeleteGroup: () -> Unit,
    onDeleteLog: (Long) -> Unit,
    formatHeaders: (String) -> String,
    formatJson: (String) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    val latest = groupedLog.latestLog
    val statusColor = if (groupedLog.success) Color(0xFF25D366) else Color(0xFFF87171)

    Card(
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SMS ID: ${groupedLog.smsId}",
                        color = KizunaColors.OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    val webhookName = if (latest.webhookId == 0L) "None" else webhookNames[latest.webhookId] ?: "ID: ${latest.webhookId}"
                    Text(
                        text = "Attempts: ${groupedLog.logs.size} | Latest: $webhookName",
                        color = KizunaColors.Muted,
                        fontSize = 12.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (groupedLog.success) "Delivered" else "Failed",
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (!groupedLog.success) {
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onRetry(latest) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Retry Latest",
                                tint = KizunaColors.Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteGroup,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Group",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = KizunaColors.Background)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Delivery History",
                    color = KizunaColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                groupedLog.logs.forEachIndexed { index, log ->
                    LogHistoryItem(
                        log = log,
                        webhookName = webhookNames[log.webhookId] ?: "ID: ${log.webhookId}",
                        onDeleteLog = { onDeleteLog(log.id) },
                        formatHeaders = formatHeaders,
                        formatJson = formatJson
                    )
                    if (index < groupedLog.logs.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LogHistoryItem(
    log: DeliveryLog,
    webhookName: String,
    onDeleteLog: () -> Unit,
    formatHeaders: (String) -> String,
    formatJson: (String) -> String
) {
    val statusColor = if (log.success) Color(0xFF25D366) else Color(0xFFF87171)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(KizunaColors.Background, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Webhook: ${if (log.webhookId == 0L) "None" else webhookName}",
                    color = KizunaColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (log.responseCode > 0) "HTTP ${log.responseCode}" else if (log.success) "Success" else "Failed",
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDeleteLog,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Log",
                            tint = Color(0xFFF87171),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Text(
                text = "Time: ${Instant.ofEpochMilli(log.createdAt).toString().take(19).replace("T", " ")}",
                color = KizunaColors.Muted,
                fontSize = 10.sp
            )

            if (log.requestHeaders.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Request Headers:",
                    color = KizunaColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                SelectionContainer {
                    Text(
                        text = formatHeaders(log.requestHeaders),
                        color = KizunaColors.OnSurface,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x11FFFFFF), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Request Payload:",
                color = KizunaColors.Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            SelectionContainer {
                Text(
                    text = formatJson(log.requestBody),
                    color = KizunaColors.OnSurface,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x11FFFFFF), RoundedCornerShape(4.dp))
                        .padding(8.dp)
                )
            }

            if (log.responseBody.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Response Body:",
                    color = KizunaColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                SelectionContainer {
                    Text(
                        text = formatJson(log.responseBody),
                        color = KizunaColors.OnSurface,
                        fontSize = 10.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x11FFFFFF), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SmsLogCard(sms: SmsMessage, onDelete: () -> Unit) {
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
                    text = sms.sender,
                    color = KizunaColors.Primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = sms.receivedAt.take(19).replace("T", " "),
                    color = KizunaColors.Muted,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = sms.message,
                color = KizunaColors.OnSurface,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (sms.processed) "Processed" else "Pending",
                    color = if (sms.processed) Color(0xFF25D366) else Color(0xFFFBBF24),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete SMS",
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
