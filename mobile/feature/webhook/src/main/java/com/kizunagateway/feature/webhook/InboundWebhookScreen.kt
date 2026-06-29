package com.kizunagateway.feature.webhook
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebhookScreen(
    viewModel: WebhookViewModel,
    onAddWebhook: () -> Unit,
    onEditWebhook: (Long) -> Unit,
    onManageGlobalHeaders: () -> Unit
) {
    val webhooks by viewModel.webhooks.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val context = LocalContext.current

    var webhookToDelete by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWebhook,
                containerColor = KizunaColors.Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_webhook))
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
                        text = stringResource(R.string.webhooks),
                        color = KizunaColors.OnSurface,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onManageGlobalHeaders) {
                        Text(stringResource(R.string.global_headers), color = KizunaColors.Primary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (webhooks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_webhooks), color = KizunaColors.Muted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(webhooks) { webhook ->
                            Card(
                                onClick = { onEditWebhook(webhook.id) },
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
                                        Text(
                                            text = webhook.name,
                                            color = KizunaColors.OnSurface,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 16.sp
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                color = KizunaColors.Primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = webhook.method,
                                                    color = KizunaColors.Primary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = webhook.url,
                                                color = KizunaColors.Muted,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }

                                        val associatedRules = rules.filter { it.webhookId == webhook.id }
                                        if (associatedRules.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "${stringResource(R.string.used_by)}: ${associatedRules.joinToString { it.name }}",
                                                color = Color(0xFF25D366),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Switch(
                                            checked = webhook.enabled,
                                            onCheckedChange = { viewModel.toggleWebhookEnabled(webhook) },
                                            colors = SwitchDefaults.colors(
                                                checkedThumbColor = KizunaColors.Primary,
                                                checkedTrackColor = KizunaColors.Primary.copy(alpha = 0.5f),
                                                uncheckedThumbColor = KizunaColors.Muted,
                                                uncheckedTrackColor = KizunaColors.Muted.copy(alpha = 0.5f)
                                            ),
                                            modifier = Modifier.scale(0.7f)
                                        )
                                        Surface(
                                            onClick = { webhookToDelete = webhook.id },
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

    if (webhookToDelete != null) {
        AlertDialog(
            onDismissRequest = { webhookToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text(stringResource(R.string.delete_webhook), color = KizunaColors.OnSurface) },
            text = { Text(stringResource(R.string.delete_webhook_confirm), color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteWebhook(webhookToDelete!!)
                        webhookToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { webhookToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = KizunaColors.Muted)
                }
            }
        )
    }
}
