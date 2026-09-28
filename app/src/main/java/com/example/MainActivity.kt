package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AccountStatus
import com.example.data.NotificationEntity
import com.example.data.RestaurantDatabase
import com.example.data.RestaurantRepository
import com.example.data.UserRole
import com.example.ui.AppPortal
import com.example.ui.RestaurantViewModel
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CustomerScreen
import com.example.ui.screens.DriverScreen
import com.example.ui.screens.ServerOnlineConfigDialog
import com.example.ui.screens.SrsBlueprintScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RestaurantSystemApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantSystemApp() {
    val context = LocalContext.current
    val repository = remember {
        RestaurantRepository(RestaurantDatabase.getInstance(context).dao())
    }
    val viewModel: RestaurantViewModel = viewModel(
        factory = RestaurantViewModel.provideFactory(repository)
    )

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authMessage by viewModel.authMessage.collectAsStateWithLifecycle()
    val isAuthError by viewModel.isAuthError.collectAsStateWithLifecycle()

    val activePortal by viewModel.activePortal.collectAsStateWithLifecycle()
    val userAccounts by viewModel.userAccounts.collectAsStateWithLifecycle()
    val allCustomerAddresses by viewModel.allCustomerAddresses.collectAsStateWithLifecycle()
    val selectedAddressId by viewModel.selectedAddressId.collectAsStateWithLifecycle()
    val paymentGateways by viewModel.paymentGateways.collectAsStateWithLifecycle()
    val selectedPaymentGatewayCode by viewModel.selectedPaymentGatewayCode.collectAsStateWithLifecycle()

    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val drivers by viewModel.drivers.collectAsStateWithLifecycle()
    val coupons by viewModel.coupons.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val commissionConfig by viewModel.commissionConfig.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val serverConfig by viewModel.serverConfig.collectAsStateWithLifecycle()

    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val couponMessage by viewModel.couponMessage.collectAsStateWithLifecycle()
    val selectedNeighborhood by viewModel.selectedNeighborhood.collectAsStateWithLifecycle()
    val customerNotes by viewModel.customerNotes.collectAsStateWithLifecycle()
    val trackedOrderId by viewModel.trackedOrderId.collectAsStateWithLifecycle()
    val selectedDriverId by viewModel.selectedDriverId.collectAsStateWithLifecycle()
    val isLiveTrackingEnabled by viewModel.isLiveMapTrackingEnabled.collectAsStateWithLifecycle()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }

    // If user is not logged in, show the Unified Login & Registration Screen
    if (currentUser == null) {
        Scaffold { innerPadding ->
            AuthScreen(
                authMessage = authMessage,
                isAuthError = isAuthError,
                serverConfig = serverConfig,
                onClearAuthMessage = viewModel::clearAuthMessage,
                onLogin = viewModel::login,
                onRegisterCustomer = viewModel::registerCustomer,
                onRegisterDriver = viewModel::registerDriver,
                onSaveServerConfig = viewModel::saveServerConfig,
                onTestAndSyncServer = viewModel::testAndSyncOnlineServer,
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    val loggedInUser = currentUser!!
    val userCustomerAddresses = allCustomerAddresses.filter {
        it.userId == loggedInUser.id
    }

    // Strict per-user notification filtering without cross-user mixing
    val userNotifications = remember(notifications, loggedInUser) {
        when (loggedInUser.roleEnum) {
            UserRole.CUSTOMER -> notifications.filter {
                it.targetRole == "CUSTOMER" && it.targetUserId == loggedInUser.id
            }
            UserRole.DRIVER -> notifications.filter {
                it.targetRole == "DRIVER" &&
                    (it.targetUserId == loggedInUser.id ||
                        (loggedInUser.linkedDriverId != null && it.targetDriverId == loggedInUser.linkedDriverId))
            }
            UserRole.SUPER_ADMIN, UserRole.RESTAURANT_ADMIN, UserRole.STAFF -> notifications.filter {
                it.targetRole == "ADMIN" && (it.targetUserId == null || it.targetUserId == loggedInUser.id)
            }
        }
    }

    val unreadCount = userNotifications.count { !it.isRead }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${activePortal.titleAr} • ${loggedInUser.fullName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الحساب: ${loggedInUser.roleEnum.titleAr.substringBefore(" (")} (${loggedInUser.username}) • ${if (serverConfig.isOnlineModeEnabled) "أونلاين 🟢" else "أوفلاين 🟠"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showServerDialog = true },
                        modifier = Modifier.testTag("topbar_server_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "إعدادات السيرفر والاتصال أونلاين",
                            tint = if (serverConfig.isOnlineModeEnabled) Color(0xFF059669) else Color(0xFFD97706)
                        )
                    }
                    IconButton(
                        onClick = {
                            showNotificationsDialog = true
                            viewModel.markNotificationsRead()
                        },
                        modifier = Modifier.testTag("notifications_bell_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "إشعارات المستخدم"
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "تسجيل الخروج",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (loggedInUser.roleEnum) {
                UserRole.CUSTOMER -> {
                    val customerOrders = orders.filter { it.customerUserId == loggedInUser.id }
                    CustomerScreen(
                        currentCustomerName = loggedInUser.fullName,
                        customerAddresses = userCustomerAddresses,
                        selectedAddressId = selectedAddressId,
                        paymentGateways = paymentGateways,
                        selectedPaymentGatewayCode = selectedPaymentGatewayCode,
                        categories = categories,
                        products = products,
                        coupons = coupons,
                        orders = customerOrders,
                        commissionConfig = commissionConfig,
                        selectedCategoryId = selectedCategoryId,
                        searchQuery = searchQuery,
                        cartItems = cartItems,
                        appliedCoupon = appliedCoupon,
                        couponMessage = couponMessage,
                        selectedNeighborhood = selectedNeighborhood,
                        customerNotes = customerNotes,
                        trackedOrderId = trackedOrderId,
                        isLiveTrackingEnabled = isLiveTrackingEnabled,
                        onSelectCategory = viewModel::selectCategory,
                        onSearchChange = viewModel::updateSearchQuery,
                        onAddToCart = viewModel::addToCart,
                        onUpdateCartQuantity = viewModel::updateCartItemQuantity,
                        onApplyCoupon = viewModel::applyCouponCode,
                        onSelectAddress = viewModel::selectCustomerAddress,
                        onSaveAddress = viewModel::saveCustomerAddress,
                        onSetDefaultAddress = viewModel::setDefaultCustomerAddress,
                        onDeleteAddress = viewModel::deleteCustomerAddress,
                        onSelectPaymentGatewayCode = viewModel::selectPaymentGatewayCode,
                        onCustomerNotesChange = viewModel::updateCustomerNotes,
                        onSubmitOrder = viewModel::submitCustomerOrder,
                        onSelectTrackedOrder = viewModel::selectTrackedOrder,
                        onToggleLiveTracking = viewModel::toggleLiveMapTracking,
                        onStepDriverForward = viewModel::stepDriverForwardOnMap
                    )
                }

                UserRole.DRIVER -> {
                    val driverAccountId = loggedInUser.linkedDriverId ?: selectedDriverId
                    DriverScreen(
                        drivers = drivers.filter { it.id == driverAccountId },
                        selectedDriverId = driverAccountId,
                        loggedInUsername = loggedInUser.username,
                        orders = orders,
                        transactions = transactions.filter { it.driverId == driverAccountId },
                        isLiveTrackingEnabled = isLiveTrackingEnabled,
                        onToggleDriverOnline = viewModel::toggleDriverOnline,
                        onUpdateOrderStatus = { ord, st, drv ->
                            viewModel.updateOrderStatus(ord, st, drv, isAdminMandatory = false)
                        },
                        onStepDriverForward = viewModel::stepDriverForwardOnMap,
                        onToggleLiveTracking = viewModel::toggleLiveMapTracking
                    )
                }

                UserRole.SUPER_ADMIN, UserRole.RESTAURANT_ADMIN, UserRole.STAFF -> {
                    AdminScreen(
                        userAccounts = userAccounts,
                        paymentGateways = paymentGateways,
                        serverConfig = serverConfig,
                        categories = categories,
                        products = products,
                        drivers = drivers,
                        coupons = coupons,
                        orders = orders,
                        commissionConfig = commissionConfig,
                        transactions = transactions,
                        isLiveTrackingEnabled = isLiveTrackingEnabled,
                        onSetAdminRole = viewModel::setAdminRole,
                        onApproveOrUpdateAccount = viewModel::updateAccountStatusByAdmin,
                        onSavePaymentGateway = viewModel::savePaymentGateway,
                        onTestPaymentGateway = viewModel::testPaymentGateway,
                        onSaveServerConfig = viewModel::saveServerConfig,
                        onTestAndSyncServer = viewModel::testAndSyncOnlineServer,
                        onUpdateOrderStatus = viewModel::updateOrderStatus,
                        onStepOrderDriver = viewModel::stepDriverForwardOnMap,
                        onToggleLiveTracking = viewModel::toggleLiveMapTracking,
                        onSaveProduct = viewModel::saveProduct,
                        onDeleteProduct = viewModel::deleteProduct,
                        onAddCategory = viewModel::addCategory,
                        onSaveCommissionConfig = viewModel::saveCommissionConfig,
                        onSettleDriverPayout = viewModel::settleDriverPayout,
                        onSaveCoupon = viewModel::saveCoupon,
                        onDeleteCoupon = viewModel::deleteCoupon
                    )
                }
            }
        }
    }

    if (showNotificationsDialog) {
        NotificationsCenterDialog(
            userFullName = loggedInUser.fullName,
            notifications = userNotifications,
            onDismiss = { showNotificationsDialog = false }
        )
    }

    if (showServerDialog) {
        ServerOnlineConfigDialog(
            config = serverConfig,
            onDismiss = { showServerDialog = false },
            onSaveConfig = { updated ->
                viewModel.saveServerConfig(updated)
                showServerDialog = false
            },
            onTestAndSync = viewModel::testAndSyncOnlineServer
        )
    }
}

@Composable
private fun NotificationsCenterDialog(
    userFullName: String,
    notifications: List<NotificationEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🔔 إشعاراتك الخاصة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الحساب: $userFullName",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد إشعارات مسجلة لهذا الحساب حالياً",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notifications, key = { it.id }) { notif ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = notif.timeFormatted,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = notif.message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
