package com.kizunagateway.feature.webhook
import com.kizunagateway.core.ui.theme.KizunaColors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalHeadersScreen(
    viewModel: WebhookViewModel,
    onBack: () -> Unit
) {
    val globalHeaders by viewModel.globalHeaders.collectAsState()
    var varKey by remember { mutableStateOf("") }
    var varValue by remember { mutableStateOf("") }
    var headerToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.global_headers)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
            Text(
                text = stringResource(R.string.global_headers_desc),
                color = KizunaColors.Muted,
                fontSize = 14.sp
            )

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
                            value = varKey,
                            onValueChange = { varKey = it },
                            label = { Text(stringResource(R.string.header_key)) },
                            placeholder = { Text(stringResource(R.string.header_key_placeholder)) },
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
                            value = varValue,
                            onValueChange = { varValue = it },
                            label = { Text(stringResource(R.string.header_value)) },
                            placeholder = { Text(stringResource(R.string.header_value_placeholder)) },
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
                            if (varKey.isNotBlank() && varValue.isNotBlank()) {
                                viewModel.saveGlobalHeader(varKey, varValue)
                                varKey = ""
                                varValue = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_global_header))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.add_global_header))
                    }

                    if (globalHeaders.isNotEmpty()) {
                        HorizontalDivider(
                            Modifier,
                            DividerDefaults.Thickness,
                            color = KizunaColors.Background
                        )
                        globalHeaders.forEach { header ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = header.key, color = KizunaColors.Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = header.value, color = KizunaColors.OnSurface, fontSize = 13.sp)
                                }
                                IconButton(onClick = { headerToDelete = header.key }) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color(0xFFF87171))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (headerToDelete != null) {
        AlertDialog(
            onDismissRequest = { headerToDelete = null },
            containerColor = KizunaColors.Surface,
            title = { Text(stringResource(R.string.delete_global_header), color = KizunaColors.OnSurface) },
            text = { Text(stringResource(R.string.delete_global_header_confirm, headerToDelete!!), color = KizunaColors.Muted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGlobalHeader(headerToDelete!!)
                        headerToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.delete), color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { headerToDelete = null }) {
                    Text(stringResource(R.string.cancel), color = KizunaColors.Muted)
                }
            }
        )
    }
}
