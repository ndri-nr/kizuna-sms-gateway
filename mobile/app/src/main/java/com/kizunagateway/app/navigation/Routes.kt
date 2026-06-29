package com.kizunagateway.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.graphics.vector.ImageVector
import com.kizunagateway.core.ui.R

sealed class MainTab(val route: String, val titleRes: Int, val icon: ImageVector) {
    data object Home : MainTab("home", R.string.home, Icons.Default.Home)
    data object Inbound : MainTab("inbound", R.string.inbound_sms, Icons.AutoMirrored.Filled.List)
    data object Outbound : MainTab("outbound", R.string.outbound_sms, Icons.AutoMirrored.Filled.Send)
    data object About : MainTab("about", R.string.about, Icons.Default.Info)
}

object Routes {
    const val MAIN = "main"
    const val ADD_WEBHOOK = "add_webhook"
    const val EDIT_WEBHOOK = "edit_webhook/{webhookId}"
    const val GLOBAL_HEADERS = "global_headers"
    const val ADD_RULE = "add_rule"
    const val EDIT_RULE = "edit_rule/{ruleId}"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PRIVACY_POLICY = "privacy_policy"

    fun editWebhook(webhookId: Long) = "edit_webhook/$webhookId"
    fun editRule(ruleId: Long) = "edit_rule/$ruleId"
}
