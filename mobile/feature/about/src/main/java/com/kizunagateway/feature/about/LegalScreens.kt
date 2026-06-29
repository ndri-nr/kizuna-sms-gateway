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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizunagateway.core.ui.R
import com.kizunagateway.core.ui.theme.KizunaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onBack: () -> Unit) {
    LegalContentScreen(
        title = stringResource(R.string.terms_and_conditions),
        onBack = onBack,
        content = {
            LegalSection(
                title = stringResource(R.string.terms_1_title),
                body = stringResource(R.string.terms_1_body)
            )
            LegalSection(
                title = stringResource(R.string.terms_2_title),
                body = stringResource(R.string.terms_2_body)
            )
            LegalSection(
                title = stringResource(R.string.terms_3_title),
                body = stringResource(R.string.terms_3_body)
            )
            LegalSection(
                title = stringResource(R.string.terms_4_title),
                body = stringResource(R.string.terms_4_body)
            )
            LegalSection(
                title = stringResource(R.string.terms_5_title),
                body = stringResource(R.string.terms_5_body)
            )
            LegalSection(
                title = stringResource(R.string.terms_6_title),
                body = stringResource(R.string.terms_6_body)
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    LegalContentScreen(
        title = stringResource(R.string.privacy_policy),
        onBack = onBack,
        content = {
            LegalSection(
                title = stringResource(R.string.privacy_1_title),
                body = stringResource(R.string.privacy_1_body)
            )
            LegalSection(
                title = stringResource(R.string.privacy_2_title),
                body = stringResource(R.string.privacy_2_body)
            )
            LegalSection(
                title = stringResource(R.string.privacy_3_title),
                body = stringResource(R.string.privacy_3_body)
            )
            LegalSection(
                title = stringResource(R.string.privacy_4_title),
                body = stringResource(R.string.privacy_4_body)
            )
            LegalSection(
                title = stringResource(R.string.privacy_5_title),
                body = stringResource(R.string.privacy_5_body)
            )
            LegalSection(
                title = stringResource(R.string.privacy_6_title),
                body = stringResource(R.string.privacy_6_body)
            )
            LegalSection(
                title = stringResource(R.string.privacy_7_title),
                body = stringResource(R.string.privacy_7_body)
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
                text = stringResource(R.string.last_updated),
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
