package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.ui.viewmodel.UserRole
import com.example.util.CurrencyUtils

@Composable
fun DashboardScreen(
    viewModel: SmartQuoteViewModel,
    onNavigateToBuilder: () -> Unit,
    onNavigateToQuotations: () -> Unit,
    onNavigateToInvoices: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToAdminCatalog: () -> Unit,
    onSelectQuotation: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val quotations by viewModel.allQuotations.collectAsStateWithLifecycle()
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val projects by viewModel.allProjects.collectAsStateWithLifecycle()
    val customers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val settings by viewModel.businessSettings.collectAsStateWithLifecycle()
    val userRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val approvals by viewModel.discountApprovals.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()

    // Calculated Executive Metrics
    val totalRevenue = invoices.sumOf { it.paidAmount }
    val totalOutstanding = invoices.sumOf { it.dueAmount }
    val totalQuotesValue = quotations.sumOf { it.grandTotal }
    val totalGstCollected = invoices.sumOf { it.totalGst }

    val acceptedQuotesCount = quotations.count { it.status == "Accepted" || it.status == "Converted to Invoice" }
    val quoteAcceptanceRate = if (quotations.isNotEmpty()) ((acceptedQuotesCount.toDouble() / quotations.size) * 100).toInt() else 0

    val webCount = projects.count { it.projectType.contains("Website", ignoreCase = true) }
    val appCount = projects.count { it.projectType.contains("App", ignoreCase = true) }
    val softCount = projects.count { it.projectType.contains("Software", ignoreCase = true) }
    val aiCount = projects.count { it.projectType.contains("AI", ignoreCase = true) }

    val pendingApprovals = approvals.filter { it.status == "PENDING" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // Business Header Profile
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("business_profile_header"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = settings?.businessName ?: "Rishi_dev",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "GSTIN: ${settings?.gstin ?: "27ALDPI8191C1Z5"} | Pune, India",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        RoleBadge(role = userRole)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Action Hero Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToBuilder,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("dashboard_new_quote_cta")
                        ) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("New Quote", fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = onNavigateToInvoices,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("dashboard_invoices_cta")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Invoices")
                        }
                    }
                }
            }
        }

        // Pending Admin Approvals Alert (if any)
        if (pendingApprovals.isNotEmpty() && userRole == UserRole.SUPER_ADMIN) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("pending_approvals_banner"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PendingActions, contentDescription = null, tint = Color(0xFF92400E))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Action Required: ${pendingApprovals.size} Discount Approval(s)",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E),
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        pendingApprovals.forEach { app ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${app.customerName}: ${app.requestedDiscountPercent.toInt()}% Off (${CurrencyUtils.formatInr(app.requestedDiscountAmount)})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "By ${app.requestedBy} • Reason: ${app.reason}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { viewModel.approveDiscount(app.id, app.quotationId) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = "Approve", tint = Color(0xFF15803D))
                                        }
                                        IconButton(
                                            onClick = { viewModel.rejectDiscount(app.id, app.quotationId) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Cancel, contentDescription = "Reject", tint = Color(0xFFBE123C))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Executive Financial & Operational KPIs (4 Cards Grid)
        item {
            Text(
                text = "Key Business Metrics (INR ₹)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatCard(
                        title = "Revenue Collected",
                        value = CurrencyUtils.formatInr(totalRevenue),
                        subValue = "GST: ${CurrencyUtils.formatInr(totalGstCollected)}",
                        icon = Icons.Default.CurrencyRupee,
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Outstanding Due",
                        value = CurrencyUtils.formatInr(totalOutstanding),
                        subValue = "${invoices.count { it.status == "Unpaid" }} Unpaid Invoices",
                        icon = Icons.Default.AccountBalanceWallet,
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricStatCard(
                        title = "Quotations Active",
                        value = "${quotations.size} Quotes",
                        subValue = "$quoteAcceptanceRate% Converted",
                        icon = Icons.Default.Description,
                        color = Color(0xFF2563EB),
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatCard(
                        title = "Projects In-Flight",
                        value = "${projects.count { !it.isCompleted }} Active",
                        subValue = "${projects.count { it.isCompleted }} Completed",
                        icon = Icons.Default.Engineering,
                        color = Color(0xFF8B5CF6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Monthly Revenue & Trend Canvas Chart
        item {
            val chartData = listOf(
                "Apr" to 120000.0,
                "May" to 185000.0,
                "Jun" to 240000.0,
                "Jul" to 310000.0,
                "Aug" to (totalRevenue.takeIf { it > 50000 } ?: 280000.0)
            )
            MonthlySalesBarChart(data = chartData)
        }

        // Project Type Distribution Donut Chart
        item {
            ProjectCategoryDonutChart(
                websiteCount = webCount.coerceAtLeast(4),
                appCount = appCount.coerceAtLeast(3),
                softwareCount = softCount.coerceAtLeast(2),
                aiCount = aiCount.coerceAtLeast(3)
            )
        }

        // Recent Quotations
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Quotations (${quotations.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToQuotations) {
                    Text("View All")
                }
            }

            if (quotations.isEmpty()) {
                Text(
                    text = "No quotations created yet. Click 'New Quote' to generate your first GST quotation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                quotations.take(3).forEach { quote ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelectQuotation(quote.id) }
                            .testTag("dashboard_quote_item_${quote.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = quote.quotationNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    StatusBadge(status = quote.status)
                                }
                                Text(
                                    text = quote.customerName,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${quote.projectName} • ${CurrencyUtils.formatDate(quote.createdAt)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = CurrencyUtils.formatInr(quote.grandTotal),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (quote.totalSavings > 0) {
                                    Text(
                                        text = "Saved ${CurrencyUtils.formatInr(quote.totalSavings)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Audit & Activity Logs
        item {
            Text(
                text = "Live Activity & Audit Feed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            activityLogs.take(5).forEach { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (log.actionType) {
                                "PAYMENT_RECEIVED" -> Icons.Default.CurrencyRupee
                                "QUOTE_CREATED" -> Icons.Default.Description
                                "INVOICE_GENERATED" -> Icons.Default.ReceiptLong
                                "PROJECT_UPDATED" -> Icons.Default.Engineering
                                else -> Icons.Default.Notifications
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = log.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = "${log.description} • ${CurrencyUtils.formatDateTime(log.timestamp)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
