package com.kizunagateway.feature.outbound

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R
import com.kizunagateway.core.ui.theme.KizunaColors
import com.kizunagateway.domain.model.ApiKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScreen(viewModel: OutboundViewModel) {
    val apiKeys by viewModel.apiKeys.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newKeyName by remember { mutableStateOf("") }
    var keyToDelete by remember { mutableStateOf<Long?>(null) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = KizunaColors.Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_api_key))
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
                    text = stringResource(R.string.api_key_credentials),
                    color = KizunaColors.OnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (apiKeys.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = stringResource(R.string.no_api_keys), color = KizunaColors.Muted)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(apiKeys) { apiKey ->
                            ApiKeyCard(
                                apiKey = apiKey,
                                onToggleActive = { viewModel.toggleApiKeyEnabled(apiKey) },
                                onDelete = { keyToDelete = apiKey.id },
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(apiKey.key))
                                    Toast.makeText(context, context.getString(R.string.api_key_copied), Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = KizunaColors.Surface,
            title = { Text(stringResource(R.string.add_api_key), color = KizunaColors.OnSurface) },
            text = {
                Column {
                    Text(stringResource(R.string.add_api_key_desc), color = KizunaColors.Muted, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newKeyName,
                        onValueChange = { newKeyName = it },
                        label = { Text(stringResource(R.string.key_name_placeholder)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = KizunaColors.Primary,
                            unfocusedBorderColor = KizunaColors.Muted,
                            focusedLabelColor = KizunaColors.Primary,
                            unfocusedLabelColor = KizunaColors.Muted,
                            cursorColor = KizunaColors.Primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newKeyName.isNotBlank()) {
                            viewModel.generateApiKey(newKeyName)
                            newKeyName = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.generate), color = KizunaColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text(stringResource(R.string.cancel), color = KizunaColors.Muted)
                }
            }
        )
    }

    if (keyToDelete != null) {
        AlertDialog(
            onDismissRequest = { keyToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text(stringResource(R.string.delete_api_key), color = KizunaColors.OnSurface) },
            text = { Text(stringResource(R.string.delete_api_key_confirm), color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteApiKey(keyToDelete!!)
                        keyToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { keyToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = KizunaColors.Muted)
                }
            }
        )
    }
}

@Composable
fun ApiKeyCard(
    apiKey: ApiKey,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    val displayKey = "•".repeat(apiKey.key.length.coerceAtMost(16))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (apiKey.isActive) KizunaColors.Surface else KizunaColors.Surface.copy(alpha = 0.5f)
        ),
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
                    text = apiKey.name,
                    color = if (apiKey.isActive) KizunaColors.OnSurface else KizunaColors.Muted,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = displayKey,
                        color = KizunaColors.Muted,
                        fontSize = 13.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.copy_key),
                            tint = KizunaColors.Muted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = KizunaColors.Primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        val rateText = if (apiKey.smsPerMinute > 0) "${apiKey.smsPerMinute} SMS/min" else stringResource(R.string.unlimited)
                        Text(
                            text = stringResource(R.string.rate, rateText),
                            color = KizunaColors.Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = apiKey.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = KizunaColors.Primary,
                        checkedTrackColor = KizunaColors.Primary.copy(alpha = 0.5f),
                        uncheckedThumbColor = KizunaColors.Muted,
                        uncheckedTrackColor = KizunaColors.Muted.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.scale(0.7f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = Color(0xFFF87171),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
