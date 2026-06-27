package com.kizunagateway.feature.outbound

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun OutboundDocumentationScreen(viewModel: OutboundViewModel) {
    val baseUrl by viewModel.baseUrl.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KizunaColors.Background)
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "API Documentation",
                    color = KizunaColors.OnSurface,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Base URL for this device:",
                    color = KizunaColors.Muted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KizunaColors.Surface, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = baseUrl,
                        color = KizunaColors.Primary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(baseUrl))
                        Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = KizunaColors.Muted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            item {
                ApiEndpointCard(
                    method = "POST",
                    path = "/api/v1/sms/send",
                    description = "Send a single SMS message",
                    headers = mapOf("X-API-KEY" to "Your-API-Key", "Content-Type" to "application/json"),
                    requestBody = """
                    {
                      "phoneNumber": "+628123456789",
                      "message": "Hello from Kizuna!",
                      "simSlot": 0,
                      "webhookUrl": "https://example.com/webhook" (Optional)
                    }
                    """.trimIndent(),
                    responseBody = """
                    {
                      "id": 123,
                      "status": "PENDING"
                    }
                    """.trimIndent()
                )
            }

            item {
                ApiEndpointCard(
                    method = "POST",
                    path = "/api/v1/sms/send-batch",
                    description = "Send multiple SMS messages",
                    headers = mapOf("X-API-KEY" to "Your-API-Key", "Content-Type" to "application/json"),
                    requestBody = """
                    {
                      "messages": [
                        {
                          "phoneNumber": "+628123456781",
                          "message": "Batch message 1",
                          "webhookUrl": "https://callback.com/1"
                        },
                        {
                          "phoneNumber": "+628123456782",
                          "message": "Batch message 2"
                        }
                      ]
                    }
                    """.trimIndent(),
                    responseBody = """
                    [
                      { "id": 124, "status": "PENDING" },
                      { "id": 125, "status": "PENDING" }
                    ]
                    """.trimIndent()
                )
            }

            item {
                ApiEndpointCard(
                    method = "GET",
                    path = "/api/v1/sms/{id}",
                    description = "Check status of a specific SMS",
                    headers = mapOf("X-API-KEY" to "Your-API-Key"),
                    responseBody = """
                    {
                      "id": 123,
                      "status": "SENT",
                      "errorMessage": null
                    }
                    """.trimIndent()
                )
            }

            item {
                ApiEndpointCard(
                    method = "GET",
                    path = "/api/v1/sms/queue",
                    description = "Check current pending queue size",
                    headers = mapOf("X-API-KEY" to "Your-API-Key"),
                    responseBody = """
                    {
                      "pendingCount": 5
                    }
                    """.trimIndent()
                )
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ApiEndpointCard(
    method: String,
    path: String,
    description: String,
    headers: Map<String, String>,
    requestBody: String? = null,
    responseBody: String? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (method == "POST") Color(0xFF25D366).copy(alpha = 0.1f) else KizunaColors.Primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = method,
                        color = if (method == "POST") Color(0xFF25D366) else KizunaColors.Primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = path, color = KizunaColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Text(text = description, color = KizunaColors.Muted, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))

            DocSection("Headers") {
                headers.forEach { (k, v) ->
                    Text("$k: $v", color = KizunaColors.OnSurface, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }

            requestBody?.let {
                DocSection("Request Body") {
                    Text(it, color = KizunaColors.OnSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            responseBody?.let {
                DocSection("Response Body") {
                    Text(it, color = KizunaColors.OnSurface, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun DocSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(title, color = KizunaColors.Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KizunaColors.Background.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(8.dp)
        ) {
            content()
        }
    }
}
