package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class SmartQuoteRepository(private val database: SmartQuoteDatabase) {

    private val customerDao = database.customerDao()
    private val catalogDao = database.serviceCatalogDao()
    private val packageDao = database.packageDao()
    private val couponDao = database.couponDao()
    private val quotationDao = database.quotationDao()
    private val invoiceDao = database.invoiceDao()
    private val paymentDao = database.paymentDao()
    private val projectDao = database.projectDao()
    private val discountApprovalDao = database.discountApprovalDao()
    private val settingsDao = database.settingsDao()
    private val activityLogDao = database.activityLogDao()

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    suspend fun getCustomerById(id: Long) = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: CustomerEntity) = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)

    // Catalog & Services
    val allServices: Flow<List<ServiceCatalogEntity>> = catalogDao.getAllServices()
    val activeServices: Flow<List<ServiceCatalogEntity>> = catalogDao.getAllActiveServices()
    fun getServicesByCategory(category: String) = catalogDao.getServicesByCategory(category)
    suspend fun insertService(service: ServiceCatalogEntity) = catalogDao.insertService(service)
    suspend fun updateService(service: ServiceCatalogEntity) = catalogDao.updateService(service)
    suspend fun deleteService(service: ServiceCatalogEntity) = catalogDao.deleteService(service)

    // Packages & Coupons
    val activePackages: Flow<List<PackageEntity>> = packageDao.getAllActivePackages()
    suspend fun insertPackage(pkg: PackageEntity) = packageDao.insertPackage(pkg)
    suspend fun deletePackage(pkg: PackageEntity) = packageDao.deletePackage(pkg)

    val allCoupons: Flow<List<CouponEntity>> = couponDao.getAllCoupons()
    suspend fun getCouponByCode(code: String) = couponDao.getCouponByCode(code)
    suspend fun insertCoupon(coupon: CouponEntity) = couponDao.insertCoupon(coupon)
    suspend fun updateCoupon(coupon: CouponEntity) = couponDao.updateCoupon(coupon)
    suspend fun deleteCoupon(coupon: CouponEntity) = couponDao.deleteCoupon(coupon)

    // Quotations
    val allQuotations: Flow<List<QuotationEntity>> = quotationDao.getAllQuotations()
    suspend fun getQuotationById(id: Long) = quotationDao.getQuotationById(id)
    fun getQuotationItems(quotationId: Long) = quotationDao.getItemsForQuotation(quotationId)
    suspend fun getQuotationItemsSync(quotationId: Long) = quotationDao.getItemsForQuotationSync(quotationId)

    suspend fun saveQuotationWithItems(
        quotation: QuotationEntity,
        items: List<QuotationItemEntity>
    ): Long {
        val quoteId = quotationDao.insertQuotation(quotation)
        val itemsWithId = items.map { it.copy(quotationId = quoteId) }
        quotationDao.insertQuotationItems(itemsWithId)

        activityLogDao.insertLog(
            ActivityLogEntity(
                actionType = "QUOTE_CREATED",
                title = "Quotation Generated: ${quotation.quotationNumber}",
                description = "For ${quotation.customerName} - ₹${quotation.grandTotal.toLong()}"
            )
        )
        return quoteId
    }

    suspend fun updateQuotationStatus(id: Long, status: String) {
        quotationDao.updateQuotationStatus(id, status)
        activityLogDao.insertLog(
            ActivityLogEntity(
                actionType = "STATUS_CHANGED",
                title = "Quotation #$id Status: $status",
                description = "Updated quotation status to $status"
            )
        )
    }

    suspend fun requestRevision(id: Long, note: String) {
        quotationDao.requestRevision(id, note)
        activityLogDao.insertLog(
            ActivityLogEntity(
                actionType = "STATUS_CHANGED",
                title = "Quotation Revision Requested",
                description = "Client requested changes: $note"
            )
        )
    }

    suspend fun deleteQuotation(quotation: QuotationEntity) {
        quotationDao.deleteQuotationItems(quotation.id)
        quotationDao.deleteQuotation(quotation)
    }

    // Invoices
    val allInvoices: Flow<List<InvoiceEntity>> = invoiceDao.getAllInvoices()
    suspend fun getInvoiceById(id: Long) = invoiceDao.getInvoiceById(id)
    fun getInvoiceItems(invoiceId: Long) = invoiceDao.getItemsForInvoice(invoiceId)
    suspend fun getInvoiceItemsSync(invoiceId: Long) = invoiceDao.getItemsForInvoiceSync(invoiceId)

    suspend fun saveInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ): Long {
        val invId = invoiceDao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invId) }
        invoiceDao.insertInvoiceItems(itemsWithId)

        activityLogDao.insertLog(
            ActivityLogEntity(
                actionType = "INVOICE_GENERATED",
                title = "Invoice Issued: ${invoice.invoiceNumber}",
                description = "Issued to ${invoice.customerName} for ₹${invoice.grandTotal.toLong()}"
            )
        )
        return invId
    }

    suspend fun deleteInvoice(invoice: InvoiceEntity) {
        invoiceDao.deleteInvoice(invoice)
    }

    // Convert Quotation to Invoice & Auto-create Project
    suspend fun convertQuotationToInvoiceAndProject(quoteId: Long): Pair<Long, Long> {
        val quote = quotationDao.getQuotationById(quoteId) ?: return Pair(0L, 0L)
        val items = quotationDao.getItemsForQuotationSync(quoteId)
        val customer = customerDao.getCustomerById(quote.customerId)

        val invCount = System.currentTimeMillis() % 10000
        val invoiceNumber = "RD-INV-${SimpleDateFormat("yyyyMM", Locale.getDefault()).format(Date())}-$invCount"

        val invoice = InvoiceEntity(
            invoiceNumber = invoiceNumber,
            quotationId = quote.id,
            customerId = quote.customerId,
            customerName = quote.customerName,
            customerBusiness = customer?.businessName ?: "",
            customerGstin = customer?.gstin ?: "",
            customerAddress = "${customer?.billingAddress ?: ""}, ${customer?.city ?: ""}, ${customer?.state ?: "Maharashtra"} ${customer?.pinCode ?: ""}",
            projectName = quote.projectName,
            invoiceType = "GST Tax Invoice",
            status = "Unpaid",
            subtotal = quote.subtotal,
            discount = quote.offerDiscount + quote.couponDiscount + quote.manualDiscount,
            taxableTotal = quote.taxableAmount,
            gstType = quote.gstType,
            gstRate = quote.gstRate,
            cgstAmount = quote.cgstAmount,
            sgstAmount = quote.sgstAmount,
            igstAmount = quote.igstAmount,
            totalGst = quote.cgstAmount + quote.sgstAmount + quote.igstAmount,
            grandTotal = quote.grandTotal,
            dueAmount = quote.grandTotal,
            paymentTerms = "Advance 30% on start, 70% on completion."
        )

        val invoiceItems = items.map { item ->
            InvoiceItemEntity(
                invoiceId = 0,
                itemName = item.serviceName,
                description = item.description,
                rate = item.taxableAmount,
                discount = item.discount,
                taxableAmount = item.taxableAmount,
                cgstAmount = if (quote.gstType == "CGST_SGST") (item.taxableAmount * (quote.gstRate / 200.0)) else 0.0,
                sgstAmount = if (quote.gstType == "CGST_SGST") (item.taxableAmount * (quote.gstRate / 200.0)) else 0.0,
                igstAmount = if (quote.gstType == "IGST") (item.taxableAmount * (quote.gstRate / 100.0)) else 0.0,
                total = item.totalAmount
            )
        }

        val invoiceId = saveInvoiceWithItems(invoice, invoiceItems)

        // Mark quote as converted
        quotationDao.updateQuotationStatus(quote.id, "Converted to Invoice")

        // Auto-create Project
        val project = ProjectEntity(
            quotationId = quote.id,
            invoiceId = invoiceId,
            customerId = quote.customerId,
            customerName = quote.customerName,
            projectName = quote.projectName,
            projectType = quote.projectType,
            stage = "Requirement Received",
            progressPercent = 10,
            totalBudget = quote.grandTotal,
            receivedAmount = 0.0
        )
        val projectId = projectDao.insertProject(project)

        val projectFeatures = items.map { item ->
            ProjectFeatureEntity(
                projectId = projectId,
                featureName = item.serviceName,
                category = item.category,
                estimatedDays = item.developmentDays,
                status = "Not Started"
            )
        }
        projectDao.insertProjectFeatures(projectFeatures)

        activityLogDao.insertLog(
            ActivityLogEntity(
                actionType = "PROJECT_UPDATED",
                title = "New Project Created: ${quote.projectName}",
                description = "Linked to Invoice $invoiceNumber"
            )
        )

        return Pair(invoiceId, projectId)
    }

    // Payments
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()
    fun getPaymentsForInvoice(invoiceId: Long) = paymentDao.getPaymentsForInvoice(invoiceId)

    suspend fun recordPayment(
        invoiceId: Long,
        amount: Double,
        paymentType: String,
        paymentMethod: String,
        ref: String,
        notes: String
    ) {
        val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return
        paymentDao.insertPayment(
            PaymentEntity(
                invoiceId = invoiceId,
                projectId = 0,
                amount = amount,
                paymentType = paymentType,
                paymentMethod = paymentMethod,
                transactionRef = ref,
                notes = notes
            )
        )

        val newPaid = invoice.paidAmount + amount
        val newDue = (invoice.grandTotal - newPaid).coerceAtLeast(0.0)
        val newStatus = when {
            newDue <= 0.5 -> "Paid"
            newPaid > 0.0 -> "Partially Paid"
            else -> "Unpaid"
        }
        invoiceDao.updatePaymentState(invoiceId, newPaid, newDue, newStatus)

        // Also update project received amount if linked
        val projects = projectDao.getAllProjects()
        // Record activity
        activityLogDao.insertLog(
            ActivityLogEntity(
                actionType = "PAYMENT_RECEIVED",
                title = "Payment Recorded: ₹${amount.toLong()}",
                description = "For Invoice #${invoice.invoiceNumber} via $paymentMethod ($paymentType)"
            )
        )
    }

    // Projects
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    suspend fun getProjectById(id: Long) = projectDao.getProjectById(id)
    fun getProjectFeatures(projectId: Long) = projectDao.getFeaturesForProject(projectId)
    suspend fun updateProjectStage(id: Long, stage: String, progress: Int, isCompleted: Boolean) =
        projectDao.updateProjectStage(id, stage, progress, isCompleted)
    suspend fun updateFeatureStatus(featureId: Long, status: String) =
        projectDao.updateFeatureStatus(featureId, status)

    // Discount Approvals
    val allDiscountApprovals: Flow<List<DiscountApprovalEntity>> = discountApprovalDao.getAllApprovals()
    suspend fun requestDiscountApproval(approval: DiscountApprovalEntity) =
        discountApprovalDao.insertApproval(approval)
    suspend fun reviewDiscountApproval(id: Long, status: String, reviewer: String) =
        discountApprovalDao.reviewApproval(id, status, reviewer, System.currentTimeMillis())

    // Settings
    val businessSettings: Flow<BusinessSettingsEntity?> = settingsDao.getSettings()
    suspend fun getSettingsSync() = settingsDao.getSettingsSync()
    suspend fun saveSettings(settings: BusinessSettingsEntity) = settingsDao.saveSettings(settings)

    // Logs
    val recentActivityLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getRecentLogs()
}
