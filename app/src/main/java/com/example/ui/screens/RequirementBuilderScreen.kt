package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.viewmodel.CustomRequirement
import com.example.ui.viewmodel.SmartQuoteViewModel
import com.example.ui.viewmodel.UserRole
import com.example.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequirementBuilderScreen(
    viewModel: SmartQuoteViewModel,
    onQuotationCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentStep by viewModel.wizardStep.collectAsStateWithLifecycle()
    val projectType by viewModel.selectedProjectType.collectAsStateWithLifecycle()
    val allServices by viewModel.allServices.collectAsStateWithLifecycle()
    val allCustomers by viewModel.allCustomers.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val selectedServices by viewModel.selectedServices.collectAsStateWithLifecycle()
    val customReqs by viewModel.customRequirements.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val couponError by viewModel.couponErrorMessage.collectAsStateWithLifecycle()
    val userRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val gstType by viewModel.selectedGstType.collectAsStateWithLifecycle()
    val packages by viewModel.activePackages.collectAsStateWithLifecycle()

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showAddCustomReqDialog by remember { mutableStateOf(false) }
    var showCouponDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }

    // Live Calculations
    val originalSubtotal = viewModel.calculateOriginalSubtotal()
    val offerDiscounts = viewModel.calculateOfferDiscounts()
    val couponDiscount = viewModel.calculateCouponDiscount()
    val manualDiscount = viewModel.calculateManualDiscount()
    val taxableAmount = viewModel.calculateTaxableAmount()
    val (cgst, sgst, igst) = viewModel.calculateGst()
    val grandTotal = viewModel.calculateGrandTotal()
    val totalSavings = viewModel.calculateTotalSavings()
    val selectedCount = selectedServices.size + customReqs.size

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 8.dp, bottom = 4.dp)
            ) {
                // Stepper Header
                WizardStepIndicator(
                    currentStep = currentStep,
                    onStepClick = { step -> viewModel.setWizardStep(step) }
                )
            }
        },
        bottomBar = {
            // Live Right-side / Bottom Sticky Price Calculator Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("sticky_price_summary_bar")
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Live Total:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = CurrencyUtils.formatInr(grandTotal),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (totalSavings > 0) {
                                Text(
                                    text = "🎁 Saving ${CurrencyUtils.formatInr(totalSavings)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (currentStep > 1) {
                                OutlinedButton(
                                    onClick = { viewModel.setWizardStep(currentStep - 1) },
                                    modifier = Modifier.testTag("wizard_prev_button")
                                ) {
                                    Text("Back")
                                }
                            }
                            if (currentStep < 5) {
                                Button(
                                    onClick = { viewModel.setWizardStep(currentStep + 1) },
                                    modifier = Modifier.testTag("wizard_next_button")
                                ) {
                                    Text("Next (${currentStep + 1}/5)")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.saveQuotation { id ->
                                            onQuotationCreated(id)
                                        }
                                    },
                                    enabled = selectedCount > 0 && selectedCustomer != null,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.testTag("generate_quotation_button")
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generate Quotation")
                                }
                            }
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
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {

            // Step 1: Project Type, Client Info & Smart AI Presets
            if (currentStep == 1) {
                item {
                    Text(
                        text = "Step 1: Select Project Type & Client",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose the digital solution and client profile to begin quotation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Customer Selection Card
                item {
                    CustomerSelectorCard(
                        customers = allCustomers,
                        selectedCustomer = selectedCustomer,
                        onSelect = { viewModel.selectedCustomer.value = it },
                        onAddNew = { showAddCustomerDialog = true }
                    )
                }

                // AI Smart Preset Presets
                item {
                    SmartAiPresetSection(
                        onSelectPreset = { presetKey ->
                            viewModel.applyAiPreset(presetKey, allServices)
                        }
                    )
                }

                // Project Type Grid
                item {
                    Text(
                        text = "Project Platform / Category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val projectTypes = listOf(
                        "Website" to Icons.Default.Language,
                        "Android App" to Icons.Default.PhoneAndroid,
                        "iOS App" to Icons.Default.PhoneIphone,
                        "Android + iOS App" to Icons.Default.Devices,
                        "Web Application" to Icons.Default.Web,
                        "Desktop Software" to Icons.Default.Computer,
                        "Business Software" to Icons.Default.BusinessCenter,
                        "AI Automation" to Icons.Default.SmartToy,
                        "AI Calling Agent" to Icons.Default.PhoneCallback,
                        "Custom Project" to Icons.Default.Build
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        projectTypes.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { (type, icon) ->
                                    ProjectTypeCard(
                                        title = type,
                                        icon = icon,
                                        isSelected = projectType == type,
                                        onClick = { viewModel.selectProjectType(type) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Architecture, Pages / Screens & UI Design Tier
            if (currentStep == 2) {
                item {
                    Text(
                        text = "Step 2: Architecture & UI/UX Tier",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select screen count / page architecture and design fidelity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pages / Screens Selection
                item {
                    Text(
                        text = "Scope & Architecture",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val pageServices = allServices.filter { it.subCategory == "Pages" || it.subCategory == "App Screens UI" || it.subCategory == "Platform" }
                    pageServices.forEach { service ->
                        ServiceSelectableItemCard(
                            service = service,
                            isSelected = viewModel.isServiceSelected(service.id),
                            onToggle = { viewModel.toggleServiceSelection(service) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // UI/UX Design Tier
                item {
                    Text(
                        text = "Design & Aesthetic Tier",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val designServices = allServices.filter { it.subCategory == "Design Tier" }
                    designServices.forEach { service ->
                        ServiceSelectableItemCard(
                            service = service,
                            isSelected = viewModel.isServiceSelected(service.id),
                            onToggle = { viewModel.toggleServiceSelection(service) }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // Step 3: Core Features & Specialized Modules
            if (currentStep == 3) {
                item {
                    Text(
                        text = "Step 3: Core Features & Modules",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select all required functional features (E-Commerce, Restaurant, Real Estate, Mobile, AI)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val categories = listOf("Website", "E-Commerce", "Restaurant", "Real Estate", "Mobile App", "Software", "AI Automation")
                categories.forEach { cat ->
                    val servicesInCat = allServices.filter { it.category == cat && it.subCategory != "Pages" && it.subCategory != "Design Tier" && it.subCategory != "Platform" && it.subCategory != "App Screens UI" }
                    if (servicesInCat.isNotEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$cat Modules",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        items(servicesInCat) { service ->
                            ServiceSelectableItemCard(
                                service = service,
                                isSelected = viewModel.isServiceSelected(service.id),
                                onToggle = { viewModel.toggleServiceSelection(service) }
                            )
                        }
                    }
                }
            }

            // Step 4: Add-on Services & Custom Requirements
            if (currentStep == 4) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Step 4: Add-on Services & Custom Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Domain, Hosting, SSL, SEO, AMC + Custom Requirements",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Bundled Package Offers
                if (packages.isNotEmpty()) {
                    item {
                        Text(
                            text = "🎁 Featured Bundled Packages",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        packages.forEach { pkg ->
                            PackageOfferCard(
                                pkg = pkg,
                                onApply = { viewModel.applyPackage(pkg, allServices) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }

                // Add Custom Requirement Button
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddCustomReqDialog = true }
                            .testTag("add_custom_req_button"),
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "+ Add Custom Requirement",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Custom requirements list
                if (customReqs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Custom User Requirements (${customReqs.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(customReqs.indices.toList()) { index ->
                        val custom = customReqs[index]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = custom.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    if (custom.description.isNotEmpty()) {
                                        Text(text = custom.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "${CurrencyUtils.formatInr(custom.price)} | ${custom.estimatedDays} days",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.removeCustomRequirement(index) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // Standard Add-on Services
                item {
                    Text(
                        text = "Standard Infrastructure & Add-ons",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                val addonServices = allServices.filter { it.category == "Add-on" }
                items(addonServices) { service ->
                    ServiceSelectableItemCard(
                        service = service,
                        isSelected = viewModel.isServiceSelected(service.id),
                        onToggle = { viewModel.toggleServiceSelection(service) }
                    )
                }
            }

            // Step 5: Final Review, Discounts, GST & Generate Quotation
            if (currentStep == 5) {
                item {
                    Text(
                        text = "Step 5: Live Price Summary & GST Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Apply coupons, manage discounts and configure GST",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Project Title Input
                item {
                    OutlinedTextField(
                        value = viewModel.customProjectName.collectAsStateWithLifecycle().value,
                        onValueChange = { viewModel.customProjectName.value = it },
                        label = { Text("Quotation / Project Title") },
                        modifier = Modifier.fillMaxWidth().testTag("quote_title_input")
                    )
                }

                // GST Mode Selector
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "GST Tax Calculation Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = gstType == "CGST_SGST",
                                    onClick = { viewModel.selectedGstType.value = "CGST_SGST" },
                                    label = { Text("Intra-State: CGST (9%) + SGST (9%)", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = gstType == "IGST",
                                    onClick = { viewModel.selectedGstType.value = "IGST" },
                                    label = { Text("Inter-State: IGST (18%)", fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Coupon & Promo Box
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Coupon & Promo Codes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                if (appliedCoupon != null) {
                                    TextButton(onClick = { viewModel.removeCoupon() }) {
                                        Text("Remove", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                    }
                                }
                            }

                            if (appliedCoupon != null) {
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "🎉 Coupon '${appliedCoupon!!.code}' Applied (-${CurrencyUtils.formatInr(couponDiscount)})",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = viewModel.couponInputText.collectAsStateWithLifecycle().value,
                                        onValueChange = { viewModel.couponInputText.value = it.uppercase() },
                                        placeholder = { Text("Try: RISHI10, DEV20, APP5000", fontSize = 12.sp) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("coupon_input_field")
                                    )
                                    Button(
                                        onClick = { viewModel.applyCouponCode(viewModel.couponInputText.value) },
                                        modifier = Modifier.testTag("apply_coupon_button")
                                    ) {
                                        Text("Apply")
                                    }
                                }
                                if (couponError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(couponError!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Negotiated Manual Discount (With role approval logic)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Special / Negotiated Discount", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                TextButton(onClick = { showDiscountDialog = true }) {
                                    Text(if (manualDiscount > 0) "Edit (${CurrencyUtils.formatInr(manualDiscount)})" else "+ Add Discount")
                                }
                            }

                            val approvalWarning by viewModel.approvalRequiredWarning.collectAsStateWithLifecycle()
                            if (approvalWarning) {
                                Surface(
                                    color = Color(0xFFFEF3C7),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF92400E), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Discount >10% by Sales Executive. Admin approval required.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Final Breakdown Table
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "LIVE QUOTATION SUMMARY",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text(
                                            text = CurrencyUtils.formatInr(grandTotal),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Light,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(Incl. GST)",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(bottom = 3.dp)
                                        )
                                    }
                                }
                                if (totalSavings > 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(percent = 50)
                                    ) {
                                        Text(
                                            text = "-${CurrencyUtils.formatInr(totalSavings)} SAVED",
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = Color.White.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Grid breakdown
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Subtotal", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(CurrencyUtils.formatInr(originalSubtotal), fontSize = 11.sp, color = Color.White)
                                }
                                
                                if (offerDiscounts > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Special Offer", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                                        Text("-${CurrencyUtils.formatInr(offerDiscounts)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiaryContainer)
                                    }
                                }
                                if (couponDiscount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Discount (${appliedCoupon?.code})", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                                        Text("-${CurrencyUtils.formatInr(couponDiscount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiaryContainer)
                                    }
                                }
                                if (manualDiscount > 0) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Negotiated", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                                        Text("-${CurrencyUtils.formatInr(manualDiscount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiaryContainer)
                                    }
                                }
                                
                                val gstLabel = if (gstType == "CGST_SGST") "GST (18%)" else "IGST (18%)"
                                val gstTotal = if (gstType == "CGST_SGST") cgst + sgst else igst
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(gstLabel, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                    Text(CurrencyUtils.formatInr(gstTotal), fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { newCust ->
                viewModel.addNewCustomer(newCust)
                showAddCustomerDialog = false
            }
        )
    }

    if (showAddCustomReqDialog) {
        AddCustomRequirementDialog(
            onDismiss = { showAddCustomReqDialog = false },
            onConfirm = { customReq ->
                viewModel.addCustomRequirement(customReq)
                showAddCustomReqDialog = false
            }
        )
    }

    if (showDiscountDialog) {
        ManualDiscountDialog(
            currentRole = userRole,
            onDismiss = { showDiscountDialog = false },
            onConfirm = { value, isPercent, reason ->
                viewModel.setManualDiscount(value, isPercent, reason)
                showDiscountDialog = false
            }
        )
    }
}

@Composable
fun WizardStepIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit
) {
    val steps = listOf("Project", "UI/UX", "Features", "Add-ons", "Summary")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isSelected = currentStep == stepNumber
            val isDone = currentStep > stepNumber

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { onStepClick(stepNumber) }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isDone -> Color(0xFF10B981)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = "$stepNumber",
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CustomerSelectorCard(
    customers: List<CustomerEntity>,
    selectedCustomer: CustomerEntity?,
    onSelect: (CustomerEntity) -> Unit,
    onAddNew: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Client Profile *", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                TextButton(onClick = onAddNew) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Customer", fontSize = 12.sp)
                }
            }

            if (selectedCustomer != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedCustomer.name + if (selectedCustomer.businessName.isNotEmpty()) " (${selectedCustomer.businessName})" else "",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${selectedCustomer.mobileNumber} | ${selectedCustomer.customerType} | ${selectedCustomer.city}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Quick Horizontal chips of other customers
            if (customers.size > 1) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(customers) { c ->
                        FilterChip(
                            selected = selectedCustomer?.id == c.id,
                            onClick = { onSelect(c) },
                            label = { Text(c.name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SmartAiPresetSection(
    onSelectPreset: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Smart Requirement Auto-Builder",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp
                )
            }
            Text(
                text = "1-Tap preselect recommended industry scopes & modules",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    PresetChip(title = "E-Commerce + App", onClick = { onSelectPreset("ecommerce_complete") })
                }
                item {
                    PresetChip(title = "Restaurant QR + Ordering", onClick = { onSelectPreset("restaurant_qr") })
                }
                item {
                    PresetChip(title = "Real Estate CRM & Calling", onClick = { onSelectPreset("realestate_crm") })
                }
                item {
                    PresetChip(title = "AI Voice Calling Suite", onClick = { onSelectPreset("ai_automation_bot") })
                }
                item {
                    PresetChip(title = "Startup Minimal 5-Page", onClick = { onSelectPreset("startup_minimal") })
                }
            }
        }
    }
}

@Composable
private fun PresetChip(title: String, onClick: () -> Unit) {
    Surface(
        color = Color(0xFF1E293B),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = title,
            color = Color(0xFF38BDF8),
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ProjectTypeCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag("project_type_$title"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ServiceSelectableItemCard(
    service: ServiceCatalogEntity,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .testTag("service_item_${service.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(20.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = service.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyUtils.formatInr(service.offerPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                if (service.description.isNotEmpty()) {
                    Text(
                        text = service.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PackageOfferCard(
    pkg: PackageEntity,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = pkg.tag,
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "Save ${CurrencyUtils.formatInr(pkg.regularPrice - pkg.packagePrice)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = pkg.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = pkg.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(CurrencyUtils.formatInr(pkg.packagePrice), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        CurrencyUtils.formatInr(pkg.regularPrice),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                    )
                }
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Apply Package", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun AddCustomRequirementDialog(
    onDismiss: () -> Unit,
    onConfirm: (CustomRequirement) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("3") }
    var isGst by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Requirement", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Requirement / Feature Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_req_name")
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Specification / Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Estimated Base Price (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_req_price")
                )
                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Estimated Dev Days") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isGst, onCheckedChange = { isGst = it })
                    Text("GST Applicable (18%)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    val d = daysText.toIntOrNull() ?: 2
                    if (name.isNotBlank() && p > 0) {
                        onConfirm(CustomRequirement(name, desc, p, 1, 0.0, isGst, d))
                    }
                },
                enabled = name.isNotBlank() && (priceText.toDoubleOrNull() ?: 0.0) > 0,
                modifier = Modifier.testTag("save_custom_req_button")
            ) {
                Text("Add to Quote")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ManualDiscountDialog(
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onConfirm: (value: Double, isPercent: Boolean, reason: String) -> Unit
) {
    var discountValueText by remember { mutableStateOf("10") }
    var isPercent by remember { mutableStateOf(true) }
    var reason by remember { mutableStateOf("Special Client Offer") }

    val reasons = listOf("Special Client Offer", "New Client Offer", "Festival Offer", "Negotiated Deal", "Long-Term Referral", "Management Approval")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply Custom Discount", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = isPercent,
                        onClick = { isPercent = true },
                        label = { Text("Percentage (%)") }
                    )
                    FilterChip(
                        selected = !isPercent,
                        onClick = { isPercent = false },
                        label = { Text("Flat (₹)") }
                    )
                }

                OutlinedTextField(
                    value = discountValueText,
                    onValueChange = { discountValueText = it },
                    label = { Text(if (isPercent) "Discount Percentage (%)" else "Flat Discount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("discount_value_input")
                )

                Text("Reason for Discount:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(reasons) { r ->
                        FilterChip(
                            selected = reason == r,
                            onClick = { reason = r },
                            label = { Text(r, fontSize = 10.sp) }
                        )
                    }
                }

                if (currentRole == UserRole.ADMIN_SALES) {
                    Surface(
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Sales Executive limit: 10%. Discounts above 10% require Super Admin approval.",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = discountValueText.toDoubleOrNull() ?: 0.0
                    onConfirm(v, isPercent, reason)
                },
                modifier = Modifier.testTag("apply_discount_confirm_button")
            ) {
                Text("Apply Discount")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
