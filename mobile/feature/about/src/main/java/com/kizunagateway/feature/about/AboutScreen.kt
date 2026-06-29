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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.kizunagateway.feature.about.BuildConfig
import com.kizunagateway.core.ui.R
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
            title = { Text(stringResource(R.string.edit_gateway_name)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateGatewayName(name)
                    showEditNameDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(stringResource(R.string.cancel))
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
        // Language Switcher Section
        Text(
            text = stringResource(R.string.language),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentLang = gatewayConfig?.language ?: "en"
                
                Button(
                    onClick = { viewModel.updateLanguage("en") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (currentLang == "en") {
                        ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = KizunaColors.Background, contentColor = KizunaColors.Muted)
                    }
                ) {
                    Text(stringResource(R.string.english))
                }

                Button(
                    onClick = { viewModel.updateLanguage("in") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = if (currentLang == "in") {
                        ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary)
                    } else {
                        ButtonDefaults.buttonColors(containerColor = KizunaColors.Background, contentColor = KizunaColors.Muted)
                    }
                ) {
                    Text(stringResource(R.string.indonesian))
                }
            }
        }

        // Gateway Name Section
        Text(
            text = stringResource(R.string.gateway_detail),
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
                        text = stringResource(R.string.gateway_name),
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
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.name), tint = KizunaColors.Primary)
                }
            }
        }

        // Backup and Restore
        Text(
            text = stringResource(R.string.backup_and_restore),
            style = MaterialTheme.typography.titleMedium,
            color = KizunaColors.OnSurface,
            fontWeight = FontWeight.Bold
        )

        Button(
            onClick = { exportLauncher.launch(viewModel.getBackupFileName()) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary)
        ) {
            Text(stringResource(R.string.backup_data))
        }

        OutlinedButton(
            onClick = { importLauncher.launch(arrayOf("application/json")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.restore_data))
        }

        // Support
        Text(
            text = stringResource(R.string.support),
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
                    text = stringResource(R.string.support_desc),
                    color = KizunaColors.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "mailto:4ndri.nr@gmail.com".toUri()
                            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.support_email_subject))
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
                    Text(stringResource(R.string.email_support))
                }
            }
        }

        // Legal
        Text(
            text = stringResource(R.string.legal),
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
                    Text(stringResource(R.string.terms_and_conditions), color = KizunaColors.Primary)
                }
                TextButton(onClick = onNavigateToPrivacy) {
                    Text(stringResource(R.string.privacy_policy), color = KizunaColors.Primary)
                }
            }
        }

        Text(
            text = stringResource(R.string.version),
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
                    text = "${stringResource(R.string.version)}: ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodySmall,
                    color = KizunaColors.Muted
                )
                Text(
                    text = stringResource(R.string.rights_reserved),
                    style = MaterialTheme.typography.bodySmall,
                    color = KizunaColors.Muted
                )
            }
        }
    }
}
