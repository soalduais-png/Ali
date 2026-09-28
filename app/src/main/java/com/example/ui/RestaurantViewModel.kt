package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AccountStatus
import com.example.data.CategoryEntity
import com.example.data.CommissionConfigEntity
import com.example.data.CouponEntity
import com.example.data.CustomerAddressEntity
import com.example.data.DriverEntity
import com.example.data.DriverTransactionEntity
import com.example.data.LoginAuthResult
import com.example.data.NeighborhoodOption
import com.example.data.NotificationEntity
import com.example.data.OrderEntity
import com.example.data.OrderStatus
import com.example.data.PaymentGatewayEntity
import com.example.data.ProductEntity
import com.example.data.RIYADH_NEIGHBORHOODS
import com.example.data.RestaurantRepository
import com.example.data.ServerConfigEntity
import com.example.data.UserAccountEntity
import com.example.data.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppPortal(val titleAr: String, val subtitleAr: String) {
    CUSTOMER("واجهة العميل", "المنيو • السلة • العناوين • تتبع الطلب الحي"),
    DRIVER("تطبيق المندوب", "الطلبات • الملاحة • العمولات"),
    ADMIN("لوحة إدارة المطعم", "التحكم • المندوبين • الدفع الإلكتروني • التقارير"),
    SRS_BLUEPRINT("وثيقة SRS والهيكلة", "التحليل الكامل • الجداول • API")
}

data class CartItem(
    val id: String,
    val product: ProductEntity,
    val isDoubleSize: Boolean,
    val selectedAddons: List<Pair<String, Double>>,
    val quantity: Int
) {
    val unitPrice: Double
        get() = product.basePrice +
            (if (isDoubleSize && product.hasSizes) product.doubleSizeExtra else 0.0) +
            selectedAddons.sumOf { it.second }

    val lineTotal: Double
        get() = unitPrice * quantity

    fun formatSummary(): String {
        val sizePart = if (product.hasSizes) {
            if (isDoubleSize) " (حجم دبل)" else " (حجم عادي)"
        } else ""
        val addonsPart = if (selectedAddons.isNotEmpty()) {
            " + " + selectedAddons.joinToString("، ") { it.first }
        } else ""
        return "${quantity}× ${product.name}$sizePart$addonsPart"
    }
}

class RestaurantViewModel(
    private val repository: RestaurantRepository
) : ViewModel() {

    // Unified Authentication State
    private val _currentUser = MutableStateFlow<UserAccountEntity?>(null)
    val currentUser: StateFlow<UserAccountEntity?> = _currentUser.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isAuthError = MutableStateFlow(false)
    val isAuthError: StateFlow<Boolean> = _isAuthError.asStateFlow()

    private val _activePortal = MutableStateFlow(AppPortal.CUSTOMER)
    val activePortal: StateFlow<AppPortal> = _activePortal.asStateFlow()

    // Customer state
    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<CouponEntity?>(null)
    val appliedCoupon: StateFlow<CouponEntity?> = _appliedCoupon.asStateFlow()

    private val _couponMessage = MutableStateFlow<String?>(null)
    val couponMessage: StateFlow<String?> = _couponMessage.asStateFlow()

    private val _selectedAddressId = MutableStateFlow<Long?>(null)
    val selectedAddressId: StateFlow<Long?> = _selectedAddressId.asStateFlow()

    private val _selectedNeighborhood = MutableStateFlow(RIYADH_NEIGHBORHOODS.first())
    val selectedNeighborhood: StateFlow<NeighborhoodOption> = _selectedNeighborhood.asStateFlow()

    private val _selectedPaymentGatewayCode = MutableStateFlow("MADA_CARD")
    val selectedPaymentGatewayCode: StateFlow<String> = _selectedPaymentGatewayCode.asStateFlow()

    private val _customerNotes = MutableStateFlow("")
    val customerNotes: StateFlow<String> = _customerNotes.asStateFlow()

    private val _trackedOrderId = MutableStateFlow<Long?>(null)
    val trackedOrderId: StateFlow<Long?> = _trackedOrderId.asStateFlow()

    // Driver state
    private val _selectedDriverId = MutableStateFlow(1L)
    val selectedDriverId: StateFlow<Long> = _selectedDriverId.asStateFlow()

    // Live GPS movement simulation toggle
    private val _isLiveMapTrackingEnabled = MutableStateFlow(true)
    val isLiveMapTrackingEnabled: StateFlow<Boolean> = _isLiveMapTrackingEnabled.asStateFlow()

    // Flows from Room
    val userAccounts: StateFlow<List<UserAccountEntity>> = repository.userAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCustomerAddresses: StateFlow<List<CustomerAddressEntity>> = repository.customerAddresses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val paymentGateways: StateFlow<List<PaymentGatewayEntity>> = repository.paymentGateways
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drivers: StateFlow<List<DriverEntity>> = repository.drivers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val coupons: StateFlow<List<CouponEntity>> = repository.coupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orders: StateFlow<List<OrderEntity>> = repository.orders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commissionConfig: StateFlow<CommissionConfigEntity> = combine(
        repository.commissionConfig
    ) { arr ->
        arr.firstOrNull() ?: CommissionConfigEntity()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CommissionConfigEntity())

    val transactions: StateFlow<List<DriverTransactionEntity>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val serverConfig: StateFlow<ServerConfigEntity> = combine(
        repository.serverConfig
    ) { arr ->
        arr.firstOrNull() ?: ServerConfigEntity()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ServerConfigEntity())

    init {
        viewModelScope.launch {
            repository.ensureSeedData()
        }
        startLiveGpsTelemetryLoop()
    }

    private fun startLiveGpsTelemetryLoop() {
        viewModelScope.launch {
            while (isActive) {
                val intervalMs = (serverConfig.value.syncIntervalSeconds.coerceIn(2, 15) * 750L).coerceIn(2200L, 4000L)
                delay(intervalMs)
                // Always run automatic real-time tracking for customer orders until arrival
                val currentOrders = orders.value
                val defaultOnlineDriver = drivers.value.firstOrNull { it.isOnline } ?: drivers.value.firstOrNull()
                for (order in currentOrders) {
                    when (order.statusEnum) {
                        OrderStatus.NEW -> {
                            repository.updateOrderStatus(order, OrderStatus.PREPARING, order.driverId?.let { id -> drivers.value.firstOrNull { it.id == id } })
                        }
                        OrderStatus.ACCEPTED, OrderStatus.PREPARING -> {
                            repository.updateOrderStatus(order, OrderStatus.READY, order.driverId?.let { id -> drivers.value.firstOrNull { it.id == id } })
                        }
                        OrderStatus.READY -> {
                            val drv = order.driverId?.let { id -> drivers.value.firstOrNull { it.id == id } } ?: defaultOnlineDriver
                            repository.updateOrderStatus(order, OrderStatus.ON_THE_WAY, drv)
                        }
                        OrderStatus.ASSIGNED_TO_DRIVER, OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY -> {
                            repository.advanceDriverOnMap(
                                order = order,
                                deltaProgress = 0.06f,
                                autoCompleteOnArrival = true
                            )
                        }
                        OrderStatus.DELIVERED, OrderStatus.CANCELLED -> Unit
                    }
                }
            }
        }
    }

    fun clearAuthMessage() {
        _authMessage.value = null
        _isAuthError.value = false
    }

    fun login(usernameOrPhone: String, passwordInput: String) {
        if (usernameOrPhone.isBlank() || passwordInput.isBlank()) {
            _isAuthError.value = true
            _authMessage.value = "الرجاء إدخال اسم المستخدم/الجوال وكلمة المرور"
            return
        }
        viewModelScope.launch {
            repository.ensureSeedData()
            when (val result = repository.authenticateUser(usernameOrPhone, passwordInput)) {
                is LoginAuthResult.Success -> {
                    val account = result.account
                    _currentUser.value = account
                    _isAuthError.value = false
                    _authMessage.value = null
                    routeUserToDedicatedPortal(account)
                }
                is LoginAuthResult.PendingDriverApproval -> {
                    _isAuthError.value = true
                    _authMessage.value = "⏳ حساب المندوب (${result.account.fullName}) مسجل بنجاح ولكنه بانتظار التفعيل من قبل إدارة المطعم."
                }
                is LoginAuthResult.SuspendedAccount -> {
                    _isAuthError.value = true
                    _authMessage.value = "⚠️ هذا الحساب موقوف مؤقتاً من قبل إدارة المطعم."
                }
                LoginAuthResult.InvalidCredentials -> {
                    _isAuthError.value = true
                    _authMessage.value = "❌ اسم المستخدم أو كلمة المرور غير صحيحة"
                }
            }
        }
    }

    private fun routeUserToDedicatedPortal(account: UserAccountEntity) {
        when (account.roleEnum) {
            UserRole.SUPER_ADMIN, UserRole.RESTAURANT_ADMIN, UserRole.STAFF -> {
                _activePortal.value = AppPortal.ADMIN
            }
            UserRole.DRIVER -> {
                account.linkedDriverId?.let { _selectedDriverId.value = it }
                _activePortal.value = AppPortal.DRIVER
            }
            UserRole.CUSTOMER -> {
                _activePortal.value = AppPortal.CUSTOMER
                val userAddrs = allCustomerAddresses.value.filter { it.userId == account.id }
                val def = userAddrs.firstOrNull { it.isDefault } ?: userAddrs.firstOrNull()
                if (def != null) {
                    _selectedAddressId.value = def.id
                    RIYADH_NEIGHBORHOODS.firstOrNull { it.nameAr == def.neighborhoodName }?.let {
                        _selectedNeighborhood.value = it
                    }
                }
            }
        }
    }

    fun registerCustomer(
        fullName: String,
        usernameOrPhone: String,
        password: String,
        addressLabel: String,
        neighborhood: NeighborhoodOption,
        streetAndBuilding: String,
        landmarkNotes: String
    ) {
        if (fullName.isBlank() || usernameOrPhone.isBlank() || password.isBlank()) {
            _isAuthError.value = true
            _authMessage.value = "الرجاء إكمال بيانات العميل الأساسية وكلمة المرور"
            return
        }
        if (streetAndBuilding.isBlank()) {
            _isAuthError.value = true
            _authMessage.value = "الرجاء إدخال تفاصيل الشارع/المبنى لعنوان التوصيل"
            return
        }
        viewModelScope.launch {
            val res = repository.registerCustomerWithAddress(
                fullName = fullName,
                usernameOrPhone = usernameOrPhone,
                password = password,
                addressLabel = addressLabel,
                neighborhood = neighborhood,
                streetAndBuilding = streetAndBuilding,
                landmarkNotes = landmarkNotes
            )
            res.onSuccess { account ->
                // Customer account is activated automatically upon registration!
                _currentUser.value = account
                _selectedNeighborhood.value = neighborhood
                _isAuthError.value = false
                _authMessage.value = null
                _activePortal.value = AppPortal.CUSTOMER
            }.onFailure { err ->
                _isAuthError.value = true
                _authMessage.value = err.message ?: "تعذر إنشاء الحساب"
            }
        }
    }

    fun registerDriver(
        fullName: String,
        usernameOrPhone: String,
        password: String,
        vehicleInfo: String,
        neighborhood: NeighborhoodOption,
        onDriverRegisteredPending: () -> Unit
    ) {
        if (fullName.isBlank() || usernameOrPhone.isBlank() || password.isBlank()) {
            _isAuthError.value = true
            _authMessage.value = "الرجاء إكمال اسم المندوب ورقم الجوال وكلمة المرور"
            return
        }
        viewModelScope.launch {
            val res = repository.registerDriverAccount(
                fullName = fullName,
                usernameOrPhone = usernameOrPhone,
                password = password,
                vehicleInfo = vehicleInfo,
                neighborhood = neighborhood
            )
            res.onSuccess { account ->
                // Driver is NOT logged in automatically; must wait for Admin approval!
                _isAuthError.value = false
                _authMessage.value = "✅ تم إرسال طلب تسجيل المندوب (${account.fullName}) بنجاح! لا يتم تفعيل حساب المندوب إلا من قبل إدارة المطعم."
                onDriverRegisteredPending()
            }.onFailure { err ->
                _isAuthError.value = true
                _authMessage.value = err.message ?: "تعذر تسجيل المندوب"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _authMessage.value = null
        _isAuthError.value = false
        _trackedOrderId.value = null
        _cartItems.value = emptyList()
        _appliedCoupon.value = null
        _couponMessage.value = null
    }

    fun updateAccountStatusByAdmin(account: UserAccountEntity, newStatus: AccountStatus) {
        viewModelScope.launch {
            repository.updateAccountApprovalStatus(account, newStatus)
        }
    }

    // Customer Address Operations
    fun selectCustomerAddress(address: CustomerAddressEntity) {
        _selectedAddressId.value = address.id
        val matchedNeighborhood = RIYADH_NEIGHBORHOODS.firstOrNull { it.nameAr == address.neighborhoodName }
            ?: NeighborhoodOption(address.neighborhoodName, address.distanceKm, address.mapX, address.mapY)
        _selectedNeighborhood.value = matchedNeighborhood
    }

    fun saveCustomerAddress(
        id: Long = 0L,
        label: String,
        neighborhood: NeighborhoodOption,
        streetAndBuilding: String,
        landmarkNotes: String,
        isDefault: Boolean
    ) {
        val userId = _currentUser.value?.id ?: 2L
        viewModelScope.launch {
            val entity = CustomerAddressEntity(
                id = id,
                userId = userId,
                label = label.ifBlank { "عنوان توصيل" },
                neighborhoodName = neighborhood.nameAr,
                streetAndBuilding = streetAndBuilding.ifBlank { "الشارع الرئيسي" },
                landmarkNotes = landmarkNotes,
                distanceKm = neighborhood.distanceKm,
                mapX = neighborhood.mapX,
                mapY = neighborhood.mapY,
                isDefault = isDefault
            )
            repository.saveCustomerAddress(entity)
            if (isDefault) {
                _selectedNeighborhood.value = neighborhood
            }
        }
    }

    fun setDefaultCustomerAddress(address: CustomerAddressEntity) {
        viewModelScope.launch {
            repository.setDefaultCustomerAddress(address)
            selectCustomerAddress(address)
        }
    }

    fun deleteCustomerAddress(address: CustomerAddressEntity) {
        viewModelScope.launch {
            repository.deleteCustomerAddress(address)
        }
    }

    // Payment Gateways Operations
    fun savePaymentGateway(gateway: PaymentGatewayEntity) {
        viewModelScope.launch {
            repository.savePaymentGateway(gateway)
        }
    }

    fun testPaymentGateway(gateway: PaymentGatewayEntity) {
        viewModelScope.launch {
            repository.testPaymentGatewayConnection(gateway)
        }
    }

    fun selectPaymentGatewayCode(code: String) {
        _selectedPaymentGatewayCode.value = code
    }

    fun setPortal(portal: AppPortal) {
        _activePortal.value = portal
    }

    fun toggleLiveMapTracking() {
        _isLiveMapTrackingEnabled.value = !_isLiveMapTrackingEnabled.value
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectNeighborhood(option: NeighborhoodOption) {
        _selectedNeighborhood.value = option
    }

    fun updateCustomerNotes(notes: String) {
        _customerNotes.value = notes
    }

    fun selectTrackedOrder(orderId: Long) {
        _trackedOrderId.value = orderId
    }

    fun selectDriver(driverId: Long) {
        _selectedDriverId.value = driverId
    }

    fun addToCart(
        product: ProductEntity,
        isDoubleSize: Boolean,
        selectedAddons: List<Pair<String, Double>>,
        quantity: Int
    ) {
        val key = "${product.id}_${isDoubleSize}_${selectedAddons.joinToString { it.first }}"
        val current = _cartItems.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.id == key }
        if (existingIdx >= 0) {
            val existing = current[existingIdx]
            current[existingIdx] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            current.add(
                CartItem(
                    id = key,
                    product = product,
                    isDoubleSize = isDoubleSize,
                    selectedAddons = selectedAddons,
                    quantity = quantity
                )
            )
        }
        _cartItems.value = current
    }

    fun updateCartItemQuantity(itemId: String, newQty: Int) {
        val current = _cartItems.value.toMutableList()
        if (newQty <= 0) {
            current.removeAll { it.id == itemId }
        } else {
            val idx = current.indexOfFirst { it.id == itemId }
            if (idx >= 0) {
                current[idx] = current[idx].copy(quantity = newQty)
            }
        }
        _cartItems.value = current
    }

    fun applyCouponCode(codeInput: String) {
        val clean = codeInput.trim().uppercase()
        if (clean.isEmpty()) {
            _appliedCoupon.value = null
            _couponMessage.value = null
            return
        }
        val subtotal = _cartItems.value.sumOf { it.lineTotal }
        val found = coupons.value.firstOrNull { it.code.equals(clean, ignoreCase = true) && it.isActive }
        if (found == null) {
            _couponMessage.value = "كود الخصم غير صالح أو منتهي الصلاحية"
            return
        }
        if (subtotal < found.minimumOrder) {
            _couponMessage.value = "الحد الأدنى لتفعيل الكود ${found.code} هو ${found.minimumOrder.toInt()} ر.س"
            return
        }
        _appliedCoupon.value = found
        _couponMessage.value = "تم تطبيق كود ${found.code} (${found.titleAr}) بنجاح!"
    }

    fun calculateCartDiscount(subtotal: Double): Double {
        val coupon = _appliedCoupon.value ?: return 0.0
        if (subtotal < coupon.minimumOrder) return 0.0
        return if (coupon.isPercentage) {
            ((subtotal * (coupon.discountValue / 100.0)).coerceAtMost(coupon.maxDiscount))
        } else {
            coupon.discountValue.coerceAtMost(coupon.maxDiscount)
        }
    }

    fun submitCustomerOrder(
        selectedAddress: CustomerAddressEntity?,
        paymentGateway: PaymentGatewayEntity?,
        paymentRef: String,
        onOrderCreated: (Long) -> Unit
    ) {
        val items = _cartItems.value
        if (items.isEmpty()) return
        val subtotal = items.sumOf { it.lineTotal }
        val discount = calculateCartDiscount(subtotal)
        val summary = items.joinToString("، ") { it.formatSummary() }

        val user = _currentUser.value
        val userId = user?.id ?: 2L
        val customerName = user?.fullName ?: "محمد الفيصل"
        val customerPhone = user?.phone ?: "0559998877"

        val neighborhood = if (selectedAddress != null) {
            NeighborhoodOption(
                nameAr = selectedAddress.neighborhoodName,
                distanceKm = selectedAddress.distanceKm,
                mapX = selectedAddress.mapX,
                mapY = selectedAddress.mapY
            )
        } else {
            _selectedNeighborhood.value
        }
        val streetDetails = selectedAddress?.fullAddressDisplay() ?: neighborhood.nameAr
        val couponCode = _appliedCoupon.value?.code
        val methodTitle = paymentGateway?.nameAr ?: "Apple Pay / مدى"
        val isElectronic = paymentGateway?.isElectronic ?: true
        val notes = _customerNotes.value

        viewModelScope.launch {
            val newOrderId = repository.createCustomerOrder(
                customerUserId = userId,
                customerName = customerName,
                customerPhone = customerPhone,
                neighborhood = neighborhood,
                streetDetails = streetDetails,
                itemsSummary = summary,
                subtotal = subtotal,
                discount = discount,
                couponCode = couponCode,
                paymentMethod = methodTitle,
                paymentTransactionRef = paymentRef,
                isElectronicPaid = isElectronic,
                customerNotes = notes
            )
            _cartItems.value = emptyList()
            _appliedCoupon.value = null
            _couponMessage.value = null
            _customerNotes.value = ""
            _trackedOrderId.value = newOrderId
            onOrderCreated(newOrderId)
        }
    }

    fun updateOrderStatus(
        order: OrderEntity,
        newStatus: OrderStatus,
        assignDriver: DriverEntity? = null,
        isAdminMandatory: Boolean = false
    ) {
        viewModelScope.launch {
            repository.updateOrderStatus(order, newStatus, assignDriver, isAdminMandatory)
        }
    }

    fun stepDriverForwardOnMap(order: OrderEntity) {
        viewModelScope.launch {
            repository.advanceDriverOnMap(order, deltaProgress = 0.20f)
        }
    }

    fun settleDriverPayout(driver: DriverEntity) {
        viewModelScope.launch {
            repository.settleDriverPayout(driver)
        }
    }

    fun toggleDriverOnline(driver: DriverEntity) {
        viewModelScope.launch {
            repository.toggleDriverOnline(driver)
        }
    }

    fun saveCommissionConfig(config: CommissionConfigEntity) {
        viewModelScope.launch {
            repository.saveCommissionConfig(config)
        }
    }

    fun setAdminRole(role: UserRole) {
        viewModelScope.launch {
            val current = commissionConfig.value
            repository.saveCommissionConfig(current.copy(activeAdminRole = role.name))
        }
    }

    fun saveProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.addOrUpdateProduct(product)
        }
    }

    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun addCategory(name: String, emoji: String) {
        viewModelScope.launch {
            repository.addCategory(name, emoji)
        }
    }

    fun saveCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.addOrUpdateCoupon(coupon)
        }
    }

    fun deleteCoupon(coupon: CouponEntity) {
        viewModelScope.launch {
            repository.deleteCoupon(coupon)
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch {
            repository.markNotificationsRead(_currentUser.value)
        }
    }

    fun saveServerConfig(config: ServerConfigEntity) {
        viewModelScope.launch {
            repository.saveServerConfig(config)
        }
    }

    fun testAndSyncOnlineServer(config: ServerConfigEntity) {
        viewModelScope.launch {
            repository.testAndSyncWithOnlineServer(config)
        }
    }

    companion object {
        fun provideFactory(repository: RestaurantRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RestaurantViewModel(repository) as T
                }
            }
    }
}
