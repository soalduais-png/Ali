package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.CategoryEntity
import com.example.data.CommissionConfigEntity
import com.example.data.CouponEntity
import com.example.data.CustomerAddressEntity
import com.example.data.NeighborhoodOption
import com.example.data.OrderEntity
import com.example.data.OrderStatus
import com.example.data.PaymentGatewayEntity
import com.example.data.ProductEntity
import com.example.data.RIYADH_NEIGHBORHOODS
import com.example.ui.CartItem
import com.example.ui.components.ProductVisualImage
import com.example.ui.components.SingleOrderHungerStationMap

enum class CustomerSubTab(val titleAr: String) {
    MENU("المنيو"),
    CART("السلة والدفع"),
    ADDRESSES("إدارة العناوين"),
    TRACKING("تتبع الطلب")
}

@Composable
fun productDrawableRes(imageKey: String): Int {
    return when (imageKey) {
        "pizza" -> R.drawable.img_pizza_feast
        "hero" -> R.drawable.img_hero_banner
        else -> R.drawable.img_burger_meal
    }
}

@Composable
fun CustomerScreen(
    currentCustomerName: String,
    customerAddresses: List<CustomerAddressEntity>,
    selectedAddressId: Long?,
    paymentGateways: List<PaymentGatewayEntity>,
    selectedPaymentGatewayCode: String,
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    coupons: List<CouponEntity>,
    orders: List<OrderEntity>,
    commissionConfig: CommissionConfigEntity,
    selectedCategoryId: Long?,
    searchQuery: String,
    cartItems: List<CartItem>,
    appliedCoupon: CouponEntity?,
    couponMessage: String?,
    selectedNeighborhood: NeighborhoodOption,
    customerNotes: String,
    trackedOrderId: Long?,
    isLiveTrackingEnabled: Boolean,
    onSelectCategory: (Long?) -> Unit,
    onSearchChange: (String) -> Unit,
    onAddToCart: (ProductEntity, Boolean, List<Pair<String, Double>>, Int) -> Unit,
    onUpdateCartQuantity: (String, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onSelectAddress: (CustomerAddressEntity) -> Unit,
    onSaveAddress: (Long, String, NeighborhoodOption, String, String, Boolean) -> Unit,
    onSetDefaultAddress: (CustomerAddressEntity) -> Unit,
    onDeleteAddress: (CustomerAddressEntity) -> Unit,
    onSelectPaymentGatewayCode: (String) -> Unit,
    onCustomerNotesChange: (String) -> Unit,
    onSubmitOrder: (CustomerAddressEntity?, PaymentGatewayEntity?, String, (Long) -> Unit) -> Unit,
    onSelectTrackedOrder: (Long) -> Unit,
    onToggleLiveTracking: () -> Unit,
    onStepDriverForward: (OrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(CustomerSubTab.MENU) }
    var customizingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var editingAddress by remember { mutableStateOf<CustomerAddressEntity?>(null) }
    var showAddAddressDialog by remember { mutableStateOf(false) }

    if (activeTab != CustomerSubTab.MENU) {
        BackHandler {
            activeTab = CustomerSubTab.MENU
        }
    }

    val totalCartCount = cartItems.sumOf { it.quantity }
    val activeAddress = customerAddresses.firstOrNull { it.id == selectedAddressId }
        ?: customerAddresses.firstOrNull { it.isDefault }
        ?: customerAddresses.firstOrNull()

    Column(modifier = modifier.fillMaxSize()) {
        // Customer Sub-navigation bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CustomerSubTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activeTab = tab }
                            .testTag("customer_tab_${tab.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tab.titleAr,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            if (tab == CustomerSubTab.CART && totalCartCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Badge(
                                    containerColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                                ) {
                                    Text("$totalCartCount")
                                }
                            }
                        }
                    }
                }
            }
        }

        when (activeTab) {
            CustomerSubTab.MENU -> {
                CustomerMenuContent(
                    customerName = currentCustomerName,
                    activeAddress = activeAddress,
                    categories = categories,
                    products = products,
                    coupons = coupons.filter { it.isActive },
                    orders = orders,
                    selectedCategoryId = selectedCategoryId,
                    searchQuery = searchQuery,
                    cartItems = cartItems,
                    onSelectCategory = onSelectCategory,
                    onSearchChange = onSearchChange,
                    onOpenProductCustomization = { customizingProduct = it },
                    onManageAddressesClick = { activeTab = CustomerSubTab.ADDRESSES },
                    onGoToCart = { activeTab = CustomerSubTab.CART },
                    onGoToTracking = { orderId ->
                        onSelectTrackedOrder(orderId)
                        activeTab = CustomerSubTab.TRACKING
                    }
                )
            }

            CustomerSubTab.CART -> {
                CustomerCartContent(
                    cartItems = cartItems,
                    customerAddresses = customerAddresses,
                    activeAddress = activeAddress,
                    paymentGateways = paymentGateways.filter { it.isEnabled },
                    selectedPaymentGatewayCode = selectedPaymentGatewayCode,
                    coupons = coupons.filter { it.isActive },
                    appliedCoupon = appliedCoupon,
                    couponMessage = couponMessage,
                    selectedNeighborhood = selectedNeighborhood,
                    customerNotes = customerNotes,
                    commissionConfig = commissionConfig,
                    onUpdateCartQuantity = onUpdateCartQuantity,
                    onApplyCoupon = onApplyCoupon,
                    onSelectAddress = onSelectAddress,
                    onOpenAddAddress = { showAddAddressDialog = true },
                    onManageAddressesTab = { activeTab = CustomerSubTab.ADDRESSES },
                    onSelectPaymentGatewayCode = onSelectPaymentGatewayCode,
                    onCustomerNotesChange = onCustomerNotesChange,
                    onSubmitOrder = { chosenAddr, chosenGw, txRef ->
                        onSubmitOrder(chosenAddr, chosenGw, txRef) { newOrderId ->
                            onSelectTrackedOrder(newOrderId)
                            activeTab = CustomerSubTab.TRACKING
                        }
                    },
                    onBackToMenu = { activeTab = CustomerSubTab.MENU }
                )
            }

            CustomerSubTab.ADDRESSES -> {
                CustomerAddressesManagementContent(
                    addresses = customerAddresses,
                    selectedAddressId = activeAddress?.id,
                    onAddNewAddress = { showAddAddressDialog = true },
                    onEditAddress = { editingAddress = it },
                    onSelectAndSetDefault = { addr ->
                        onSetDefaultAddress(addr)
                        onSelectAddress(addr)
                    },
                    onDeleteAddress = onDeleteAddress
                )
            }

            CustomerSubTab.TRACKING -> {
                CustomerTrackingContent(
                    orders = orders,
                    trackedOrderId = trackedOrderId,
                    isLiveTrackingEnabled = isLiveTrackingEnabled,
                    onSelectTrackedOrder = onSelectTrackedOrder,
                    onToggleLiveTracking = onToggleLiveTracking,
                    onStepDriverForward = onStepDriverForward,
                    onCloseTrackingScreen = { activeTab = CustomerSubTab.MENU }
                )
            }
        }
    }

    customizingProduct?.let { product ->
        ProductCustomizationDialog(
            product = product,
            onDismiss = { customizingProduct = null },
            onConfirmAdd = { isDouble, addons, qty ->
                onAddToCart(product, isDouble, addons, qty)
                customizingProduct = null
            }
        )
    }

    if (showAddAddressDialog || editingAddress != null) {
        CustomerAddressEditorDialog(
            initial = editingAddress,
            onDismiss = {
                showAddAddressDialog = false
                editingAddress = null
            },
            onSave = { id, label, neighborhood, street, notes, isDefault ->
                onSaveAddress(id, label, neighborhood, street, notes, isDefault)
                showAddAddressDialog = false
                editingAddress = null
            }
        )
    }
}

@Composable
private fun CustomerMenuContent(
    customerName: String,
    activeAddress: CustomerAddressEntity?,
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    coupons: List<CouponEntity>,
    orders: List<OrderEntity>,
    selectedCategoryId: Long?,
    searchQuery: String,
    cartItems: List<CartItem>,
    onSelectCategory: (Long?) -> Unit,
    onSearchChange: (String) -> Unit,
    onOpenProductCustomization: (ProductEntity) -> Unit,
    onManageAddressesClick: () -> Unit,
    onGoToCart: () -> Unit,
    onGoToTracking: (Long) -> Unit
) {
    val activeLiveOrder = orders.firstOrNull {
        it.statusEnum == OrderStatus.ON_THE_WAY ||
            it.statusEnum == OrderStatus.PICKED_UP ||
            it.statusEnum == OrderStatus.PREPARING ||
            it.statusEnum == OrderStatus.NEW
    }

    val filteredProducts = products.filter { p ->
        val matchesCat = selectedCategoryId == null || p.categoryId == selectedCategoryId
        val matchesSearch = searchQuery.isBlank() ||
            p.name.contains(searchQuery, ignoreCase = true) ||
            p.description.contains(searchQuery, ignoreCase = true)
        matchesCat && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Active Delivery Address Quick Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onManageAddressesClick() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (activeAddress != null) {
                                        "التوصيل إلى: ${activeAddress.label} (${activeAddress.neighborhoodName.substringBefore(" -")})"
                                    } else {
                                        "لم يتم تحديد عنوان التوصيل بعد"
                                    },
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = activeAddress?.streetAndBuilding ?: "اضغط لإضافة أو تعديل عناوين التوصيل الخاصة بك",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = "تغيير / إدارة ←",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Hero Restaurant Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner),
                            contentDescription = "صورة المطعم",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.25f),
                                            Color.Black.copy(alpha = 0.82f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "⭐ 4.9 • دفع إلكتروني آمن (مدى / Apple Pay / STC Pay)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF1C1917),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "مرحباً بك يا $customerName 👋",
                                style = MaterialTheme.typography.headlineSmall,
                                color = Color.White
                            )
                            Text(
                                text = "برجر مشوي على اللهب • بروستد ذهبي • بيتزا بالحطب",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFE5E7EB)
                            )
                        }
                    }
                }
            }

            // Live Active Order Banner
            if (activeLiveOrder != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onGoToTracking(activeLiveOrder.id) }
                            .testTag("active_order_live_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = Color.White
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "طلبك #${activeLiveOrder.orderNumber} (${activeLiveOrder.statusEnum.titleAr})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "اضغط لفتح خريطة تتبع المندوب التفاعلية المباشرة 🛵",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                    )
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "تتبع الخريطة",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("customer_search_input"),
                    placeholder = { Text("🔍 ابحث عن وجبتك المفضلة (برجر، وجبة، بيتزا...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Close, contentDescription = "مسح البحث")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }

            // Active Promotional Offers Carousel
            if (coupons.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = "🔥 العروض والكوبونات الفعالة",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(coupons, key = { it.id }) { coupon ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.width(240.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalOffer,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "🔥 ${coupon.titleAr}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                text = "كود: ${coupon.code} • للطلبات فوق ${coupon.minimumOrder.toInt()} ر.س",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Categories Row
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "الأقسام",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { onSelectCategory(null) },
                                label = { Text("✨ الكل (${products.size})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        items(categories, key = { it.id }) { category ->
                            FilterChip(
                                selected = selectedCategoryId == category.id,
                                onClick = { onSelectCategory(category.id) },
                                label = { Text("${category.emoji} ${category.name}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Products List
            items(filteredProducts, key = { it.id }) { product ->
                ProductMenuItemCard(
                    product = product,
                    onClick = {
                        if (product.isAvailable) {
                            onOpenProductCustomization(product)
                        }
                    }
                )
            }
        }

        // Floating Cart Summary Bar
        if (cartItems.isNotEmpty()) {
            val cartSubtotal = cartItems.sumOf { it.lineTotal }
            val count = cartItems.sumOf { it.quantity }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(14.dp)
                    .clickable { onGoToCart() }
                    .testTag("floating_cart_bar"),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary) {
                                    Text("$count")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "عرض السلة والدفع الإلكتروني",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                            Text(
                                text = "${cartItems.size} أصناف في السلة",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Text(
                        text = "${cartSubtotal.toInt()} ر.س ←",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerAddressesManagementContent(
    addresses: List<CustomerAddressEntity>,
    selectedAddressId: Long?,
    onAddNewAddress: () -> Unit,
    onEditAddress: (CustomerAddressEntity) -> Unit,
    onSelectAndSetDefault: (CustomerAddressEntity) -> Unit,
    onDeleteAddress: (CustomerAddressEntity) -> Unit
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
                    Text(
                        text = "إدارة عناوين التوصيل الخاصة بك",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "أضف عناوين متعددة (المنزل، العمل، الاستراحة) أو عدّل عنوانك الحالي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = onAddNewAddress,
                    modifier = Modifier.testTag("add_new_address_btn")
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("إضافة عنوان")
                }
            }
        }

        if (addresses.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد عناوين محفوظة حالياً", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddNewAddress) {
                            Text("إضافة عنوان توصيل جديد")
                        }
                    }
                }
            }
        } else {
            items(addresses, key = { it.id }) { addr ->
                val isSelected = addr.id == selectedAddressId || addr.isDefault
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${addr.label} • ${addr.neighborhoodName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (addr.isDefault) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF059669)
                                ) {
                                    Text(
                                        text = "العنوان الافتراضي ✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "العنوان التفصيلي: ${addr.streetAndBuilding}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (addr.landmarkNotes.isNotBlank()) {
                            Text(
                                text = "إرشادات الموقع: ${addr.landmarkNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "المسافة عن المطعم: ${addr.distanceKm} كم",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!addr.isDefault) {
                                FilledTonalButton(onClick = { onSelectAndSetDefault(addr) }) {
                                    Text("تعيين كعنوان افتراضي للتوصيل")
                                }
                            } else {
                                Text(
                                    text = "محدد حالياً لتوصيل الطلبات",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF059669)
                                )
                            }

                            Row {
                                IconButton(onClick = { onEditAddress(addr) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل العنوان")
                                }
                                if (addresses.size > 1) {
                                    IconButton(onClick = { onDeleteAddress(addr) }) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "حذف العنوان",
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerAddressEditorDialog(
    initial: CustomerAddressEntity?,
    onDismiss: () -> Unit,
    onSave: (Long, String, NeighborhoodOption, String, String, Boolean) -> Unit
) {
    var label by remember { mutableStateOf(initial?.label ?: "المنزل") }
    var selectedNeighborhood by remember {
        mutableStateOf(
            RIYADH_NEIGHBORHOODS.firstOrNull { it.nameAr == initial?.neighborhoodName }
                ?: RIYADH_NEIGHBORHOODS.first()
        )
    }
    var street by remember { mutableStateOf(initial?.streetAndBuilding ?: "") }
    var notes by remember { mutableStateOf(initial?.landmarkNotes ?: "") }
    var isDefault by remember { mutableStateOf(initial?.isDefault ?: true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (initial == null) "إضافة عنوان توصيل جديد" else "تعديل عنوان التوصيل",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text("تسمية العنوان:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("المنزل", "العمل", "الاستراحة", "أخرى").forEach { lbl ->
                        FilterChip(
                            selected = label == lbl,
                            onClick = { label = lbl },
                            label = { Text(lbl) }
                        )
                    }
                }

                Text("اختر الحي السكني:", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RIYADH_NEIGHBORHOODS.forEach { n ->
                        FilterChip(
                            selected = selectedNeighborhood.nameAr == n.nameAr,
                            onClick = { selectedNeighborhood = n },
                            label = { Text("${n.nameAr.substringBefore(" -")} (${n.distanceKm} كم)") }
                        )
                    }
                }

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("اسم الشارع ورقم المبنى / الفيلا") },
                    placeholder = { Text("مثال: شارع الأمير سلطان، فيلا 22") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("معلم قريب أو إرشادات للمندوب") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isDefault, onCheckedChange = { isDefault = it })
                    Text("تعيين كعنوان التوصيل الافتراضي")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (street.isNotBlank()) {
                                onSave(
                                    initial?.id ?: 0L,
                                    label,
                                    selectedNeighborhood,
                                    street.trim(),
                                    notes.trim(),
                                    isDefault
                                )
                            }
                        }
                    ) {
                        Text("حفظ العنوان")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductMenuItemCard(
    product: ProductEntity,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = product.isAvailable) { onClick() }
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductVisualImage(
                imageKey = product.imageKey,
                contentDescription = product.name,
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!product.isAvailable) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "متوقف مؤقتاً",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${product.basePrice.toInt()} ريال",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    if (product.isAvailable) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "إضافة",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تخصيص وإضافة",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
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
private fun ProductCustomizationDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirmAdd: (Boolean, List<Pair<String, Double>>, Int) -> Unit
) {
    var isDoubleSize by remember { mutableStateOf(false) }
    var quantity by remember { mutableIntStateOf(1) }

    val parsedAddons = remember(product.availableAddons) {
        product.availableAddons.split(",")
            .mapNotNull { token ->
                val parts = token.split(":")
                if (parts.size == 2) {
                    val name = parts[0].trim()
                    val price = parts[1].trim().toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty()) name to price else null
                } else null
            }
    }
    val selectedAddons = remember { mutableStateListOf<Pair<String, Double>>() }

    val unitTotal = product.basePrice +
        (if (isDoubleSize && product.hasSizes) product.doubleSizeExtra else 0.0) +
        selectedAddons.sumOf { it.second }
    val grandTotal = unitTotal * quantity

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (product.hasSizes) {
                    Text(
                        text = "الحجم:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDoubleSize = false }
                    ) {
                        RadioButton(selected = !isDoubleSize, onClick = { isDoubleSize = false })
                        Text("عادي (${product.basePrice.toInt()} ريال)", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isDoubleSize = true }
                    ) {
                        RadioButton(selected = isDoubleSize, onClick = { isDoubleSize = true })
                        Text(
                            "دبل +${product.doubleSizeExtra.toInt()} ريال",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (parsedAddons.isNotEmpty()) {
                    Text(
                        text = "الإضافات الاختيارية:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    parsedAddons.forEach { addon ->
                        val isChecked = selectedAddons.any { it.first == addon.first }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isChecked) {
                                        selectedAddons.removeAll { it.first == addon.first }
                                    } else {
                                        selectedAddons.add(addon)
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedAddons.add(addon)
                                    } else {
                                        selectedAddons.removeAll { it.first == addon.first }
                                    }
                                }
                            )
                            Text(
                                text = "${addon.first} +${addon.second.toInt()} ريال",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("الكمية:", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "إنقاص")
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        OutlinedButton(
                            onClick = { quantity++ },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onConfirmAdd(isDoubleSize, selectedAddons.toList(), quantity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("confirm_add_to_cart_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة إلى السلة • ${grandTotal.toInt()} ريال",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerCartContent(
    cartItems: List<CartItem>,
    customerAddresses: List<CustomerAddressEntity>,
    activeAddress: CustomerAddressEntity?,
    paymentGateways: List<PaymentGatewayEntity>,
    selectedPaymentGatewayCode: String,
    coupons: List<CouponEntity>,
    appliedCoupon: CouponEntity?,
    couponMessage: String?,
    selectedNeighborhood: NeighborhoodOption,
    customerNotes: String,
    commissionConfig: CommissionConfigEntity,
    onUpdateCartQuantity: (String, Int) -> Unit,
    onApplyCoupon: (String) -> Unit,
    onSelectAddress: (CustomerAddressEntity) -> Unit,
    onOpenAddAddress: () -> Unit,
    onManageAddressesTab: () -> Unit,
    onSelectPaymentGatewayCode: (String) -> Unit,
    onCustomerNotesChange: (String) -> Unit,
    onSubmitOrder: (CustomerAddressEntity?, PaymentGatewayEntity?, String) -> Unit,
    onBackToMenu: () -> Unit
) {
    var couponInput by remember(appliedCoupon) { mutableStateOf(appliedCoupon?.code ?: "") }
    var showElectronicPaymentSheet by remember { mutableStateOf(false) }

    if (cartItems.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("سلة المشتريات فارغة حالياً", style = MaterialTheme.typography.headlineSmall)
            Text(
                "اختر وجبتك المفضلة من قائمة المطعم أو أدر عناوين التوصيل الخاصة بك.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(18.dp))
            Button(onClick = onBackToMenu) {
                Text("تصفح قائمة الطعام")
            }
        }
        return
    }

    val distanceKm = activeAddress?.distanceKm ?: selectedNeighborhood.distanceKm
    val subtotal = cartItems.sumOf { it.lineTotal }
    val deliveryFee = commissionConfig.calculateDeliveryFee(distanceKm)
    val discount = if (appliedCoupon != null && subtotal >= appliedCoupon.minimumOrder) {
        if (appliedCoupon.isPercentage) {
            (subtotal * (appliedCoupon.discountValue / 100.0)).coerceAtMost(appliedCoupon.maxDiscount)
        } else {
            appliedCoupon.discountValue.coerceAtMost(appliedCoupon.maxDiscount)
        }
    } else 0.0
    val grandTotal = (subtotal + deliveryFee - discount).coerceAtLeast(0.0)
    val calculatedDriverCommission = commissionConfig.calculateCommission(subtotal, distanceKm)

    val availableGateways = remember(paymentGateways) {
        val enabled = paymentGateways.filter { it.isEnabled }
        enabled.ifEmpty {
            listOf(
                PaymentGatewayEntity(
                    id = 1,
                    code = "MADA_CARD",
                    nameAr = "بطاقة مدى البنكية (Mada)",
                    providerName = "Moyasar / PayTabs",
                    isElectronic = true,
                    isEnabled = true,
                    isLiveMode = true,
                    merchantId = "MERCH-MADA-90812"
                ),
                PaymentGatewayEntity(
                    id = 2,
                    code = "APPLE_PAY",
                    nameAr = "Apple Pay (دفع سريع بالبصمة)",
                    providerName = "Apple Pay Merchant",
                    isElectronic = true,
                    isEnabled = true,
                    isLiveMode = true,
                    merchantId = "merchant.sa.goldenember.pay"
                ),
                PaymentGatewayEntity(
                    id = 3,
                    code = "STC_PAY",
                    nameAr = "محفظة STC Pay الرقمية",
                    providerName = "STC Pay Direct",
                    isElectronic = true,
                    isEnabled = true,
                    isLiveMode = true,
                    merchantId = "STCPAY-SA-44210"
                ),
                PaymentGatewayEntity(
                    id = 4,
                    code = "VISA_MASTER",
                    nameAr = "بطاقة ائتمانية (Visa / MasterCard)",
                    providerName = "HyperPay 3DS2",
                    isElectronic = true,
                    isEnabled = true,
                    isLiveMode = true,
                    merchantId = "HYPER-CC-77531"
                ),
                PaymentGatewayEntity(
                    id = 5,
                    code = "COD",
                    nameAr = "الدفع نقداً عند الاستلام (كاش للمندوب)",
                    providerName = "تحصيل مباشر عبر المندوب",
                    isElectronic = false,
                    isEnabled = true,
                    isLiveMode = true,
                    merchantId = "CASH-DIRECT"
                )
            )
        }
    }

    val selectedGateway = availableGateways.firstOrNull { it.code == selectedPaymentGatewayCode }
        ?: availableGateways.first()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("مراجعة السلة والدفع الإلكتروني", style = MaterialTheme.typography.headlineSmall)
        }

        items(cartItems, key = { it.id }) { item ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.product.name, style = MaterialTheme.typography.titleMedium)
                        if (item.product.hasSizes) {
                            Text(
                                text = if (item.isDoubleSize) "الحجم: دبل (+${item.product.doubleSizeExtra.toInt()} ر.س)" else "الحجم: عادي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.selectedAddons.isNotEmpty()) {
                            Text(
                                text = "الإضافات: " + item.selectedAddons.joinToString("، ") { "${it.first} (+${it.second.toInt()})" },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "${item.lineTotal.toInt()} ريال",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onUpdateCartQuantity(item.id, item.quantity - 1) }) {
                            Icon(
                                imageVector = if (item.quantity == 1) Icons.Default.DeleteOutline else Icons.Default.Remove,
                                contentDescription = "إنقاص"
                            )
                        }
                        Text("${item.quantity}", style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { onUpdateCartQuantity(item.id, item.quantity + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة")
                        }
                    }
                }
            }
        }

        // Customer Saved Delivery Addresses Picker + Add/Edit Address
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("عنوان توصيل العميل:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = onOpenAddAddress,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("عنوان جديد", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = onManageAddressesTab,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("إدارة العناوين", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    customerAddresses.forEach { addr ->
                        val selected = activeAddress?.id == addr.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectAddress(addr) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selected, onClick = { onSelectAddress(addr) })
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${addr.label} • ${addr.neighborhoodName} (${addr.distanceKm} كم)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = addr.streetAndBuilding,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Electronic Payment Gateways Selection (linked dynamically to Admin settings!)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "خيارات الدفع الإلكتروني والنقدي (جاهزة وتعمل أونلاين):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    availableGateways.forEach { gw ->
                        val selected = selectedGateway.code == gw.code
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectPaymentGatewayCode(gw.code) }
                                .testTag("payment_gateway_option_${gw.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    RadioButton(
                                        selected = selected,
                                        onClick = { onSelectPaymentGatewayCode(gw.code) }
                                    )
                                    Column {
                                        Text(
                                            text = gw.nameAr,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${gw.providerName} • ${if (gw.isElectronic) "دفع إلكتروني فوري آمن 🔒" else "تحصيل نقدي عند التسليم"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF059669)
                                ) {
                                    Text(
                                        text = if (gw.isElectronic) {
                                            if (gw.isLiveMode) "متصل Live ✓" else "Sandbox ✓"
                                        } else {
                                            "متاح ✓"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customerNotes,
                        onValueChange = onCustomerNotesChange,
                        label = { Text("ملاحظات للمطعم أو المندوب (اختياري)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Coupon Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("كود الخصم:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = couponInput,
                            onValueChange = { couponInput = it },
                            placeholder = { Text("مثال: FIRST20 أو DAY10") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(onClick = { onApplyCoupon(couponInput) }) {
                            Text("تطبيق")
                        }
                    }
                    if (couponMessage != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = couponMessage,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (appliedCoupon != null) Color(0xFF059669) else MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        coupons.forEach { c ->
                            FilterChip(
                                selected = appliedCoupon?.code == c.code,
                                onClick = {
                                    couponInput = c.code
                                    onApplyCoupon(c.code)
                                },
                                label = { Text("${c.code} (${c.titleAr})") }
                            )
                        }
                    }
                }
            }
        }

        // Financial Invoice Summary
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("ملخص الفاتورة والحساب المالي الآلي", style = MaterialTheme.typography.titleMedium)
                    HorizontalDivider()
                    InvoiceRow("المجموع الفرعي للمنتجات", "${subtotal.toInt()} ريال")
                    InvoiceRow("رسوم التوصيل ($distanceKm كم)", "${deliveryFee.toInt()} ريال")
                    if (discount > 0) {
                        InvoiceRow(
                            "الخصم (${appliedCoupon?.code ?: ""})",
                            "-${discount.toInt()} ريال",
                            valueColor = Color(0xFF059669)
                        )
                    }
                    HorizontalDivider()
                    InvoiceRow(
                        label = "الإجمالي المستحق للدفع",
                        value = "${grandTotal.toInt()} ريال",
                        isBold = true,
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• عمولة المندوب المحسوبة آلياً: ${calculatedDriverCommission.toInt()} ر.س | صافي المطعم: ${(grandTotal - calculatedDriverCommission).toInt()} ر.س",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (selectedGateway.isElectronic) {
                        showElectronicPaymentSheet = true
                    } else {
                        onSubmitOrder(
                            activeAddress,
                            selectedGateway,
                            "COD-${(100000..999999).random()}"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_customer_order_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = if (selectedGateway.isElectronic) Icons.Default.Lock else Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedGateway.isElectronic) {
                        "الدفع عبر ${selectedGateway.nameAr.substringBefore(" (")} (${grandTotal.toInt()} ريال)"
                    } else {
                        "تأكيد الطلب (الدفع عند الاستلام ${grandTotal.toInt()} ريال)"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }

    if (showElectronicPaymentSheet) {
        ElectronicPaymentAuthorizationDialog(
            gateway = selectedGateway,
            amountSar = grandTotal,
            onDismiss = { showElectronicPaymentSheet = false },
            onPaymentAuthorized = { txRef ->
                showElectronicPaymentSheet = false
                onSubmitOrder(activeAddress, selectedGateway, txRef)
            }
        )
    }
}

@Composable
private fun ElectronicPaymentAuthorizationDialog(
    gateway: PaymentGatewayEntity,
    amountSar: Double,
    onDismiss: () -> Unit,
    onPaymentAuthorized: (String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("4455 8210 9042 8821") }
    var cardHolder by remember { mutableStateOf("MOHAMMED ALFAISAL") }
    var cardExpiry by remember { mutableStateOf("09/28") }
    var cardCvv by remember { mutableStateOf("842") }
    var stcPhone by remember { mutableStateOf("0559998877") }
    var stcOtp by remember { mutableStateOf("4920") }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF059669))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بوابة الدفع الإلكتروني الآمنة أونلاين",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = gateway.nameAr,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "مزود الربط: ${gateway.providerName} • التاجر: ${gateway.merchantId}",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "المبلغ المستحق: ${amountSar.toInt()} ريال سعودي (متصل أونلاين 🟢)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (validationError != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = validationError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                when (gateway.code) {
                    "APPLE_PAY" -> {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF18181B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(" Pay", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "بطاقة مدى - مصرف الراجحي (•••• 4910) جاهزة للمصادقة الفورية",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD4D4D8)
                                )
                            }
                        }
                    }
                    "STC_PAY" -> {
                        OutlinedTextField(
                            value = stcPhone,
                            onValueChange = {
                                validationError = null
                                stcPhone = it
                            },
                            label = { Text("رقم جوال محفظة STC Pay") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = stcOtp,
                            onValueChange = {
                                validationError = null
                                stcOtp = it
                            },
                            label = { Text("رمز التحقق الفوري (OTP)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        OutlinedTextField(
                            value = cardNumber,
                            onValueChange = {
                                validationError = null
                                cardNumber = it
                            },
                            label = {
                                Text(
                                    if (gateway.code == "MADA_CARD") "رقم بطاقة مدى البنكية" else "رقم البطاقة الائتمانية (Visa / MasterCard)"
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = cardHolder,
                            onValueChange = {
                                validationError = null
                                cardHolder = it
                            },
                            label = { Text("اسم حامل البطاقة") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardExpiry,
                                onValueChange = {
                                    validationError = null
                                    cardExpiry = it
                                },
                                label = { Text("تاريخ الانتهاء") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = cardCvv,
                                onValueChange = {
                                    validationError = null
                                    cardCvv = it
                                },
                                label = { Text("رمز CVV") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        when (gateway.code) {
                            "STC_PAY" -> {
                                if (stcPhone.isBlank() || stcOtp.isBlank()) {
                                    validationError = "الرجاء إدخال رقم جوال STC Pay ورمز التحقق"
                                    return@Button
                                }
                            }
                            "MADA_CARD", "VISA_MASTER" -> {
                                if (cardNumber.isBlank() || cardCvv.isBlank()) {
                                    validationError = "الرجاء التأكد من بيانات البطاقة ورمز الأمان CVV"
                                    return@Button
                                }
                            }
                        }
                        val ref = "${gateway.code.take(4)}-LIVE-${(100000..999999).random()}"
                        onPaymentAuthorized(ref)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("authorize_electronic_payment_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد وإتمام الدفع الإلكتروني (${amountSar.toInt()} ر.س)")
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = if (isBold) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Composable
private fun CustomerTrackingContent(
    orders: List<OrderEntity>,
    trackedOrderId: Long?,
    isLiveTrackingEnabled: Boolean,
    onSelectTrackedOrder: (Long) -> Unit,
    onToggleLiveTracking: () -> Unit,
    onStepDriverForward: (OrderEntity) -> Unit,
    onCloseTrackingScreen: () -> Unit
) {
    // Only active (not yet delivered or cancelled) orders can be tracked
    val activeOrders = remember(orders) {
        orders.filter {
            it.statusEnum != OrderStatus.DELIVERED && it.statusEnum != OrderStatus.CANCELLED
        }
    }
    val completedOrders = remember(orders) {
        orders.filter {
            it.statusEnum == OrderStatus.DELIVERED || it.statusEnum == OrderStatus.CANCELLED
        }
    }

    val specificallyTrackedOrder = orders.firstOrNull { it.id == trackedOrderId }

    // If the order that was being tracked transitions to DELIVERED or CANCELLED, close tracking screen immediately!
    LaunchedEffect(specificallyTrackedOrder?.status) {
        if (specificallyTrackedOrder != null &&
            (specificallyTrackedOrder.statusEnum == OrderStatus.DELIVERED ||
                specificallyTrackedOrder.statusEnum == OrderStatus.CANCELLED)
        ) {
            onCloseTrackingScreen()
        }
    }

    val activeOrder = activeOrders.firstOrNull { it.id == trackedOrderId }
        ?: activeOrders.firstOrNull()

    if (activeOrder == null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "لا توجد طلبات نشطة قيد التوصيل حالياً",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تم إغلاق شاشة التتبع المباشر وإخفاء خريطة التتبع وزر التواصل مع المندوب لجميع الطلبات المنتهية أو المستلمة.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = onCloseTrackingScreen) {
                            Text("العودة إلى قائمة الطعام")
                        }
                    }
                }
            }

            if (completedOrders.isNotEmpty()) {
                item {
                    Text(
                        text = "سجل الطلبات المنتهية والمستلمة (التتبع والخريطة والتواصل مع المندوب مغلق):",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(completedOrders, key = { it.id }) { doneOrder ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "طلب #${doneOrder.orderNumber}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (doneOrder.statusEnum == OrderStatus.DELIVERED) {
                                        Color(0xFF059669).copy(alpha = 0.15f)
                                    } else {
                                        MaterialTheme.colorScheme.errorContainer
                                    }
                                ) {
                                    Text(
                                        text = doneOrder.statusEnum.titleAr,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (doneOrder.statusEnum == OrderStatus.DELIVERED) {
                                            Color(0xFF059669)
                                        } else {
                                            MaterialTheme.colorScheme.onErrorContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Text(
                                text = doneOrder.itemsSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "الإجمالي: ${doneOrder.totalPaid.toInt()} ر.س • تم إغلاق التتبع والخريطة وبيانات المندوب بعد الاستلام",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "الطلبات النشطة قيد التوصيل فقط (${activeOrders.size}):",
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(activeOrders, key = { it.id }) { ord ->
                    FilterChip(
                        selected = ord.id == activeOrder.id,
                        onClick = { onSelectTrackedOrder(ord.id) },
                        label = {
                            Text("طلب نشط #${ord.orderNumber} (${ord.statusEnum.titleAr})")
                        }
                    )
                }
            }
        }

        // HungerStation-style Interactive Live Map (Strictly shown ONLY for active non-delivered orders, without manual simulation buttons)
        if (activeOrder.statusEnum != OrderStatus.DELIVERED && activeOrder.statusEnum != OrderStatus.CANCELLED) {
            item {
                SingleOrderHungerStationMap(
                    order = activeOrder,
                    isLiveTrackingEnabled = true,
                    onToggleLiveTracking = onToggleLiveTracking,
                    onStepDriverForward = { onStepDriverForward(activeOrder) },
                    showSimulationControls = false
                )
            }
        }

        // Driver Info & Contact Button (Strictly shown ONLY while order is active and NOT delivered)
        if (activeOrder.driverName != null &&
            activeOrder.statusEnum != OrderStatus.DELIVERED &&
            activeOrder.statusEnum != OrderStatus.CANCELLED
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = activeOrder.driverName.take(1),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المندوب: ${activeOrder.driverName}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text("4.9", style = MaterialTheme.typography.labelMedium)
                                }
                                Text(
                                    text = "جوال المندوب: ${activeOrder.driverPhone ?: "0551234567"} • المسافة: ${activeOrder.distanceKm} كم",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF059669),
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("contact_driver_btn_${activeOrder.orderNumber}")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "التواصل مع المندوب",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5-Step Visual Order Progress Stepper
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مراحل حالة الطلب #${activeOrder.orderNumber}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${activeOrder.totalPaid.toInt()} ر.س • ${activeOrder.paymentMethod}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (activeOrder.customerStreetDetails.isNotBlank()) {
                        Text(
                            text = "📍 عنوان التوصيل: ${activeOrder.customerStreetDetails}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (activeOrder.paymentTransactionRef.isNotBlank()) {
                        Text(
                            text = "🔒 مرجع عملية الدفع: ${activeOrder.paymentTransactionRef}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF059669)
                        )
                    }

                    Text(
                        text = activeOrder.itemsSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()

                    val stages = listOf(
                        "تم استلام الطلب" to OrderStatus.NEW.stepIndex,
                        "المطعم يحضر طلبك" to OrderStatus.PREPARING.stepIndex,
                        "الطلب جاهز للتسليم" to OrderStatus.READY.stepIndex,
                        "المندوب استلم الطلب وفي الطريق" to OrderStatus.ON_THE_WAY.stepIndex,
                        "تم التسليم" to OrderStatus.DELIVERED.stepIndex
                    )
                    val currentStep = activeOrder.statusEnum.stepIndex

                    stages.forEach { (title, threshold) ->
                        val isDone = currentStep > threshold || (currentStep == OrderStatus.DELIVERED.stepIndex)
                        val isCurrent = currentStep == threshold ||
                            (threshold == OrderStatus.ON_THE_WAY.stepIndex &&
                                (currentStep == OrderStatus.ASSIGNED_TO_DRIVER.stepIndex ||
                                    currentStep == OrderStatus.PICKED_UP.stepIndex ||
                                    currentStep == OrderStatus.ON_THE_WAY.stepIndex)) ||
                            (threshold == OrderStatus.NEW.stepIndex && currentStep == OrderStatus.ACCEPTED.stepIndex)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = when {
                                    isDone -> Color(0xFF059669)
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                },
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when {
                                    isDone -> "✓ $title"
                                    isCurrent -> "● $title (الحالة الحالية)"
                                    else -> "○ $title"
                                },
                                style = if (isCurrent) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
                                color = when {
                                    isDone -> Color(0xFF059669)
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
