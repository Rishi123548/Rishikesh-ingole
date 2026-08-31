package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.SmartQuoteRepository
import com.example.util.CurrencyUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class UserRole(val label: String, val badgeColor: Long) {
    SUPER_ADMIN("Super Admin", 0xFF2563EB),
    ADMIN_SALES("Admin / Sales", 0xFF0D9488),
    CUSTOMER("Customer View", 0xFF7C3AED)
}

data class RequirementSelection(
    val service: ServiceCatalogEntity,
    var quantity: Int = 1,
    var customPrice: Double = service.offerPrice
)

data class CustomRequirement(
    val name: String,
    val description: String = "",
    val price: Double,
    val quantity: Int = 1,
    val discount: Double = 0.0,
    val isGstApplicable: Boolean = true,
    val estimatedDays: Int = 2
)

class SmartQuoteViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SmartQuoteDatabase.getDatabase(application, viewModelScope)
    val repository = SmartQuoteRepository(database)

    // Current Role
    private val _currentRole = MutableStateFlow(UserRole.SUPER_ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    fun switchRole(role: UserRole) {
        _currentRole.value = role
    }

    // Database Observables
    val businessSettings = repository.businessSettings.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessSettingsEntity()
    )
    val allServices = repository.allServices.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allCustomers = repository.allCustomers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allQuotations = repository.allQuotations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allInvoices = repository.allInvoices.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allProjects = repository.allProjects.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allCoupons = repository.allCoupons.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val activePackages = repository.activePackages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val discountApprovals = repository.allDiscountApprovals.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val activityLogs = repository.recentActivityLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Search and Filters
    val searchQuery = MutableStateFlow("")
    val filterStatus = MutableStateFlow("ALL")
    val filterCategory = MutableStateFlow("ALL")

    // --- REQUIREMENT BUILDER WIZARD STATE ---
    val wizardStep = MutableStateFlow(1) // 1: Project Type, 2: Category & UI, 3: Features & Modules, 4: Add-ons & Custom, 5: Review & Pricing
    val selectedProjectType = MutableStateFlow("Website")
    val selectedWebsiteCategory = MutableStateFlow("Business Website")
    val selectedPagesOption = MutableStateFlow("5 Pages Website")
    val selectedDesignTier = MutableStateFlow("Standard UI")
    val selectedScreensOption = MutableStateFlow("6–10 Screens UI")
    val selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val customProjectName = MutableStateFlow("")

    val selectedServices = MutableStateFlow<Map<Long, RequirementSelection>>(emptyMap())
    val customRequirements = MutableStateFlow<List<CustomRequirement>>(emptyList())

    // Discount & GST States in Wizard
    val appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val couponInputText = MutableStateFlow("")
    val couponErrorMessage = MutableStateFlow<String?>(null)

    val manualDiscountPercent = MutableStateFlow(0.0)
    val manualDiscountAmount = MutableStateFlow(0.0)
    val manualDiscountReason = MutableStateFlow("")
    val isDiscountPercentMode = MutableStateFlow(true)
    val approvalRequiredWarning = MutableStateFlow(false)

    val selectedGstType = MutableStateFlow("CGST_SGST") // "CGST_SGST" or "IGST"
    val customGstRate = MutableStateFlow(18.0)

    init {
        // Auto-select first customer if available when loaded
        viewModelScope.launch(Dispatchers.IO) {
            allCustomers.collect { list ->
                if (selectedCustomer.value == null && list.isNotEmpty()) {
                    selectedCustomer.value = list.first()
                }
            }
        }
    }

    fun setWizardStep(step: Int) {
        wizardStep.value = step
    }

    fun selectProjectType(type: String) {
        selectedProjectType.value = type
        if (customProjectName.value.isEmpty()) {
            customProjectName.value = "$type Solution"
        }
    }

    fun toggleServiceSelection(service: ServiceCatalogEntity) {
        val current = selectedServices.value.toMutableMap()
        if (current.containsKey(service.id)) {
            current.remove(service.id)
        } else {
            current[service.id] = RequirementSelection(service = service, quantity = 1, customPrice = service.offerPrice)
        }
        selectedServices.value = current
    }

    fun isServiceSelected(serviceId: Long): Boolean {
        return selectedServices.value.containsKey(serviceId)
    }

    fun addCustomRequirement(req: CustomRequirement) {
        customRequirements.value = customRequirements.value + req
    }

    fun removeCustomRequirement(index: Int) {
        val list = customRequirements.value.toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            customRequirements.value = list
        }
    }

    fun applyPackage(pkg: PackageEntity, servicesList: List<ServiceCatalogEntity>) {
        // Auto select services matching package keywords
        val keywords = pkg.includedFeatureIdsJson.split(",").map { it.trim().lowercase() }
        val current = selectedServices.value.toMutableMap()
        servicesList.forEach { s ->
            if (keywords.any { k -> s.name.lowercase().contains(k) || s.category.lowercase().contains(k) }) {
                current[s.id] = RequirementSelection(service = s, quantity = 1, customPrice = s.offerPrice)
            }
        }
        selectedServices.value = current
    }

    fun applyCouponCode(code: String) {
        couponErrorMessage.value = null
        val cleanCode = code.trim().uppercase()
        viewModelScope.launch(Dispatchers.IO) {
            val coupon = repository.getCouponByCode(cleanCode)
            if (coupon != null) {
                val subtotal = calculateOriginalSubtotal()
                if (subtotal >= coupon.minOrderValue) {
                    appliedCoupon.value = coupon
                    couponErrorMessage.value = null
                } else {
                    couponErrorMessage.value = "Min order value for this coupon is ${CurrencyUtils.formatInr(coupon.minOrderValue)}"
                }
            } else {
                couponErrorMessage.value = "Invalid or expired coupon code: $cleanCode"
            }
        }
    }

    fun removeCoupon() {
        appliedCoupon.value = null
        couponInputText.value = ""
        couponErrorMessage.value = null
    }

    fun setManualDiscount(percentOrFlat: Double, isPercent: Boolean, reason: String) {
        isDiscountPercentMode.value = isPercent
        manualDiscountReason.value = reason
        val settings = businessSettings.value ?: BusinessSettingsEntity()
        val maxAllowed = settings.salesMaxAllowedDiscountPercent

        if (isPercent) {
            manualDiscountPercent.value = percentOrFlat
            manualDiscountAmount.value = 0.0
            approvalRequiredWarning.value = (currentRole.value == UserRole.ADMIN_SALES && percentOrFlat > maxAllowed)
        } else {
            manualDiscountAmount.value = percentOrFlat
            manualDiscountPercent.value = 0.0
            val subtotal = calculateTaxableBeforeManual()
            val effectivePercent = if (subtotal > 0) (percentOrFlat / subtotal) * 100.0 else 0.0
            approvalRequiredWarning.value = (currentRole.value == UserRole.ADMIN_SALES && effectivePercent > maxAllowed)
        }
    }

    // --- CALCULATION LOGIC ---
    fun calculateOriginalSubtotal(): Double {
        val servicesSum = selectedServices.value.values.sumOf { it.service.basePrice * it.quantity }
        val customSum = customRequirements.value.sumOf { it.price * it.quantity }
        return servicesSum + customSum
    }

    fun calculateOfferDiscounts(): Double {
        return selectedServices.value.values.sumOf {
            val diff = (it.service.basePrice - it.service.offerPrice).coerceAtLeast(0.0)
            diff * it.quantity
        }
    }

    fun calculateSubtotalAfterOffers(): Double {
        val servicesSum = selectedServices.value.values.sumOf { it.service.offerPrice * it.quantity }
        val customSum = customRequirements.value.sumOf { (it.price - it.discount).coerceAtLeast(0.0) * it.quantity }
        return servicesSum + customSum
    }

    fun calculateCouponDiscount(): Double {
        val coupon = appliedCoupon.value ?: return 0.0
        val baseForCoupon = calculateSubtotalAfterOffers()
        val discount = if (coupon.discountType == "PERCENTAGE") {
            (baseForCoupon * (coupon.discountValue / 100.0)).coerceAtMost(coupon.maxDiscountValue)
        } else {
            coupon.discountValue.coerceAtMost(baseForCoupon)
        }
        return discount
    }

    private fun calculateTaxableBeforeManual(): Double {
        val afterOffer = calculateSubtotalAfterOffers()
        val couponDisc = calculateCouponDiscount()
        return (afterOffer - couponDisc).coerceAtLeast(0.0)
    }

    fun calculateManualDiscount(): Double {
        val taxableBefore = calculateTaxableBeforeManual()
        return if (isDiscountPercentMode.value) {
            taxableBefore * (manualDiscountPercent.value / 100.0)
        } else {
            manualDiscountAmount.value.coerceAtMost(taxableBefore)
        }
    }

    fun calculateTaxableAmount(): Double {
        val beforeManual = calculateTaxableBeforeManual()
        val manual = calculateManualDiscount()
        return (beforeManual - manual).coerceAtLeast(0.0)
    }

    fun calculateGst(): Triple<Double, Double, Double> { // CGST, SGST, IGST
        val taxable = calculateTaxableAmount()
        val rate = customGstRate.value
        return if (selectedGstType.value == "CGST_SGST") {
            val halfRate = rate / 200.0
            val cgst = taxable * halfRate
            val sgst = taxable * halfRate
            Triple(cgst, sgst, 0.0)
        } else {
            val igst = taxable * (rate / 100.0)
            Triple(0.0, 0.0, igst)
        }
    }

    fun calculateGrandTotal(): Double {
        val taxable = calculateTaxableAmount()
        val (cgst, sgst, igst) = calculateGst()
        return taxable + cgst + sgst + igst
    }

    fun calculateTotalSavings(): Double {
        val offerDisc = calculateOfferDiscounts()
        val couponDisc = calculateCouponDiscount()
        val manualDisc = calculateManualDiscount()
        val customDiscounts = customRequirements.value.sumOf { it.discount * it.quantity }
        return offerDisc + couponDisc + manualDisc + customDiscounts
    }

    // --- ACTIONS ---
    fun resetBuilder() {
        selectedServices.value = emptyMap()
        customRequirements.value = emptyList()
        appliedCoupon.value = null
        couponInputText.value = ""
        couponErrorMessage.value = null
        manualDiscountPercent.value = 0.0
        manualDiscountAmount.value = 0.0
        manualDiscountReason.value = ""
        wizardStep.value = 1
    }

    fun saveQuotation(onSuccess: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val customer = selectedCustomer.value ?: return@launch
            val count = System.currentTimeMillis() % 10000
            val quoteNumber = "RD-QT-${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())}-$count"

            val subtotal = calculateOriginalSubtotal()
            val offerDiscount = calculateOfferDiscounts()
            val couponDiscount = calculateCouponDiscount()
            val manualDiscount = calculateManualDiscount()
            val taxable = calculateTaxableAmount()
            val (cgst, sgst, igst) = calculateGst()
            val grandTotal = calculateGrandTotal()
            val totalSavings = calculateTotalSavings()

            val quote = QuotationEntity(
                quotationNumber = quoteNumber,
                customerId = customer.id,
                customerName = if (customer.businessName.isNotEmpty()) "${customer.name} (${customer.businessName})" else customer.name,
                customerPhone = customer.mobileNumber,
                customerEmail = customer.email,
                projectName = customProjectName.value.ifEmpty { "${selectedProjectType.value} for ${customer.name}" },
                projectType = selectedProjectType.value,
                status = if (approvalRequiredWarning.value) "Pending Admin Approval" else "Sent",
                subtotal = subtotal,
                offerDiscount = offerDiscount,
                couponCode = appliedCoupon.value?.code ?: "",
                couponDiscount = couponDiscount,
                manualDiscount = manualDiscount,
                manualDiscountReason = manualDiscountReason.value,
                isDiscountApprovalPending = approvalRequiredWarning.value,
                taxableAmount = taxable,
                gstType = selectedGstType.value,
                gstRate = customGstRate.value,
                cgstAmount = cgst,
                sgstAmount = sgst,
                igstAmount = igst,
                grandTotal = grandTotal,
                totalSavings = totalSavings,
                createdByRole = currentRole.value.label
            )

            val items = mutableListOf<QuotationItemEntity>()
            selectedServices.value.values.forEach { req ->
                val s = req.service
                val disc = (s.basePrice - s.offerPrice).coerceAtLeast(0.0) * req.quantity
                val taxAmt = s.offerPrice * req.quantity
                val totalWithGst = taxAmt * (1.0 + customGstRate.value / 100.0)
                items.add(
                    QuotationItemEntity(
                        quotationId = 0,
                        serviceName = s.name,
                        description = s.description,
                        category = s.category,
                        quantity = req.quantity,
                        originalPrice = s.basePrice * req.quantity,
                        offerPrice = s.offerPrice * req.quantity,
                        discount = disc,
                        taxableAmount = taxAmt,
                        gstRate = customGstRate.value,
                        totalAmount = totalWithGst,
                        developmentDays = s.developmentDays
                    )
                )
            }

            customRequirements.value.forEach { custom ->
                val orig = custom.price * custom.quantity
                val taxAmt = (orig - (custom.discount * custom.quantity)).coerceAtLeast(0.0)
                val totalWithGst = if (custom.isGstApplicable) taxAmt * (1.0 + customGstRate.value / 100.0) else taxAmt
                items.add(
                    QuotationItemEntity(
                        quotationId = 0,
                        serviceName = custom.name,
                        description = custom.description,
                        category = "Custom Requirement",
                        quantity = custom.quantity,
                        originalPrice = orig,
                        offerPrice = taxAmt,
                        discount = custom.discount * custom.quantity,
                        taxableAmount = taxAmt,
                        gstRate = if (custom.isGstApplicable) customGstRate.value else 0.0,
                        totalAmount = totalWithGst,
                        developmentDays = custom.estimatedDays,
                        isCustom = true
                    )
                )
            }

            val savedId = repository.saveQuotationWithItems(quote, items)

            // If discount approval needed, log approval request
            if (approvalRequiredWarning.value) {
                repository.requestDiscountApproval(
                    DiscountApprovalEntity(
                        quotationId = savedId,
                        requestedBy = currentRole.value.label,
                        customerName = customer.name,
                        originalTotal = subtotal,
                        requestedDiscountPercent = if (isDiscountPercentMode.value) manualDiscountPercent.value else ((manualDiscount / subtotal) * 100.0),
                        requestedDiscountAmount = manualDiscount,
                        reason = manualDiscountReason.value
                    )
                )
            }

            launch(Dispatchers.Main) {
                resetBuilder()
                onSuccess(savedId)
            }
        }
    }

    fun updateQuotationStatus(id: Long, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuotationStatus(id, status)
        }
    }

    fun requestRevision(id: Long, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.requestRevision(id, note)
        }
    }

    fun convertQuotationToInvoice(quoteId: Long, onComplete: (Long, Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val (invId, projId) = repository.convertQuotationToInvoiceAndProject(quoteId)
            launch(Dispatchers.Main) {
                onComplete(invId, projId)
            }
        }
    }

    fun recordPayment(invoiceId: Long, amount: Double, paymentType: String, method: String, ref: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.recordPayment(invoiceId, amount, paymentType, method, ref, notes)
        }
    }

    fun updateProjectStage(id: Long, stage: String, progress: Int, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProjectStage(id, stage, progress, isCompleted)
        }
    }

    fun updateProjectFeatureStatus(featureId: Long, status: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateFeatureStatus(featureId, status)
        }
    }

    fun approveDiscount(approvalId: Long, quoteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reviewDiscountApproval(approvalId, "APPROVED", currentRole.value.label)
            repository.updateQuotationStatus(quoteId, "Approved")
        }
    }

    fun rejectDiscount(approvalId: Long, quoteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.reviewDiscountApproval(approvalId, "REJECTED", currentRole.value.label)
            repository.updateQuotationStatus(quoteId, "Discount Rejected")
        }
    }

    fun addNewCustomer(customer: CustomerEntity, onAdded: (CustomerEntity) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertCustomer(customer)
            val inserted = customer.copy(id = id)
            launch(Dispatchers.Main) {
                selectedCustomer.value = inserted
                onAdded(inserted)
            }
        }
    }

    fun saveService(service: ServiceCatalogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (service.id == 0L) {
                repository.insertService(service)
            } else {
                repository.updateService(service)
            }
        }
    }

    fun deleteService(service: ServiceCatalogEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteService(service)
        }
    }

    fun saveCoupon(coupon: CouponEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (coupon.id == 0L) {
                repository.insertCoupon(coupon)
            } else {
                repository.updateCoupon(coupon)
            }
        }
    }

    fun deleteCoupon(coupon: CouponEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCoupon(coupon)
        }
    }

    fun saveSettings(settings: BusinessSettingsEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveSettings(settings)
        }
    }

    // Smart AI Auto-Builder Preset (interprets a client brief and pre-selects relevant catalog items)
    fun applyAiPreset(presetType: String, servicesList: List<ServiceCatalogEntity>) {
        val current = mutableMapOf<Long, RequirementSelection>()
        when (presetType) {
            "ecommerce_complete" -> {
                selectedProjectType.value = "Website & Android App"
                val matchKeys = listOf("e-commerce", "product & variant", "cart", "payment gateway", "whatsapp", "android native", "gst invoicing")
                servicesList.forEach { s ->
                    if (matchKeys.any { k -> s.name.lowercase().contains(k) }) {
                        current[s.id] = RequirementSelection(s, 1, s.offerPrice)
                    }
                }
            }
            "restaurant_qr" -> {
                selectedProjectType.value = "Restaurant Solution"
                val matchKeys = listOf("digital qr menu", "online food ordering", "kitchen dashboard", "whatsapp ordering", "upi instant")
                servicesList.forEach { s ->
                    if (matchKeys.any { k -> s.name.lowercase().contains(k) }) {
                        current[s.id] = RequirementSelection(s, 1, s.offerPrice)
                    }
                }
            }
            "realestate_crm" -> {
                selectedProjectType.value = "Real Estate Portal"
                val matchKeys = listOf("property listing", "360°", "site visit", "ai real estate calling", "google maps")
                servicesList.forEach { s ->
                    if (matchKeys.any { k -> s.name.lowercase().contains(k) }) {
                        current[s.id] = RequirementSelection(s, 1, s.offerPrice)
                    }
                }
            }
            "ai_automation_bot" -> {
                selectedProjectType.value = "AI Automation"
                val matchKeys = listOf("ai voice calling", "ai chatbot", "whatsapp business api", "lead generation", "appointment & booking")
                servicesList.forEach { s ->
                    if (matchKeys.any { k -> s.name.lowercase().contains(k) }) {
                        current[s.id] = RequirementSelection(s, 1, s.offerPrice)
                    }
                }
            }
            "startup_minimal" -> {
                selectedProjectType.value = "Website"
                val matchKeys = listOf("5 pages website", "standard ui", "contact & lead", "domain name", "high-speed ssd", "logo & brand")
                servicesList.forEach { s ->
                    if (matchKeys.any { k -> s.name.lowercase().contains(k) }) {
                        current[s.id] = RequirementSelection(s, 1, s.offerPrice)
                    }
                }
            }
        }
        selectedServices.value = current
    }
}
