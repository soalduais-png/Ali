package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val titleAr: String, val badgeAr: String) {
    SUPER_ADMIN("مدير النظام العام (Super Admin)", "كامل الصلاحيات + تعديل العمولات والأسعار وبوابات الدفع"),
    RESTAURANT_ADMIN("مدير المطعم (Restaurant Admin)", "إدارة المطعم والمنتجات والعروض والمندوبين"),
    STAFF("موظف كاشير/مطبخ (Staff)", "استلام وتحضير الطلبات فقط بدون تعديل العمولات"),
    DRIVER("مندوب توصيل (Driver)", "استلام وتوصيل الطلبات والملاحة والأرباح"),
    CUSTOMER("عميل (Customer)", "تصفح المنيو، السلة، العناوين، الدفع، وتتبع الطلب الحي")
}

enum class AccountStatus(val titleAr: String) {
    ACTIVE("مفعل ونشط"),
    PENDING_APPROVAL("بانتظار تفعيل الإدارة"),
    SUSPENDED("موقوف مؤقتاً")
}

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String, // username or phone number used for login
    val password: String,
    val fullName: String,
    val phone: String,
    val role: String, // UserRole.name
    val status: String = AccountStatus.ACTIVE.name, // AccountStatus.name
    val linkedDriverId: Long? = null,
    val vehicleInfo: String = "",
    val createdAtFormatted: String = "2025/01/15"
) {
    val roleEnum: UserRole
        get() = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.CUSTOMER)

    val statusEnum: AccountStatus
        get() = runCatching { AccountStatus.valueOf(status) }.getOrDefault(AccountStatus.ACTIVE)
}

@Entity(tableName = "customer_addresses")
data class CustomerAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val label: String, // مثال: المنزل، العمل، الاستراحة
    val neighborhoodName: String, // حي النخيل - شمال الرياض
    val streetAndBuilding: String, // شارع التخصصي، فيلا 14
    val landmarkNotes: String = "",
    val distanceKm: Double = 4.2,
    val mapX: Float = 0.28f,
    val mapY: Float = 0.24f,
    val isDefault: Boolean = false
) {
    fun fullAddressDisplay(): String {
        val details = if (streetAndBuilding.isNotBlank()) " - $streetAndBuilding" else ""
        return "$label: $neighborhoodName$details"
    }
}

@Entity(tableName = "payment_gateways")
data class PaymentGatewayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String, // "MADA_CARD", "APPLE_PAY", "STC_PAY", "TABBY", "COD"
    val nameAr: String,
    val providerName: String, // Moyasar, HyperPay, PayTabs, Tap Payments, Cash
    val isElectronic: Boolean = true,
    val isEnabled: Boolean = true,
    val isLiveMode: Boolean = true, // true = Live Production, false = Sandbox Test
    val merchantId: String = "",
    val publishableKey: String = "",
    val secretKey: String = "",
    val webhookUrl: String = "",
    val feePercentage: Double = 0.0,
    val lastTestedStatus: String = "متصل وجاهز لاستقبال المدفوعات ✓"
)

enum class OrderStatus(
    val titleAr: String,
    val stepIndex: Int,
    val customerDescAr: String
) {
    NEW("طلب جديد", 0, "تم إرسال طلبك بانتظار تأكيد المطعم"),
    ACCEPTED("تم القبول", 1, "تم استلام طلبك وتأكيده من المطعم"),
    PREPARING("قيد التحضير", 2, "المطعم يحضر طلبك الآن بعناية"),
    READY("جاهز للتسليم", 3, "طلبك جاهز وبانتظار استلام المندوب"),
    ASSIGNED_TO_DRIVER("تم تعيين مندوب", 4, "تم توجيه المندوب للمطعم لاستلام طلبك"),
    PICKED_UP("استلم المندوب الطلب", 5, "المندوب استلم الطلب من المطعم"),
    ON_THE_WAY("المندوب في الطريق", 6, "المندوب في طريقه إليك الآن - يمكنك تتبعه مباشرة"),
    DELIVERED("تم التسليم", 7, "تم تسليم الطلب بنجاح، بالعافية!"),
    CANCELLED("ملغي", -1, "تم إلغاء هذا الطلب")
}

enum class CommissionType(val titleAr: String, val descriptionAr: String) {
    PERCENTAGE("نسبة مئوية (%)", "نسبة مئوية من قيمة الطلب الفرعية"),
    FIXED("مبلغ ثابت لكل طلب", "مبلغ مقطوع ثابت للمندوب عن كل عملية توصيل"),
    DISTANCE_TIERS("شرائح المسافة (كم)", "0-5 كم = شريحة 1 | 5-10 كم = شريحة 2 | +10 كم = شريحة 3"),
    HYBRID("نظام مختلط (أساسي + مسافة)", "عمولة أساسية ثابتة + مبلغ إضافي لكل 5 كم")
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String,
    val sortOrder: Int = 0
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val description: String,
    val basePrice: Double,
    val imageKey: String,
    val isAvailable: Boolean = true,
    val hasSizes: Boolean = true,
    val doubleSizeExtra: Double = 8.0,
    val availableAddons: String = "جبن شيدر:3.0,صوص خاص:2.0,بطاطس مقرمشة:5.0",
    val orderCount: Int = 0
)

@Entity(tableName = "drivers")
data class DriverEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val vehicleInfo: String,
    val isOnline: Boolean = true,
    val accountStatus: String = AccountStatus.ACTIVE.name,
    val totalOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val totalDeliveryValue: Double = 0.0,
    val totalCommissions: Double = 0.0,
    val paidCommissions: Double = 0.0,
    val rating: Double = 4.9,
    val joinedDate: String = "2025/01/15",
    val currentMapX: Float = 0.48f,
    val currentMapY: Float = 0.52f,
    val currentNeighborhood: String = "حي العليا - وسط الرياض"
) {
    val dueAmount: Double
        get() = (totalCommissions - paidCommissions).coerceAtLeast(0.0)

    val statusEnum: AccountStatus
        get() = runCatching { AccountStatus.valueOf(accountStatus) }.getOrDefault(AccountStatus.ACTIVE)
}

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val titleAr: String,
    val isPercentage: Boolean,
    val discountValue: Double,
    val minimumOrder: Double,
    val maxDiscount: Double,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: Int,
    val customerUserId: Long = 2L,
    val customerName: String,
    val customerPhone: String,
    val customerNeighborhood: String,
    val customerStreetDetails: String = "",
    val distanceKm: Double,
    val customerMapX: Float,
    val customerMapY: Float,
    val driverId: Long? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val isDriverAcceptedAutomatically: Boolean = false,
    val isMandatoryAdminAssignment: Boolean = false,
    val status: String = OrderStatus.NEW.name,
    val itemsSummary: String,
    val subtotal: Double,
    val discount: Double,
    val couponCode: String? = null,
    val deliveryFee: Double,
    val totalPaid: Double,
    val driverCommission: Double,
    val restaurantNet: Double,
    val paymentMethod: String,
    val paymentTransactionRef: String = "",
    val isElectronicPaid: Boolean = true,
    val customerNotes: String = "",
    val driverRouteProgress: Float = 0.0f,
    val estimatedMinutesRemaining: Int = 25,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val createdTimeFormatted: String = "12:30 م"
) {
    val statusEnum: OrderStatus
        get() = runCatching { OrderStatus.valueOf(status) }.getOrDefault(OrderStatus.NEW)
}

@Entity(tableName = "commission_config")
data class CommissionConfigEntity(
    @PrimaryKey val id: Int = 1,
    val activeType: String = CommissionType.HYBRID.name,
    val percentageRate: Double = 12.0,
    val fixedAmount: Double = 8.0,
    val tier1MaxKm: Double = 5.0,
    val tier1Amount: Double = 7.0,
    val tier2MaxKm: Double = 10.0,
    val tier2Amount: Double = 10.0,
    val tier3Amount: Double = 15.0,
    val hybridBaseAmount: Double = 5.0,
    val hybridExtraPer5Km: Double = 2.5,
    val activeAdminRole: String = UserRole.SUPER_ADMIN.name
) {
    val typeEnum: CommissionType
        get() = runCatching { CommissionType.valueOf(activeType) }.getOrDefault(CommissionType.HYBRID)

    val adminRoleEnum: UserRole
        get() = runCatching { UserRole.valueOf(activeAdminRole) }.getOrDefault(UserRole.SUPER_ADMIN)

    fun calculateCommission(orderSubtotal: Double, distanceKm: Double): Double {
        return when (typeEnum) {
            CommissionType.PERCENTAGE -> {
                ((orderSubtotal * (percentageRate / 100.0)) * 10.0).toInt() / 10.0
            }
            CommissionType.FIXED -> fixedAmount
            CommissionType.DISTANCE_TIERS -> {
                when {
                    distanceKm <= tier1MaxKm -> tier1Amount
                    distanceKm <= tier2MaxKm -> tier2Amount
                    else -> tier3Amount
                }
            }
            CommissionType.HYBRID -> {
                val extraBlocks = kotlin.math.ceil((distanceKm / 5.0)).coerceAtLeast(1.0)
                hybridBaseAmount + (extraBlocks * hybridExtraPer5Km)
            }
        }
    }

    fun calculateDeliveryFee(distanceKm: Double): Double {
        return when {
            distanceKm <= 5.0 -> 7.0
            distanceKm <= 10.0 -> 10.0
            else -> 14.0
        }
    }
}

@Entity(tableName = "driver_transactions")
data class DriverTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val driverId: Long,
    val driverName: String,
    val orderNumber: Int?,
    val amount: Double,
    val transactionType: String,
    val description: String,
    val timeFormatted: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetRole: String,
    val targetUserId: Long? = null,
    val targetDriverId: Long? = null,
    val title: String,
    val message: String,
    val orderNumber: Int? = null,
    val timeFormatted: String,
    val isRead: Boolean = false
)

@Entity(tableName = "server_config")
data class ServerConfigEntity(
    @PrimaryKey val id: Int = 1,
    val isOnlineModeEnabled: Boolean = true,
    val serverBaseUrl: String = "https://api.goldenember-restaurant.sa/v1",
    val websocketUrl: String = "wss://live.goldenember-restaurant.sa/ws/tracking",
    val apiKey: String = "GE-LIVE-CLOUD-994820-SA",
    val syncIntervalSeconds: Int = 4,
    val autoSyncOrders: Boolean = true,
    val autoSyncDriverLocations: Boolean = true,
    val autoSyncPayments: Boolean = true,
    val connectionStatus: String = "متصل أونلاين بالسيرفر السحابي 🟢",
    val lastSyncFormatted: String = "متزامن فورياً",
    val lastLatencyMs: Int = 24
)
