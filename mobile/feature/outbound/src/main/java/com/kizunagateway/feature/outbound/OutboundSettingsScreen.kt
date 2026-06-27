package com.kizunagateway.feature.outbound

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.theme.KizunaColors

@Composable
fun OutboundSettingsScreen(viewModel: OutboundViewModel) {
    val webhookUrl by viewModel.webhookUrl.collectAsState()
    val tunnelServerUrl by viewModel.tunnelServerUrl.collectAsState()
    val baseUrl by viewModel.baseUrl.collectAsState()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                text = "Settings",
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
                    // Base URL Section (Styled like Device Secret Token)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Base URL",
                            color = KizunaColors.OnSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = baseUrl,
                                color = KizunaColors.Primary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            
                            IconButton(onClick = {
                                if (baseUrl.isNotBlank()) {
                                    clipboardManager.setText(AnnotatedString(baseUrl))
                                }
                                Toast.makeText(context, "Base URL copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Base URL",
                                    tint = KizunaColors.Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Text(
                            text = "Use this URL to send SMS from external applications. Make sure to append the correct endpoint and parameters.",
                            color = KizunaColors.Muted,
                            fontSize = 11.sp
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = KizunaColors.Background
                    )

                    // Tunnel Server URL Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tunnel Server URL",
                            color = KizunaColors.OnSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        TextField(
                            value = tunnelServerUrl,
                            onValueChange = { viewModel.updateTunnelServerUrl(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("sms-gateway.artivy.id") },
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = KizunaColors.Background,
                                unfocusedContainerColor = KizunaColors.Background,
                                disabledContainerColor = KizunaColors.Background,
                                cursorColor = KizunaColors.Primary,
                                focusedIndicatorColor = KizunaColors.Primary,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Text(
                            text = "The server address used for the WebSocket tunnel. Change this to your local IP (e.g., 192.168.1.10:8081) for local testing.",
                            color = KizunaColors.Muted,
                            fontSize = 11.sp
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = KizunaColors.Background
                    )

                    // Webhook URL Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Global Webhook Callback URL",
                            color = KizunaColors.OnSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        
                        TextField(
                            value = webhookUrl,
                            onValueChange = { viewModel.updateWebhookUrl(it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("https://your-app.com/webhook/sms-status") },
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = KizunaColors.Background,
                                unfocusedContainerColor = KizunaColors.Background,
                                disabledContainerColor = KizunaColors.Background,
                                cursorColor = KizunaColors.Primary,
                                focusedIndicatorColor = KizunaColors.Primary,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        
                        Text(
                            text = "Status updates (Sent, Failed) will be POSTed to this URL to notify your application about the SMS delivery status.",
                            color = KizunaColors.Muted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
