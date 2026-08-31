package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.PriceSummaryRow
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.util.CurrencyUtils
import com.example.util.ShareAndPrintUtils

@Composable
fun InvoiceListScreen(
    viewModel: SmartQuoteViewModel,
    onSelectInvoice: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    var searchStr by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "Unpaid", "Partially Paid", "Paid")

    val filteredList = invoices.filter { inv ->
        val matchesSearch = inv.invoiceNumber.contains(searchStr, ignoreCase = true) ||
                inv.customerName.contains(searchStr, ignoreCase = true) ||
                inv.projectName.contains(searchStr, ignoreCase = true)

        val matchesFilter = if (selectedFilter == "ALL") true else inv.status.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchStr,
            onValueChange = { searchStr = it },
            placeholder = { Text("Search by Invoice #, Client...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchStr.isNotEmpty()) {
                    IconButton(onClick = { searchStr = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("invoice_search_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(filterOptions) { opt ->
                FilterChip(
                    selected = selectedFilter == opt,
                    onClick = { selectedFilter = opt },
                    label = { Text(opt, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No invoices found", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Convert an accepted quotation to generate a GST Tax Invoice", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
            ) {
                items(filteredList) { inv ->
                    InvoiceCardItem(
                        invoice = inv,
                        onClick = { onSelectInvoice(inv.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun InvoiceCardItem(
    invoice: InvoiceEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.invoiceNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF059669)
                )
                StatusBadge(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = invoice.projectName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = "Billed to: ${invoice.customerName}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total: ${CurrencyUtils.formatInr(invoice.grandTotal)}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Date: ${CurrencyUtils.formatDate(invoice.issueDate)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (invoice.dueAmount > 0) "Due: ${CurrencyUtils.formatInr(invoice.dueAmount)}" else "Paid in Full",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (invoice.dueAmount > 0) Color(0xFFDC2626) else Color(0xFF15803D)
                    )
                    if (invoice.paidAmount > 0) {
                        Text(
                            text = "Paid: ${CurrencyUtils.formatInr(invoice.paidAmount)}",
                            fontSize = 11.sp,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    viewModel: SmartQuoteViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
    val invoice = invoices.find { it.id == invoiceId }
    val settings by viewModel.businessSettings.collectAsStateWithLifecycle()

    var invoiceItems by remember { mutableStateOf<List<InvoiceItemEntity>>(emptyList()) }
    var payments by remember { mutableStateOf<List<PaymentEntity>>(emptyList()) }
    var showPaymentDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        invoiceItems = viewModel.repository.getInvoiceItemsSync(invoiceId)
        viewModel.repository.getPaymentsForInvoice(invoiceId).collect {
            payments = it
        }
    }

    if (invoice == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Print Tax Invoice PDF
                    IconButton(
                        onClick = {
                            val html = ShareAndPrintUtils.generateInvoiceHtml(invoice, invoiceItems, settings ?: BusinessSettingsEntity())
                            ShareAndPrintUtils.printHtmlDocument(context, html, "Invoice_${invoice.invoiceNumber}")
                        },
                        modifier = Modifier.testTag("print_tax_invoice_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Print Tax Invoice")
                    }
                }
            )
        },
        bottomBar = {
            if (invoice.dueAmount > 0) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Pending Balance:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyUtils.formatInr(invoice.dueAmount), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFFDC2626))
                        }
                        Button(
                            onClick = { showPaymentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("record_payment_cta")
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Record Payment")
                        }
                    }
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 32.dp)
        ) {
            // Header Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("GST TAX INVOICE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF059669))
                                Text(invoice.projectName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            StatusBadge(status = invoice.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Billed To:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (invoice.customerBusiness.isNotEmpty()) Text(invoice.customerBusiness, fontSize = 12.sp)
                                if (invoice.customerGstin.isNotEmpty()) Text("GSTIN: ${invoice.customerGstin}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Dates:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Issued: ${CurrencyUtils.formatDate(invoice.issueDate)}", fontSize = 11.sp)
                                Text("Due: ${CurrencyUtils.formatDate(invoice.dueDate)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            // Payment Progress Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        val progress = if (invoice.grandTotal > 0) (invoice.paidAmount / invoice.grandTotal).toFloat().coerceIn(0f, 1f) else 0f
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Received", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Color(0xFF059669),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paid: ${CurrencyUtils.formatInr(invoice.paidAmount)}", fontSize = 12.sp, color = Color(0xFF059669), fontWeight = FontWeight.Bold)
                            Text("Due: ${CurrencyUtils.formatInr(invoice.dueAmount)}", fontSize = 12.sp, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tax Invoice Line Items
            item {
                Text("Billable Services & HSN/SAC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(invoiceItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("SAC Code: ${item.hsnSacCode} • Qty: ${item.quantity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(CurrencyUtils.formatInr(item.taxableAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Total: ${CurrencyUtils.formatInr(item.total)}", fontSize = 11.sp, color = Color(0xFF059669), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Totals and GST breakdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("GST & Tax Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        PriceSummaryRow(label = "Taxable Amount", value = CurrencyUtils.formatInr(invoice.taxableTotal))
                        if (invoice.gstType == "CGST_SGST") {
                            PriceSummaryRow(label = "CGST @ ${(invoice.gstRate / 2)}%", value = CurrencyUtils.formatInr(invoice.cgstAmount))
                            PriceSummaryRow(label = "SGST @ ${(invoice.gstRate / 2)}%", value = CurrencyUtils.formatInr(invoice.sgstAmount))
                        } else {
                            PriceSummaryRow(label = "IGST @ ${invoice.gstRate}%", value = CurrencyUtils.formatInr(invoice.igstAmount))
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        PriceSummaryRow(label = "Grand Total", value = CurrencyUtils.formatInr(invoice.grandTotal), isBold = true)
                    }
                }
            }

            // Payment Logs
            if (payments.isNotEmpty()) {
                item {
                    Text("Payment Transactions (${payments.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                items(payments) { p ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("${p.paymentType} via ${p.paymentMethod}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Ref: ${p.transactionRef.ifEmpty { "N/A" }} • ${CurrencyUtils.formatDateTime(p.paymentDate)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(CurrencyUtils.formatInr(p.amount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF059669))
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        RecordPaymentDialog(
            invoice = invoice,
            onDismiss = { showPaymentDialog = false },
            onConfirm = { amount, type, method, ref, notes ->
                viewModel.recordPayment(invoice.id, amount, type, method, ref, notes)
                showPaymentDialog = false
            }
        )
    }
}
