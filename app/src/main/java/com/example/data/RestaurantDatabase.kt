package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {
    // Unified User Accounts
    @Query("SELECT * FROM user_accounts ORDER BY id ASC")
    fun getAllUserAccounts(): Flow<List<UserAccountEntity>>

    @Query("SELECT * FROM user_accounts WHERE LOWER(username) = LOWER(:username) OR phone = :username LIMIT 1")
    suspend fun findAccountByUsernameOrPhone(username: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :userId LIMIT 1")
    suspend fun getAccountById(userId: Long): UserAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccounts(accounts: List<UserAccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAccount(account: UserAccountEntity): Long

    @Update
    suspend fun updateUserAccount(account: UserAccountEntity)

    // Customer Delivery Addresses
    @Query("SELECT * FROM customer_addresses ORDER BY isDefault DESC, id ASC")
    fun getAllCustomerAddresses(): Flow<List<CustomerAddressEntity>>

    @Query("SELECT * FROM customer_addresses WHERE userId = :userId ORDER BY isDefault DESC, id ASC")
    suspend fun getAddressesForUserOnce(userId: Long): List<CustomerAddressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerAddresses(addresses: List<CustomerAddressEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerAddress(address: CustomerAddressEntity): Long

    @Update
    suspend fun updateCustomerAddress(address: CustomerAddressEntity)

    @Delete
    suspend fun deleteCustomerAddress(address: CustomerAddressEntity)

    @Query("UPDATE customer_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddressesForUser(userId: Long)

    // Electronic Payment Gateways
    @Query("SELECT * FROM payment_gateways ORDER BY id ASC")
    fun getAllPaymentGateways(): Flow<List<PaymentGatewayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentGateways(gateways: List<PaymentGatewayEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentGateway(gateway: PaymentGatewayEntity): Long

    @Update
    suspend fun updatePaymentGateway(gateway: PaymentGatewayEntity)

    // Categories & Products
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("SELECT * FROM products ORDER BY id ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // Drivers
    @Query("SELECT * FROM drivers ORDER BY id ASC")
    fun getAllDrivers(): Flow<List<DriverEntity>>

    @Query("SELECT * FROM drivers WHERE id = :driverId LIMIT 1")
    suspend fun getDriverById(driverId: Long): DriverEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrivers(drivers: List<DriverEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDriver(driver: DriverEntity): Long

    @Update
    suspend fun updateDriver(driver: DriverEntity)

    // Coupons
    @Query("SELECT * FROM coupons ORDER BY id ASC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupons(coupons: List<CouponEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity): Long

    @Update
    suspend fun updateCoupon(coupon: CouponEntity)

    @Delete
    suspend fun deleteCoupon(coupon: CouponEntity)

    // Orders
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    // Commission Config
    @Query("SELECT * FROM commission_config WHERE id = 1 LIMIT 1")
    fun getCommissionConfig(): Flow<CommissionConfigEntity?>

    @Query("SELECT * FROM commission_config WHERE id = 1 LIMIT 1")
    suspend fun getCommissionConfigOnce(): CommissionConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCommissionConfig(config: CommissionConfigEntity)

    // Driver Transactions
    @Query("SELECT * FROM driver_transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<DriverTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<DriverTransactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: DriverTransactionEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY id DESC LIMIT 60")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE targetRole = :role AND (:userId IS NULL OR targetUserId = :userId OR targetUserId IS NULL) AND (:driverId IS NULL OR targetDriverId = :driverId OR targetDriverId IS NULL)")
    suspend fun markNotificationsReadForUser(role: String, userId: Long?, driverId: Long?)

    // Online Server Config
    @Query("SELECT * FROM server_config WHERE id = 1 LIMIT 1")
    fun getServerConfig(): Flow<ServerConfigEntity?>

    @Query("SELECT * FROM server_config WHERE id = 1 LIMIT 1")
    suspend fun getServerConfigOnce(): ServerConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveServerConfig(config: ServerConfigEntity)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}

@Database(
    entities = [
        UserAccountEntity::class,
        CustomerAddressEntity::class,
        PaymentGatewayEntity::class,
        CategoryEntity::class,
        ProductEntity::class,
        DriverEntity::class,
        CouponEntity::class,
        OrderEntity::class,
        CommissionConfigEntity::class,
        DriverTransactionEntity::class,
        NotificationEntity::class,
        ServerConfigEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class RestaurantDatabase : RoomDatabase() {
    abstract fun dao(): RestaurantDao

    companion object {
        @Volatile
        private var INSTANCE: RestaurantDatabase? = null

        fun getInstance(context: Context): RestaurantDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    RestaurantDatabase::class.java,
                    "restaurant_delivery_system.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
