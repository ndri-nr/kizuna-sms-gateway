package com.kizunagateway.feature.about

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kizunagateway.core.ui.theme.KizunaColors

@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit
) {
    val gatewayConfig by viewModel.gatewayConfig.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var showEditNameDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportToFile(it, context.contentResolver) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromFile(it, context.contentResolver) }
    }

    if (showEditNameDialog) {
        var name by remember { mutableStateOf(gatewayConfig?.gatewayName ?: "") }
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Gateway Name") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateGatewayName(name)
                    showEditNameDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KizunaColors.Background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Gateway Name Section
        Text(
            text = "Gateway Detail",
            style = MaterialTheme.typography.titleMedium,
            color = KizunaColors.OnSurface,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gateway Name",
                        style = MaterialTheme.typography.labelMedium,
                        color = KizunaColors.Muted
                    )
                    Text(
                        text = gatewayConfig?.gatewayName ?: "Kizuna: SMS Gateway",
                        style = MaterialTheme.typography.titleLarge,
                        color = KizunaColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { showEditNameDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = KizunaColors.Primary)
                }
            }
        }

        // Backup and Restore
        Text(
            text = "Backup and Restore",
            style = MaterialTheme.typography.titleMedium,
            color = KizunaColors.OnSurface,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = { exportLauncher.launch(viewModel.getBackupFileName()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary)
        ) {
            Text("Backup Data")
        }

        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore Data")
        }

        // Support
        Text(
            text = "Support",
            style = MaterialTheme.typography.titleMedium,
            color = KizunaColors.OnSurface,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Need help or have suggestions? Contact us via email.",
                    color = KizunaColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:4ndri.nr@gmail.com".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, "Kizuna: SMS Gateway Support")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            // Handle if no email client is installed
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary)
                ) {
                    Text("Email Support")
                }
            }
        }

        // Legal
        Text(
            text = "Legal",
            style = MaterialTheme.typography.titleMedium,
            color = KizunaColors.OnSurface,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onNavigateToTerms) {
                    Text("Terms and Conditions", color = KizunaColors.Primary)
                }
                TextButton(onClick = onNavigateToPrivacy) {
                    Text("Privacy Policy", color = KizunaColors.Primary)
                }
            }
        }

        Text(
            text = "Version",
            style = MaterialTheme.typography.titleMedium,
            color = KizunaColors.OnSurface,
            fontWeight = FontWeight.Bold
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Version: 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = KizunaColors.Muted
                )
                Text(
                    text = "© 2026 Kizuna: SMS Gateway. All rights reserved.",
                    style = MaterialTheme.typography.bodySmall,
                    color = KizunaColors.Muted
                )
            }
        }
    }
}
