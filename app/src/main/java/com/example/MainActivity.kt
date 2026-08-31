package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.components.RoleBadge
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.ui.viewmodel.UserRole

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SmartQuoteApp()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Builder : Screen("builder", "Requirement Wizard", Icons.Default.Calculate)
    object Quotations : Screen("quotations", "Quotations", Icons.Default.Description)
    object Invoices : Screen("invoices", "Invoices", Icons.Default.ReceiptLong)
    object Projects : Screen("projects", "Projects", Icons.Default.Engineering)
    object CRM : Screen("crm", "Customers", Icons.Default.People)
    object Catalog : Screen("catalog", "Pricing & Catalog", Icons.Default.Tune)
    object Settings : Screen("settings", "Business Settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartQuoteApp() {
    val viewModel: SmartQuoteViewModel = viewModel()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val userRole by viewModel.currentRole.collectAsStateWithLifecycle()
    var showRoleMenu by remember { mutableStateOf(false) }

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.Builder,
        Screen.Quotations,
        Screen.Invoices,
        Screen.Projects
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SmartQuote Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Rishi_dev",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "GSTIN: 27ALDPI8191C1Z5",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Role Switcher Button
                    Box {
                        FilledTonalButton(
                            onClick = { showRoleMenu = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("role_switcher_button")
                        ) {
                            RoleBadge(role = userRole)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch Role", modifier = Modifier.size(16.dp))
                        }

                        DropdownMenu(
                            expanded = showRoleMenu,
                            onDismissRequest = { showRoleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("👑 Super Admin (Full Control)") },
                                onClick = {
                                    viewModel.switchRole(UserRole.SUPER_ADMIN)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("💼 Admin / Sales Executive (10% limit)") },
                                onClick = {
                                    viewModel.switchRole(UserRole.ADMIN_SALES)
                                    showRoleMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("👤 Customer View (Read & Request)") },
                                onClick = {
                                    viewModel.switchRole(UserRole.CUSTOMER)
                                    showRoleMenu = false
                                }
                            )
                        }
                    }

                    // More Menu (CRM, Master Catalog Pricing, Business Settings)
                    var showMoreMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.testTag("more_menu_button")
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("👥 Customer CRM") },
                            leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                            onClick = {
                                navController.navigate(Screen.CRM.route)
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚙️ Master Pricing & Catalog") },
                            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null) },
                            onClick = {
                                navController.navigate(Screen.Catalog.route)
                                showMoreMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🏢 Business & GST Settings") },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            onClick = {
                                navController.navigate(Screen.Settings.route)
                                showMoreMenu = false
                            }
                        )
                    }
                }
            )
        },
        bottomBar = {
            val isRootScreen = bottomNavItems.any { it.route == currentRoute } || currentRoute == Screen.CRM.route || currentRoute == Screen.Catalog.route || currentRoute == Screen.Settings.route
            if (isRootScreen) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Dashboard.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToBuilder = { navController.navigate(Screen.Builder.route) },
                    onNavigateToQuotations = { navController.navigate(Screen.Quotations.route) },
                    onNavigateToInvoices = { navController.navigate(Screen.Invoices.route) },
                    onNavigateToProjects = { navController.navigate(Screen.Projects.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.CRM.route) },
                    onNavigateToAdminCatalog = { navController.navigate(Screen.Catalog.route) },
                    onSelectQuotation = { id -> navController.navigate("quote_detail/$id") }
                )
            }

            composable(Screen.Builder.route) {
                RequirementBuilderScreen(
                    viewModel = viewModel,
                    onQuotationCreated = { id ->
                        navController.navigate("quote_detail/$id")
                    }
                )
            }

            composable(Screen.Quotations.route) {
                QuotationListScreen(
                    viewModel = viewModel,
                    onSelectQuotation = { id -> navController.navigate("quote_detail/$id") },
                    onNavigateToBuilder = { navController.navigate(Screen.Builder.route) }
                )
            }

            composable(
                route = "quote_detail/{quoteId}",
                arguments = listOf(navArgument("quoteId") { type = NavType.LongType })
            ) { backStack ->
                val quoteId = backStack.arguments?.getLong("quoteId") ?: 0L
                QuotationDetailScreen(
                    quotationId = quoteId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToInvoice = { invId -> navController.navigate("invoice_detail/$invId") },
                    onNavigateToProject = { projId -> navController.navigate("project_detail/$projId") }
                )
            }

            composable(Screen.Invoices.route) {
                InvoiceListScreen(
                    viewModel = viewModel,
                    onSelectInvoice = { id -> navController.navigate("invoice_detail/$id") }
                )
            }

            composable(
                route = "invoice_detail/{invoiceId}",
                arguments = listOf(navArgument("invoiceId") { type = NavType.LongType })
            ) { backStack ->
                val invoiceId = backStack.arguments?.getLong("invoiceId") ?: 0L
                InvoiceDetailScreen(
                    invoiceId = invoiceId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Projects.route) {
                ProjectManagementScreen(
                    viewModel = viewModel,
                    onSelectProject = { id -> navController.navigate("project_detail/$id") }
                )
            }

            composable(
                route = "project_detail/{projectId}",
                arguments = listOf(navArgument("projectId") { type = NavType.LongType })
            ) { backStack ->
                val projectId = backStack.arguments?.getLong("projectId") ?: 0L
                ProjectDetailScreen(
                    projectId = projectId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CRM.route) {
                CustomerCrmScreen(
                    viewModel = viewModel,
                    onCustomerSelected = { cust ->
                        viewModel.selectedCustomer.value = cust
                        navController.navigate(Screen.Builder.route)
                    }
                )
            }

            composable(Screen.Catalog.route) {
                AdminCatalogScreen(
                    viewModel = viewModel
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
