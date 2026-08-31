package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): Flow<Int>
}

@Dao
interface ServiceCatalogDao {
    @Query("SELECT * FROM service_catalog WHERE isActive = 1 ORDER BY category ASC, name ASC")
    fun getAllActiveServices(): Flow<List<ServiceCatalogEntity>>

    @Query("SELECT * FROM service_catalog ORDER BY category ASC, name ASC")
    fun getAllServices(): Flow<List<ServiceCatalogEntity>>

    @Query("SELECT * FROM service_catalog WHERE category = :category AND isActive = 1")
    fun getServicesByCategory(category: String): Flow<List<ServiceCatalogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceCatalogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceCatalogEntity>)

    @Update
    suspend fun updateService(service: ServiceCatalogEntity)

    @Delete
    suspend fun deleteService(service: ServiceCatalogEntity)

    @Query("SELECT COUNT(*) FROM service_catalog")
    suspend fun getServiceCount(): Int
}

@Dao
interface PackageDao {
    @Query("SELECT * FROM packages WHERE isActive = 1")
    fun getAllActivePackages(): Flow<List<PackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: PackageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(pkgs: List<PackageEntity>)

    @Update
    suspend fun updatePackage(pkg: PackageEntity)

    @Delete
    suspend fun deletePackage(pkg: PackageEntity)
}

@Dao
interface CouponDao {
    @Query("SELECT * FROM coupons ORDER BY code ASC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE code = :code AND isActive = 1 LIMIT 1")
    suspend fun getCouponByCode(code: String): CouponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Delete
    suspend fun deleteCoupon(coupon: CouponEntity)
}

@Dao
interface QuotationDao {
    @Query("SELECT * FROM quotations ORDER BY createdAt DESC")
    fun getAllQuotations(): Flow<List<QuotationEntity>>

    @Query("SELECT * FROM quotations WHERE id = :id")
    suspend fun getQuotationById(id: Long): QuotationEntity?

    @Query("SELECT * FROM quotation_items WHERE quotationId = :quotationId")
    fun getItemsForQuotation(quotationId: Long): Flow<List<QuotationItemEntity>>

    @Query("SELECT * FROM quotation_items WHERE quotationId = :quotationId")
    suspend fun getItemsForQuotationSync(quotationId: Long): List<QuotationItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotation(quotation: QuotationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotationItems(items: List<QuotationItemEntity>)

    @Update
    suspend fun updateQuotation(quotation: QuotationEntity)

    @Query("UPDATE quotations SET status = :status WHERE id = :id")
    suspend fun updateQuotationStatus(id: Long, status: String)

    @Query("UPDATE quotations SET status = 'Revision Requested', revisionNote = :note WHERE id = :id")
    suspend fun requestRevision(id: Long, note: String)

    @Delete
    suspend fun deleteQuotation(quotation: QuotationEntity)

    @Query("DELETE FROM quotation_items WHERE quotationId = :quotationId")
    suspend fun deleteQuotationItems(quotationId: Long)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY issueDate DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Long): Flow<List<InvoiceItemEntity>>

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getItemsForInvoiceSync(invoiceId: Long): List<InvoiceItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    @Query("UPDATE invoices SET paidAmount = :paid, dueAmount = :due, status = :status WHERE id = :id")
    suspend fun updatePaymentState(id: Long, paid: Double, due: Double, status: String)

    @Delete
    suspend fun deleteInvoice(invoice: InvoiceEntity)
}

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY paymentDate DESC")
    fun getPaymentsForInvoice(invoiceId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY startDate DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM project_features WHERE projectId = :projectId")
    fun getFeaturesForProject(projectId: Long): Flow<List<ProjectFeatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectFeatures(features: List<ProjectFeatureEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE project_features SET status = :status WHERE id = :featureId")
    suspend fun updateFeatureStatus(featureId: Long, status: String)

    @Query("UPDATE projects SET stage = :stage, progressPercent = :progress, isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateProjectStage(id: Long, stage: String, progress: Int, isCompleted: Boolean)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}

@Dao
interface DiscountApprovalDao {
    @Query("SELECT * FROM discount_approvals ORDER BY requestedAt DESC")
    fun getAllApprovals(): Flow<List<DiscountApprovalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: DiscountApprovalEntity): Long

    @Query("UPDATE discount_approvals SET status = :status, reviewedBy = :reviewer, reviewedAt = :time WHERE id = :id")
    suspend fun reviewApproval(id: Long, status: String, reviewer: String, time: Long)
}

@Dao
interface SettingsDao {
    @Query("SELECT * FROM business_settings WHERE id = 1")
    fun getSettings(): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings WHERE id = 1")
    suspend fun getSettingsSync(): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: BusinessSettingsEntity)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long
}
