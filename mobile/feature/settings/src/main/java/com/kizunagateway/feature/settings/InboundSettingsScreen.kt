package com.kizunagateway.feature.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R
import com.kizunagateway.core.ui.theme.KizunaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: InboundSettingsViewModel) {
    val gatewayConfig by viewModel.gatewayConfig.collectAsState()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var isTokenVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KizunaColors.Background)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings),
                color = KizunaColors.OnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.delete_untracked_sms),
                                color = KizunaColors.OnSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Text(
                                text = stringResource(R.string.delete_untracked_sms_desc),
                                color = KizunaColors.Muted,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = gatewayConfig?.deleteUntrackedSms ?: false,
                            onCheckedChange = { viewModel.updateDeleteUntrackedSms(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = KizunaColors.Primary,
                                checkedTrackColor = KizunaColors.Primary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = KizunaColors.Background
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.device_secret_token),
                            color = KizunaColors.OnSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val token = gatewayConfig?.deviceSecret ?: "---"
                            val displayedToken = if (isTokenVisible || token == "---") {
                                token
                            } else {
                                "*".repeat(token.length.coerceAtMost(16))
                            }

                            Text(
                                text = displayedToken,
                                color = KizunaColors.Primary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                                Icon(
                                    imageVector = if (isTokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isTokenVisible) stringResource(R.string.hide_token) else stringResource(R.string.show_token),
                                    tint = KizunaColors.Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            IconButton(onClick = {
                                if (token != "---") {
                                    clipboardManager.setText(AnnotatedString(token))
                                    Toast.makeText(context, context.getString(R.string.token_copied), Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(R.string.copy_token),
                                    tint = KizunaColors.Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = stringResource(R.string.device_token_desc),
                            color = KizunaColors.Muted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
