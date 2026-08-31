package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val businessName: String = "",
    val mobileNumber: String,
    val whatsAppNumber: String = "",
    val email: String = "",
    val billingAddress: String = "",
    val city: String = "",
    val state: String = "Maharashtra",
    val pinCode: String = "",
    val gstin: String = "",
    val pan: String = "",
    val customerType: String = "Business", // Individual, Business, Company, Restaurant, Hotel, Real Estate, Clinic, Gym, Retail Store, E-Commerce, Startup, Other
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "service_catalog")
data class ServiceCatalogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // "Website", "Mobile App", "Software", "AI Automation", "Add-on", "UI/UX", "Custom"
    val subCategory: String = "", // e.g. "E-Commerce", "Restaurant", "Real Estate", "Core Features"
    val name: String,
    val description: String = "",
    val basePrice: Double,
    val offerPrice: Double = basePrice,
    val isGstApplicable: Boolean = true,
    val gstRate: Double = 18.0,
    val developmentDays: Int = 3,
    val isActive: Boolean = true,
    val hsnSacCode: String = "998314", // IT Design & Development SAC
    val iconName: String = "code"
)

@Entity(tableName = "packages")
data class PackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val regularPrice: Double,
    val packagePrice: Double,
    val includedFeatureIdsJson: String, // JSON or comma-separated names/ids
    val tag: String = "POPULAR",
    val isActive: Boolean = true
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val discountType: String, // "PERCENTAGE" or "FLAT"
    val discountValue: Double,
    val minOrderValue: Double = 0.0,
    val maxDiscountValue: Double = 50000.0,
    val expiryDateMillis: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val usageLimit: Int = 100,
    val timesUsed: Int = 0,
    val isActive: Boolean = true
)

@Entity(tableName = "quotations")
data class QuotationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quotationNumber: String, // e.g., RD-QT-2026-001
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String = "",
    val projectName: String,
    val projectType: String, // Website, Android App, iOS App, Android + iOS, Software, AI Automation, Custom
    val status: String = "Draft", // Draft, Sent, Viewed, Accepted, Rejected, Expired, Revision Requested, Converted to Invoice
    val revisionNote: String = "",
    val subtotal: Double,
    val offerDiscount: Double = 0.0,
    val couponCode: String = "",
    val couponDiscount: Double = 0.0,
    val manualDiscount: Double = 0.0,
    val manualDiscountReason: String = "",
    val isDiscountApprovalPending: Boolean = false,
    val discountApprovedBy: String = "",
    val taxableAmount: Double,
    val gstType: String = "CGST_SGST", // "CGST_SGST" or "IGST"
    val gstRate: Double = 18.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val grandTotal: Double,
    val totalSavings: Double = 0.0,
    val termsAndConditions: String = "",
    val validityDays: Int = 15,
    val createdAt: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000,
    val createdByRole: String = "Super Admin" // Super Admin, Admin / Sales Executive, Customer
)

@Entity(tableName = "quotation_items")
data class QuotationItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quotationId: Long,
    val serviceName: String,
    val description: String = "",
    val category: String = "General",
    val quantity: Int = 1,
    val originalPrice: Double,
    val offerPrice: Double,
    val discount: Double = 0.0,
    val taxableAmount: Double,
    val gstRate: Double = 18.0,
    val totalAmount: Double,
    val developmentDays: Int = 1,
    val isCustom: Boolean = false
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String, // e.g. RD-INV-2026-001
    val quotationId: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerBusiness: String = "",
    val customerGstin: String = "",
    val customerAddress: String = "",
    val projectName: String,
    val invoiceType: String = "GST Tax Invoice", // GST Tax Invoice or Proforma Invoice
    val status: String = "Unpaid", // Paid, Partially Paid, Unpaid, Overdue
    val issueDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis() + 15L * 24 * 60 * 60 * 1000,
    val subtotal: Double,
    val discount: Double = 0.0,
    val taxableTotal: Double,
    val gstType: String = "CGST_SGST",
    val gstRate: Double = 18.0,
    val cgstAmount: Double = 0.0,
    val sgstAmount: Double = 0.0,
    val igstAmount: Double = 0.0,
    val totalGst: Double = 0.0,
    val roundOff: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double = 0.0,
    val dueAmount: Double,
    val amountInWords: String = "",
    val paymentTerms: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items")
data class InvoiceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val itemName: String,
    val description: String = "",
    val hsnSacCode: String = "998314",
    val quantity: Int = 1,
    val rate: Double,
    val discount: Double = 0.0,
    val taxableAmount: Double,
    val cgstRate: Double = 9.0,
    val cgstAmount: Double = 0.0,
    val sgstRate: Double = 9.0,
    val sgstAmount: Double = 0.0,
    val igstRate: Double = 18.0,
    val igstAmount: Double = 0.0,
    val total: Double
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val projectId: Long = 0,
    val amount: Double,
    val paymentType: String = "Advance Payment", // Advance Payment, Milestone Payment, Full Payment
    val paymentMethod: String = "UPI", // Cash, UPI, Bank Transfer, Credit Card, Debit Card, Cheque, Other
    val transactionRef: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quotationId: Long,
    val invoiceId: Long = 0,
    val customerId: Long,
    val customerName: String,
    val projectName: String,
    val projectType: String,
    val stage: String = "Requirement Received", // Requirement Received, UI/UX Design, UI Approved, Development Started, Backend Development, Testing, Customer Review, Changes Requested, Final Testing, Deployment, Completed
    val progressPercent: Int = 10,
    val startDate: Long = System.currentTimeMillis(),
    val targetEndDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
    val totalBudget: Double,
    val receivedAmount: Double = 0.0,
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "project_features")
data class ProjectFeatureEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val featureName: String,
    val category: String = "Core",
    val estimatedDays: Int = 2,
    val status: String = "Not Started" // Not Started, In Progress, Completed, On Hold, Removed
)

@Entity(tableName = "discount_approvals")
data class DiscountApprovalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quotationId: Long,
    val requestedBy: String,
    val customerName: String,
    val originalTotal: Double,
    val requestedDiscountPercent: Double,
    val requestedDiscountAmount: Double,
    val reason: String,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestedAt: Long = System.currentTimeMillis(),
    val reviewedBy: String = "",
    val reviewedAt: Long = 0
)

@Entity(tableName = "business_settings")
data class BusinessSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Rishi_dev",
    val tagline: String = "AI GST Quotation, Billing & Project Requirement Builder",
    val gstin: String = "27ALDPI8191C1Z5",
    val pan: String = "ALDPI8191C",
    val email: String = "rishiingole4@gmail.com",
    val phone: String = "+91 98765 43210",
    val address: String = "Tech Hub, Pune, Maharashtra 411045",
    val state: String = "Maharashtra",
    val stateCode: String = "27",
    val country: String = "India",
    val currencySymbol: String = "₹",
    val defaultGstRate: Double = 18.0,
    val salesMaxAllowedDiscountPercent: Double = 10.0,
    val bankName: String = "HDFC Bank Ltd",
    val accountHolder: String = "Rishi_dev Solutions",
    val accountNumber: String = "50200067891234",
    val ifscCode: String = "HDFC0001234",
    val upiId: String = "rishi.dev@okhdfcbank",
    val termsAndConditions: String = "1. Quotation is valid for 15 days.\n2. Work starts after 30% advance payment.\n3. Additional requirements will be charged separately.\n4. Third-party API, domain & hosting charges billed at actuals.\n5. Project timeline starts after wireframes/requirements are confirmed.\n6. Final handover and source code release after 100% payment clearance."
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String, // QUOTE_CREATED, INVOICE_GENERATED, PAYMENT_RECEIVED, PROJECT_UPDATED, DISCOUNT_REQUESTED, STATUS_CHANGED
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
