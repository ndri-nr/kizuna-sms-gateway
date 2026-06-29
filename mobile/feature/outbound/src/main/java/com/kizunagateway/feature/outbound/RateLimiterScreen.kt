package com.kizunagateway.feature.outbound

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R
import com.kizunagateway.core.ui.theme.KizunaColors
import com.kizunagateway.domain.model.ApiKey

@Composable
fun RateLimiterScreen(viewModel: OutboundViewModel) {
    val apiKeys by viewModel.apiKeys.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KizunaColors.Background)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.rate_limiter),
                color = KizunaColors.OnSurface,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.rate_limiter_desc),
                color = KizunaColors.Muted,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
            )

            if (apiKeys.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_api_keys_found), color = KizunaColors.Muted)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(apiKeys) { apiKey ->
                        RateLimiterCard(
                            apiKey = apiKey,
                            onUpdateLimit = { perMinute ->
                                viewModel.updateApiKeyRateLimit(apiKey, perMinute)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RateLimiterCard(
    apiKey: ApiKey,
    onUpdateLimit: (Int) -> Unit
) {
    var perMinuteText by remember(apiKey) { mutableStateOf(if (apiKey.smsPerMinute > 0) apiKey.smsPerMinute.toString() else "") }

    Card(
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = apiKey.name,
                color = KizunaColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = stringResource(R.string.key_prefix, apiKey.key.take(8)),
                color = KizunaColors.Muted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = perMinuteText,
                onValueChange = { perMinuteText = it.filter { char -> char.isDigit() } },
                label = { Text(stringResource(R.string.sms_per_minute), fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = KizunaColors.Primary,
                    unfocusedBorderColor = KizunaColors.Background,
                    focusedLabelColor = KizunaColors.Primary,
                    unfocusedLabelColor = KizunaColors.Muted
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            Button(
                onClick = {
                    val perMin = perMinuteText.toIntOrNull() ?: 0
                    onUpdateLimit(perMin)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = KizunaColors.Primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(stringResource(R.string.save_limit), color = Color.White)
            }
        }
    }
}
