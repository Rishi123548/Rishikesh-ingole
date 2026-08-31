package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CustomerEntity::class,
        ServiceCatalogEntity::class,
        PackageEntity::class,
        CouponEntity::class,
        QuotationEntity::class,
        QuotationItemEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        PaymentEntity::class,
        ProjectEntity::class,
        ProjectFeatureEntity::class,
        DiscountApprovalEntity::class,
        BusinessSettingsEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmartQuoteDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun serviceCatalogDao(): ServiceCatalogDao
    abstract fun packageDao(): PackageDao
    abstract fun couponDao(): CouponDao
    abstract fun quotationDao(): QuotationDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun paymentDao(): PaymentDao
    abstract fun projectDao(): ProjectDao
    abstract fun discountApprovalDao(): DiscountApprovalDao
    abstract fun settingsDao(): SettingsDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: SmartQuoteDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SmartQuoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartQuoteDatabase::class.java,
                    "rishi_smartquote_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            scope.launch(Dispatchers.IO) {
                                DatabaseSeeder.seedDatabase(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
