package com.kizunagateway.feature.rules
import com.kizunagateway.core.ui.theme.KizunaColors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RulesScreen(
    viewModel: InboundRulesViewModel,
    onAddRule: () -> Unit,
    onEditRule: (Long) -> Unit
) {
    val rules by viewModel.rules.collectAsState()
    val webhooks by viewModel.webhooks.collectAsState()

    var ruleToDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRule,
                containerColor = KizunaColors.Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_rule))
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
                Text(
                    text = stringResource(R.string.routing_rules),
                    color = KizunaColors.OnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (rules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_routing_rules), color = KizunaColors.Muted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(rules) { rule ->
                            val targetWebhook = webhooks.find { it.id == rule.webhookId }?.name ?: stringResource(R.string.unknown)
                            Card(
                                onClick = { onEditRule(rule.id) },
                                colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = rule.name,
                                                color = KizunaColors.OnSurface,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = KizunaColors.Primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "P${rule.priority}",
                                                    color = KizunaColors.Primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.forward_to, targetWebhook),
                                            color = Color(0xFF25D366),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (!rule.senderRegex.isNullOrBlank()) {
                                            Text(
                                                text = stringResource(R.string.sender_regex,
                                                    rule.senderRegex!!
                                                ),
                                                color = KizunaColors.Muted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        if (!rule.containsText.isNullOrBlank()) {
                                            Text(
                                                text = stringResource(R.string.contains,
                                                    rule.containsText!!
                                                ),
                                                color = KizunaColors.Muted,
                                                fontSize = 12.sp
                                            )
                                        }
                                        if (rule.senderRegex.isNullOrBlank() && rule.containsText.isNullOrBlank()) {
                                            Text(
                                                text = stringResource(R.string.catch_all_rule),
                                                color = Color(0xFFFBBF24),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Switch(
                                            checked = rule.enabled,
                                            onCheckedChange = { viewModel.toggleRuleEnabled(rule) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = KizunaColors.Primary,
                                                checkedTrackColor = KizunaColors.Primary.copy(alpha = 0.5f),
                                                uncheckedThumbColor = KizunaColors.Muted,
                                                uncheckedTrackColor = KizunaColors.Muted.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier.scale(0.7f)
                                        )
                                        Surface(
                                            onClick = { ruleToDelete = rule.id },
                                            color = Color.Transparent,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = stringResource(R.string.delete),
                                                    tint = Color(0xFFF87171),
                                                    modifier = Modifier.size(30.dp)
                                                )
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
    }

    if (ruleToDelete != null) {
        AlertDialog(
            onDismissRequest = { ruleToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text(stringResource(R.string.delete_rule), color = KizunaColors.OnSurface) },
            text = { Text(stringResource(R.string.delete_rule_confirm), color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRule(ruleToDelete!!)
                        ruleToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { ruleToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = KizunaColors.Muted)
                }
            }
        )
    }
}
