package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.DriverEntity
import com.example.data.DriverTransactionEntity
import com.example.data.OrderEntity
import com.example.data.OrderStatus
import com.example.data.RESTAURANT_NAME_AR
import com.example.ui.components.SingleOrderHungerStationMap
import com.example.ui.components.openGoogleMapsDirections

enum class DriverSubTab(val titleAr: String) {
    ORDERS("الطلبات والتوصيل"),
    NAVIGATION("خريطة الملاحة"),
    WALLET("العمولات والأرباح")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DriverScreen(
    drivers: List<DriverEntity>,
    selectedDriverId: Long,
    loggedInUsername: String = "",
    orders: List<OrderEntity>,
    transactions: List<DriverTransactionEntity>,
    isLiveTrackingEnabled: Boolean,
    onToggleDriverOnline: (DriverEntity) -> Unit,
    onUpdateOrderStatus: (OrderEntity, OrderStatus, DriverEntity) -> Unit,
    onStepDriverForward: (OrderEntity) -> Unit,
    onToggleLiveTracking: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(DriverSubTab.ORDERS) }
    var navigatedOrderId by remember { mutableStateOf<Long?>(null) }

    if (activeTab != DriverSubTab.ORDERS) {
        BackHandler { activeTab = DriverSubTab.ORDERS }
    }

    val currentDriver = drivers.firstOrNull { it.id == selectedDriverId } ?: drivers.firstOrNull()
    if (currentDriver == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("جاري تحميل ملف المندوب...")
        }
        return
    }

    // Available orders (READY or ACCEPTED/PREPARING without a driver)
    val availableOrders = orders.filter {
        it.driverId == null &&
            (it.statusEnum == OrderStatus.READY ||
                it.statusEnum == OrderStatus.PREPARING ||
                it.statusEnum == OrderStatus.ACCEPTED ||
                it.statusEnum == OrderStatus.NEW)
    }

    // Active orders assigned to this specific logged-in driver only
    val myActiveOrders = orders.filter {
        it.driverId == currentDriver.id &&
            it.statusEnum != OrderStatus.DELIVERED &&
            it.statusEnum != OrderStatus.CANCELLED
    }

    val myCompletedOrders = orders.filter {
        it.driverId == currentDriver.id && it.statusEnum == OrderStatus.DELIVERED
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Sub-navigation tabs
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DriverSubTab.entries.forEach { tab ->
                    val selected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activeTab = tab }
                            .testTag("driver_tab_${tab.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.titleAr,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Driver Profile Greeting & Switcher Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "مرحباً، ${currentDriver.name} 👋",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Text(
                                    text = "${currentDriver.vehicleInfo} • ⭐ ${currentDriver.rating}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentDriver.isOnline) "متصل 🟢" else "غير متصل",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = currentDriver.isOnline,
                                    onCheckedChange = { onToggleDriverOnline(currentDriver) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = "🔒 حساب المندوب المستقل: ${currentDriver.name} (${loggedInUsername.ifBlank { currentDriver.phone }}) — جلسة خاصة",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // 4 Driver Summary KPI Cards (طلبات اليوم | طلبات مكتملة | قيد التوصيل | الأرباح اليوم)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DriverKpiCard(
                        title = "طلبات اليوم",
                        value = "${currentDriver.totalOrders}",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DriverKpiCard(
                        title = "طلبات مكتملة",
                        value = "${currentDriver.completedOrders}",
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DriverKpiCard(
                        title = "قيد التوصيل",
                        value = "${myActiveOrders.size}",
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    DriverKpiCard(
                        title = "الأرباح الكلية",
                        value = "${currentDriver.totalCommissions.toInt()} ر.س",
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            when (activeTab) {
                DriverSubTab.ORDERS -> {
                    // Active Orders Assigned to Driver
                    if (myActiveOrders.isNotEmpty()) {
                        item {
                            Text(
                                text = "🛵 الطلبات الجارية معك الآن (${myActiveOrders.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(myActiveOrders, key = { "active_${it.id}" }) { order ->
                            DriverOrderActionCard(
                                order = order,
                                driver = currentDriver,
                                isAvailablePool = false,
                                onUpdateOrderStatus = onUpdateOrderStatus,
                                onStepDriverForward = onStepDriverForward,
                                onOpenMap = {
                                    navigatedOrderId = order.id
                                    activeTab = DriverSubTab.NAVIGATION
                                }
                            )
                        }
                    }

                    // Available Orders Pool Ready for Pickup
                    item {
                        Text(
                            text = "📦 الطلبات المتاحة للاستلام والتوصيل (${availableOrders.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    if (availableOrders.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "لا توجد طلبات جديدة بانتظار مندوب حالياً.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(availableOrders, key = { "avail_${it.id}" }) { order ->
                            DriverOrderActionCard(
                                order = order,
                                driver = currentDriver,
                                isAvailablePool = true,
                                onUpdateOrderStatus = onUpdateOrderStatus,
                                onStepDriverForward = onStepDriverForward,
                                onOpenMap = {
                                    navigatedOrderId = order.id
                                    activeTab = DriverSubTab.NAVIGATION
                                }
                            )
                        }
                    }
                }

                DriverSubTab.NAVIGATION -> {
                    val candidateOrders = (myActiveOrders + availableOrders).distinctBy { it.id }
                    val navOrder = candidateOrders.firstOrNull { it.id == navigatedOrderId }
                        ?: myActiveOrders.firstOrNull()
                        ?: availableOrders.firstOrNull()
                    if (navOrder != null) {
                        if (candidateOrders.size > 1) {
                            item {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    candidateOrders.forEach { ord ->
                                        FilterChip(
                                            selected = ord.id == navOrder.id,
                                            onClick = { navigatedOrderId = ord.id },
                                            label = { Text("طلب #${ord.orderNumber}") }
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                text = "ملاحة التوصيل للطلب #${navOrder.orderNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        item {
                            SingleOrderHungerStationMap(
                                order = navOrder,
                                isLiveTrackingEnabled = isLiveTrackingEnabled,
                                onToggleLiveTracking = onToggleLiveTracking,
                                onStepDriverForward = { onStepDriverForward(navOrder) },
                                showGoogleMapsDirectionsButton = true
                            )
                        }
                        item {
                            DriverOrderActionCard(
                                order = navOrder,
                                driver = currentDriver,
                                isAvailablePool = navOrder.driverId == null,
                                onUpdateOrderStatus = onUpdateOrderStatus,
                                onStepDriverForward = onStepDriverForward,
                                onOpenMap = {}
                            )
                        }
                    } else {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = "لا توجد طلبات نشطة حالياً لعرضها على خريطة الملاحة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(18.dp)
                                )
                            }
                        }
                    }
                }

                DriverSubTab.WALLET -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "الملخص المالي للمندوب ${currentDriver.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                HorizontalDivider()
                                InvoiceMetricRow("إجمالي قيمة الطلبات الموصلة", "${currentDriver.totalDeliveryValue.toInt()} ر.س")
                                InvoiceMetricRow("إجمالي العمولات المكتسبة", "${currentDriver.totalCommissions.toInt()} ر.س")
                                InvoiceMetricRow("المبالغ المدفوعة من الإدارة", "${currentDriver.paidCommissions.toInt()} ر.س")
                                InvoiceMetricRow(
                                    "المبالغ المستحقة حالياً (غير مدفوعة)",
                                    "${currentDriver.dueAmount.toInt()} ر.س",
                                    isHighlight = true
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "سجل حركات العمولات والدفعات",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    val myTx = transactions.filter { it.driverId == currentDriver.id }
                    items(myTx, key = { it.id }) { tx ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
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
                                    Text(tx.description, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = "${tx.timeFormatted} • ${if (tx.transactionType == "COMMISSION_EARNED") "عمولة توصيل" else "دفعة تسوية"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "+${tx.amount.toInt()} ر.س",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Bold
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
private fun DriverKpiCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun InvoiceMetricRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun DriverOrderActionCard(
    order: OrderEntity,
    driver: DriverEntity,
    isAvailablePool: Boolean,
    onUpdateOrderStatus: (OrderEntity, OrderStatus, DriverEntity) -> Unit,
    onStepDriverForward: (OrderEntity) -> Unit,
    onOpenMap: () -> Unit
) {
    val context = LocalContext.current

    ElevatedCard(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("driver_order_card_${order.orderNumber}")
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
                Text(
                    text = "طلب #${order.orderNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF059669).copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "عمولة التوصيل: ${order.driverCommission.toInt()} ريال",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF059669),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            if (order.isMandatoryAdminAssignment) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚡ إسناد إجباري من إدارة المطعم — يلزم استلام وتوصيل الطلب للعميل",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF92400E),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Text(
                text = "📍 المسار: $RESTAURANT_NAME_AR ← ${order.customerNeighborhood}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )

            if (order.customerStreetDetails.isNotBlank()) {
                Text(
                    text = "العنوان التفصيلي: ${order.customerStreetDetails}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "العميل: ${order.customerName} (${order.customerPhone}) • المسافة: ${order.distanceKm} كم",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "الأصناف: ${order.itemsSummary}",
                style = MaterialTheme.typography.bodyMedium
            )

            if (order.customerNotes.isNotBlank()) {
                Text(
                    text = "📝 ملاحظة العميل: ${order.customerNotes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "قيمة الطلب: ${order.totalPaid.toInt()} ريال (التوصيل ${order.deliveryFee.toInt()} ر.س)",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "الدفع: ${order.paymentMethod}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Progressive workflow buttons: قبول الطلب -> استلام الطلب -> في الطريق -> تم التسليم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAvailablePool) {
                    Button(
                        onClick = {
                            onUpdateOrderStatus(order, OrderStatus.ASSIGNED_TO_DRIVER, driver)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("driver_accept_btn_${order.orderNumber}")
                    ) {
                        Text("قبول الطلب (#${order.orderNumber})")
                    }
                } else {
                    when (order.statusEnum) {
                        OrderStatus.ASSIGNED_TO_DRIVER -> {
                            Button(
                                onClick = {
                                    onUpdateOrderStatus(order, OrderStatus.PICKED_UP, driver)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("استلام الطلب من المطعم")
                            }
                        }
                        OrderStatus.PICKED_UP -> {
                            Button(
                                onClick = {
                                    onUpdateOrderStatus(order, OrderStatus.ON_THE_WAY, driver)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("في الطريق للعميل (تفعيل التتبع)")
                            }
                        }
                        OrderStatus.ON_THE_WAY -> {
                            FilledTonalButton(
                                onClick = { onStepDriverForward(order) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.FastForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تحريك الموقع")
                            }
                            Button(
                                onClick = {
                                    onUpdateOrderStatus(order, OrderStatus.DELIVERED, driver)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("driver_deliver_btn_${order.orderNumber}")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تم التسليم")
                            }
                        }
                        else -> {
                            Button(
                                onClick = {
                                    onUpdateOrderStatus(order, OrderStatus.PICKED_UP, driver)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("استلام الطلب")
                            }
                        }
                    }
                }

                FilledTonalButton(
                    onClick = { openGoogleMapsDirections(context, order) },
                    modifier = Modifier.testTag("driver_order_directions_btn_${order.orderNumber}")
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "الاتجاهات عبر خرائط جوجل", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("الاتجاهات")
                }

                FilledTonalButton(onClick = onOpenMap) {
                    Icon(Icons.Default.Map, contentDescription = "خريطة الملاحة", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
