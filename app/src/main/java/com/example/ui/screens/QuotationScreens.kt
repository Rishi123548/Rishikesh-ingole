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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.PriceSummaryRow
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.ui.viewmodel.UserRole
import com.example.util.CurrencyUtils
import com.example.util.ShareAndPrintUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationListScreen(
    viewModel: SmartQuoteViewModel,
    onSelectQuotation: (Long) -> Unit,
    onNavigateToBuilder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quotations by viewModel.allQuotations.collectAsStateWithLifecycle()
    var searchStr by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filterOptions = listOf("ALL", "Sent", "Accepted", "Pending Admin Approval", "Revision Requested", "Converted to Invoice", "Draft", "Rejected")

    val filteredList = quotations.filter { quote ->
        val matchesSearch = quote.quotationNumber.contains(searchStr, ignoreCase = true) ||
                quote.customerName.contains(searchStr, ignoreCase = true) ||
                quote.projectName.contains(searchStr, ignoreCase = true)

        val matchesFilter = if (selectedFilter == "ALL") true else quote.status.equals(selectedFilter, ignoreCase = true)
        matchesSearch && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToBuilder,
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("create_quote_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Quotation")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchStr,
                onValueChange = { searchStr = it },
                placeholder = { Text("Search by Quote #, Client, or Project...") },
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
                modifier = Modifier.fillMaxWidth().testTag("quotation_search_input")
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("No quotations found", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Try changing search or filter parameters", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 80.dp)
                ) {
                    items(filteredList) { quote ->
                        QuotationCardItem(
                            quote = quote,
                            onClick = { onSelectQuotation(quote.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuotationCardItem(
    quote: QuotationEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("quote_card_${quote.id}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = quote.quotationNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                StatusBadge(status = quote.status)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = quote.projectName,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Client: ${quote.customerName}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                        text = "Date: ${CurrencyUtils.formatDate(quote.createdAt)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Valid till: ${CurrencyUtils.formatDate(quote.validUntil)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.formatInr(quote.grandTotal),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotationDetailScreen(
    quotationId: Long,
    viewModel: SmartQuoteViewModel,
    onBack: () -> Unit,
    onNavigateToInvoice: (Long) -> Unit,
    onNavigateToProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val quotes by viewModel.allQuotations.collectAsStateWithLifecycle()
    val quote = quotes.find { it.id == quotationId }
    val settings by viewModel.businessSettings.collectAsStateWithLifecycle()
    val userRole by viewModel.currentRole.collectAsStateWithLifecycle()

    var quoteItems by remember { mutableStateOf<List<QuotationItemEntity>>(emptyList()) }
    var showRevisionDialog by remember { mutableStateOf(false) }
    var revisionNote by remember { mutableStateOf("") }

    LaunchedEffect(quotationId) {
        quoteItems = viewModel.repository.getQuotationItemsSync(quotationId)
    }

    if (quote == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(quote.quotationNumber, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // WhatsApp Share
                    IconButton(
                        onClick = {
                            ShareAndPrintUtils.shareQuotationToWhatsApp(context, quote, quoteItems, settings ?: BusinessSettingsEntity())
                        },
                        modifier = Modifier.testTag("whatsapp_share_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share on WhatsApp", tint = Color(0xFF25D366))
                    }
                    // Print / PDF Export
                    IconButton(
                        onClick = {
                            val html = ShareAndPrintUtils.generateQuotationHtml(quote, quoteItems, settings ?: BusinessSettingsEntity())
                            ShareAndPrintUtils.printHtmlDocument(context, html, "Quotation_${quote.quotationNumber}")
                        },
                        modifier = Modifier.testTag("print_pdf_quote_button")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Download PDF")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (quote.status != "Converted to Invoice" && quote.status != "Rejected") {
                        // Accept / Convert to Invoice
                        Button(
                            onClick = {
                                viewModel.convertQuotationToInvoice(quote.id) { invId, projId ->
                                    onNavigateToInvoice(invId)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("convert_invoice_cta")
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Convert to Invoice")
                        }
                    }

                    // Request Revision (For Customer or Sales)
                    OutlinedButton(
                        onClick = { showRevisionDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("request_revision_button")
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request Revision")
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
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
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
                                Text(text = quote.projectName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "Type: ${quote.projectType}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(status = quote.status)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Client:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(quote.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(quote.customerPhone, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Validity:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyUtils.formatDate(quote.validUntil), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Date: ${CurrencyUtils.formatDate(quote.createdAt)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Quick Status Changer (for Admin / Sales)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Update Status:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Sent", "Accepted", "Rejected").forEach { st ->
                                FilterChip(
                                    selected = quote.status == st,
                                    onClick = { viewModel.updateQuotationStatus(quote.id, st) },
                                    label = { Text(st, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // Line Items
            item {
                Text(
                    text = "Selected Modules & Services (${quoteItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(quoteItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.serviceName, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(text = CurrencyUtils.formatInr(item.taxableAmount), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        if (item.description.isNotEmpty()) {
                            Text(text = item.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Category: ${item.category} • Dev: ${item.developmentDays} days", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            if (item.discount > 0) {
                                Text(text = "Discount: -${CurrencyUtils.formatInr(item.discount)}", fontSize = 10.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Calculation Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Cost & GST Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        PriceSummaryRow(label = "Subtotal", value = CurrencyUtils.formatInr(quote.subtotal))
                        if (quote.offerDiscount > 0) {
                            PriceSummaryRow(label = "Offer Discount", value = "-${CurrencyUtils.formatInr(quote.offerDiscount)}", isDiscount = true)
                        }
                        if (quote.couponDiscount > 0) {
                            PriceSummaryRow(label = "Coupon (${quote.couponCode})", value = "-${CurrencyUtils.formatInr(quote.couponDiscount)}", isDiscount = true)
                        }
                        if (quote.manualDiscount > 0) {
                            PriceSummaryRow(label = "Negotiated Discount", value = "-${CurrencyUtils.formatInr(quote.manualDiscount)}", isDiscount = true)
                        }

                        Divider(modifier = Modifier.padding(vertical = 6.dp))

                        PriceSummaryRow(label = "Taxable Value", value = CurrencyUtils.formatInr(quote.taxableAmount), isBold = true)

                        if (quote.gstType == "CGST_SGST") {
                            PriceSummaryRow(label = "CGST @ ${(quote.gstRate / 2)}%", value = CurrencyUtils.formatInr(quote.cgstAmount))
                            PriceSummaryRow(label = "SGST @ ${(quote.gstRate / 2)}%", value = CurrencyUtils.formatInr(quote.sgstAmount))
                        } else {
                            PriceSummaryRow(label = "IGST @ ${quote.gstRate}%", value = CurrencyUtils.formatInr(quote.igstAmount))
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Grand Total:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text(CurrencyUtils.formatInr(quote.grandTotal), fontWeight = FontWeight.Bold, fontSize = 19.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    if (showRevisionDialog) {
        AlertDialog(
            onDismissRequest = { showRevisionDialog = false },
            title = { Text("Request Quotation Revision", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Describe changes or adjustments required by client:", fontSize = 12.sp)
                    OutlinedTextField(
                        value = revisionNote,
                        onValueChange = { revisionNote = it },
                        placeholder = { Text("e.g. Remove iOS scope, add Payment Gateway") },
                        modifier = Modifier.fillMaxWidth().testTag("revision_note_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.requestRevision(quote.id, revisionNote)
                        showRevisionDialog = false
                    },
                    modifier = Modifier.testTag("submit_revision_button")
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevisionDialog = false }) { Text("Cancel") }
            }
        )
    }
}
