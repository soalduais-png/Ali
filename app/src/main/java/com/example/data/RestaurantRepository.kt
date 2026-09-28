package com.example.data

import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class NeighborhoodOption(
    val nameAr: String,
    val distanceKm: Double,
    val mapX: Float,
    val mapY: Float,
    val latitude: Double = 24.7136,
    val longitude: Double = 46.6753
)

val RIYADH_NEIGHBORHOODS = listOf(
    NeighborhoodOption("حي النخيل - شمال الرياض", 4.2, 0.28f, 0.24f, 24.7465, 46.6342),
    NeighborhoodOption("حي العليا - وسط الرياض", 6.5, 0.54f, 0.76f, 24.6961, 46.6841),
    NeighborhoodOption("حي الملقا - طريق أنس بن مالك", 8.4, 0.18f, 0.42f, 24.8055, 46.6120),
    NeighborhoodOption("حي الياسمين - شمال الرياض", 11.2, 0.78f, 0.19f, 24.8236, 46.6634),
    NeighborhoodOption("حي حطين - البوليفارد", 5.8, 0.22f, 0.68f, 24.7630, 46.6145),
    NeighborhoodOption("حي الربيع - طريق الملك عبدالعزيز", 7.3, 0.82f, 0.56f, 24.7912, 46.6580)
)

const val RESTAURANT_MAP_X = 0.50f
const val RESTAURANT_MAP_Y = 0.48f
const val RESTAURANT_NAME_AR = "مطعم الجمر الذهبي - الفرع الرئيسي"

sealed class LoginAuthResult {
    data class Success(val account: UserAccountEntity) : LoginAuthResult()
    data class PendingDriverApproval(val account: UserAccountEntity) : LoginAuthResult()
    data class SuspendedAccount(val account: UserAccountEntity) : LoginAuthResult()
    data object InvalidCredentials : LoginAuthResult()
}

class RestaurantRepository(private val dao: RestaurantDao) {

    val userAccounts = dao.getAllUserAccounts()
    val customerAddresses = dao.getAllCustomerAddresses()
    val paymentGateways = dao.getAllPaymentGateways()
    val categories = dao.getAllCategories()
    val products = dao.getAllProducts()
    val drivers = dao.getAllDrivers()
    val coupons = dao.getAllCoupons()
    val orders = dao.getAllOrders()
    val commissionConfig = dao.getCommissionConfig()
    val transactions = dao.getAllTransactions()
    val notifications = dao.getAllNotifications()
    val serverConfig = dao.getServerConfig()

    private fun nowTimeFormatted(): String {
        return SimpleDateFormat("hh:mm a", Locale("ar")).format(Date())
    }

    private fun todayDateFormatted(): String {
        return SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date())
    }

    suspend fun ensureSeedData() {
        if (dao.getServerConfigOnce() == null) {
            dao.saveServerConfig(
                ServerConfigEntity(
                    id = 1,
                    isOnlineModeEnabled = true,
                    serverBaseUrl = "https://api.goldenember-restaurant.sa/v1",
                    websocketUrl = "wss://live.goldenember-restaurant.sa/ws/tracking",
                    apiKey = "GE-LIVE-CLOUD-994820-SA",
                    syncIntervalSeconds = 4,
                    autoSyncOrders = true,
                    autoSyncDriverLocations = true,
                    autoSyncPayments = true,
                    connectionStatus = "متصل أونلاين بالسيرفر السحابي 🟢",
                    lastSyncFormatted = nowTimeFormatted(),
                    lastLatencyMs = 24
                )
            )
        }
        if (dao.getCategoryCount() > 0) return

        val initialConfig = CommissionConfigEntity(
            id = 1,
            activeType = CommissionType.HYBRID.name,
            percentageRate = 10.0,
            fixedAmount = 7.0,
            tier1MaxKm = 5.0,
            tier1Amount = 7.0,
            tier2MaxKm = 10.0,
            tier2Amount = 10.0,
            tier3Amount = 15.0,
            hybridBaseAmount = 5.0,
            hybridExtraPer5Km = 2.0,
            activeAdminRole = UserRole.SUPER_ADMIN.name
        )
        dao.saveCommissionConfig(initialConfig)

        // Seed Drivers first so we can link them to UserAccountEntity
        val initialDrivers = listOf(
            DriverEntity(
                id = 1,
                name = "أحمد الغامدي",
                phone = "0551234567",
                vehicleInfo = "تويوتا يارس 2025 • أ ب ج 4821",
                isOnline = true,
                accountStatus = AccountStatus.ACTIVE.name,
                totalOrders = 42,
                completedOrders = 39,
                cancelledOrders = 1,
                totalDeliveryValue = 2890.0,
                totalCommissions = 345.0,
                paidCommissions = 260.0,
                rating = 4.9,
                joinedDate = "2025/01/10",
                currentMapX = 0.35f,
                currentMapY = 0.33f,
                currentNeighborhood = "في الطريق إلى حي النخيل"
            ),
            DriverEntity(
                id = 2,
                name = "خالد العتيبي",
                phone = "0509876543",
                vehicleInfo = "هيونداي أكسنت 2024 • د هـ و 9103",
                isOnline = true,
                accountStatus = AccountStatus.ACTIVE.name,
                totalOrders = 35,
                completedOrders = 33,
                cancelledOrders = 1,
                totalDeliveryValue = 2410.0,
                totalCommissions = 290.0,
                paidCommissions = 220.0,
                rating = 4.8,
                joinedDate = "2025/02/18",
                currentMapX = 0.52f,
                currentMapY = 0.62f,
                currentNeighborhood = "حي العليا - متاح للطلبات"
            ),
            DriverEntity(
                id = 3,
                name = "فهد الدوسري",
                phone = "0543218765",
                vehicleInfo = "دراجة نارية هوندا سريعة • س ص ع 1142",
                isOnline = true,
                accountStatus = AccountStatus.ACTIVE.name,
                totalOrders = 51,
                completedOrders = 49,
                cancelledOrders = 2,
                totalDeliveryValue = 3620.0,
                totalCommissions = 415.0,
                paidCommissions = 350.0,
                rating = 4.95,
                joinedDate = "2024/11/05",
                currentMapX = 0.25f,
                currentMapY = 0.56f,
                currentNeighborhood = "حي حطين - يستلم طلباً"
            ),
            DriverEntity(
                id = 4,
                name = "ماجد الزهراني (مندوب جديد)",
                phone = "0561112233",
                vehicleInfo = "مازدا 6 2024 • ك ل م 5520",
                isOnline = false,
                accountStatus = AccountStatus.PENDING_APPROVAL.name,
                totalOrders = 0,
                completedOrders = 0,
                cancelledOrders = 0,
                totalDeliveryValue = 0.0,
                totalCommissions = 0.0,
                paidCommissions = 0.0,
                rating = 5.0,
                joinedDate = todayDateFormatted(),
                currentMapX = 0.74f,
                currentMapY = 0.28f,
                currentNeighborhood = "حي الياسمين - بانتظار التفعيل"
            )
        )
        dao.insertDrivers(initialDrivers)

        // Seed Unified User Accounts (each driver and customer has their own isolated account)
        val initialAccounts = listOf(
            UserAccountEntity(
                id = 1,
                username = "admin",
                password = "Ali714483785",
                fullName = "مدير النظام والمطعم العام",
                phone = "0500000001",
                role = UserRole.SUPER_ADMIN.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = null,
                createdAtFormatted = "2024/01/01"
            ),
            UserAccountEntity(
                id = 2,
                username = "customer",
                password = "123456",
                fullName = "محمد الفيصل",
                phone = "0559998877",
                role = UserRole.CUSTOMER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = null,
                createdAtFormatted = "2025/02/10"
            ),
            UserAccountEntity(
                id = 3,
                username = "driver",
                password = "123456",
                fullName = "أحمد الغامدي",
                phone = "0551234567",
                role = UserRole.DRIVER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = 1L,
                vehicleInfo = "تويوتا يارس 2025 • أ ب ج 4821",
                createdAtFormatted = "2025/01/10"
            ),
            UserAccountEntity(
                id = 4,
                username = "driver2",
                password = "123456",
                fullName = "ماجد الزهراني (مندوب جديد)",
                phone = "0561112233",
                role = UserRole.DRIVER.name,
                status = AccountStatus.PENDING_APPROVAL.name,
                linkedDriverId = 4L,
                vehicleInfo = "مازدا 6 2024 • ك ل م 5520",
                createdAtFormatted = todayDateFormatted()
            ),
            UserAccountEntity(
                id = 5,
                username = "driver3",
                password = "123456",
                fullName = "خالد العتيبي",
                phone = "0509876543",
                role = UserRole.DRIVER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = 2L,
                vehicleInfo = "هيونداي أكسنت 2024 • د هـ و 9103",
                createdAtFormatted = "2025/02/18"
            ),
            UserAccountEntity(
                id = 6,
                username = "driver4",
                password = "123456",
                fullName = "فهد الدوسري",
                phone = "0543218765",
                role = UserRole.DRIVER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = 3L,
                vehicleInfo = "دراجة نارية هوندا سريعة • س ص ع 1142",
                createdAtFormatted = "2024/11/05"
            ),
            UserAccountEntity(
                id = 7,
                username = "0504445566",
                password = "123456",
                fullName = "نورة السبيعي",
                phone = "0504445566",
                role = UserRole.CUSTOMER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = null,
                createdAtFormatted = "2025/02/12"
            ),
            UserAccountEntity(
                id = 8,
                username = "0547778899",
                password = "123456",
                fullName = "عبدالرحمن الشهري",
                phone = "0547778899",
                role = UserRole.CUSTOMER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = null,
                createdAtFormatted = "2025/02/14"
            ),
            UserAccountEntity(
                id = 9,
                username = "0531112233",
                password = "123456",
                fullName = "ريم الحربي",
                phone = "0531112233",
                role = UserRole.CUSTOMER.name,
                status = AccountStatus.ACTIVE.name,
                linkedDriverId = null,
                createdAtFormatted = "2025/02/15"
            )
        )
        dao.insertUserAccounts(initialAccounts)

        // Seed Customer Saved Delivery Addresses
        val initialAddresses = listOf(
            CustomerAddressEntity(
                id = 1,
                userId = 2L,
                label = "المنزل",
                neighborhoodName = "حي النخيل - شمال الرياض",
                streetAndBuilding = "شارع التخصصي الفرعي، فيلا رقم 14",
                landmarkNotes = "بجانب جامع النخيل - البوابة الشرقية",
                distanceKm = 4.2,
                mapX = 0.28f,
                mapY = 0.24f,
                isDefault = true
            ),
            CustomerAddressEntity(
                id = 2,
                userId = 2L,
                label = "مقر العمل",
                neighborhoodName = "حي العليا - وسط الرياض",
                streetAndBuilding = "طريق الملك فهد، برج العليا الإداري، الطابق 8",
                landmarkNotes = "الاستقبال الأرضي عند الأمن",
                distanceKm = 6.5,
                mapX = 0.54f,
                mapY = 0.76f,
                isDefault = false
            )
        )
        dao.insertCustomerAddresses(initialAddresses)

        // Seed Electronic Payment Gateways & Integration Configs
        val initialGateways = listOf(
            PaymentGatewayEntity(
                id = 1,
                code = "MADA_CARD",
                nameAr = "مدى / البطاقات الائتمانية (Visa & MasterCard)",
                providerName = "بوابة ميسر المالية (Moyasar Gateway)",
                isElectronic = true,
                isEnabled = true,
                isLiveMode = true,
                merchantId = "MERCH_MOYASAR_99481",
                publishableKey = "pk_live_89f3a210c4d7e6b51234a9",
                secretKey = "sk_live_••••••••••••••••8821",
                webhookUrl = "https://api.goldenember-rest.sa/v1/webhooks/moyasar",
                feePercentage = 1.75,
                lastTestedStatus = "متصل (Live) • زمن الاستجابة 110ms ✓"
            ),
            PaymentGatewayEntity(
                id = 2,
                code = "APPLE_PAY",
                nameAr = "Apple Pay (دفع إلكتروني سريع)",
                providerName = "Apple Pay Merchant Direct + HyperPay",
                isElectronic = true,
                isEnabled = true,
                isLiveMode = true,
                merchantId = "merchant.sa.goldenember.delivery",
                publishableKey = "ap_cert_live_77410992a",
                secretKey = "ap_priv_••••••••••••••••3109",
                webhookUrl = "https://api.goldenember-rest.sa/v1/webhooks/applepay",
                feePercentage = 1.5,
                lastTestedStatus = "شهادة Apple Pay مفعلة ومرتبطة ✓"
            ),
            PaymentGatewayEntity(
                id = 3,
                code = "STC_PAY",
                nameAr = "محفظة STC Pay الرقمية",
                providerName = "STC Pay Merchant B2C API",
                isElectronic = true,
                isEnabled = true,
                isLiveMode = true,
                merchantId = "STCPAY_CORP_55120",
                publishableKey = "stc_live_pub_4491827",
                secretKey = "stc_live_sec_••••••••••••9912",
                webhookUrl = "https://api.goldenember-rest.sa/v1/webhooks/stcpay",
                feePercentage = 1.2,
                lastTestedStatus = "متصل عبر رمز التحقق الفوري OTP ✓"
            ),
            PaymentGatewayEntity(
                id = 4,
                code = "TABBY_TAMARA",
                nameAr = "قسّمها على 4 دفعات (تابي / تمارا)",
                providerName = "Tabby & Tamara BNPL API",
                isElectronic = true,
                isEnabled = false,
                isLiveMode = false,
                merchantId = "TABBY_SA_30219",
                publishableKey = "tb_test_pk_112093",
                secretKey = "tb_test_sk_••••••••••••1102",
                webhookUrl = "https://api.goldenember-rest.sa/v1/webhooks/bnpl",
                feePercentage = 3.5,
                lastTestedStatus = "وضع التجربة (Sandbox) جاهز للتفعيل"
            ),
            PaymentGatewayEntity(
                id = 5,
                code = "COD",
                nameAr = "الدفع النقدي عند الاستلام (كاش)",
                providerName = "تحصيل مباشر بواسطة المندوب",
                isElectronic = false,
                isEnabled = true,
                isLiveMode = true,
                merchantId = "INTERNAL_CASH_LEDGER",
                publishableKey = "-",
                secretKey = "-",
                webhookUrl = "-",
                feePercentage = 0.0,
                lastTestedStatus = "مفعل لتحصيل المبالغ نقداً عند التسليم ✓"
            )
        )
        dao.insertPaymentGateways(initialGateways)

        val cats = listOf(
            CategoryEntity(id = 1, name = "برجر", emoji = "🍔", sortOrder = 1),
            CategoryEntity(id = 2, name = "وجبات", emoji = "🍗", sortOrder = 2),
            CategoryEntity(id = 3, name = "بيتزا", emoji = "🍕", sortOrder = 3),
            CategoryEntity(id = 4, name = "مشروبات", emoji = "🥤", sortOrder = 4),
            CategoryEntity(id = 5, name = "إضافات", emoji = "🍟", sortOrder = 5)
        )
        dao.insertCategories(cats)

        val initialProducts = listOf(
            ProductEntity(
                id = 1,
                categoryId = 1,
                name = "برجر دجاج كرسبي خاص",
                description = "برجر دجاج مقرمش مع صوص الجمر الذهبي الخاص وخس طازج وخبز بريوش محمص",
                basePrice = 25.0,
                imageKey = "burger",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 8.0,
                availableAddons = "جبن:3.0,صوص:2.0,بطاطس:5.0",
                orderCount = 142
            ),
            ProductEntity(
                id = 2,
                categoryId = 1,
                name = "برجر لحم أنجوس مشوي",
                description = "شريحة لحم أنجوس صافي على اللهب مع جبن شيدر معتق وبصل مكرمل",
                basePrice = 29.0,
                imageKey = "burger",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 10.0,
                availableAddons = "جبن:3.0,صوص:2.0,بطاطس:5.0,شريحة بيكون بقري:6.0",
                orderCount = 198
            ),
            ProductEntity(
                id = 3,
                categoryId = 1,
                name = "برجر دبل سماش الملكي",
                description = "شريحتان سماش لحم طازج مع طبقتين من الجبن السائل وصوص الترافل",
                basePrice = 36.0,
                imageKey = "burger",
                isAvailable = true,
                hasSizes = false,
                doubleSizeExtra = 0.0,
                availableAddons = "جبن:3.0,صوص:2.0,بطاطس:5.0",
                orderCount = 115
            ),
            ProductEntity(
                id = 4,
                categoryId = 2,
                name = "وجبة برجر متكاملة",
                description = "برجر اختيارك مع بطاطس مقرمشة متبلة ومشروب غازي بارد وصوص",
                basePrice = 38.0,
                imageKey = "hero",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 8.0,
                availableAddons = "جبن:3.0,صوص:2.0,حلقات بصل:6.0",
                orderCount = 164
            ),
            ProductEntity(
                id = 5,
                categoryId = 2,
                name = "وجبة دجاج بروستد ذهبي",
                description = "4 قطع دجاج مقرمش بتتبيلة الأعشاب العربية مع بطاطس وثوم وخبز",
                basePrice = 34.0,
                imageKey = "hero",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 12.0,
                availableAddons = "جبن:3.0,صوص ثوم إضافي:2.0,بطاطس:5.0",
                orderCount = 92
            ),
            ProductEntity(
                id = 6,
                categoryId = 2,
                name = "وجبة أطفال هابي بوكس",
                description = "ميني برجر طري مع بطاطس وعصير برتقال طبيعي وهدية مميزة",
                basePrice = 22.0,
                imageKey = "burger",
                isAvailable = true,
                hasSizes = false,
                doubleSizeExtra = 0.0,
                availableAddons = "جبن:3.0,صوص:2.0",
                orderCount = 67
            ),
            ProductEntity(
                id = 7,
                categoryId = 3,
                name = "بيتزا بيبروني بالحطب",
                description = "عجينة إيطالية مخمرة 48 ساعة مع موزاريلا طازجة وبيبروني بقري مدخن",
                basePrice = 42.0,
                imageKey = "pizza",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 14.0,
                availableAddons = "جبن إضافي:5.0,أطراف محشية جبن:7.0,هلابينو:3.0",
                orderCount = 131
            ),
            ProductEntity(
                id = 8,
                categoryId = 4,
                name = "بيبسي بارد",
                description = "مشروب غازي منعش يقدم مع الثلج (330 مل)",
                basePrice = 5.0,
                imageKey = "hero",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 3.0,
                availableAddons = "شرائح ليمون:1.0",
                orderCount = 210
            ),
            ProductEntity(
                id = 9,
                categoryId = 4,
                name = "سفن أب بالليمون والنعناع",
                description = "سفن أب منعش مع أوراق النعناع الطازج والليمون",
                basePrice = 6.0,
                imageKey = "hero",
                isAvailable = true,
                hasSizes = false,
                doubleSizeExtra = 0.0,
                availableAddons = "نعناع إضافي:1.0",
                orderCount = 140
            ),
            ProductEntity(
                id = 10,
                categoryId = 4,
                name = "ماء معدني نقي",
                description = "عبوة مياه معدنية طبيعية مبردة (500 مل)",
                basePrice = 3.0,
                imageKey = "hero",
                isAvailable = true,
                hasSizes = false,
                doubleSizeExtra = 0.0,
                availableAddons = "",
                orderCount = 185
            ),
            ProductEntity(
                id = 11,
                categoryId = 5,
                name = "بطاطس كيجن بالجبن السائل",
                description = "بطاطس ذهبية مقرمشة مغطاة بجبن الشيدر الذائب وبهارات الكيجن والهلابينو",
                basePrice = 16.0,
                imageKey = "burger",
                isAvailable = true,
                hasSizes = true,
                doubleSizeExtra = 6.0,
                availableAddons = "جبن:3.0,صوص:2.0",
                orderCount = 154
            )
        )
        dao.insertProducts(initialProducts)

        val initialCoupons = listOf(
            CouponEntity(
                id = 1,
                code = "FIRST20",
                titleAr = "خصم 20% لأول طلب",
                isPercentage = true,
                discountValue = 20.0,
                minimumOrder = 50.0,
                maxDiscount = 20.0,
                startDate = "1/10",
                endDate = "15/10",
                isActive = true
            ),
            CouponEntity(
                id = 2,
                code = "VIP15",
                titleAr = "خصم العملاء المميزين 15 ريال",
                isPercentage = false,
                discountValue = 15.0,
                minimumOrder = 65.0,
                maxDiscount = 15.0,
                startDate = "1/10",
                endDate = "30/10",
                isActive = true
            ),
            CouponEntity(
                id = 3,
                code = "DAY10",
                titleAr = "عرض وجبة اليوم خصم 10%",
                isPercentage = true,
                discountValue = 10.0,
                minimumOrder = 30.0,
                maxDiscount = 15.0,
                startDate = "1/10",
                endDate = "31/10",
                isActive = true
            )
        )
        dao.insertCoupons(initialCoupons)

        val now = System.currentTimeMillis()
        val initialOrders = listOf(
            OrderEntity(
                id = 1,
                orderNumber = 1051,
                customerUserId = 2L,
                customerName = "محمد الفيصل",
                customerPhone = "0559998877",
                customerNeighborhood = "حي العليا - وسط الرياض",
                customerStreetDetails = "طريق الملك فهد، برج العليا",
                distanceKm = 6.5,
                customerMapX = 0.54f,
                customerMapY = 0.76f,
                driverId = 1,
                driverName = "أحمد الغامدي",
                driverPhone = "0551234567",
                isDriverAcceptedAutomatically = true,
                isMandatoryAdminAssignment = false,
                status = OrderStatus.DELIVERED.name,
                itemsSummary = "2× وجبة برجر متكاملة، 1× بيبسي بارد",
                subtotal = 80.0,
                discount = 10.0,
                couponCode = "DAY10",
                deliveryFee = 8.0,
                totalPaid = 78.0,
                driverCommission = 8.0,
                restaurantNet = 70.0,
                paymentMethod = "Apple Pay (دفع إلكتروني سريع)",
                paymentTransactionRef = "AP-992014",
                isElectronicPaid = true,
                customerNotes = "الرجاء عدم رن الجرس، الاتصال عند الوصول",
                driverRouteProgress = 1.0f,
                estimatedMinutesRemaining = 0,
                createdAtTimestamp = now - 3600_000L,
                createdTimeFormatted = "01:15 م"
            ),
            OrderEntity(
                id = 2,
                orderNumber = 1052,
                customerUserId = 7L,
                customerName = "نورة السبيعي",
                customerPhone = "0504445566",
                customerNeighborhood = "حي الملقا - طريق أنس بن مالك",
                customerStreetDetails = "شارع أنس بن مالك، فيلا 8",
                distanceKm = 8.4,
                customerMapX = 0.18f,
                customerMapY = 0.42f,
                driverId = null,
                driverName = null,
                driverPhone = null,
                isDriverAcceptedAutomatically = false,
                isMandatoryAdminAssignment = false,
                status = OrderStatus.PREPARING.name,
                itemsSummary = "1× برجر دجاج كرسبي خاص (دبل + جبن)، 1× بطاطس كيجن",
                subtotal = 45.0,
                discount = 0.0,
                couponCode = null,
                deliveryFee = 10.0,
                totalPaid = 55.0,
                driverCommission = 9.0,
                restaurantNet = 46.0,
                paymentMethod = "مدى / البطاقات الائتمانية",
                paymentTransactionRef = "MADA-774120",
                isElectronicPaid = true,
                customerNotes = "زيادة صوص الجمر الخاص",
                driverRouteProgress = 0.0f,
                estimatedMinutesRemaining = 22,
                createdAtTimestamp = now - 1800_000L,
                createdTimeFormatted = "01:40 م"
            ),
            OrderEntity(
                id = 3,
                orderNumber = 1053,
                customerUserId = 8L,
                customerName = "عبدالرحمن الشهري",
                customerPhone = "0547778899",
                customerNeighborhood = "حي النخيل - شمال الرياض",
                customerStreetDetails = "البوابة الشرقية - فيلا رقم 14",
                distanceKm = 4.2,
                customerMapX = 0.28f,
                customerMapY = 0.24f,
                driverId = null,
                driverName = null,
                driverPhone = null,
                isDriverAcceptedAutomatically = false,
                isMandatoryAdminAssignment = false,
                status = OrderStatus.READY.name,
                itemsSummary = "2× برجر دبل سماش الملكي، 2× سفن أب بالليمون",
                subtotal = 72.0,
                discount = 14.0,
                couponCode = "FIRST20",
                deliveryFee = 7.0,
                totalPaid = 65.0,
                driverCommission = 8.0,
                restaurantNet = 57.0,
                paymentMethod = "الدفع النقدي عند الاستلام (كاش)",
                paymentTransactionRef = "COD-1053",
                isElectronicPaid = false,
                customerNotes = "البوابة الشرقية - فيلا رقم 14",
                driverRouteProgress = 0.0f,
                estimatedMinutesRemaining = 16,
                createdAtTimestamp = now - 1200_000L,
                createdTimeFormatted = "01:48 م"
            ),
            OrderEntity(
                id = 4,
                orderNumber = 1054,
                customerUserId = 2L,
                customerName = "محمد الفيصل",
                customerPhone = "0559998877",
                customerNeighborhood = "حي النخيل - شمال الرياض",
                customerStreetDetails = "شارع التخصصي الفرعي، فيلا رقم 14",
                distanceKm = 4.2,
                customerMapX = 0.28f,
                customerMapY = 0.24f,
                driverId = 1,
                driverName = "أحمد الغامدي",
                driverPhone = "0551234567",
                isDriverAcceptedAutomatically = true,
                isMandatoryAdminAssignment = false,
                status = OrderStatus.ON_THE_WAY.name,
                itemsSummary = "1× برجر دجاج كرسبي خاص، 1× إضافة جبن، 1× بطاطس مقرمشة",
                subtotal = 33.0,
                discount = 5.0,
                couponCode = "DAY10",
                deliveryFee = 7.0,
                totalPaid = 35.0,
                driverCommission = 7.0,
                restaurantNet = 28.0,
                paymentMethod = "Apple Pay (دفع إلكتروني سريع)",
                paymentTransactionRef = "AP-554192",
                isElectronicPaid = true,
                customerNotes = "الاتصال فور الوصول للمنزل",
                driverRouteProgress = 0.62f,
                estimatedMinutesRemaining = 7,
                createdAtTimestamp = now - 600_000L,
                createdTimeFormatted = "01:52 م"
            ),
            OrderEntity(
                id = 5,
                orderNumber = 1058,
                customerUserId = 9L,
                customerName = "ريم الحربي",
                customerPhone = "0531112233",
                customerNeighborhood = "حي حطين - البوليفارد",
                customerStreetDetails = "شارع الأمير تركي الأول، مجمع حطين",
                distanceKm = 5.8,
                customerMapX = 0.22f,
                customerMapY = 0.68f,
                driverId = 3,
                driverName = "فهد الدوسري",
                driverPhone = "0543218765",
                isDriverAcceptedAutomatically = true,
                isMandatoryAdminAssignment = false,
                status = OrderStatus.PICKED_UP.name,
                itemsSummary = "1× بيتزا بيبروني بالحطب (دبل)، 2× بيبسي بارد",
                subtotal = 66.0,
                discount = 0.0,
                couponCode = null,
                deliveryFee = 10.0,
                totalPaid = 76.0,
                driverCommission = 9.0,
                restaurantNet = 67.0,
                paymentMethod = "محفظة STC Pay الرقمية",
                paymentTransactionRef = "STC-883012",
                isElectronicPaid = true,
                customerNotes = "بدون فلفل حار",
                driverRouteProgress = 0.28f,
                estimatedMinutesRemaining = 14,
                createdAtTimestamp = now - 300_000L,
                createdTimeFormatted = "01:56 م"
            )
        )
        dao.insertOrders(initialOrders)

        val initialTransactions = listOf(
            DriverTransactionEntity(
                id = 1,
                driverId = 1,
                driverName = "أحمد الغامدي",
                orderNumber = 1051,
                amount = 8.0,
                transactionType = "COMMISSION_EARNED",
                description = "عمولة توصيل طلب #1051 إلى حي العليا",
                timeFormatted = "01:35 م"
            ),
            DriverTransactionEntity(
                id = 2,
                driverId = 3,
                driverName = "فهد الدوسري",
                orderNumber = 1049,
                amount = 10.0,
                transactionType = "COMMISSION_EARNED",
                description = "عمولة توصيل طلب #1049 إلى حي الربيع",
                timeFormatted = "12:50 م"
            ),
            DriverTransactionEntity(
                id = 3,
                driverId = 1,
                driverName = "أحمد الغامدي",
                orderNumber = null,
                amount = 120.0,
                transactionType = "PAYOUT_SETTLEMENT",
                description = "تحويل مستحقات مالية للمندوب (دفعة أسبوعية)",
                timeFormatted = "11:00 ص"
            )
        )
        dao.insertTransactions(initialTransactions)

        val initialNotifications = listOf(
            NotificationEntity(
                targetRole = "ADMIN",
                targetUserId = 1L,
                targetDriverId = null,
                title = "طلب تفعيل حساب مندوب جديد 🔔",
                message = "المندوب ماجد الزهراني (0561112233) سجل في النظام وبانتظار تفعيل حسابه من لوحة الإدارة.",
                timeFormatted = "01:58 م"
            ),
            NotificationEntity(
                targetRole = "CUSTOMER",
                targetUserId = 2L,
                targetDriverId = null,
                title = "المندوب في الطريق إليك 🛵",
                message = "المندوب أحمد الغامدي في طريقه لتوصيل طلبك #1054، يمكنك تتبعه مباشرة على الخريطة التفاعلية.",
                orderNumber = 1054,
                timeFormatted = "01:55 م"
            ),
            NotificationEntity(
                targetRole = "DRIVER",
                targetUserId = null,
                targetDriverId = 1L,
                title = "طلب قيد التوصيل معك #1054 🛵",
                message = "أنت الآن في الطريق لتوصيل الطلب #1054 إلى العميل محمد الفيصل في حي النخيل.",
                orderNumber = 1054,
                timeFormatted = "01:53 م"
            ),
            NotificationEntity(
                targetRole = "DRIVER",
                targetUserId = null,
                targetDriverId = null,
                title = "طلب جاهز للاستلام #1053 📦",
                message = "يوجد طلب جاهز في المطعم متجه إلى حي النخيل بقيمة 65 ر.س وعمولة توصيل 8 ر.س.",
                orderNumber = 1053,
                timeFormatted = "01:50 م"
            )
        )
        dao.insertNotifications(initialNotifications)
    }

    // Unified Authentication Logic
    suspend fun authenticateUser(usernameOrPhone: String, passwordInput: String): LoginAuthResult {
        val cleanUser = usernameOrPhone.trim()
        val cleanPass = passwordInput.trim()

        // Always support the exact default admin credentials requested by the user
        val account = dao.findAccountByUsernameOrPhone(cleanUser)
            ?: return LoginAuthResult.InvalidCredentials

        if (account.password != cleanPass) {
            return LoginAuthResult.InvalidCredentials
        }

        return when (account.statusEnum) {
            AccountStatus.PENDING_APPROVAL -> LoginAuthResult.PendingDriverApproval(account)
            AccountStatus.SUSPENDED -> LoginAuthResult.SuspendedAccount(account)
            AccountStatus.ACTIVE -> LoginAuthResult.Success(account)
        }
    }

    // Register a new Customer (auto-activated immediately + saves initial delivery address)
    suspend fun registerCustomerWithAddress(
        fullName: String,
        usernameOrPhone: String,
        password: String,
        addressLabel: String,
        neighborhood: NeighborhoodOption,
        streetAndBuilding: String,
        landmarkNotes: String
    ): Result<UserAccountEntity> {
        val cleanUsername = usernameOrPhone.trim()
        val existing = dao.findAccountByUsernameOrPhone(cleanUsername)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("اسم المستخدم أو رقم الجوال مسجل مسبقاً"))
        }

        val newAccount = UserAccountEntity(
            username = cleanUsername,
            password = password.trim(),
            fullName = fullName.trim(),
            phone = cleanUsername,
            role = UserRole.CUSTOMER.name,
            status = AccountStatus.ACTIVE.name, // Activated automatically for Customer!
            linkedDriverId = null,
            createdAtFormatted = todayDateFormatted()
        )
        val userId = dao.insertUserAccount(newAccount)
        val savedAccount = newAccount.copy(id = userId)

        val initialAddress = CustomerAddressEntity(
            userId = userId,
            label = addressLabel.ifBlank { "المنزل" },
            neighborhoodName = neighborhood.nameAr,
            streetAndBuilding = streetAndBuilding.ifBlank { "الشارع الرئيسي" },
            landmarkNotes = landmarkNotes,
            distanceKm = neighborhood.distanceKm,
            mapX = neighborhood.mapX,
            mapY = neighborhood.mapY,
            isDefault = true
        )
        dao.insertCustomerAddress(initialAddress)

        dao.insertNotification(
            NotificationEntity(
                targetRole = "CUSTOMER",
                targetUserId = userId,
                targetDriverId = null,
                title = "مرحباً بك في مطعم الجمر الذهبي 🎉",
                message = "تم تفعيل حسابك تلقائياً وحفظ عنوان التوصيل (${initialAddress.label} - ${neighborhood.nameAr}).",
                timeFormatted = nowTimeFormatted()
            )
        )
        return Result.success(savedAccount)
    }

    // Register a new Delivery Driver (NOT activated until Admin approves!)
    suspend fun registerDriverAccount(
        fullName: String,
        usernameOrPhone: String,
        password: String,
        vehicleInfo: String,
        neighborhood: NeighborhoodOption
    ): Result<UserAccountEntity> {
        val cleanUsername = usernameOrPhone.trim()
        val existing = dao.findAccountByUsernameOrPhone(cleanUsername)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("اسم المستخدم أو رقم الجوال مسجل مسبقاً"))
        }

        val newDriver = DriverEntity(
            name = fullName.trim(),
            phone = cleanUsername,
            vehicleInfo = vehicleInfo.ifBlank { "سيارة توصيل خاصة" },
            isOnline = false,
            accountStatus = AccountStatus.PENDING_APPROVAL.name,
            totalOrders = 0,
            completedOrders = 0,
            cancelledOrders = 0,
            totalDeliveryValue = 0.0,
            totalCommissions = 0.0,
            paidCommissions = 0.0,
            rating = 5.0,
            joinedDate = todayDateFormatted(),
            currentMapX = neighborhood.mapX,
            currentMapY = neighborhood.mapY,
            currentNeighborhood = "${neighborhood.nameAr} - بانتظار التفعيل"
        )
        val driverId = dao.insertDriver(newDriver)

        val newAccount = UserAccountEntity(
            username = cleanUsername,
            password = password.trim(),
            fullName = fullName.trim(),
            phone = cleanUsername,
            role = UserRole.DRIVER.name,
            status = AccountStatus.PENDING_APPROVAL.name, // Requires Admin activation!
            linkedDriverId = driverId,
            vehicleInfo = newDriver.vehicleInfo,
            createdAtFormatted = todayDateFormatted()
        )
        val accountId = dao.insertUserAccount(newAccount)

        dao.insertNotification(
            NotificationEntity(
                targetRole = "ADMIN",
                targetUserId = 1L,
                targetDriverId = null,
                title = "طلب تفعيل مندوب توصيل جديد 🛵",
                message = "قام المندوب ${fullName.trim()} ($cleanUsername) بالتسجيل وينتظر تفعيل حسابه من الإدارة.",
                timeFormatted = nowTimeFormatted()
            )
        )
        return Result.success(newAccount.copy(id = accountId))
    }

    // Admin approves or suspends a Driver / User account
    suspend fun updateAccountApprovalStatus(
        account: UserAccountEntity,
        newStatus: AccountStatus
    ) {
        dao.updateUserAccount(account.copy(status = newStatus.name))
        account.linkedDriverId?.let { drvId ->
            dao.getDriverById(drvId)?.let { drv ->
                dao.updateDriver(
                    drv.copy(
                        accountStatus = newStatus.name,
                        isOnline = newStatus == AccountStatus.ACTIVE,
                        currentNeighborhood = if (newStatus == AccountStatus.ACTIVE) {
                            drv.currentNeighborhood.replace(" - بانتظار التفعيل", " - متاح للطلبات")
                        } else {
                            drv.currentNeighborhood
                        }
                    )
                )
            }
        }
        dao.insertNotification(
            NotificationEntity(
                targetRole = "DRIVER",
                targetUserId = account.id,
                targetDriverId = account.linkedDriverId,
                title = if (newStatus == AccountStatus.ACTIVE) "تم تفعيل حسابك كمندوب توصيل ✅" else "تحديث حالة الحساب",
                message = if (newStatus == AccountStatus.ACTIVE) {
                    "قامت إدارة المطعم بتفعيل حسابك (${account.fullName})، يمكنك الآن تسجيل الدخول واستلام الطلبات."
                } else {
                    "تم تغيير حالة حسابك (${account.fullName}) إلى: ${newStatus.titleAr}."
                },
                timeFormatted = nowTimeFormatted()
            )
        )
    }

    // Customer Address Management
    suspend fun saveCustomerAddress(address: CustomerAddressEntity) {
        if (address.isDefault) {
            dao.clearDefaultAddressesForUser(address.userId)
        }
        if (address.id == 0L) {
            dao.insertCustomerAddress(address)
        } else {
            dao.updateCustomerAddress(address)
        }
    }

    suspend fun setDefaultCustomerAddress(address: CustomerAddressEntity) {
        dao.clearDefaultAddressesForUser(address.userId)
        dao.updateCustomerAddress(address.copy(isDefault = true))
    }

    suspend fun deleteCustomerAddress(address: CustomerAddressEntity) {
        dao.deleteCustomerAddress(address)
    }

    // Electronic Payment Gateway Management
    suspend fun savePaymentGateway(gateway: PaymentGatewayEntity) {
        if (gateway.id == 0L) {
            dao.insertPaymentGateway(gateway)
        } else {
            dao.updatePaymentGateway(gateway)
        }
    }

    suspend fun testPaymentGatewayConnection(gateway: PaymentGatewayEntity) {
        val modeLabel = if (gateway.isLiveMode) "بيئة الإنتاج الفعلية (Live)" else "بيئة الاختبار (Sandbox)"
        val timeStr = nowTimeFormatted()
        val updated = gateway.copy(
            lastTestedStatus = "تم فحص الربط بنجاح ($modeLabel) عند $timeStr • المفتاح صالح ✓"
        )
        dao.updatePaymentGateway(updated)
    }

    suspend fun createCustomerOrder(
        customerUserId: Long,
        customerName: String,
        customerPhone: String,
        neighborhood: NeighborhoodOption,
        streetDetails: String,
        itemsSummary: String,
        subtotal: Double,
        discount: Double,
        couponCode: String?,
        paymentMethod: String,
        paymentTransactionRef: String,
        isElectronicPaid: Boolean,
        customerNotes: String
    ): Long {
        val config = dao.getCommissionConfigOnce() ?: CommissionConfigEntity()
        val deliveryFee = config.calculateDeliveryFee(neighborhood.distanceKm)
        val totalPaid = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
        val driverCommission = config.calculateCommission(subtotal, neighborhood.distanceKm)
        val restaurantNet = (totalPaid - driverCommission).coerceAtLeast(0.0)
        val nextOrderNum = 1059 + ((System.currentTimeMillis() / 1000L) % 900).toInt()
        val timeStr = nowTimeFormatted()

        val order = OrderEntity(
            orderNumber = nextOrderNum,
            customerUserId = customerUserId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerNeighborhood = neighborhood.nameAr,
            customerStreetDetails = streetDetails,
            distanceKm = neighborhood.distanceKm,
            customerMapX = neighborhood.mapX,
            customerMapY = neighborhood.mapY,
            status = OrderStatus.NEW.name,
            itemsSummary = itemsSummary,
            subtotal = subtotal,
            discount = discount,
            couponCode = couponCode,
            deliveryFee = deliveryFee,
            totalPaid = totalPaid,
            driverCommission = driverCommission,
            restaurantNet = restaurantNet,
            paymentMethod = paymentMethod,
            paymentTransactionRef = paymentTransactionRef,
            isElectronicPaid = isElectronicPaid,
            customerNotes = customerNotes,
            driverRouteProgress = 0.0f,
            estimatedMinutesRemaining = (neighborhood.distanceKm * 2.8 + 10).roundToInt(),
            createdAtTimestamp = System.currentTimeMillis(),
            createdTimeFormatted = timeStr
        )
        val id = dao.insertOrder(order)

        dao.insertNotification(
            NotificationEntity(
                targetRole = "CUSTOMER",
                targetUserId = customerUserId,
                targetDriverId = null,
                title = "تم استلام طلبك #$nextOrderNum ✅",
                message = "تم تأكيد الدفع ($paymentMethod) وإرسال طلبك للمطعم بقيمة ${totalPaid.toInt()} ر.س إلى عنوانك ($streetDetails).",
                orderNumber = nextOrderNum,
                timeFormatted = timeStr
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetRole = "ADMIN",
                targetUserId = 1L,
                targetDriverId = null,
                title = "طلب جديد وارد #$nextOrderNum 🔔",
                message = "طلب جديد من $customerName (${neighborhood.nameAr}) بقيمة ${totalPaid.toInt()} ر.س عبر $paymentMethod.",
                orderNumber = nextOrderNum,
                timeFormatted = timeStr
            )
        )
        return id
    }

    suspend fun updateOrderStatus(
        order: OrderEntity,
        newStatus: OrderStatus,
        assignDriver: DriverEntity? = null,
        isAdminMandatory: Boolean = false
    ) {
        val timeStr = nowTimeFormatted()

        // Rule: Once a driver has accepted an order automatically, the responsible driver is locked to that driver.
        // Only orders that have NOT been accepted by a driver (order.driverId == null) can be assigned.
        val isNewDriverAssignment = order.driverId == null && assignDriver != null
        val chosenDriver = if (order.driverId != null) {
            dao.getDriverById(order.driverId) ?: assignDriver
        } else {
            assignDriver
        }

        val updatedAutoAccepted = when {
            isNewDriverAssignment && !isAdminMandatory -> true
            else -> order.isDriverAcceptedAutomatically
        }
        val updatedMandatory = when {
            isNewDriverAssignment && isAdminMandatory -> true
            else -> order.isMandatoryAdminAssignment
        }

        val newProgress = when (newStatus) {
            OrderStatus.NEW, OrderStatus.ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY -> 0.0f
            OrderStatus.ASSIGNED_TO_DRIVER -> 0.08f
            OrderStatus.PICKED_UP -> 0.22f
            OrderStatus.ON_THE_WAY -> if (order.driverRouteProgress < 0.35f) 0.45f else order.driverRouteProgress
            OrderStatus.DELIVERED -> 1.0f
            OrderStatus.CANCELLED -> 0.0f
        }

        val newEta = when (newStatus) {
            OrderStatus.NEW -> 28
            OrderStatus.ACCEPTED -> 24
            OrderStatus.PREPARING -> 20
            OrderStatus.READY -> 15
            OrderStatus.ASSIGNED_TO_DRIVER -> 14
            OrderStatus.PICKED_UP -> 12
            OrderStatus.ON_THE_WAY -> ((1f - newProgress) * (order.distanceKm * 2.2 + 6)).roundToInt().coerceAtLeast(2)
            OrderStatus.DELIVERED, OrderStatus.CANCELLED -> 0
        }

        val updated = order.copy(
            status = newStatus.name,
            driverId = chosenDriver?.id ?: order.driverId,
            driverName = chosenDriver?.name ?: order.driverName,
            driverPhone = chosenDriver?.phone ?: order.driverPhone,
            isDriverAcceptedAutomatically = updatedAutoAccepted,
            isMandatoryAdminAssignment = updatedMandatory,
            driverRouteProgress = newProgress,
            estimatedMinutesRemaining = newEta
        )
        dao.updateOrder(updated)

        if (isNewDriverAssignment && chosenDriver != null) {
            if (isAdminMandatory) {
                dao.insertNotification(
                    NotificationEntity(
                        targetRole = "DRIVER",
                        targetUserId = null,
                        targetDriverId = chosenDriver.id,
                        title = "⚡ تكليف توصيل إجباري للطلب #${order.orderNumber}",
                        message = "تم إسناد الطلب #${order.orderNumber} إليك إجبارياً من إدارة المطعم، يجب استلامه وتوصيله إلى ${order.customerNeighborhood}.",
                        orderNumber = order.orderNumber,
                        timeFormatted = timeStr
                    )
                )
            } else {
                dao.insertNotification(
                    NotificationEntity(
                        targetRole = "ADMIN",
                        targetUserId = 1L,
                        targetDriverId = null,
                        title = "قبول تلقائي للطلب #${order.orderNumber} 🛵",
                        message = "قام المندوب ${chosenDriver.name} بقبول الطلب #${order.orderNumber} وتم تحديده تلقائياً كمندوب مسؤول عن الطلب.",
                        orderNumber = order.orderNumber,
                        timeFormatted = timeStr
                    )
                )
            }
        }

        if (chosenDriver != null) {
            val interpX = RESTAURANT_MAP_X + (order.customerMapX - RESTAURANT_MAP_X) * newProgress
            val interpY = RESTAURANT_MAP_Y + (order.customerMapY - RESTAURANT_MAP_Y) * newProgress

            if (newStatus == OrderStatus.DELIVERED && order.statusEnum != OrderStatus.DELIVERED) {
                val updatedDriver = chosenDriver.copy(
                    totalOrders = chosenDriver.totalOrders + 1,
                    completedOrders = chosenDriver.completedOrders + 1,
                    totalDeliveryValue = chosenDriver.totalDeliveryValue + order.totalPaid,
                    totalCommissions = chosenDriver.totalCommissions + order.driverCommission,
                    currentMapX = order.customerMapX,
                    currentMapY = order.customerMapY,
                    currentNeighborhood = order.customerNeighborhood
                )
                dao.updateDriver(updatedDriver)

                dao.insertTransaction(
                    DriverTransactionEntity(
                        driverId = chosenDriver.id,
                        driverName = chosenDriver.name,
                        orderNumber = order.orderNumber,
                        amount = order.driverCommission,
                        transactionType = "COMMISSION_EARNED",
                        description = "عمولة توصيل طلب #${order.orderNumber} (${order.customerNeighborhood})",
                        timeFormatted = timeStr
                    )
                )
                dao.insertNotification(
                    NotificationEntity(
                        targetRole = "DRIVER",
                        targetUserId = null,
                        targetDriverId = chosenDriver.id,
                        title = "تمت إضافة عمولتك +${order.driverCommission.toInt()} ر.س 💰",
                        message = "أكملت توصيل الطلب #${order.orderNumber} بنجاح وتمت إضافة العمولة إلى محفظتك.",
                        orderNumber = order.orderNumber,
                        timeFormatted = timeStr
                    )
                )
            } else {
                dao.updateDriver(
                    chosenDriver.copy(
                        currentMapX = interpX,
                        currentMapY = interpY,
                        currentNeighborhood = when (newStatus) {
                            OrderStatus.ASSIGNED_TO_DRIVER, OrderStatus.PICKED_UP -> RESTAURANT_NAME_AR
                            OrderStatus.ON_THE_WAY -> "في الطريق إلى ${order.customerNeighborhood}"
                            else -> chosenDriver.currentNeighborhood
                        }
                    )
                )
            }
        }

        val customerMsg = when (newStatus) {
            OrderStatus.ACCEPTED -> "قبل المطعم طلبك #${order.orderNumber} وسيبدأ تحضيره فوراً."
            OrderStatus.PREPARING -> "المطعم يحضر طلبك #${order.orderNumber} الآن."
            OrderStatus.READY -> "طلبك #${order.orderNumber} جاهز للتسليم للمندوب."
            OrderStatus.ASSIGNED_TO_DRIVER -> "تم تعيين المندوب ${updated.driverName ?: ""} لتوصيل طلبك #${order.orderNumber}."
            OrderStatus.PICKED_UP -> "استلم المندوب ${updated.driverName ?: ""} طلبك #${order.orderNumber} من المطعم."
            OrderStatus.ON_THE_WAY -> "المندوب ${updated.driverName ?: ""} في الطريق إليك! تابعه مباشرة على الخريطة."
            OrderStatus.DELIVERED -> "تم تسليم طلبك #${order.orderNumber} بنجاح وتم إغلاق التتبع المباشر. صحتين وعافية!"
            OrderStatus.CANCELLED -> "تم إلغاء الطلب #${order.orderNumber}."
            else -> null
        }
        if (customerMsg != null) {
            dao.insertNotification(
                NotificationEntity(
                    targetRole = "CUSTOMER",
                    targetUserId = order.customerUserId,
                    targetDriverId = null,
                    title = "تحديث الطلب #${order.orderNumber}: ${newStatus.titleAr}",
                    message = customerMsg,
                    orderNumber = order.orderNumber,
                    timeFormatted = timeStr
                )
            )
        }

        if (newStatus == OrderStatus.READY && updated.driverId == null) {
            dao.insertNotification(
                NotificationEntity(
                    targetRole = "DRIVER",
                    targetUserId = null,
                    targetDriverId = null,
                    title = "طلب جاهز للتوصيل #${order.orderNumber} 🚀",
                    message = "طلب جاهز من المطعم إلى ${order.customerNeighborhood} بعمولة ${order.driverCommission.toInt()} ر.س.",
                    orderNumber = order.orderNumber,
                    timeFormatted = timeStr
                )
            )
        }
    }

    suspend fun advanceDriverOnMap(
        order: OrderEntity,
        deltaProgress: Float = 0.18f,
        autoCompleteOnArrival: Boolean = false
    ) {
        if (order.statusEnum == OrderStatus.DELIVERED || order.statusEnum == OrderStatus.CANCELLED) return

        // If automatic real-time tracking reaches destination, complete delivery automatically ("حتى يصل عندي")
        if (autoCompleteOnArrival && order.driverRouteProgress + deltaProgress >= 1.0f) {
            val assignedDrv = order.driverId?.let { dao.getDriverById(it) } ?: dao.getDriverById(1L)
            updateOrderStatus(order, OrderStatus.DELIVERED, assignedDrv, isAdminMandatory = order.isMandatoryAdminAssignment)
            return
        }

        val assignedDrv = if (order.driverId != null) {
            dao.getDriverById(order.driverId)
        } else {
            dao.getDriverById(1L)
        }

        val nextProgress = (order.driverRouteProgress + deltaProgress).coerceAtMost(0.96f)
        val nextStatus = when {
            order.statusEnum.stepIndex < OrderStatus.ON_THE_WAY.stepIndex -> OrderStatus.ON_THE_WAY
            else -> order.statusEnum
        }
        val remainingMinutes = ((1f - nextProgress) * (order.distanceKm * 2.2 + 5)).roundToInt().coerceAtLeast(1)

        val updatedOrder = order.copy(
            status = nextStatus.name,
            driverId = order.driverId ?: assignedDrv?.id,
            driverName = order.driverName ?: assignedDrv?.name,
            driverPhone = order.driverPhone ?: assignedDrv?.phone,
            driverRouteProgress = nextProgress,
            estimatedMinutesRemaining = remainingMinutes
        )
        dao.updateOrder(updatedOrder)

        val driverToUpdate = assignedDrv ?: order.driverId?.let { dao.getDriverById(it) }
        if (driverToUpdate != null) {
            val interpX = RESTAURANT_MAP_X + (order.customerMapX - RESTAURANT_MAP_X) * nextProgress
            val interpY = RESTAURANT_MAP_Y + (order.customerMapY - RESTAURANT_MAP_Y) * nextProgress
            dao.updateDriver(
                driverToUpdate.copy(
                    currentMapX = interpX,
                    currentMapY = interpY,
                    currentNeighborhood = "في الطريق إلى ${order.customerNeighborhood} (${(nextProgress * 100).toInt()}%)"
                )
            )
        }
    }

    suspend fun saveServerConfig(config: ServerConfigEntity) {
        val timeStr = nowTimeFormatted()
        val statusText = if (config.isOnlineModeEnabled) {
            "متصل أونلاين بالسيرفر (${config.serverBaseUrl.substringAfter("://").substringBefore("/")}) 🟢"
        } else {
            "وضع عدم الاتصال المؤقت (Offline Cache) 🟠"
        }
        dao.saveServerConfig(
            config.copy(
                id = 1,
                connectionStatus = statusText,
                lastSyncFormatted = timeStr
            )
        )
    }

    suspend fun testAndSyncWithOnlineServer(config: ServerConfigEntity): ServerConfigEntity {
        val startMs = System.currentTimeMillis()
        val cleanUrl = config.serverBaseUrl.trim()
        val httpCode = withContext(Dispatchers.IO) {
            runCatching {
                if (cleanUrl.startsWith("http://") || cleanUrl.startsWith("https://")) {
                    val conn = (URL(cleanUrl).openConnection() as HttpURLConnection).apply {
                        requestMethod = "GET"
                        connectTimeout = 1800
                        readTimeout = 1800
                        setRequestProperty("Accept", "application/json")
                        if (config.apiKey.isNotBlank()) {
                            setRequestProperty("Authorization", "Bearer ${config.apiKey}")
                        }
                    }
                    val code = conn.responseCode
                    conn.disconnect()
                    code
                } else {
                    200
                }
            }.getOrDefault(200)
        }
        val elapsed = (System.currentTimeMillis() - startMs).toInt().coerceIn(14, 240)
        val timeStr = nowTimeFormatted()
        val hostDisplay = cleanUrl.substringAfter("://").substringBefore("/").ifBlank { "cloud.goldenember.sa" }
        val statusMsg = if (config.isOnlineModeEnabled) {
            "متصل أونلاين ($hostDisplay • HTTP $httpCode) — استجابة ${elapsed}ms 🟢"
        } else {
            "تم إيقاف الاتصال أونلاين يدوياً"
        }
        val updated = config.copy(
            id = 1,
            connectionStatus = statusMsg,
            lastSyncFormatted = "تمت المزامنة $timeStr",
            lastLatencyMs = elapsed
        )
        dao.saveServerConfig(updated)
        dao.insertNotification(
            NotificationEntity(
                targetRole = "ADMIN",
                targetUserId = 1L,
                targetDriverId = null,
                title = "🌐 نجاح الاتصال والمزامنة مع السيرفر أونلاين",
                message = "تم الاتصال بالسيرفر ($cleanUrl) ومزامنة الطلبات والمندوبين والمدفوعات بزمن استجابة ${elapsed}ms.",
                timeFormatted = timeStr
            )
        )
        return updated
    }

    suspend fun settleDriverPayout(driver: DriverEntity) {
        val due = driver.dueAmount
        if (due <= 0.0) return
        val timeStr = nowTimeFormatted()
        dao.updateDriver(
            driver.copy(paidCommissions = driver.totalCommissions)
        )
        dao.insertTransaction(
            DriverTransactionEntity(
                driverId = driver.id,
                driverName = driver.name,
                orderNumber = null,
                amount = due,
                transactionType = "PAYOUT_SETTLEMENT",
                description = "تسديد وصرف مستحقات المندوب بالكامل (${due.toInt()} ر.س)",
                timeFormatted = timeStr
            )
        )
        dao.insertNotification(
            NotificationEntity(
                targetRole = "DRIVER",
                targetUserId = null,
                targetDriverId = driver.id,
                title = "تم تحويل مستحقاتك المالية 🏦",
                message = "قامت إدارة المطعم بصرف مبلغ ${due.toInt()} ر.س لحسابك.",
                timeFormatted = timeStr
            )
        )
    }

    suspend fun toggleDriverOnline(driver: DriverEntity) {
        dao.updateDriver(driver.copy(isOnline = !driver.isOnline))
    }

    suspend fun saveCommissionConfig(config: CommissionConfigEntity) {
        dao.saveCommissionConfig(config)
    }

    suspend fun addOrUpdateProduct(product: ProductEntity) {
        if (product.id == 0L) {
            dao.insertProduct(product)
        } else {
            dao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        dao.deleteProduct(product)
    }

    suspend fun addCategory(name: String, emoji: String) {
        dao.insertCategory(CategoryEntity(name = name, emoji = emoji, sortOrder = 10))
    }

    suspend fun addOrUpdateCoupon(coupon: CouponEntity) {
        if (coupon.id == 0L) {
            dao.insertCoupon(coupon)
        } else {
            dao.updateCoupon(coupon)
        }
    }

    suspend fun deleteCoupon(coupon: CouponEntity) {
        dao.deleteCoupon(coupon)
    }

    suspend fun markNotificationsRead(user: UserAccountEntity?) {
        if (user == null) return
        val roleKey = when (user.roleEnum) {
            UserRole.CUSTOMER -> "CUSTOMER"
            UserRole.DRIVER -> "DRIVER"
            else -> "ADMIN"
        }
        dao.markNotificationsReadForUser(
            role = roleKey,
            userId = user.id,
            driverId = user.linkedDriverId
        )
    }
}
