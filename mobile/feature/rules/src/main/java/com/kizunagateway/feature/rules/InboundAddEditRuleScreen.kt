package com.kizunagateway.feature.rules
import com.kizunagateway.core.ui.theme.KizunaColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.domain.model.Rule
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRuleScreen(
    ruleId: Long?,
    viewModel: InboundRulesViewModel,
    onBack: () -> Unit
) {
    val rules by viewModel.rules.collectAsState()
    val webhooks by viewModel.webhooks.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var priority by remember { mutableStateOf("0") }
    var senderRegex by remember { mutableStateOf("") }
    var containsText by remember { mutableStateOf("") }
    var selectedWebhookId by remember { mutableLongStateOf(0L) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredWebhooks = remember(webhooks, searchQuery) {
        if (searchQuery.isBlank()) {
            webhooks
        } else {
            webhooks.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    val editingRule = remember(ruleId, rules) {
        rules.find { it.id == ruleId }
    }

    LaunchedEffect(editingRule, webhooks) {
        if (editingRule != null) {
            name = editingRule.name
            enabled = editingRule.enabled
            priority = editingRule.priority.toString()
            senderRegex = editingRule.senderRegex ?: ""
            containsText = editingRule.containsText ?: ""
            selectedWebhookId = editingRule.webhookId
        } else if (ruleId == null) {
            name = ""
            enabled = true
            priority = "0"
            senderRegex = ""
            containsText = ""
            selectedWebhookId = webhooks.firstOrNull()?.id ?: 0L
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ruleId == null) "Create Rule" else "Edit Rule") },
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
                label = { Text("Rule Name") },
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
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Enable Rule", color = KizunaColors.OnSurface)
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
                value = priority,
                onValueChange = { priority = it },
                label = { Text("Priority (Higher runs first)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = senderRegex,
                onValueChange = { senderRegex = it },
                label = { Text("Sender Regex (Optional)") },
                placeholder = { Text("^\\+628.*") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = containsText,
                onValueChange = { containsText = it },
                label = { Text("Contains Text (Optional)") },
                placeholder = { Text("OTP") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Muted,
                    focusedTextColor = KizunaColors.OnSurface,
                    unfocusedTextColor = KizunaColors.OnSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Target Webhook selector
            Box(modifier = Modifier.fillMaxWidth()) {
                val selectedName = webhooks.find { it.id == selectedWebhookId }?.name ?: "Select Webhook"
                OutlinedButton(
                    onClick = {
                        searchQuery = ""
                        expandedDropdown = true
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = KizunaColors.OnSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(KizunaColors.Muted)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Target: $selectedName")
                }
                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(KizunaColors.Surface)
                ) {
                    // Search Field inside Dropdown
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search webhook...", color = KizunaColors.Muted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KizunaColors.Primary,
                            unfocusedBorderColor = KizunaColors.Muted,
                            focusedTextColor = KizunaColors.OnSurface,
                            unfocusedTextColor = KizunaColors.OnSurface
                        ),
                        singleLine = true
                    )
                    
                    if (filteredWebhooks.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No results found", color = KizunaColors.Muted) },
                            onClick = { },
                            enabled = false
                        )
                    } else {
                        filteredWebhooks.forEach { wh ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(wh.name, color = KizunaColors.OnSurface, fontWeight = FontWeight.Bold)
                                        Text(wh.url, color = KizunaColors.Muted, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    selectedWebhookId = wh.id
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        val success = viewModel.saveRule(
                            Rule(
                                id = editingRule?.id ?: 0L,
                                name = name,
                                enabled = enabled,
                                priority = priority.toIntOrNull() ?: 0,
                                senderRegex = senderRegex.takeIf { it.isNotBlank() },
                                containsText = containsText.takeIf { it.isNotBlank() },
                                webhookId = selectedWebhookId
                            )
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
                Text("Save Rule", fontWeight = FontWeight.Bold)
            }
        }
    }
}
