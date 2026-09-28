package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DriverEntity
import com.example.data.OrderEntity
import com.example.data.OrderStatus
import com.example.data.RESTAURANT_MAP_X
import com.example.data.RESTAURANT_MAP_Y
import com.example.data.RIYADH_NEIGHBORHOODS

@Composable
fun SingleOrderHungerStationMap(
    order: OrderEntity,
    isLiveTrackingEnabled: Boolean,
    onToggleLiveTracking: () -> Unit,
    onStepDriverForward: () -> Unit,
    showGoogleMapsDirectionsButton: Boolean = false,
    showSimulationControls: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val animatedProgress by animateFloatAsState(
        targetValue = order.driverRouteProgress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "driverProgress"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_order_tracking_map"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Top HungerStation-style Live Telemetry Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF18181B))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLiveTrackingEnabled) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "خريطة التتبع التفاعلية المباشرة (GPS Live)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                        Text(
                            text = when (order.statusEnum) {
                                OrderStatus.ASSIGNED_TO_DRIVER, OrderStatus.PICKED_UP, OrderStatus.ON_THE_WAY ->
                                    "المندوب ${order.driverName ?: "أحمد"} يتحرك نحو ${order.customerNeighborhood} • ${(animatedProgress * 100).toInt()}%"
                                OrderStatus.DELIVERED ->
                                    "وصل المندوب إلى موقع العميل وتم التسليم بنجاح"
                                else ->
                                    "يبدأ تحرك المندوب على الخريطة فور استلام الطلب من المطعم"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFD4D4D8)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFD84315),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (order.estimatedMinutesRemaining > 0) {
                            "الوصول خلال ${order.estimatedMinutesRemaining} د"
                        } else {
                            "وصل المندوب"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Interactive Map Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(265.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            zoom = (zoom * gestureZoom).coerceIn(0.85f, 2.2f)
                            panOffset = Offset(
                                x = (panOffset.x + pan.x).coerceIn(-220f, 220f),
                                y = (panOffset.y + pan.y).coerceIn(-160f, 160f)
                            )
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRiyadhUrbanBaseMap(
                        textMeasurer = textMeasurer,
                        zoom = zoom,
                        panOffset = panOffset
                    )

                    val restPos = mapCoordToCanvas(RESTAURANT_MAP_X, RESTAURANT_MAP_Y, size, zoom, panOffset)
                    val custPos = mapCoordToCanvas(order.customerMapX, order.customerMapY, size, zoom, panOffset)

                    // Curved control point for realistic road routing
                    val midX = (restPos.x + custPos.x) / 2f
                    val midY = (restPos.y + custPos.y) / 2f - 42f * zoom

                    // Draw full planned route (dashed)
                    val fullRoutePath = Path().apply {
                        moveTo(restPos.x, restPos.y)
                        quadraticTo(midX, midY, custPos.x, custPos.y)
                    }
                    drawPath(
                        path = fullRoutePath,
                        color = Color(0xFF94A3B8),
                        style = Stroke(
                            width = 7f * zoom,
                            cap = StrokeCap.Round,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                        )
                    )

                    // Calculate current driver point on quadratic Bezier curve
                    val t = animatedProgress
                    val oneMinusT = 1f - t
                    val driverX = oneMinusT * oneMinusT * restPos.x +
                        2f * oneMinusT * t * midX +
                        t * t * custPos.x
                    val driverY = oneMinusT * oneMinusT * restPos.y +
                        2f * oneMinusT * t * midY +
                        t * t * custPos.y
                    val driverPos = Offset(driverX, driverY)

                    // Draw active glowing orange/gold route from Driver to Customer
                    val remainingPath = Path().apply {
                        moveTo(driverPos.x, driverPos.y)
                        lineTo(custPos.x, custPos.y)
                    }
                    drawPath(
                        path = remainingPath,
                        color = Color(0xFFFF5722),
                        style = Stroke(width = 8f * zoom, cap = StrokeCap.Round)
                    )

                    // Draw Restaurant Pin
                    drawPinBadge(
                        center = restPos,
                        color = Color(0xFFD84315),
                        label = "المطعم",
                        textMeasurer = textMeasurer
                    )

                    // Draw Customer Pin
                    drawPinBadge(
                        center = custPos,
                        color = Color(0xFF059669),
                        label = "موقع العميل",
                        textMeasurer = textMeasurer
                    )

                    // Draw Live Driver Marker directly on the map for all active orders until arrival
                    val showDriverPin = order.statusEnum != OrderStatus.DELIVERED &&
                        order.statusEnum != OrderStatus.CANCELLED
                    if (showDriverPin) {
                        drawCircle(
                            color = Color(0xFFF59E0B).copy(alpha = pulseAlpha),
                            radius = pulseRadius * zoom,
                            center = driverPos
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 16f * zoom,
                            center = driverPos
                        )
                        drawCircle(
                            color = Color(0xFFEA580C),
                            radius = 13f * zoom,
                            center = driverPos
                        )
                        drawPinLabelTag(
                            center = Offset(driverPos.x, driverPos.y - 28f * zoom),
                            bgColor = Color(0xFF18181B),
                            textColor = Color(0xFFFBBF24),
                            text = "🛵 ${order.driverName ?: "مندوب التوصيل"} (${order.estimatedMinutesRemaining} د)",
                            textMeasurer = textMeasurer
                        )
                    }
                }

                // Floating Zoom & Recenter Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledIconButton(
                        onClick = { zoom = (zoom + 0.2f).coerceAtMost(2.2f) },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xCC18181B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "تكبير الخريطة", modifier = Modifier.size(18.dp))
                    }
                    FilledIconButton(
                        onClick = { zoom = (zoom - 0.2f).coerceAtLeast(0.85f) },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xCC18181B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "تصغير الخريطة", modifier = Modifier.size(18.dp))
                    }
                    FilledIconButton(
                        onClick = {
                            zoom = 1.0f
                            panOffset = Offset.Zero
                        },
                        modifier = Modifier.size(36.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color(0xCC18181B),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "إعادة ضبط الخريطة", modifier = Modifier.size(18.dp))
                    }
                }

                // Bottom overlay Legend
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xD918181B))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MapLegendDot(color = Color(0xFFD84315), text = "المطعم")
                    MapLegendDot(color = Color(0xFFEA580C), text = "المندوب مباشر")
                    MapLegendDot(color = Color(0xFF059669), text = "العميل (${order.distanceKm} كم)")
                }
            }

            if (showSimulationControls) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onToggleLiveTracking,
                        modifier = Modifier.testTag("toggle_live_gps_btn")
                    ) {
                        Icon(
                            imageVector = if (isLiveTrackingEnabled) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLiveTrackingEnabled) "تتبع GPS تلقائي: نشط" else "تفعيل التتبع التلقائي",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    if (order.statusEnum != OrderStatus.DELIVERED && order.statusEnum != OrderStatus.CANCELLED) {
                        FilledTonalButton(
                            onClick = onStepDriverForward,
                            modifier = Modifier.testTag("step_driver_forward_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FastForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تقديم موقع المندوب +20%",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF059669).copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "التتبع الفعلي المباشر لموقع المندوب متصل الآن تلقائياً حتى يصل الطلب إليك",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF059669),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (showGoogleMapsDirectionsButton) {
                Button(
                    onClick = { openGoogleMapsDirections(context, order) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("open_google_maps_nav_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "فتح الاتجاهات للموقع عن طريق خرائط جوجل (Google Maps)",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminFleetRadarMap(
    drivers: List<DriverEntity>,
    orders: List<OrderEntity>,
    isLiveTrackingEnabled: Boolean,
    onToggleLiveTracking: () -> Unit,
    onStepOrderDriver: (OrderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedDriverId by remember { mutableStateOf<Long?>(drivers.firstOrNull()?.id) }
    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val infiniteTransition = rememberInfiniteTransition(label = "fleetPulse")
    val radarSweepRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweep"
    )

    val activeDeliveryOrders = orders.filter {
        it.statusEnum == OrderStatus.ASSIGNED_TO_DRIVER ||
            it.statusEnum == OrderStatus.PICKED_UP ||
            it.statusEnum == OrderStatus.ON_THE_WAY
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_fleet_radar_map"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "رادار المراقبة الدائمة للأسطول والطلبات (Live Fleet Radar)",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                    Text(
                        text = "المندوبون المتصلون: ${drivers.count { it.isOnline }}/${drivers.size} • التوصيلات الجارية الآن: ${activeDeliveryOrders.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }

                AssistChip(
                    onClick = onToggleLiveTracking,
                    label = {
                        Text(
                            text = if (isLiveTrackingEnabled) "مباشر 🟢" else "متوقف ⏸",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFF1E293B)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(275.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            zoom = (zoom * gestureZoom).coerceIn(0.85f, 2.2f)
                            panOffset = Offset(
                                x = (panOffset.x + pan.x).coerceIn(-220f, 220f),
                                y = (panOffset.y + pan.y).coerceIn(-160f, 160f)
                            )
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRiyadhUrbanBaseMap(
                        textMeasurer = textMeasurer,
                        zoom = zoom,
                        panOffset = panOffset
                    )

                    val restPos = mapCoordToCanvas(RESTAURANT_MAP_X, RESTAURANT_MAP_Y, size, zoom, panOffset)

                    // Restaurant central radar pulse
                    drawCircle(
                        color = Color(0xFFD84315).copy(alpha = 0.18f),
                        radius = radarSweepRadius * zoom,
                        center = restPos
                    )

                    // Draw routes for all active delivery orders
                    activeDeliveryOrders.forEach { ord ->
                        val custPos = mapCoordToCanvas(ord.customerMapX, ord.customerMapY, size, zoom, panOffset)
                        val driverPos = Offset(
                            x = restPos.x + (custPos.x - restPos.x) * ord.driverRouteProgress,
                            y = restPos.y + (custPos.y - restPos.y) * ord.driverRouteProgress
                        )
                        drawLine(
                            color = Color(0xFF94A3B8),
                            start = restPos,
                            end = custPos,
                            strokeWidth = 4f * zoom,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                        drawLine(
                            color = Color(0xFFF97316),
                            start = driverPos,
                            end = custPos,
                            strokeWidth = 6f * zoom,
                            cap = StrokeCap.Round
                        )
                        drawPinBadge(
                            center = custPos,
                            color = Color(0xFF059669),
                            label = "طلب #${ord.orderNumber}",
                            textMeasurer = textMeasurer
                        )
                    }

                    // Draw Restaurant Headquarters
                    drawPinBadge(
                        center = restPos,
                        color = Color(0xFFD84315),
                        label = "مركز المطعم",
                        textMeasurer = textMeasurer
                    )

                    // Draw all Drivers on Fleet Map
                    drivers.forEach { drv ->
                        val pos = mapCoordToCanvas(drv.currentMapX, drv.currentMapY, size, zoom, panOffset)
                        val isSelected = drv.id == selectedDriverId
                        val pinColor = when {
                            !drv.isOnline -> Color(0xFF64748B)
                            isSelected -> Color(0xFFF59E0B)
                            else -> Color(0xFF2563EB)
                        }
                        if (isSelected) {
                            drawCircle(
                                color = Color(0xFFF59E0B).copy(alpha = 0.30f),
                                radius = 26f * zoom,
                                center = pos
                            )
                        }
                        drawCircle(color = Color.White, radius = 14f * zoom, center = pos)
                        drawCircle(color = pinColor, radius = 11f * zoom, center = pos)
                        drawPinLabelTag(
                            center = Offset(pos.x, pos.y - 24f * zoom),
                            bgColor = if (isSelected) Color(0xFF18181B) else Color(0xCC1E293B),
                            textColor = if (isSelected) Color(0xFFFBBF24) else Color.White,
                            text = "🛵 ${drv.name.substringBefore(" ")}",
                            textMeasurer = textMeasurer
                        )
                    }
                }
            }

            // Selectable Driver Chips & Selected Driver Telemetry Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "اختر مندوباً على الرادار لعرض موقعه الحي والطلب المربوط به:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    drivers.forEach { drv ->
                        val selected = drv.id == selectedDriverId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedDriverId = drv.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (drv.isOnline) Color(0xFF10B981) else Color(0xFF9CA3AF))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = drv.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }

                val selectedDriver = drivers.firstOrNull { it.id == selectedDriverId } ?: drivers.firstOrNull()
                if (selectedDriver != null) {
                    val assignedOrder = activeDeliveryOrders.firstOrNull { it.driverId == selectedDriver.id }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
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
                                    text = "${selectedDriver.name} • ⭐ ${selectedDriver.rating} • ${selectedDriver.vehicleInfo}",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = "الموقع الحالي: ${selectedDriver.currentNeighborhood}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (assignedOrder != null) {
                                    Text(
                                        text = "يوصل الآن طلب #${assignedOrder.orderNumber} إلى ${assignedOrder.customerName} (متبقي ${assignedOrder.estimatedMinutesRemaining} د)",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (assignedOrder != null) {
                                FilledTonalButton(
                                    onClick = { onStepOrderDriver(assignedOrder) }
                                ) {
                                    Text("تحريك +20%", style = MaterialTheme.typography.labelSmall)
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
private fun MapLegendDot(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}

private fun mapCoordToCanvas(
    normX: Float,
    normY: Float,
    canvasSize: Size,
    zoom: Float,
    pan: Offset
): Offset {
    val cx = canvasSize.width / 2f
    val cy = canvasSize.height / 2f
    val rawX = normX * canvasSize.width
    val rawY = normY * canvasSize.height
    return Offset(
        x = cx + (rawX - cx) * zoom + pan.x,
        y = cy + (rawY - cy) * zoom + pan.y
    )
}

private fun DrawScope.drawRiyadhUrbanBaseMap(
    textMeasurer: TextMeasurer,
    zoom: Float,
    panOffset: Offset
) {
    // Rich dark-beige urban map aesthetic inspired by HungerStation / Google Maps Navigation
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
        )
    )

    // Green park zones
    val park1 = mapCoordToCanvas(0.32f, 0.30f, size, zoom, panOffset)
    drawRoundRect(
        color = Color(0xFF064E3B).copy(alpha = 0.45f),
        topLeft = Offset(park1.x - 45f * zoom, park1.y - 25f * zoom),
        size = Size(90f * zoom, 50f * zoom),
        cornerRadius = CornerRadius(12f * zoom, 12f * zoom)
    )
    val park2 = mapCoordToCanvas(0.72f, 0.65f, size, zoom, panOffset)
    drawRoundRect(
        color = Color(0xFF064E3B).copy(alpha = 0.40f),
        topLeft = Offset(park2.x - 50f * zoom, park2.y - 30f * zoom),
        size = Size(100f * zoom, 58f * zoom),
        cornerRadius = CornerRadius(14f * zoom, 14f * zoom)
    )

    // Secondary street grid
    val gridSteps = listOf(0.15f, 0.30f, 0.45f, 0.60f, 0.75f, 0.88f)
    gridSteps.forEach { g ->
        val pStartH = mapCoordToCanvas(0f, g, size, zoom, panOffset)
        val pEndH = mapCoordToCanvas(1f, g, size, zoom, panOffset)
        drawLine(
            color = Color(0xFF334155).copy(alpha = 0.55f),
            start = pStartH,
            end = pEndH,
            strokeWidth = 2.5f * zoom
        )
        val pStartV = mapCoordToCanvas(g, 0f, size, zoom, panOffset)
        val pEndV = mapCoordToCanvas(g, 1f, size, zoom, panOffset)
        drawLine(
            color = Color(0xFF334155).copy(alpha = 0.55f),
            start = pStartV,
            end = pEndV,
            strokeWidth = 2.5f * zoom
        )
    }

    // Major Riyadh Arteries (King Fahd Rd & Northern Ring Rd)
    val hwy1Start = mapCoordToCanvas(0.50f, 0.0f, size, zoom, panOffset)
    val hwy1End = mapCoordToCanvas(0.46f, 1.0f, size, zoom, panOffset)
    drawLine(
        color = Color(0xFF475569),
        start = hwy1Start,
        end = hwy1End,
        strokeWidth = 10f * zoom
    )
    val hwy2Start = mapCoordToCanvas(0.0f, 0.48f, size, zoom, panOffset)
    val hwy2End = mapCoordToCanvas(1.0f, 0.44f, size, zoom, panOffset)
    drawLine(
        color = Color(0xFF475569),
        start = hwy2Start,
        end = hwy2End,
        strokeWidth = 10f * zoom
    )

    // Neighborhood labels
    RIYADH_NEIGHBORHOODS.forEach { n ->
        val pos = mapCoordToCanvas(n.mapX, n.mapY, size, zoom, panOffset)
        val shortName = n.nameAr.substringBefore(" -")
        val measured = textMeasurer.measure(
            text = shortName,
            style = TextStyle(
                color = Color(0xFF94A3B8),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        )
        drawText(
            textLayoutResult = measured,
            topLeft = Offset(
                x = (pos.x - measured.size.width / 2f).coerceIn(4f, (size.width - measured.size.width - 4f).coerceAtLeast(4f)),
                y = (pos.y + 14f * zoom).coerceIn(4f, (size.height - measured.size.height - 4f).coerceAtLeast(4f))
            )
        )
    }
}

private fun DrawScope.drawPinBadge(
    center: Offset,
    color: Color,
    label: String,
    textMeasurer: TextMeasurer
) {
    drawCircle(
        color = Color.White,
        radius = 13f,
        center = center
    )
    drawCircle(
        color = color,
        radius = 10f,
        center = center
    )
    drawPinLabelTag(
        center = Offset(center.x, center.y - 22f),
        bgColor = color,
        textColor = Color.White,
        text = label,
        textMeasurer = textMeasurer
    )
}

private fun DrawScope.drawPinLabelTag(
    center: Offset,
    bgColor: Color,
    textColor: Color,
    text: String,
    textMeasurer: TextMeasurer
) {
    val measured = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    )
    val padH = 12f
    val padV = 6f
    val boxW = measured.size.width + padH * 2
    val boxH = measured.size.height + padV * 2
    val left = (center.x - boxW / 2f).coerceIn(4f, (size.width - boxW - 4f).coerceAtLeast(4f))
    val top = (center.y - boxH / 2f).coerceIn(4f, (size.height - boxH - 4f).coerceAtLeast(4f))

    drawRoundRect(
        color = bgColor,
        topLeft = Offset(left, top),
        size = Size(boxW, boxH),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawText(
        textLayoutResult = measured,
        topLeft = Offset(left + padH, top + padV)
    )
}
