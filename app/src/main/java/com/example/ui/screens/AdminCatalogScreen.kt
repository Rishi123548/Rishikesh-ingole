package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCatalogScreen(
    viewModel: SmartQuoteViewModel,
    modifier: Modifier = Modifier
) {
    val services by viewModel.allServices.collectAsStateWithLifecycle()
    val packages by viewModel.activePackages.collectAsStateWithLifecycle()
    val coupons by viewModel.allCoupons.collectAsStateWithLifecycle()
    val approvals by viewModel.discountApprovals.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(0) } // 0: Services, 1: Packages, 2: Coupons, 3: Approvals
    var searchStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }

    var editingService by remember { mutableStateOf<ServiceCatalogEntity?>(null) }
    var showEditServiceDialog by remember { mutableStateOf(false) }

    var editingCoupon by remember { mutableStateOf<CouponEntity?>(null) }
    var showEditCouponDialog by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "Website", "Mobile App", "Software", "AI Automation", "Add-on", "Custom")

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        editingService = ServiceCatalogEntity(name = "", category = "Website", subCategory = "Custom", basePrice = 5000.0, offerPrice = 4500.0)
                        showEditServiceDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_service_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Service")
                }
            } else if (selectedTab == 2) {
                FloatingActionButton(
                    onClick = {
                        editingCoupon = CouponEntity(code = "", discountType = "PERCENTAGE", discountValue = 10.0, maxDiscountValue = 5000.0, minOrderValue = 10000.0)
                        showEditCouponDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag("add_coupon_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Coupon")
                }
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

            // Dynamic Price Management Note
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Dynamic Pricing Engine: Update base & offer prices in real-time across the app.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Services (${services.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Packages (${packages.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Coupons (${coupons.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
                Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Approvals (${approvals.count { it.status == "PENDING" }})", fontWeight = FontWeight.Bold, fontSize = 12.sp) })
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab 0: Service Catalog
            if (selectedTab == 0) {
                OutlinedTextField(
                    value = searchStr,
                    onValueChange = { searchStr = it },
                    placeholder = { Text("Search catalog items...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("catalog_search_input")
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                val filteredServices = services.filter { s ->
                    val matchCat = if (selectedCategory == "ALL") true else s.category.equals(selectedCategory, ignoreCase = true)
                    val matchSearch = s.name.contains(searchStr, ignoreCase = true) || s.description.contains(searchStr, ignoreCase = true)
                    matchCat && matchSearch
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredServices) { service ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingService = service
                                    showEditServiceDialog = true
                                }
                                .testTag("admin_service_${service.id}"),
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
                                        Text(service.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (!service.isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(color = Color(0xFFFFE4E6), shape = RoundedCornerShape(4.dp)) {
                                                Text("Disabled", color = Color(0xFFBE123C), fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                            }
                                        }
                                    }
                                    Text("${service.category} • ${service.subCategory} • ${service.developmentDays}d dev", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(CurrencyUtils.formatInr(service.offerPrice), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    if (service.basePrice > service.offerPrice) {
                                        Text(CurrencyUtils.formatInr(service.basePrice), fontSize = 10.sp, color = MaterialTheme.colorScheme.outline, style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Tab 1: Bundled Packages
            if (selectedTab == 1) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(packages) { pkg ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) {
                                        Text(pkg.tag, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text(pkg.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Package Price: ${CurrencyUtils.formatInr(pkg.packagePrice)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Regular: ${CurrencyUtils.formatInr(pkg.regularPrice)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    }
                }
            }

            // Tab 2: Coupons & Promos
            if (selectedTab == 2) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(coupons) { coupon ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingCoupon = coupon
                                    showEditCouponDialog = true
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Surface(color = Color(0xFFDCFCE7), shape = RoundedCornerShape(6.dp)) {
                                        Text(coupon.code, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF15803D), modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (coupon.discountType == "PERCENTAGE") "${coupon.discountValue.toInt()}% Off (Max ${CurrencyUtils.formatInr(coupon.maxDiscountValue)})" else "Flat ${CurrencyUtils.formatInr(coupon.discountValue)} Off",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text("Min Order: ${CurrencyUtils.formatInr(coupon.minOrderValue)}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Tab 3: Discount Approvals Queue
            if (selectedTab == 3) {
                if (approvals.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No discount approval requests pending", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(approvals) { app ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Quote #${app.quotationId} - ${app.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            color = if (app.status == "APPROVED") Color(0xFFDCFCE7) else if (app.status == "REJECTED") Color(0xFFFFE4E6) else Color(0xFFFEF3C7),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(app.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                    Text("Requested Discount: ${app.requestedDiscountPercent.toInt()}% (${CurrencyUtils.formatInr(app.requestedDiscountAmount)})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("By: ${app.requestedBy} • Reason: ${app.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (app.status == "PENDING") {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = { viewModel.approveDiscount(app.id, app.quotationId) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Approve")
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.rejectDiscount(app.id, app.quotationId) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Reject")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Editing/Adding Service
    if (showEditServiceDialog && editingService != null) {
        var name by remember { mutableStateOf(editingService!!.name) }
        var desc by remember { mutableStateOf(editingService!!.description) }
        var category by remember { mutableStateOf(editingService!!.category) }
        var subCat by remember { mutableStateOf(editingService!!.subCategory) }
        var basePriceText by remember { mutableStateOf(editingService!!.basePrice.toLong().toString()) }
        var offerPriceText by remember { mutableStateOf(editingService!!.offerPrice.toLong().toString()) }
        var daysText by remember { mutableStateOf(editingService!!.developmentDays.toString()) }
        var isActive by remember { mutableStateOf(editingService!!.isActive) }

        AlertDialog(
            onDismissRequest = { showEditServiceDialog = false },
            title = { Text(if (editingService!!.id == 0L) "Add Service to Catalog" else "Edit Catalog Pricing", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Service Name *") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("edit_service_name"))
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = basePriceText, onValueChange = { basePriceText = it }, label = { Text("Base Price (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).testTag("edit_base_price"))
                        OutlinedTextField(value = offerPriceText, onValueChange = { offerPriceText = it }, label = { Text("Offer Price (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f).testTag("edit_offer_price"))
                    }

                    OutlinedTextField(value = daysText, onValueChange = { daysText = it }, label = { Text("Est. Dev Days") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isActive, onCheckedChange = { isActive = it })
                        Text("Active & Selectable in Builder", fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bp = basePriceText.toDoubleOrNull() ?: 0.0
                        val op = offerPriceText.toDoubleOrNull() ?: bp
                        val d = daysText.toIntOrNull() ?: 2
                        viewModel.saveService(
                            editingService!!.copy(
                                name = name,
                                description = desc,
                                category = category,
                                subCategory = subCat,
                                basePrice = bp,
                                offerPrice = op,
                                developmentDays = d,
                                isActive = isActive
                            )
                        )
                        showEditServiceDialog = false
                    },
                    modifier = Modifier.testTag("save_service_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditServiceDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Dialog for Editing/Adding Coupon
    if (showEditCouponDialog && editingCoupon != null) {
        var code by remember { mutableStateOf(editingCoupon!!.code) }
        var discValText by remember { mutableStateOf(editingCoupon!!.discountValue.toLong().toString()) }
        var maxDiscText by remember { mutableStateOf(editingCoupon!!.maxDiscountValue.toLong().toString()) }
        var minOrderText by remember { mutableStateOf(editingCoupon!!.minOrderValue.toLong().toString()) }
        var isPercent by remember { mutableStateOf(editingCoupon!!.discountType == "PERCENTAGE") }

        AlertDialog(
            onDismissRequest = { showEditCouponDialog = false },
            title = { Text(if (editingCoupon!!.id == 0L) "Create Coupon Code" else "Edit Coupon", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code (e.g. FESTIVAL20)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(selected = isPercent, onClick = { isPercent = true }, label = { Text("Percentage (%)") })
                        FilterChip(selected = !isPercent, onClick = { isPercent = false }, label = { Text("Flat (₹)") })
                    }
                    OutlinedTextField(value = discValText, onValueChange = { discValText = it }, label = { Text(if (isPercent) "Discount Value (%)" else "Discount Value (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = maxDiscText, onValueChange = { maxDiscText = it }, label = { Text("Max Cap (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = minOrderText, onValueChange = { minOrderText = it }, label = { Text("Min Order Value (₹)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dv = discValText.toDoubleOrNull() ?: 10.0
                        val md = maxDiscText.toDoubleOrNull() ?: 5000.0
                        val mo = minOrderText.toDoubleOrNull() ?: 10000.0
                        viewModel.saveCoupon(
                            editingCoupon!!.copy(
                                code = code,
                                discountType = if (isPercent) "PERCENTAGE" else "FLAT",
                                discountValue = dv,
                                maxDiscountValue = md,
                                minOrderValue = mo
                            )
                        )
                        showEditCouponDialog = false
                    }
                ) {
                    Text("Save Coupon")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCouponDialog = false }) { Text("Cancel") }
            }
        )
    }
}
