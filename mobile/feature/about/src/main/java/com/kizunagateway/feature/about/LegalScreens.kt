package com.kizunagateway.feature.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.theme.KizunaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onBack: () -> Unit) {
    LegalContentScreen(
        title = "Terms and Conditions",
        onBack = onBack,
        content = {
            LegalSection(
                title = "1. Acceptance of Terms",
                body = "By using Kizuna: SMS Gateway (\"the App\"), you agree to be bound by these Terms and Conditions. This App is designed as a bridge between mobile SMS services and external web applications."
            )
            LegalSection(
                title = "2. Nature of Service",
                body = "Kizuna: SMS Gateway provides two primary functions:\n• Inbound: Relaying incoming SMS messages to user-defined webhooks.\n• Outbound: Providing a local REST API and WebSocket tunnel to send SMS from external systems."
            )
            LegalSection(
                title = "3. User Responsibility & Compliance",
                body = "You are solely responsible for all message content processed through the App. You must comply with all local and international laws regarding telecommunications, privacy, and anti-spam (e.g., TCPA, GDPR). Unauthorized use for spamming, harassment, or fraud is strictly prohibited."
            )
            LegalSection(
                title = "4. Usage & Limitations",
                body = "Kizuna: SMS Gateway is provided as a free tool. There are no hidden fees or premium restrictions for core gateway functionalities, such as rule-based routing, webhook delivery, and outbound API access."
            )
            LegalSection(
                title = "5. Limitation of Liability",
                body = "The App is provided \"as is\" without warranties. We are not liable for any data loss, delivery failures, carrier charges, or damages resulting from service interruptions or misuse of the gateway."
            )
            LegalSection(
                title = "6. Modifications",
                body = "We reserve the right to update these terms to reflect changes in the App's functionality or legal requirements. Continued use after changes constitutes acceptance."
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalContentScreen(
        title = "Privacy Policy",
        onBack = onBack,
        content = {
            LegalSection(
                title = "1. Data Collection & Processing",
                body = "The App processes SMS messages received on your device. This data is stored locally in a Room database on your device. We do not host your SMS data on our own servers."
            )
            LegalSection(
                title = "2. Data Transmission",
                body = "SMS content and device metadata (Sender, Timestamp, etc.) are transmitted only to the webhooks and API endpoints explicitly configured by you. You are responsible for the security of the endpoints you define."
            )
            LegalSection(
                title = "3. Device Permissions",
                body = "To function as a gateway, the App requires critical permissions:\n• RECEIVE_SMS & READ_SMS: To intercept and relay inbound messages.\n• SEND_SMS: To process outbound requests.\n• READ_PHONE_STATE: To identify the device for gateway routing.\nThese permissions are used exclusively for core gateway functionality."
            )
            LegalSection(
                title = "4. Local Storage & Security",
                body = "Message history is stored locally in your device's database. We recommend securing your device with biometric or passcode locks to protect local data from unauthorized physical access."
            )
            LegalSection(
                title = "5. Outbound Security",
                body = "Outbound API access is secured via API Keys. For enhanced security, API secrets are masked in the UI after creation. Rate limiting is available to prevent carrier-level detection of unusual activity."
            )
            LegalSection(
                title = "6. Third-Party Services",
                body = "Payment processing is handled by the Google Play Store. Please refer to their privacy policy for billing-related data handling."
            )
            LegalSection(
                title = "7. Contact Information",
                body = "For privacy inquiries or support, please contact the developer at 4ndri.nr@gmail.com."
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalContentScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Last Updated: June 2026",
                style = MaterialTheme.typography.bodySmall,
                color = KizunaColors.Muted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun LegalSection(title: String, body: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KizunaColors.Surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = KizunaColors.Primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = KizunaColors.OnSurface,
                lineHeight = 20.sp
            )
        }
    }
}
