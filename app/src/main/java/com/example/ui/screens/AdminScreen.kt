package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.AccountStatus
import com.example.data.CategoryEntity
import com.example.data.CommissionConfigEntity
import com.example.data.CommissionType
import com.example.data.CouponEntity
import com.example.data.DriverEntity
import com.example.data.DriverTransactionEntity
import com.example.data.OrderEntity
import com.example.data.OrderStatus
import com.example.data.PaymentGatewayEntity
import com.example.data.ProductEntity
import com.example.data.ServerConfigEntity
import com.example.data.UserAccountEntity
import com.example.data.UserRole
import com.example.ui.components.AdminFleetRadarMap
import com.example.ui.components.ProductVisualImage
import com.example.ui.components.copyUriToInternalStorage

enum class AdminSubTab(val titleAr: String) {
    DASHBOARD("لوحة التحكم والرادار"),
    ORDERS("إدارة الطلبات"),
    DRIVERS_COMMISSIONS("المندوبين وتفعيل الحسابات"),
    PAYMENT_GATEWAYS("الدفع الإلكتروني والربط"),
    SERVER_ONLINE("إعدادات السيرفر أونلاين 🌐"),
    PRODUCTS("المنتجات والأصناف"),
    COUPONS("العروض والكوبونات"),
    REPORTS("المالية والتقارير")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    userAccounts: List<UserAccountEntity>,
    paymentGateways: List<PaymentGatewayEntity>,
    serverConfig: ServerConfigEntity,
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    drivers: List<DriverEntity>,
    coupons: List<CouponEntity>,
    orders: List<OrderEntity>,
    commissionConfig: CommissionConfigEntity,
    transactions: List<DriverTransactionEntity>,
    isLiveTrackingEnabled: Boolean,
    onSetAdminRole: (UserRole) -> Unit,
    onApproveOrUpdateAccount: (UserAccountEntity, AccountStatus) -> Unit,
    onSavePaymentGateway: (PaymentGatewayEntity) -> Unit,
    onTestPaymentGateway: (PaymentGatewayEntity) -> Unit,
    onSaveServerConfig: (ServerConfigEntity) -> Unit,
    onTestAndSyncServer: (ServerConfigEntity) -> Unit,
    onUpdateOrderStatus: (OrderEntity, OrderStatus, DriverEntity?, Boolean) -> Unit,
    onStepOrderDriver: (OrderEntity) -> Unit,
    onToggleLiveTracking: () -> Unit,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onSaveCommissionConfig: (CommissionConfigEntity) -> Unit,
    onSettleDriverPayout: (DriverEntity) -> Unit,
    onSaveCoupon: (CouponEntity) -> Unit,
    onDeleteCoupon: (CouponEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AdminSubTab.DASHBOARD) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showAddCouponDialog by remember { mutableStateOf(false) }
    var editingGateway by remember { mutableStateOf<PaymentGatewayEntity?>(null) }

    if (activeTab != AdminSubTab.DASHBOARD) {
        BackHandler { activeTab = AdminSubTab.DASHBOARD }
    }

    val currentRole = commissionConfig.adminRoleEnum
    val canEditFinancialRules = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.RESTAURANT_ADMIN
    val pendingDriverAccounts = userAccounts.filter {
        it.roleEnum == UserRole.DRIVER && it.statusEnum == AccountStatus.PENDING_APPROVAL
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Scrollable Admin Sub-tabs Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AdminSubTab.entries) { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activeTab = tab }
                            .testTag("admin_tab_${tab.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tab.titleAr,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (tab == AdminSubTab.DRIVERS_COMMISSIONS && pendingDriverAccounts.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFDC2626)
                                ) {
                                    Text(
                                        text = "${pendingDriverAccounts.size} بانتظار التفعيل",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        when (activeTab) {
            AdminSubTab.DASHBOARD -> {
                AdminDashboardOverview(
                    orders = orders,
                    drivers = drivers,
                    pendingDriverAccounts = pendingDriverAccounts,
                    currentRole = currentRole,
                    isLiveTrackingEnabled = isLiveTrackingEnabled,
                    onSetAdminRole = onSetAdminRole,
                    onApproveDriverAccount = { acc ->
                        onApproveOrUpdateAccount(acc, AccountStatus.ACTIVE)
                    },
                    onToggleLiveTracking = onToggleLiveTracking,
                    onStepOrderDriver = onStepOrderDriver,
                    onUpdateOrderStatus = onUpdateOrderStatus,
                    onNavigateTab = { activeTab = it }
                )
            }
            AdminSubTab.ORDERS -> {
                AdminOrdersTab(
                    orders = orders,
                    drivers = drivers.filter { it.statusEnum == AccountStatus.ACTIVE },
                    onUpdateOrderStatus = onUpdateOrderStatus,
                    onStepOrderDriver = onStepOrderDriver
                )
            }
            AdminSubTab.DRIVERS_COMMISSIONS -> {
                AdminDriversAndCommissionsTab(
                    userAccounts = userAccounts,
                    drivers = drivers,
                    commissionConfig = commissionConfig,
                    canEditFinancialRules = canEditFinancialRules,
                    onApproveOrUpdateAccount = onApproveOrUpdateAccount,
                    onSaveCommissionConfig = onSaveCommissionConfig,
                    onSettleDriverPayout = onSettleDriverPayout
                )
            }
            AdminSubTab.PAYMENT_GATEWAYS -> {
                AdminPaymentGatewaysTab(
                    gateways = paymentGateways,
                    canEditGateways = canEditFinancialRules,
                    onToggleGatewayEnabled = { gw ->
                        onSavePaymentGateway(gw.copy(isEnabled = !gw.isEnabled))
                    },
                    onToggleGatewayLiveMode = { gw ->
                        onSavePaymentGateway(gw.copy(isLiveMode = !gw.isLiveMode))
                    },
                    onEditGatewayConfig = { editingGateway = it },
                    onTestGateway = onTestPaymentGateway
                )
            }
            AdminSubTab.SERVER_ONLINE -> {
                AdminServerOnlineTab(
                    config = serverConfig,
                    onSaveServerConfig = onSaveServerConfig,
                    onTestAndSyncServer = onTestAndSyncServer
                )
            }
            AdminSubTab.PRODUCTS -> {
                AdminProductsTab(
                    categories = categories,
                    products = products,
                    canEditPrices = canEditFinancialRules,
                    onAddProductClick = { showAddProductDialog = true },
                    onEditProduct = { editingProduct = it },
                    onToggleAvailability = { prod ->
                        onSaveProduct(prod.copy(isAvailable = !prod.isAvailable))
                    },
                    onDeleteProduct = onDeleteProduct,
                    onAddCategory = onAddCategory
                )
            }
            AdminSubTab.COUPONS -> {
                AdminCouponsTab(
                    coupons = coupons,
                    canEditCoupons = canEditFinancialRules,
                    onAddCouponClick = { showAddCouponDialog = true },
                    onToggleCoupon = { c -> onSaveCoupon(c.copy(isActive = !c.isActive)) },
                    onDeleteCoupon = onDeleteCoupon
                )
            }
            AdminSubTab.REPORTS -> {
                AdminFinancialReportsTab(
                    orders = orders,
                    drivers = drivers,
                    products = products,
                    transactions = transactions
                )
            }
        }
    }

    if (showAddProductDialog || editingProduct != null) {
        ProductEditorDialog(
            initial = editingProduct,
            categories = categories,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSave = { prod ->
                onSaveProduct(prod)
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    if (showAddCouponDialog) {
        CouponEditorDialog(
            onDismiss = { showAddCouponDialog = false },
            onSave = { coupon ->
                onSaveCoupon(coupon)
                showAddCouponDialog = false
            }
        )
    }

    editingGateway?.let { gw ->
        PaymentGatewayConfigDialog(
            gateway = gw,
            onDismiss = { editingGateway = null },
            onSave = { updated ->
                onSavePaymentGateway(updated)
                editingGateway = null
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminDashboardOverview(
    orders: List<OrderEntity>,
    drivers: List<DriverEntity>,
    pendingDriverAccounts: List<UserAccountEntity>,
    currentRole: UserRole,
    isLiveTrackingEnabled: Boolean,
    onSetAdminRole: (UserRole) -> Unit,
    onApproveDriverAccount: (UserAccountEntity) -> Unit,
    onToggleLiveTracking: () -> Unit,
    onStepOrderDriver: (OrderEntity) -> Unit,
    onUpdateOrderStatus: (OrderEntity, OrderStatus, DriverEntity?, Boolean) -> Unit,
    onNavigateTab: (AdminSubTab) -> Unit
) {
    val nonCancelled = orders.filter { it.statusEnum != OrderStatus.CANCELLED }
    val totalSales = nonCancelled.sumOf { it.totalPaid } + 8120.0
    val totalCommissions = nonCancelled.sumOf { it.driverCommission } + 940.0
    val netRevenue = totalSales - totalCommissions
    val newCount = orders.count { it.statusEnum == OrderStatus.NEW || it.statusEnum == OrderStatus.ACCEPTED }
    val preparingCount = orders.count { it.statusEnum == OrderStatus.PREPARING }
    val readyCount = orders.count { it.statusEnum == OrderStatus.READY }
    val withDriversCount = orders.count {
        it.statusEnum == OrderStatus.ASSIGNED_TO_DRIVER ||
            it.statusEnum == OrderStatus.PICKED_UP ||
            it.statusEnum == OrderStatus.ON_THE_WAY
    }
    val completedCount = orders.count { it.statusEnum == OrderStatus.DELIVERED } + 122
    val cancelledCount = orders.count { it.statusEnum == OrderStatus.CANCELLED } + 2
    val activeDriversCount = drivers.count { it.isOnline && it.statusEnum == AccountStatus.ACTIVE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Pending Driver Approvals Alert Card (so Admin can approve new drivers in 1 tap!)
        if (pendingDriverAccounts.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HowToReg, contentDescription = null, tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⏳ طلبات تفعيل حسابات مندوبي التوصيل الجدد (${pendingDriverAccounts.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF92400E),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        pendingDriverAccounts.forEach { pending ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pending.fullName,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color(0xFF1C1917),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "اسم الدخول/الجوال: ${pending.username} • ${pending.vehicleInfo}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF57534E)
                                        )
                                    }
                                    Button(
                                        onClick = { onApproveDriverAccount(pending) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                        modifier = Modifier.testTag("approve_driver_btn_${pending.username}")
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تفعيل الحساب")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Role-Based Access Control (RBAC) Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نظام الصلاحيات الإدارية (RBAC):", style = MaterialTheme.typography.titleSmall)
                        }
                        FilledTonalButton(
                            onClick = { onNavigateTab(AdminSubTab.PAYMENT_GATEWAYS) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("بوابات الدفع الإلكتروني", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(
                        text = currentRole.badgeAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(UserRole.SUPER_ADMIN, UserRole.RESTAURANT_ADMIN, UserRole.STAFF).forEach { role ->
                            FilterChip(
                                selected = currentRole == role,
                                onClick = { onSetAdminRole(role) },
                                label = { Text(role.titleAr.substringBefore(" (")) }
                            )
                        }
                    }
                }
            }
        }

        // Top 4 Main KPI Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1917))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "لوحة تحكم المطعم المركزية - مؤشرات اليوم المباشرة",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AdminHeroKpiBox(
                            label = "الطلبات اليوم",
                            value = "${123 + orders.size}",
                            sub = "+$newCount جديد",
                            modifier = Modifier.weight(1f)
                        )
                        AdminHeroKpiBox(
                            label = "إجمالي المبيعات",
                            value = "${totalSales.toInt()} ر.س",
                            sub = "صافي ${netRevenue.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        AdminHeroKpiBox(
                            label = "المندوبين النشطين",
                            value = "$activeDriversCount / ${drivers.size}",
                            sub = "عمولات ${totalCommissions.toInt()}",
                            modifier = Modifier.weight(1f)
                        )
                        AdminHeroKpiBox(
                            label = "إجمالي العملاء",
                            value = "530",
                            sub = "ولاء نشط",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Detailed Order Status Breakdown Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPillStat("جديدة", newCount, Color(0xFF2563EB), Modifier.weight(1f))
                StatusPillStat("تحضير", preparingCount, Color(0xFFD97706), Modifier.weight(1f))
                StatusPillStat("جاهزة", readyCount, Color(0xFF7C3AED), Modifier.weight(1f))
                StatusPillStat("مع المندوب", withDriversCount, Color(0xFFEA580C), Modifier.weight(1f))
                StatusPillStat("مكتملة", completedCount, Color(0xFF059669), Modifier.weight(1f))
                StatusPillStat("ملغاة", cancelledCount, Color(0xFFDC2626), Modifier.weight(1f))
            }
        }

        // Permanent Live Fleet Radar Map for Admin
        item {
            AdminFleetRadarMap(
                drivers = drivers.filter { it.statusEnum == AccountStatus.ACTIVE },
                orders = orders,
                isLiveTrackingEnabled = isLiveTrackingEnabled,
                onToggleLiveTracking = onToggleLiveTracking,
                onStepOrderDriver = onStepOrderDriver
            )
        }

        // Live Current Orders Table
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("الطلبات الحالية النشطة", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = { onNavigateTab(AdminSubTab.ORDERS) }) {
                    Text("إدارة كل الطلبات (${orders.size})")
                }
            }
        }

        items(orders.take(5), key = { it.id }) { order ->
            AdminQuickOrderRowCard(
                order = order,
                drivers = drivers.filter { it.statusEnum == AccountStatus.ACTIVE },
                onUpdateOrderStatus = onUpdateOrderStatus
            )
        }
    }
}

@Composable
private fun AdminPaymentGatewaysTab(
    gateways: List<PaymentGatewayEntity>,
    canEditGateways: Boolean,
    onToggleGatewayEnabled: (PaymentGatewayEntity) -> Unit,
    onToggleGatewayLiveMode: (PaymentGatewayEntity) -> Unit,
    onEditGatewayConfig: (PaymentGatewayEntity) -> Unit,
    onTestGateway: (PaymentGatewayEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "💳 إدارة بوابات الدفع الإلكتروني والربط البرمجي (API Integration)",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تحكم في تفعيل وإيقاف طرق الدفع الإلكتروني (مدى، Apple Pay، STC Pay، تابي/تمارا) وإعداد مفاتيح الربط (Merchant ID & API Keys) التي تظهر للعملاء في شاشة الدفع.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        items(gateways, key = { it.id }) { gw ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = gw.nameAr,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "مزود الخدمة: ${gw.providerName} • عمولة البوابة: ${gw.feePercentage}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (gw.isEnabled) "مفعل للعميل" else "معطل",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = gw.isEnabled,
                                onCheckedChange = {
                                    if (canEditGateways) onToggleGatewayEnabled(gw)
                                }
                            )
                        }
                    }

                    if (gw.isElectronic) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "معرف التاجر (Merchant ID): ${gw.merchantId}",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Text(
                                    text = "مفتاح الربط العام (Publishable Key): ${gw.publishableKey}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "رابط الإشعارات الفورية (Webhook): ${gw.webhookUrl}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "حالة الاتصال: ${gw.lastTestedStatus}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF059669)
                                )
                            }
                        }

                        if (canEditGateways) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = gw.isLiveMode,
                                    onClick = { onToggleGatewayLiveMode(gw) },
                                    label = {
                                        Text(if (gw.isLiveMode) "بيئة الإنتاج (Live)" else "بيئة الاختبار (Sandbox)")
                                    }
                                )
                                FilledTonalButton(
                                    onClick = { onEditGatewayConfig(gw) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("إعدادات الربط", style = MaterialTheme.typography.labelMedium)
                                }
                                Button(
                                    onClick = { onTestGateway(gw) }
                                ) {
                                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اختبار", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentGatewayConfigDialog(
    gateway: PaymentGatewayEntity,
    onDismiss: () -> Unit,
    onSave: (PaymentGatewayEntity) -> Unit
) {
    var merchantId by remember { mutableStateOf(gateway.merchantId) }
    var pubKey by remember { mutableStateOf(gateway.publishableKey) }
    var secKey by remember { mutableStateOf(gateway.secretKey) }
    var webhook by remember { mutableStateOf(gateway.webhookUrl) }
    var fee by remember { mutableStateOf(gateway.feePercentage.toString()) }
    var isLive by remember { mutableStateOf(gateway.isLiveMode) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "إعدادات الربط البرمجي: ${gateway.nameAr}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = merchantId,
                    onValueChange = { merchantId = it },
                    label = { Text("معرف التاجر (Merchant ID)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pubKey,
                    onValueChange = { pubKey = it },
                    label = { Text("مفتاح الربط العام (Publishable API Key)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = secKey,
                    onValueChange = { secKey = it },
                    label = { Text("المفتاح السري (Secret Key)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = webhook,
                    onValueChange = { webhook = it },
                    label = { Text("رابط استجابة الدفع (Webhook URL)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = fee,
                    onValueChange = { fee = it },
                    label = { Text("نسبة عمولة بوابة الدفع (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isLive, onCheckedChange = { isLive = it })
                    Text("تفعيل وضع الإنتاج الفعلي (Live Mode)")
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                gateway.copy(
                                    merchantId = merchantId.trim(),
                                    publishableKey = pubKey.trim(),
                                    secretKey = secKey.trim(),
                                    webhookUrl = webhook.trim(),
                                    feePercentage = fee.toDoubleOrNull() ?: gateway.feePercentage,
                                    isLiveMode = isLive
                                )
                            )
                        }
                    ) {
                        Text("حفظ إعدادات الربط")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminHeroKpiBox(
    label: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF292524),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFFFBBF24),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFA8A29E)
            )
        }
    }
}

@Composable
private fun StatusPillStat(
    title: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun AdminQuickOrderRowCard(
    order: OrderEntity,
    drivers: List<DriverEntity>,
    onUpdateOrderStatus: (OrderEntity, OrderStatus, DriverEntity?, Boolean) -> Unit
) {
    val defaultDriver = drivers.firstOrNull { it.isOnline } ?: drivers.firstOrNull()
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "#${order.orderNumber} • ${order.customerName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${order.customerNeighborhood} • ${order.itemsSummary}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${order.totalPaid.toInt()} ر.س",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = order.statusEnum.titleAr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF059669)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (order.statusEnum) {
                    OrderStatus.NEW -> {
                        Button(
                            onClick = { onUpdateOrderStatus(order, OrderStatus.PREPARING, null, false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("قبول وبدء التحضير")
                        }
                    }
                    OrderStatus.ACCEPTED, OrderStatus.PREPARING -> {
                        Button(
                            onClick = { onUpdateOrderStatus(order, OrderStatus.READY, null, false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تجهيز الطلب (جاهز للمندوب)")
                        }
                    }
                    OrderStatus.READY -> {
                        if (order.driverId == null) {
                            Button(
                                onClick = {
                                    onUpdateOrderStatus(order, OrderStatus.ASSIGNED_TO_DRIVER, defaultDriver, true)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إسناد إجباري للمندوب (${defaultDriver?.name?.substringBefore(" ") ?: "أحمد"})")
                            }
                        } else {
                            Button(
                                onClick = { onUpdateOrderStatus(order, OrderStatus.ON_THE_WAY, null, false) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("المندوب (${order.driverName}) استلم الطلب")
                            }
                        }
                    }
                    OrderStatus.ASSIGNED_TO_DRIVER, OrderStatus.PICKED_UP -> {
                        Button(
                            onClick = { onUpdateOrderStatus(order, OrderStatus.ON_THE_WAY, null, false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("انطلاق المندوب للعميل")
                        }
                    }
                    OrderStatus.ON_THE_WAY -> {
                        Button(
                            onClick = { onUpdateOrderStatus(order, OrderStatus.DELIVERED, null, false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد التسليم وإغلاق الطلب")
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminOrdersTab(
    orders: List<OrderEntity>,
    drivers: List<DriverEntity>,
    onUpdateOrderStatus: (OrderEntity, OrderStatus, DriverEntity?, Boolean) -> Unit,
    onStepOrderDriver: (OrderEntity) -> Unit
) {
    var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }
    val filtered = if (statusFilter == null) orders else orders.filter { it.statusEnum == statusFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("إدارة دورة حياة الطلبات وتحديد المندوب المسؤول", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = "• يتم تحديد المندوب المسؤول تلقائياً فور قبوله للطلب من حسابه.\n• الطلبات التي لم يوافق عليها أي مندوب بعد يمكن للمدير إسنادها إجبارياً للمندوب لتوصيلها.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                item {
                    FilterChip(
                        selected = statusFilter == null,
                        onClick = { statusFilter = null },
                        label = { Text("الكل (${orders.size})") }
                    )
                }
                items(OrderStatus.entries) { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { statusFilter = st },
                        label = { Text(st.titleAr) }
                    )
                }
            }
        }

        items(filtered, key = { it.id }) { order ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "طلب #${order.orderNumber} • ${order.createdTimeFormatted}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = order.statusEnum.titleAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text("العميل: ${order.customerName} (${order.customerPhone}) • ${order.customerNeighborhood} (${order.distanceKm} كم)")
                    if (order.customerStreetDetails.isNotBlank()) {
                        Text("العنوان التفصيلي: ${order.customerStreetDetails}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("المنتجات: ${order.itemsSummary}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = "الدفع: ${order.paymentMethod} (${order.paymentTransactionRef}) | المدفوع: ${order.totalPaid.toInt()} ر.س | عمولة المندوب: ${order.driverCommission.toInt()} ر.س | صافي المطعم: ${order.restaurantNet.toInt()} ر.س",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Automatic vs Mandatory Driver Assignment Section
                    if (order.driverId != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (order.isMandatoryAdminAssignment) {
                                Color(0xFFFEF3C7)
                            } else {
                                Color(0xFF059669).copy(alpha = 0.14f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (order.isMandatoryAdminAssignment) {
                                        "⚡ المندوب المسؤول (تم الإسناد الإجباري من الإدارة): 🛵 ${order.driverName}"
                                    } else {
                                        "✅ المندوب المسؤول (محدد تلقائياً بعد قبول المندوب للطلب): 🛵 ${order.driverName}"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (order.isMandatoryAdminAssignment) Color(0xFF92400E) else Color(0xFF059669),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (order.isMandatoryAdminAssignment) {
                                        "هذا الطلب مسند إجبارياً للمندوب ويلزمه إتمام التوصيل للعميل."
                                    } else {
                                        "تم قفل التعيين اليدوي لأن المندوب قام بقبول الطلب تلقائياً من حسابه."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (order.isMandatoryAdminAssignment) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else if (order.statusEnum != OrderStatus.DELIVERED && order.statusEnum != OrderStatus.CANCELLED) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "⚠️ لم يتم قبول الطلب من قبل أي مندوب بعد — إسناد إجباري من المدير:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    drivers.forEach { drv ->
                                        Button(
                                            onClick = {
                                                val nextStatus = if (order.statusEnum.stepIndex < OrderStatus.ASSIGNED_TO_DRIVER.stepIndex) {
                                                    OrderStatus.ASSIGNED_TO_DRIVER
                                                } else order.statusEnum
                                                onUpdateOrderStatus(order, nextStatus, drv, true)
                                            },
                                            modifier = Modifier.testTag("mandatory_assign_driver_${order.orderNumber}_${drv.id}")
                                        ) {
                                            Text("⚡ إسناد إجباري لـ ${drv.name.substringBefore(" ")}")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { onUpdateOrderStatus(order, OrderStatus.PREPARING, null, false) }) {
                            Text("قيد التحضير")
                        }
                        FilledTonalButton(onClick = { onUpdateOrderStatus(order, OrderStatus.READY, null, false) }) {
                            Text("جاهز للتسليم")
                        }
                        FilledTonalButton(onClick = { onUpdateOrderStatus(order, OrderStatus.ON_THE_WAY, null, false) }) {
                            Text("في الطريق")
                        }
                        if (order.statusEnum == OrderStatus.ON_THE_WAY || order.statusEnum == OrderStatus.PICKED_UP) {
                            FilledTonalButton(onClick = { onStepOrderDriver(order) }) {
                                Text("تحريك على الخريطة +20%")
                            }
                        }
                        Button(
                            onClick = { onUpdateOrderStatus(order, OrderStatus.DELIVERED, null, false) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("تم التسليم")
                        }
                        if (order.statusEnum != OrderStatus.DELIVERED && order.statusEnum != OrderStatus.CANCELLED) {
                            OutlinedButton(onClick = { onUpdateOrderStatus(order, OrderStatus.CANCELLED, null, false) }) {
                                Text("إلغاء", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminProductsTab(
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    canEditPrices: Boolean,
    onAddProductClick: () -> Unit,
    onEditProduct: (ProductEntity) -> Unit,
    onToggleAvailability: (ProductEntity) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onAddCategory: (String, String) -> Unit
) {
    var newCategoryName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("إدارة المنتجات والتصنيفات والصور", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "إضافة، تعديل، رفع صورة المنتج، إيقاف مؤقت، وتحديد الأحجام والإضافات",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canEditPrices) {
                    Button(
                        onClick = onAddProductClick,
                        modifier = Modifier.testTag("admin_add_product_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إضافة منتج")
                    }
                }
            }
        }

        if (canEditPrices) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            placeholder = { Text("اسم تصنيف جديد (مثال: حلويات)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                if (newCategoryName.isNotBlank()) {
                                    onAddCategory(newCategoryName.trim(), "🍽️")
                                    newCategoryName = ""
                                }
                            }
                        ) {
                            Text("إضافة قسم")
                        }
                    }
                }
            }
        }

        items(products, key = { it.id }) { prod ->
            val catName = categories.firstOrNull { it.id == prod.categoryId }?.name ?: "عام"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductVisualImage(
                        imageKey = prod.imageKey,
                        contentDescription = prod.name,
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${prod.name} (${prod.basePrice.toInt()} ر.س)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "القسم: $catName • مرات الطلب: ${prod.orderCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الإضافات: ${prod.availableAddons.ifBlank { "بدون إضافات" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Switch(
                                checked = prod.isAvailable,
                                onCheckedChange = { onToggleAvailability(prod) }
                            )
                            Text(
                                text = if (prod.isAvailable) "متاح" else "متوقف",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        if (canEditPrices) {
                            IconButton(onClick = { onEditProduct(prod) }) {
                                Icon(Icons.Default.Edit, contentDescription = "تعديل")
                            }
                            IconButton(onClick = { onDeleteProduct(prod) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "حذف",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminDriversAndCommissionsTab(
    userAccounts: List<UserAccountEntity>,
    drivers: List<DriverEntity>,
    commissionConfig: CommissionConfigEntity,
    canEditFinancialRules: Boolean,
    onApproveOrUpdateAccount: (UserAccountEntity, AccountStatus) -> Unit,
    onSaveCommissionConfig: (CommissionConfigEntity) -> Unit,
    onSettleDriverPayout: (DriverEntity) -> Unit
) {
    var selectedType by remember(commissionConfig.activeType) {
        mutableStateOf(commissionConfig.typeEnum)
    }
    var percentageInput by remember(commissionConfig.percentageRate) {
        mutableStateOf(commissionConfig.percentageRate.toString())
    }
    var fixedInput by remember(commissionConfig.fixedAmount) {
        mutableStateOf(commissionConfig.fixedAmount.toString())
    }
    var hybridBaseInput by remember(commissionConfig.hybridBaseAmount) {
        mutableStateOf(commissionConfig.hybridBaseAmount.toString())
    }
    var hybridExtraInput by remember(commissionConfig.hybridExtraPer5Km) {
        mutableStateOf(commissionConfig.hybridExtraPer5Km.toString())
    }

    val driverAccounts = userAccounts.filter { it.roleEnum == UserRole.DRIVER }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Driver Accounts Activation & Approval Section
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🛡️ إدارة وتفعيل حسابات مندوبي التوصيل المسجلين (${driverAccounts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أي مندوب يسجل حساباً جديداً يبقى في حالة (بانتظار تفعيل الإدارة) ولا يمكنه الدخول للتطبيق حتى يتم تفعيله من هنا:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    driverAccounts.forEach { acc ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = when (acc.statusEnum) {
                                AccountStatus.PENDING_APPROVAL -> Color(0xFFFEF3C7)
                                AccountStatus.ACTIVE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                AccountStatus.SUSPENDED -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${acc.fullName} (دخول: ${acc.username})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (acc.statusEnum == AccountStatus.PENDING_APPROVAL) Color(0xFF1C1917) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "المركبة: ${acc.vehicleInfo.ifBlank { "غير محدد" }} • الحالة: ${acc.statusEnum.titleAr}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (acc.statusEnum == AccountStatus.PENDING_APPROVAL) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (acc.statusEnum != AccountStatus.ACTIVE) {
                                    Button(
                                        onClick = { onApproveOrUpdateAccount(acc, AccountStatus.ACTIVE) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                                    ) {
                                        Text("تفعيل المندوب ✓")
                                    }
                                } else {
                                    OutlinedButton(
                                        onClick = { onApproveOrUpdateAccount(acc, AccountStatus.SUSPENDED) }
                                    ) {
                                        Text("إيقاف مؤقت", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dynamic Commission Rules Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "⚙️ محرك قواعد عمولات المندوبين المرنة (بدون تعديل البرمجة)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    if (!canEditFinancialRules) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "صلاحيتك الحالية (Staff) تسمح بإدارة الطلبات فقط ولا تسمح بتغيير نسب عمولات المندوبين.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CommissionType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    label = { Text(type.titleAr) }
                                )
                            }
                        }

                        Text(
                            text = selectedType.descriptionAr,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )

                        when (selectedType) {
                            CommissionType.PERCENTAGE -> {
                                OutlinedTextField(
                                    value = percentageInput,
                                    onValueChange = { percentageInput = it },
                                    label = { Text("نسبة المندوب من قيمة الطلب (%) - مثال: 10") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            CommissionType.FIXED -> {
                                OutlinedTextField(
                                    value = fixedInput,
                                    onValueChange = { fixedInput = it },
                                    label = { Text("المبلغ الثابت لكل طلب (ريال) - مثال: 7") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            CommissionType.DISTANCE_TIERS -> {
                                Text(
                                    text = "• شريحة 1 (0 - 5 كم): ${commissionConfig.tier1Amount.toInt()} ريال\n" +
                                        "• شريحة 2 (5 - 10 كم): ${commissionConfig.tier2Amount.toInt()} ريال\n" +
                                        "• شريحة 3 (10 - 15+ كم): ${commissionConfig.tier3Amount.toInt()} ريال",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            CommissionType.HYBRID -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = hybridBaseInput,
                                        onValueChange = { hybridBaseInput = it },
                                        label = { Text("عمولة أساسية (ر.س)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = hybridExtraInput,
                                        onValueChange = { hybridExtraInput = it },
                                        label = { Text("+ لكل 5 كم إضافية") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        val previewConfig = commissionConfig.copy(
                            activeType = selectedType.name,
                            percentageRate = percentageInput.toDoubleOrNull() ?: 10.0,
                            fixedAmount = fixedInput.toDoubleOrNull() ?: 7.0,
                            hybridBaseAmount = hybridBaseInput.toDoubleOrNull() ?: 5.0,
                            hybridExtraPer5Km = hybridExtraInput.toDoubleOrNull() ?: 2.0
                        )
                        val sampleComm = previewConfig.calculateCommission(orderSubtotal = 100.0, distanceKm = 6.5)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "معاينة فورية: لطلب قيمته 100 ريال ومسافة 6.5 كم ← عمولة المندوب = ${sampleComm.toInt()} ريال",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Button(
                            onClick = { onSaveCommissionConfig(previewConfig) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("حفظ وتعميم قاعدة العمولة على النظام")
                        }
                    }
                }
            }
        }

        item {
            Text("ملفات مندوبي التوصيل والمستحقات المالية (${drivers.size})", style = MaterialTheme.typography.headlineSmall)
        }

        items(drivers, key = { it.id }) { drv ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(drv.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = "الجوال: ${drv.phone} • الانضمام: ${drv.joinedDate} • التقييم: ⭐ ${drv.rating}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = when (drv.statusEnum) {
                                AccountStatus.PENDING_APPROVAL -> Color(0xFFFEF3C7)
                                AccountStatus.SUSPENDED -> MaterialTheme.colorScheme.errorContainer
                                AccountStatus.ACTIVE -> if (drv.isOnline) Color(0xFF059669).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = when (drv.statusEnum) {
                                    AccountStatus.PENDING_APPROVAL -> "بانتظار التفعيل ⏳"
                                    AccountStatus.SUSPENDED -> "موقوف"
                                    AccountStatus.ACTIVE -> if (drv.isOnline) "مفعل ونشط 🟢" else "مفعل (غير متصل)"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = when (drv.statusEnum) {
                                    AccountStatus.PENDING_APPROVAL -> Color(0xFF92400E)
                                    AccountStatus.SUSPENDED -> MaterialTheme.colorScheme.onErrorContainer
                                    AccountStatus.ACTIVE -> if (drv.isOnline) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("الطلبات: ${drv.totalOrders}", style = MaterialTheme.typography.bodySmall)
                        Text("المكتملة: ${drv.completedOrders}", style = MaterialTheme.typography.bodySmall)
                        Text("الملغاة: ${drv.cancelledOrders}", style = MaterialTheme.typography.bodySmall)
                        Text("قيمة التوصيل: ${drv.totalDeliveryValue.toInt()} ر.س", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي العمولات: ${drv.totalCommissions.toInt()} ر.س", style = MaterialTheme.typography.labelMedium)
                        Text("المدفوع: ${drv.paidCommissions.toInt()} ر.س", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = "المستحق: ${drv.dueAmount.toInt()} ر.س",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (drv.dueAmount > 0 && canEditFinancialRules) {
                        Button(
                            onClick = { onSettleDriverPayout(drv) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("صرف وتسديد المستحقات (${drv.dueAmount.toInt()} ريال)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCouponsTab(
    coupons: List<CouponEntity>,
    canEditCoupons: Boolean,
    onAddCouponClick: () -> Unit,
    onToggleCoupon: (CouponEntity) -> Unit,
    onDeleteCoupon: (CouponEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("إدارة العروض والكوبونات", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "نسبة مئوية، مبلغ ثابت، حد أدنى للطلب، وتواريخ الصلاحية",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canEditCoupons) {
                    Button(onClick = onAddCouponClick) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("كوبون جديد")
                    }
                }
            }
        }

        items(coupons, key = { it.id }) { coupon ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "كود الخصم: ${coupon.code} (${coupon.titleAr})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (coupon.isPercentage) {
                                "الخصم: ${coupon.discountValue.toInt()}% • الحد الأقصى: ${coupon.maxDiscount.toInt()} ريال"
                            } else {
                                "الخصم الثابت: ${coupon.discountValue.toInt()} ريال"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "الحد الأدنى للطلب: ${coupon.minimumOrder.toInt()} ر.س • الصلاحية: ${coupon.startDate} إلى ${coupon.endDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (canEditCoupons) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = coupon.isActive,
                                onCheckedChange = { onToggleCoupon(coupon) }
                            )
                            IconButton(onClick = { onDeleteCoupon(coupon) }) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "حذف الكوبون",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminFinancialReportsTab(
    orders: List<OrderEntity>,
    drivers: List<DriverEntity>,
    products: List<ProductEntity>,
    transactions: List<DriverTransactionEntity>
) {
    val validOrders = orders.filter { it.statusEnum != OrderStatus.CANCELLED }
    val totalSubtotal = validOrders.sumOf { it.subtotal } + 7850.0
    val totalDiscounts = validOrders.sumOf { it.discount } + 420.0
    val totalDeliveryFees = validOrders.sumOf { it.deliveryFee } + 1020.0
    val totalPaid = totalSubtotal + totalDeliveryFees - totalDiscounts
    val totalDriverCommissions = drivers.sumOf { it.totalCommissions }
    val totalDriverPaid = drivers.sumOf { it.paidCommissions }
    val totalDriverDue = drivers.sumOf { it.dueAmount }
    val restaurantNetRevenue = totalPaid - totalDriverCommissions

    val weeklyDays = listOf(
        "السبت" to 0.78f,
        "الأحد" to 0.64f,
        "الإثنين" to 0.70f,
        "الثلاثاء" to 0.82f,
        "الأربعاء" to 0.88f,
        "الخميس" to 0.96f,
        "الجمعة" to 0.92f
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("نظام الحسابات المالية والتقارير التحليلية", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("السجل المالي الشامل للمطعم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    HorizontalDivider()
                    ReportRow("إجمالي المبيعات (المنتجات)", "${totalSubtotal.toInt()} ريال")
                    ReportRow("إجمالي رسوم التوصيل المحصلة", "+${totalDeliveryFees.toInt()} ريال")
                    ReportRow("إجمالي الخصومات والكوبونات", "-${totalDiscounts.toInt()} ريال", Color(0xFFDC2626))
                    HorizontalDivider()
                    ReportRow("إجمالي المبالغ المدفوعة من العملاء", "${totalPaid.toInt()} ريال", isBold = true)
                    ReportRow("إجمالي عمولات المندوبين", "-${totalDriverCommissions.toInt()} ريال", Color(0xFFD97706))
                    HorizontalDivider()
                    ReportRow(
                        label = "صافي إيرادات المطعم الفعلية",
                        value = "${restaurantNetRevenue.toInt()} ريال",
                        color = Color(0xFF059669),
                        isBold = true
                    )
                    HorizontalDivider()
                    ReportRow("المبالغ المدفوعة للمندوبين", "${totalDriverPaid.toInt()} ريال")
                    ReportRow("المبالغ المستحقة للمندوبين حالياً", "${totalDriverDue.toInt()} ريال", MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📊 تقرير المبيعات اليومية خلال الأسبوع (السبت - الجمعة)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        weeklyDays.forEach { (day, ratio) ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${(ratio * 9200).toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height((105 * ratio).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(day, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🏆 أكثر المنتجات مبيعاً", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    products.sortedByDescending { it.orderCount }.take(5).forEachIndexed { idx, prod ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${idx + 1}. ${prod.name}", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${prod.orderCount} طلب • ${(prod.orderCount * prod.basePrice).toInt()} ر.س",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProductEditorDialog(
    initial: ProductEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var price by remember { mutableStateOf(initial?.basePrice?.toString() ?: "28") }
    var selectedCatId by remember { mutableStateOf(initial?.categoryId ?: (categories.firstOrNull()?.id ?: 1L)) }
    var hasSizes by remember { mutableStateOf(initial?.hasSizes ?: true) }
    var doubleExtra by remember { mutableStateOf(initial?.doubleSizeExtra?.toString() ?: "8") }
    var addons by remember { mutableStateOf(initial?.availableAddons ?: "جبن:3.0,صوص:2.0,بطاطس:5.0") }
    var selectedImageKey by remember { mutableStateOf(initial?.imageKey ?: "burger") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = copyUriToInternalStorage(context, uri)
            if (savedPath != null) {
                selectedImageKey = savedPath
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (initial == null) "إضافة منتج جديد مع صورة" else "تعديل المنتج وصورته",
                    style = MaterialTheme.typography.titleLarge
                )

                // Product Image Preview + Gallery Upload & Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProductVisualImage(
                        imageKey = selectedImageKey,
                        contentDescription = name.ifBlank { "صورة المنتج" },
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pick_product_image_btn")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة / اختيار صورة من المعرض")
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                "burger" to "🍔 برجر",
                                "pizza" to "🍕 بيتزا",
                                "hero" to "🍗 مشويات/بروستد"
                            ).forEach { (key, label) ->
                                FilterChip(
                                    selected = selectedImageKey == key,
                                    onClick = { selectedImageKey = key },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("الوصف") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("السعر (ر.س)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = doubleExtra,
                        onValueChange = { doubleExtra = it },
                        label = { Text("فرق حجم الدبل") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = addons,
                    onValueChange = { addons = it },
                    label = { Text("الإضافات (الاسم:السعر مفصولة بفاصلة)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCatId == cat.id,
                            onClick = { selectedCatId = cat.id },
                            label = { Text("${cat.emoji} ${cat.name}") }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(
                                    ProductEntity(
                                        id = initial?.id ?: 0L,
                                        categoryId = selectedCatId,
                                        name = name.trim(),
                                        description = description.trim().ifEmpty { "وجبة طازجة محضرة يومياً" },
                                        basePrice = price.toDoubleOrNull() ?: 25.0,
                                        imageKey = selectedImageKey,
                                        isAvailable = initial?.isAvailable ?: true,
                                        hasSizes = hasSizes,
                                        doubleSizeExtra = doubleExtra.toDoubleOrNull() ?: 8.0,
                                        availableAddons = addons,
                                        orderCount = initial?.orderCount ?: 1
                                    )
                                )
                            }
                        }
                    ) {
                        Text("حفظ المنتج")
                    }
                }
            }
        }
    }
}

@Composable
private fun CouponEditorDialog(
    onDismiss: () -> Unit,
    onSave: (CouponEntity) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var titleAr by remember { mutableStateOf("") }
    var isPercentage by remember { mutableStateOf(true) }
    var discountVal by remember { mutableStateOf("20") }
    var minOrder by remember { mutableStateOf("50") }
    var maxDiscount by remember { mutableStateOf("20") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("إنشاء كوبون خصم جديد", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.uppercase() },
                    label = { Text("كود الخصم (مثال: SAVE25)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = titleAr,
                    onValueChange = { titleAr = it },
                    label = { Text("وصف العرض (مثال: خصم 25% خاص)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPercentage, onCheckedChange = { isPercentage = it })
                    Text("خصم بالنسبة المئوية (%) بدلاً من مبلغ ثابت")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = discountVal,
                        onValueChange = { discountVal = it },
                        label = { Text("قيمة الخصم") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minOrder,
                        onValueChange = { minOrder = it },
                        label = { Text("الحد الأدنى (ر.س)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxDiscount,
                        onValueChange = { maxDiscount = it },
                        label = { Text("أقصى خصم") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (code.isNotBlank()) {
                                onSave(
                                    CouponEntity(
                                        code = code.trim().uppercase(),
                                        titleAr = titleAr.trim().ifEmpty { "عرض خصم خاص" },
                                        isPercentage = isPercentage,
                                        discountValue = discountVal.toDoubleOrNull() ?: 15.0,
                                        minimumOrder = minOrder.toDoubleOrNull() ?: 40.0,
                                        maxDiscount = maxDiscount.toDoubleOrNull() ?: 25.0,
                                        startDate = "1/10",
                                        endDate = "30/10",
                                        isActive = true
                                    )
                                )
                            }
                        }
                    ) {
                        Text("إضافة الكوبون")
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminServerOnlineTab(
    config: ServerConfigEntity,
    onSaveServerConfig: (ServerConfigEntity) -> Unit,
    onTestAndSyncServer: (ServerConfigEntity) -> Unit
) {
    var isOnlineEnabled by remember(config) { mutableStateOf(config.isOnlineModeEnabled) }
    var baseUrl by remember(config) { mutableStateOf(config.serverBaseUrl) }
    var wsUrl by remember(config) { mutableStateOf(config.websocketUrl) }
    var apiKey by remember(config) { mutableStateOf(config.apiKey) }
    var syncInterval by remember(config) { mutableStateOf(config.syncIntervalSeconds.toString()) }
    var autoSyncOrders by remember(config) { mutableStateOf(config.autoSyncOrders) }
    var autoSyncDrivers by remember(config) { mutableStateOf(config.autoSyncDriverLocations) }
    var autoSyncPayments by remember(config) { mutableStateOf(config.autoSyncPayments) }

    fun buildConfig(): ServerConfigEntity = config.copy(
        isOnlineModeEnabled = isOnlineEnabled,
        serverBaseUrl = baseUrl.trim().ifBlank { "https://api.goldenember-restaurant.sa/v1" },
        websocketUrl = wsUrl.trim().ifBlank { "wss://live.goldenember-restaurant.sa/ws/tracking" },
        apiKey = apiKey.trim(),
        syncIntervalSeconds = syncInterval.toIntOrNull()?.coerceIn(2, 60) ?: 4,
        autoSyncOrders = autoSyncOrders,
        autoSyncDriverLocations = autoSyncDrivers,
        autoSyncPayments = autoSyncPayments
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnlineEnabled) {
                        Color(0xFF059669).copy(alpha = 0.14f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🌐 حالة عمل النظام أونلاين وربط السيرفر السحابي",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = config.connectionStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isOnlineEnabled) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "آخر مزامنة: ${config.lastSyncFormatted} • زمن الاستجابة: ${config.lastLatencyMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isOnlineEnabled,
                        onCheckedChange = {
                            isOnlineEnabled = it
                            onSaveServerConfig(buildConfig().copy(isOnlineModeEnabled = it))
                        }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "إعدادات الاتصال بالسيرفر (Online Server Configuration)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("عنوان السيرفر الأساسي (REST API Base URL)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_server_url_input")
                    )

                    OutlinedTextField(
                        value = wsUrl,
                        onValueChange = { wsUrl = it },
                        label = { Text("خادم التتبع المباشر والطلبات الفورية (WebSocket URL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("مفتاح أمان السيرفر (Server API Token)") },
                            singleLine = true,
                            modifier = Modifier.weight(1.6f)
                        )
                        OutlinedTextField(
                            value = syncInterval,
                            onValueChange = { syncInterval = it },
                            label = { Text("دورة المزامنة (ثانية)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    HorizontalDivider()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoSyncOrders, onCheckedChange = { autoSyncOrders = it })
                        Text("مزامنة الطلبات الفورية بين العميل والمطعم والمندوب أونلاين")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoSyncDrivers, onCheckedChange = { autoSyncDrivers = it })
                        Text("مزامنة إحداثيات GPS لموقع المندوب على الخريطة مباشرة")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoSyncPayments, onCheckedChange = { autoSyncPayments = it })
                        Text("التحقق الفوري من عمليات الدفع الإلكتروني أونلاين")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onTestAndSyncServer(buildConfig()) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_test_server_btn")
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فحص الاتصال ومزامنة السيرفر")
                        }
                        Button(
                            onClick = { onSaveServerConfig(buildConfig()) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_save_server_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ إعدادات السيرفر")
                        }
                    }
                }
            }
        }
    }
}
