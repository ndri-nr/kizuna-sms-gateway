package com.kizunagateway.feature.webhook
import com.kizunagateway.core.ui.theme.KizunaColors

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.domain.model.Webhook
import com.kizunagateway.domain.model.WebhookHeader

import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditWebhookScreen(
    webhookId: Long?,
    viewModel: WebhookViewModel,
    onBack: () -> Unit
) {
    val webhooks by viewModel.webhooks.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var url by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("POST") }
    var timeout by remember { mutableStateOf("30") }
    var retryCount by remember { mutableStateOf("5") }
    var autoReplyMessage by remember { mutableStateOf("") }
    var bodyTemplate by remember { mutableStateOf("") }
    val headers = remember { mutableStateListOf<WebhookHeader>() }
    
    var headerKey by remember { mutableStateOf("") }
    var headerValue by remember { mutableStateOf("") }
    var headerToDeleteIndex by remember { mutableStateOf<Int?>(null) }
    
    val editingWebhook = remember(webhookId, webhooks) {
        webhooks.find { it.id == webhookId }
    }

    LaunchedEffect(editingWebhook) {
        if (editingWebhook != null) {
            name = editingWebhook.name
            enabled = editingWebhook.enabled
            url = editingWebhook.url
            method = editingWebhook.method
            timeout = editingWebhook.timeoutSeconds.toString()
            retryCount = editingWebhook.retryCount.toString()
            autoReplyMessage = editingWebhook.autoReplyMessage ?: ""
            headers.clear()
            headers.addAll(viewModel.getHeaders(editingWebhook.id))
            bodyTemplate = viewModel.getTemplate(editingWebhook.id)?.bodyTemplate ?: ""
        } else if (webhookId == null) {
            name = ""
            enabled = true
            url = "https://"
            method = "POST"
            timeout = "30"
            retryCount = "5"
            bodyTemplate = "{\n  \"sender\":\"{{sender}}\",\n  \"message\":\"{{message}}\",\n  \"receivedAt\":\"{{receivedAt}}\"\n}"
            headers.clear()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (webhookId == null) "Create Webhook" else "Edit Webhook") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = KizunaColors.Surface,
                    titleContentColor = KizunaColors.OnSurface,
                    navigationIconContentColor = KizunaColors.OnSurface
                )
            )
        },
        containerColor = KizunaColors.Background
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Webhook Name") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Webhook", color = KizunaColors.OnSurface)
                Switch(
                    checked = enabled,
                    onCheckedChange = { enabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KizunaColors.Primary,
                        checkedTrackColor = KizunaColors.Primary.copy(alpha = 0.5f)
                    )
                )
            }

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Webhook URL") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Method", color = KizunaColors.Muted, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("POST", "PUT", "PATCH").forEach { m ->
                    Button(
                        onClick = { method = m },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (method == m) KizunaColors.Primary else KizunaColors.Surface,
                            contentColor = if (method == m) Color.White else KizunaColors.Muted
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(m)
                    }
                }
            }

            OutlinedTextField(
                value = timeout,
                onValueChange = { timeout = it },
                label = { Text("Timeout (Seconds)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Webhook-Specific Headers
            Text("Webhook Headers", color = KizunaColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Card(
                colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = headerKey,
                            onValueChange = { headerKey = it },
                            label = { Text("Key") },
                            placeholder = { Text("X-Custom") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KizunaColors.Primary,
                                focusedLabelColor = KizunaColors.Primary,
                                unfocusedBorderColor = KizunaColors.Muted,
                                focusedTextColor = KizunaColors.OnSurface,
                                unfocusedTextColor = KizunaColors.OnSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = headerValue,
                            onValueChange = { headerValue = it },
                            label = { Text("Value") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = KizunaColors.Primary,
                                focusedLabelColor = KizunaColors.Primary,
                                unfocusedBorderColor = KizunaColors.Muted,
                                focusedTextColor = KizunaColors.OnSurface,
                                unfocusedTextColor = KizunaColors.OnSurface
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            if (headerKey.isNotBlank() && headerValue.isNotBlank()) {
                                headers.add(WebhookHeader(webhookId = webhookId ?: 0L, key = headerKey, value = headerValue))
                                headerKey = ""
                                headerValue = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Header")
                    }

                    headers.forEachIndexed { index, h ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(h.key, color = KizunaColors.Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(h.value, color = KizunaColors.OnSurface, fontSize = 13.sp)
                            }
                            IconButton(onClick = { headerToDeleteIndex = index }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("JSON Payload Template", color = KizunaColors.OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row {
                        TextButton(onClick = {
                            bodyTemplate = viewModel.formatJson(bodyTemplate)
                        }) {
                            Text("Beautify", fontSize = 12.sp)
                        }
                        TextButton(onClick = { bodyTemplate = "" }) {
                            Text("Clear", color = Color(0xFFF87171), fontSize = 12.sp)
                        }
                    }
                }
                OutlinedTextField(
                    value = bodyTemplate,
                    onValueChange = { bodyTemplate = it },
                    placeholder = { Text("{ \"message\": \"{{message}}\" }") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KizunaColors.Primary,
                        focusedLabelColor = KizunaColors.Primary,
                        unfocusedBorderColor = KizunaColors.Muted,
                        focusedTextColor = KizunaColors.OnSurface,
                        unfocusedTextColor = KizunaColors.OnSurface
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    ),
                    minLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                VariableHelper()
            }

            OutlinedTextField(
                value = autoReplyMessage,
                onValueChange = { autoReplyMessage = it },
                label = { Text("Auto Reply Message (Optional)") },
                placeholder = { Text("Thank you, we already accepted your message.") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.testWebhook(
                        Webhook(
                            id = editingWebhook?.id ?: 0L,
                            name = name,
                            url = url,
                            method = method,
                            timeoutSeconds = timeout.toIntOrNull() ?: 30,
                            retryCount = retryCount.toIntOrNull() ?: 5,
                            autoReplyMessage = autoReplyMessage.takeIf { it.isNotBlank() }
                        ),
                        headers,
                        bodyTemplate,
                        "62812345678",
                        "Hello, this is message from Kizuna-Gateway"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Webhook")
            }

            if (testResult != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (testResult?.success == true) "SUCCESS" else "FAILED",
                            color = if (testResult?.success == true) Color(0xFF25D366) else Color(0xFFF87171),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Code: ${testResult?.responseCode}",
                            color = KizunaColors.OnSurface,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Sent Headers:",
                            color = KizunaColors.OnSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        SelectionContainer {
                            Text(
                                text = testResult?.sentHeaders?.entries?.joinToString("\n") { "${it.key}: ${it.value}" } ?: "",
                                color = KizunaColors.OnSurface,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(KizunaColors.Background)
                                    .padding(8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Rendered JSON Payload:",
                            color = KizunaColors.OnSurface,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        SelectionContainer {
                            Text(
                                text = viewModel.formatJson(testResult?.renderedBody ?: ""),
                                color = KizunaColors.OnSurface,
                                fontSize = 10.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(KizunaColors.Background)
                                    .padding(8.dp)
                            )
                        }

                        if (!testResult?.responseBody.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Response:",
                                color = KizunaColors.OnSurface,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            SelectionContainer {
                                Text(
                                    text = viewModel.formatJson(testResult?.responseBody ?: ""),
                                    color = KizunaColors.OnSurface,
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(KizunaColors.Background)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = viewModel.saveWebhook(
                            Webhook(
                                id = editingWebhook?.id ?: 0L,
                                name = name,
                                enabled = enabled,
                                url = url,
                                method = method,
                                timeoutSeconds = timeout.toIntOrNull() ?: 30,
                                retryCount = retryCount.toIntOrNull() ?: 5,
                                autoReplyMessage = autoReplyMessage.takeIf { it.isNotBlank() }
                            ),
                            headers,
                            bodyTemplate
                        )
                        if (success) {
                            onBack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Webhook", fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (headerToDeleteIndex != null) {
        AlertDialog(
            onDismissRequest = { headerToDeleteIndex = null },
            containerColor = KizunaColors.Surface,
            title = { Text("Remove Header", color = KizunaColors.OnSurface) },
            text = { Text("Are you sure you want to remove this header?", color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        headerToDeleteIndex?.let { headers.removeAt(it) }
                        headerToDeleteIndex = null
                    }
                ) {
                    Text("Remove", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { headerToDeleteIndex = null }) {
                    Text("Cancel", color = KizunaColors.Muted)
                }
            }
        )
    }
}

@Composable
fun VariableHelper() {
    var expanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val variables = listOf(
        "sender" to "Sender's phone number",
        "receiver" to "Receiver's phone number",
        "message" to "SMS message body",
        "receivedAt" to "Time when message was received",
        "messageId" to "Unique message ID",
        "gatewayId" to "Gateway ID",
        "gatewayName" to "Gateway Name",
        "deviceId" to "Android Device ID",
        "deviceModel" to "Phone Model",
        "manufacturer" to "Device Manufacturer",
        "androidVersion" to "Android Version",
        "appVersion" to "Kizuna: SMS Gateway App Version",
        "phoneNumber" to "SIM Phone Number",
        "displayName" to "SIM Display Name",
        "simSlot" to "SIM Slot Index (0/1)",
        "subId" to "SIM Subscription ID",
        "currentDate" to "Today's Date (YYYY-MM-DD)",
        "currentTime" to "Current Time (HH:mm:ss)",
        "currentTimestamp" to "Current Unix Timestamp (ms)"
    )

    Column {
        TextButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                if (expanded) "Hide variables" else "Show available variables",
                color = KizunaColors.Primary,
                fontSize = 12.sp
            )
        }

        if (expanded) {
            Card(
                colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Click a variable to copy its placeholder (e.g. {{sender}})",
                        color = KizunaColors.Muted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    variables.forEach { (key, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("{{$key}}"))
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "{{$key}}",
                                color = KizunaColors.Primary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(0.4f)
                            )
                            Text(
                                text = desc,
                                color = KizunaColors.OnSurface,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
