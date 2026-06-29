package com.kizunagateway.app.navigation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.kizunagateway.core.ui.R
import com.kizunagateway.core.ui.components.KizunaSnackbarHost
import com.kizunagateway.core.ui.theme.KizunaColors
import com.kizunagateway.feature.about.AboutScreen
import com.kizunagateway.feature.about.AboutViewModel
import com.kizunagateway.feature.about.PrivacyPolicyScreen
import com.kizunagateway.feature.about.TermsAndConditionsScreen
import com.kizunagateway.feature.dashboard.DashboardScreen
import com.kizunagateway.feature.dashboard.DashboardViewModel
import com.kizunagateway.feature.home.HomeScreen
import com.kizunagateway.feature.home.HomeViewModel
import com.kizunagateway.feature.outbound.ApiKeyScreen
import com.kizunagateway.feature.outbound.OutboundDashboardScreen
import com.kizunagateway.feature.outbound.OutboundDocumentationScreen
import com.kizunagateway.feature.outbound.OutboundLogsScreen
import com.kizunagateway.feature.outbound.OutboundSettingsScreen
import com.kizunagateway.feature.outbound.OutboundViewModel
import com.kizunagateway.feature.logs.LogsScreen
import com.kizunagateway.feature.logs.LogsViewModel
import com.kizunagateway.feature.rules.AddEditRuleScreen
import com.kizunagateway.feature.rules.RulesScreen
import com.kizunagateway.feature.rules.InboundRulesViewModel
import com.kizunagateway.feature.settings.SettingsScreen
import com.kizunagateway.feature.settings.InboundSettingsViewModel
import com.kizunagateway.feature.webhook.AddEditWebhookScreen
import com.kizunagateway.feature.webhook.GlobalHeadersScreen
import com.kizunagateway.feature.webhook.WebhookScreen
import com.kizunagateway.feature.webhook.WebhookViewModel
import kotlinx.coroutines.launch

private val mainTabs = listOf(
    MainTab.Home,
    MainTab.Inbound,
    MainTab.Outbound,
    MainTab.About
)

private val inboundSubTabs = listOf(
    R.string.dashboard,
    R.string.webhooks,
    R.string.routing_rules,
    R.string.inbound_logs,
    R.string.settings
)

private val outboundSubTabs = listOf(
    R.string.dashboard,
    R.string.credentials,
    R.string.rate_limiter,
    R.string.outbound_logs,
    R.string.documentation,
    R.string.settings
)

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun KizunaNavGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { mainTabs.size })
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    var inboundSubIndex by remember { mutableIntStateOf(0) }
    var outboundSubIndex by remember { mutableIntStateOf(0) }

    val currentMainTab = mainTabs[pagerState.currentPage]

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentMainTab == MainTab.Inbound || currentMainTab == MainTab.Outbound,
        drawerContent = {
            if (currentMainTab == MainTab.Inbound) {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.inbound_sms), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    inboundSubTabs.forEachIndexed { index, resId ->
                        NavigationDrawerItem(
                            label = { Text(stringResource(resId)) },
                            selected = inboundSubIndex == index,
                            onClick = {
                                inboundSubIndex = index
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            } else if (currentMainTab == MainTab.Outbound) {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(stringResource(R.string.outbound_sms), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    outboundSubTabs.forEachIndexed { index, resId ->
                        NavigationDrawerItem(
                            label = { Text(stringResource(resId)) },
                            selected = outboundSubIndex == index,
                            onClick = {
                                outboundSubIndex = index
                                coroutineScope.launch { drawerState.close() }
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            topBar = {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route
                if (currentRoute == Routes.MAIN) {
                    TopAppBar(
                        title = {
                            val title = when (currentMainTab) {
                                MainTab.Inbound -> stringResource(inboundSubTabs[inboundSubIndex])
                                MainTab.Outbound -> stringResource(outboundSubTabs[outboundSubIndex])
                                else -> stringResource(currentMainTab.titleRes)
                            }
                            Text(title)
                        },
                        navigationIcon = {
                            if (currentMainTab == MainTab.Inbound || currentMainTab == MainTab.Outbound) {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = KizunaColors.Surface,
                            titleContentColor = KizunaColors.OnSurface,
                            navigationIconContentColor = KizunaColors.OnSurface
                        )
                    )
                }
            },
            bottomBar = {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry.value?.destination?.route
                val isMainRoute = currentRoute == Routes.MAIN

                if (isMainRoute) {
                    NavigationBar(
                        containerColor = KizunaColors.Surface,
                        contentColor = KizunaColors.OnSurface
                    ) {
                        mainTabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                icon = { Icon(tab.icon, contentDescription = stringResource(tab.titleRes)) },
                                label = { Text(stringResource(tab.titleRes)) },
                                selected = pagerState.currentPage == index,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = KizunaColors.OnSurface,
                                    selectedTextColor = KizunaColors.OnSurface,
                                    unselectedIconColor = KizunaColors.Muted,
                                    unselectedTextColor = KizunaColors.Muted,
                                    indicatorColor = KizunaColors.Primary.copy(alpha = 0.3f)
                                ),
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                            )
                        }
                    }
                }
            },
            snackbarHost = {
                KizunaSnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .padding(16.dp)
                )
            }
        ) { innerPadding ->
            val uri = "kizuna://gateway"
            val webUri = "https://kizuna-gateway.com/app"

            NavHost(
                navController = navController,
                startDestination = Routes.MAIN,
                modifier = Modifier
            ) {
                composable(
                    route = Routes.MAIN,
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "$uri/{tab}" },
                        navDeepLink { uriPattern = "$webUri/{tab}" }
                    )
                ) { backStackEntry ->
                    val tab = backStackEntry.arguments?.getString("tab")
                    LaunchedEffect(tab) {
                        tab?.let {
                            val index = mainTabs.indexOfFirst { it.route == tab }
                            if (index >= 0) pagerState.scrollToPage(index)
                        }
                    }

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        userScrollEnabled = false // Disable swipe to avoid conflict with drawer
                    ) { page ->
                        when (mainTabs[page]) {
                            MainTab.Home -> {
                                val viewModel: HomeViewModel = hiltViewModel()
                                HomeScreen(
                                    viewModel = viewModel,
                                    onInboundClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(mainTabs.indexOf(MainTab.Inbound))
                                            inboundSubIndex = 0
                                        }
                                    },
                                    onOutboundClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(mainTabs.indexOf(MainTab.Outbound))
                                            outboundSubIndex = 0
                                        }
                                    }
                                )
                            }
                            MainTab.Inbound -> {
                                when (inboundSubTabs[inboundSubIndex]) {
                                    R.string.dashboard -> {
                                        val viewModel: DashboardViewModel = hiltViewModel()
                                        DashboardScreen(viewModel = viewModel, showHeaderOnly = true)
                                    }
                                    R.string.webhooks -> {
                                        val viewModel: WebhookViewModel = hiltViewModel()
                                        WebhookScreen(
                                            viewModel = viewModel,
                                            onAddWebhook = { navController.navigate(Routes.ADD_WEBHOOK) },
                                            onEditWebhook = { id -> navController.navigate(Routes.editWebhook(id)) },
                                            onManageGlobalHeaders = { navController.navigate(Routes.GLOBAL_HEADERS) }
                                        )
                                    }
                                    R.string.routing_rules -> {
                                        val viewModel: InboundRulesViewModel = hiltViewModel()
                                        RulesScreen(
                                            viewModel = viewModel,
                                            onAddRule = { navController.navigate(Routes.ADD_RULE) },
                                            onEditRule = { id -> navController.navigate(Routes.editRule(id)) }
                                        )
                                    }
                                    R.string.inbound_logs -> {
                                        val viewModel: LogsViewModel = hiltViewModel()
                                        LogsScreen(viewModel = viewModel)
                                    }
                                    R.string.settings -> {
                                        val viewModel: InboundSettingsViewModel = hiltViewModel()
                                        SettingsScreen(viewModel = viewModel)
                                    }
                                }
                            }
                            MainTab.Outbound -> {
                                val viewModel: OutboundViewModel = hiltViewModel()
                                when (outboundSubTabs[outboundSubIndex]) {
                                    R.string.dashboard -> {
                                        OutboundDashboardScreen(viewModel = viewModel)
                                    }
                                    R.string.credentials -> {
                                        ApiKeyScreen(viewModel = viewModel)
                                    }
                                    R.string.outbound_logs -> {
                                        OutboundLogsScreen(viewModel = viewModel)
                                    }
                                    R.string.rate_limiter -> {
                                        com.kizunagateway.feature.outbound.RateLimiterScreen(viewModel = viewModel)
                                    }
                                    R.string.documentation -> {
                                        OutboundDocumentationScreen(viewModel = viewModel)
                                    }
                                    R.string.settings -> {
                                        OutboundSettingsScreen(viewModel = viewModel)
                                    }
                                }
                            }
                            MainTab.About -> {
                                val viewModel: AboutViewModel = hiltViewModel()
                                AboutScreen(
                                    viewModel = viewModel,
                                    onNavigateToTerms = { navController.navigate(Routes.TERMS_AND_CONDITIONS) },
                                    onNavigateToPrivacy = { navController.navigate(Routes.PRIVACY_POLICY) }
                                )
                            }
                        }
                    }
                }

                composable(route = Routes.TERMS_AND_CONDITIONS) {
                    TermsAndConditionsScreen(onBack = { navController.popBackStack() })
                }
                composable(route = Routes.PRIVACY_POLICY) {
                    PrivacyPolicyScreen(onBack = { navController.popBackStack() })
                }
                composable(route = Routes.ADD_WEBHOOK) {
                    val viewModel: WebhookViewModel = hiltViewModel()
                    AddEditWebhookScreen(
                        webhookId = null,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(route = Routes.GLOBAL_HEADERS) {
                    val viewModel: WebhookViewModel = hiltViewModel()
                    GlobalHeadersScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(
                    route = Routes.EDIT_WEBHOOK,
                    arguments = listOf(navArgument("webhookId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val webhookId = backStackEntry.arguments?.getLong("webhookId")
                    val viewModel: WebhookViewModel = hiltViewModel()
                    AddEditWebhookScreen(
                        webhookId = webhookId,
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(route = Routes.ADD_RULE) {
                    val viewModel: InboundRulesViewModel = hiltViewModel()
                    AddEditRuleScreen(ruleId = null, viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(
                    route = Routes.EDIT_RULE,
                    arguments = listOf(navArgument("ruleId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val ruleId = backStackEntry.arguments?.getLong("ruleId")
                    val viewModel: InboundRulesViewModel = hiltViewModel()
                    AddEditRuleScreen(ruleId = ruleId, viewModel = viewModel, onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
