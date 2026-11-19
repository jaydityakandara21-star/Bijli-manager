package com.example

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.ConsumerEntity
import com.example.ui.components.EditEmployeeIdDialog
import com.example.ui.i18n.BijliLabels
import com.example.ui.screens.BillingScreen
import com.example.ui.screens.ConsumerDetailScreen
import com.example.ui.screens.ConsumerFormScreen
import com.example.ui.screens.ConsumersScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.theme.BijliManagerTheme
import com.example.ui.theme.LocalBijliColors
import com.example.ui.viewmodel.BijliViewModel
import com.example.util.ShareUtils
import kotlinx.coroutines.launch

object BijliRoutes {
    const val OVERVIEW = "overview"
    const val CONSUMERS = "consumers"
    const val BILLING = "billing"
    const val TOOLS = "tools"
    const val CONSUMER_DETAIL = "consumer_detail/{consumerId}"
    const val CONSUMER_FORM = "consumer_form?consumerId={consumerId}"

    fun consumerDetail(id: Int): String = "consumer_detail/$id"
    fun consumerForm(id: Int = -1): String = "consumer_form?consumerId=$id"
}

private data class TopNavTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: BijliViewModel = viewModel()
            val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()

            BijliManagerTheme(darkTheme = prefs.isDarkTheme) {
                BijliManagerApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BijliManagerApp(viewModel: BijliViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()

    val isInitializing by viewModel.isInitializing.collectAsStateWithLifecycle()
    val prefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val breakdowns by viewModel.categoryBreakdowns.collectAsStateWithLifecycle()
    val priorityConsumers by viewModel.priorityConsumers.collectAsStateWithLifecycle()
    val dueQueue by viewModel.dueQueueConsumers.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val pagedState by viewModel.pagedConsumers.collectAsStateWithLifecycle()

    val labels = remember(prefs.languageMode) { BijliLabels(prefs.languageMode) }
    var showEditEmployeeDialog by remember { mutableStateOf(false) }

    val csvCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val res = viewModel.writeCsvToUri(uri)
                res.onSuccess { count ->
                    snackbarHostState.showSnackbar("Exported $count consumers to CSV")
                }.onFailure { err ->
                    snackbarHostState.showSnackbar("Export failed: ${err.message ?: "Error"}")
                }
            }
        }
    }

    fun showMessage(msg: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun copyToClipboard(label: String, value: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        showMessage("Copied $label: $value")
    }

    fun dialConsumer(consumer: ConsumerEntity) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${consumer.mobileNumber}")
        }
        runCatching { context.startActivity(intent) }
            .onFailure { showMessage("No dialer available for ${consumer.mobileNumber}") }
    }

    fun shareAppOnWhatsApp() {
        ShareUtils.shareTextOnWhatsApp(
            context = context,
            text = ShareUtils.buildAppShareText(),
            chooserTitle = "Share Bijli Manager App"
        )
    }

    fun shareConsumerOnWhatsApp(consumer: ConsumerEntity) {
        val fullText = ShareUtils.buildSingleConsumerFullText(consumer)
        ShareUtils.shareTextOnWhatsApp(
            context = context,
            text = fullText,
            chooserTitle = "Share ${consumer.name} Full Data"
        )
    }

    fun sendToConsumerWhatsApp(consumer: ConsumerEntity) {
        val fullText = ShareUtils.buildSingleConsumerFullText(consumer)
        ShareUtils.sendToConsumerWhatsApp(
            context = context,
            mobileNumber = consumer.mobileNumber,
            text = fullText
        )
    }

    fun shareSingleConsumerCsv(consumer: ConsumerEntity) {
        scope.launch {
            runCatching {
                val uri = viewModel.createSingleConsumerCsvUri(consumer)
                ShareUtils.shareFileOnWhatsAppOrChooser(
                    context = context,
                    uri = uri,
                    mimeType = "text/csv",
                    subject = "Consumer Data - ${consumer.name} (${consumer.kNumber})",
                    caption = "Consumer Data: ${consumer.name} (${consumer.accountNumber} / ${consumer.kNumber})"
                )
            }.onFailure {
                showMessage("Unable to export consumer CSV")
            }
        }
    }

    fun shareCsv() {
        scope.launch {
            runCatching {
                val uri = viewModel.createShareCsvUri()
                ShareUtils.shareFileOnWhatsAppOrChooser(
                    context = context,
                    uri = uri,
                    mimeType = "text/csv",
                    subject = "Bijli Manager Consumer Registry CSV",
                    caption = "Bijli Manager Registry (${summary.totalConsumers} consumers)",
                    preferWhatsApp = false
                )
            }.onFailure {
                showMessage("Unable to share CSV file")
            }
        }
    }

    if (isInitializing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "Loading 1,000 Consumer Records...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topTabs = listOf(
        TopNavTab(
            route = BijliRoutes.OVERVIEW,
            label = labels.navOverview,
            selectedIcon = Icons.Filled.Dashboard,
            unselectedIcon = Icons.Outlined.Dashboard,
            testTag = "nav_overview"
        ),
        TopNavTab(
            route = BijliRoutes.CONSUMERS,
            label = labels.navConsumers,
            selectedIcon = Icons.Filled.Groups,
            unselectedIcon = Icons.Outlined.Groups,
            testTag = "nav_consumers"
        ),
        TopNavTab(
            route = BijliRoutes.BILLING,
            label = labels.navBilling,
            selectedIcon = Icons.Filled.Payments,
            unselectedIcon = Icons.Outlined.Payments,
            testTag = "nav_billing"
        ),
        TopNavTab(
            route = BijliRoutes.TOOLS,
            label = labels.navTools,
            selectedIcon = Icons.Filled.Build,
            unselectedIcon = Icons.Outlined.Build,
            testTag = "nav_tools"
        )
    )

    val isTopLevelRoute = topTabs.any { it.route == currentRoute }

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        if (showEditEmployeeDialog) {
            EditEmployeeIdDialog(
                currentProfile = prefs.employeeProfile,
                onDismiss = { showEditEmployeeDialog = false },
                onSave = { updated ->
                    viewModel.updateEmployeeProfile(updated)
                    showEditEmployeeDialog = false
                    showMessage("Official Employee ID Card updated")
                }
            )
        }

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                if (isTopLevelRoute) {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bolt,
                                        contentDescription = "Bijli Manager",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = labels.appTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = labels.appSubtitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        },
                        actions = {
                            val sem = LocalBijliColors.current
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                    .border(1.dp, sem.cardBorder, RoundedCornerShape(10.dp))
                                    .clickable { viewModel.toggleLanguageMode() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("language_toggle_pill")
                            ) {
                                Text(
                                    text = prefs.languageMode.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { shareAppOnWhatsApp() },
                                modifier = Modifier.testTag("topbar_share_app")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = "Share App on WhatsApp"
                                )
                            }
                            IconButton(
                                onClick = { viewModel.toggleDarkTheme() },
                                modifier = Modifier.testTag("topbar_theme_toggle")
                            ) {
                                Icon(
                                    imageVector = if (prefs.isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = "Toggle Theme"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            },
            bottomBar = {
                if (isTopLevelRoute && !useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        topTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isTopLevelRoute && useNavigationRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.weight(0.1f))
                        topTabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = BijliRoutes.OVERVIEW,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(BijliRoutes.OVERVIEW) {
                        OverviewScreen(
                            labels = labels,
                            employeeProfile = prefs.employeeProfile,
                            summary = summary,
                            priorityConsumers = priorityConsumers,
                            onEditEmployeeClick = { showEditEmployeeDialog = true },
                            onQuickFilterClick = { cat, billStatus ->
                                viewModel.applyQuickFilter(category = cat, billingStatus = billStatus)
                                navigateToTab(BijliRoutes.CONSUMERS)
                            },
                            onConsumerClick = { id ->
                                navController.navigate(BijliRoutes.consumerDetail(id))
                            },
                            onTogglePaid = { consumer ->
                                viewModel.togglePaidStatus(consumer)
                                showMessage("Payment updated for ${consumer.name}")
                            },
                            onAddConsumerClick = {
                                navController.navigate(BijliRoutes.consumerForm())
                            },
                            onExportCsvClick = {
                                csvCreateLauncher.launch("bijli_consumers_export.csv")
                            },
                            onViewAllConsumersClick = {
                                navigateToTab(BijliRoutes.CONSUMERS)
                            },
                            onShareAppClick = { shareAppOnWhatsApp() }
                        )
                    }

                    composable(BijliRoutes.CONSUMERS) {
                        ConsumersScreen(
                            labels = labels,
                            filterState = filterState,
                            pagedState = pagedState,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onCategoryFilterChange = viewModel::setCategoryFilter,
                            onConnectionFilterChange = viewModel::setConnectionStatusFilter,
                            onBillingFilterChange = viewModel::setBillingStatusFilter,
                            onSortChange = viewModel::setSortOption,
                            onPageSizeChange = viewModel::setPageSize,
                            onPageChange = viewModel::goToPage,
                            onClearFilters = viewModel::clearAllFilters,
                            onAddConsumerClick = {
                                navController.navigate(BijliRoutes.consumerForm())
                            },
                            onViewConsumerClick = { id ->
                                navController.navigate(BijliRoutes.consumerDetail(id))
                            },
                            onEditConsumerClick = { id ->
                                navController.navigate(BijliRoutes.consumerForm(id))
                            },
                            onTogglePaidClick = { consumer ->
                                viewModel.togglePaidStatus(consumer)
                                val msg = if (consumer.billingStatus.name == "PAID") {
                                    "Marked ${consumer.name} as Unpaid"
                                } else {
                                    "Marked ${consumer.name} as Paid"
                                }
                                showMessage(msg)
                            },
                            onCallClick = ::dialConsumer,
                            onCopyKNumberClick = { consumer ->
                                copyToClipboard("K. Number", consumer.kNumber)
                            },
                            onShareConsumerClick = ::shareConsumerOnWhatsApp
                        )
                    }

                    composable(BijliRoutes.BILLING) {
                        BillingScreen(
                            breakdowns = breakdowns,
                            dueQueue = dueQueue,
                            tariffSettings = prefs.tariffSettings,
                            onCategoryTap = { category ->
                                viewModel.applyQuickFilter(category = category, billingStatus = null)
                                navigateToTab(BijliRoutes.CONSUMERS)
                            },
                            onConsumerClick = { id ->
                                navController.navigate(BijliRoutes.consumerDetail(id))
                            },
                            onTogglePaid = { consumer ->
                                viewModel.togglePaidStatus(consumer)
                                showMessage("Collected payment for ${consumer.name}")
                            }
                        )
                    }

                    composable(BijliRoutes.TOOLS) {
                        ToolsScreen(
                            labels = labels,
                            preferences = prefs,
                            summary = summary,
                            onLanguageChange = viewModel::setLanguageMode,
                            onToggleTheme = viewModel::toggleDarkTheme,
                            onSaveTariffs = { tariffs ->
                                viewModel.updateTariffs(tariffs)
                                showMessage("Default tariff rates saved")
                            },
                            onExportCsvSave = {
                                csvCreateLauncher.launch("bijli_consumers_export.csv")
                            },
                            onShareCsv = { shareCsv() },
                            onShareAppWhatsApp = { shareAppOnWhatsApp() },
                            onEditEmployeeProfile = { showEditEmployeeDialog = true },
                            onResetTo1000 = {
                                viewModel.resetToSampleData {
                                    showMessage("Reset complete: 1,000 sample consumers restored")
                                }
                            }
                        )
                    }

                    composable(
                        route = BijliRoutes.CONSUMER_DETAIL,
                        arguments = listOf(navArgument("consumerId") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val consumerId = backStackEntry.arguments?.getInt("consumerId") ?: -1
                        val consumer by viewModel.observeConsumer(consumerId)
                            .collectAsStateWithLifecycle(initialValue = null)

                        ConsumerDetailScreen(
                            consumer = consumer,
                            labels = labels,
                            onBack = { navController.popBackStack() },
                            onEditClick = { id ->
                                navController.navigate(BijliRoutes.consumerForm(id))
                            },
                            onCallClick = ::dialConsumer,
                            onCopyText = ::copyToClipboard,
                            onSaveMeterReading = { c, reading, rate ->
                                viewModel.updateMeterReading(c, reading, rate)
                                showMessage("Meter reading & bill updated for ${c.name}")
                            },
                            onTogglePaid = { c ->
                                viewModel.togglePaidStatus(c)
                                showMessage("Updated payment status for ${c.name}")
                            },
                            onDeleteConsumer = { id ->
                                viewModel.deleteConsumer(id)
                                showMessage("Consumer record deleted")
                            },
                            onShareWhatsApp = ::shareConsumerOnWhatsApp,
                            onSendDirectWhatsApp = ::sendToConsumerWhatsApp,
                            onCopyFullData = { c ->
                                copyToClipboard("Full Consumer Data", ShareUtils.buildSingleConsumerFullText(c))
                            },
                            onExportSingleCsv = ::shareSingleConsumerCsv
                        )
                    }

                    composable(
                        route = BijliRoutes.CONSUMER_FORM,
                        arguments = listOf(
                            navArgument("consumerId") {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { backStackEntry ->
                        val consumerId = backStackEntry.arguments?.getInt("consumerId") ?: -1
                        val existing by if (consumerId > 0) {
                            viewModel.observeConsumer(consumerId).collectAsStateWithLifecycle(initialValue = null)
                        } else {
                            remember { androidx.compose.runtime.mutableStateOf(null) }
                        }

                        ConsumerFormScreen(
                            existingConsumer = existing,
                            tariffSettings = prefs.tariffSettings,
                            labels = labels,
                            onGetSuggestedNumbers = viewModel::getSuggestedNumbers,
                            onSave = viewModel::saveConsumer,
                            onBack = { navController.popBackStack() },
                            onSavedSuccess = { _, wasEditing ->
                                showMessage(if (wasEditing) "Consumer updated" else "New consumer added")
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}
