package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schema
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppPortal

enum class SrsSection(val titleAr: String) {
    SCREENS_BUTTONS("1. الشاشات والأزرار"),
    LIVE_MAP_TRACKING("2. الخريطة التفاعلية (HungerStation)"),
    DB_SCHEMA("3. قاعدة البيانات والعلاقات"),
    API_ENDPOINTS("4. الـ API والـ WebSockets"),
    ROLES_COMMISSIONS("5. الصلاحيات والعمولات")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SrsBlueprintScreen(
    onJumpToPortal: (AppPortal) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSection by remember { mutableStateOf(SrsSection.SCREENS_BUTTONS) }
    var copiedBanner by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF18181B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "📋 وثيقة تحليل المتطلبات البرمجية الشاملة (SRS)",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تشمل جميع الشاشات والأزرار، صلاحيات المستخدمين، حالات الطلب، قاعدة البيانات والعلاقات، الـ API، ونظام التتبع التفاعلي المباشر للمندوب والعميل والإدارة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD4D4D8)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(buildFullSrsMarkdown()))
                                copiedBanner = true
                            },
                            modifier = Modifier.testTag("copy_srs_doc_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (copiedBanner) "تم نسخ وثيقة SRS بالكامل ✓" else "نسخ وثيقة SRS كاملة")
                        }
                    }
                }
            }
        }

        // Section selector chips
        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SrsSection.entries.forEach { sec ->
                    FilterChip(
                        selected = activeSection == sec,
                        onClick = { activeSection = sec },
                        label = { Text(sec.titleAr) }
                    )
                }
            }
        }

        when (activeSection) {
            SrsSection.SCREENS_BUTTONS -> {
                item {
                    SrsSpecCard(
                        title = "أولاً: تطبيق العميل (Customer App) - الشاشات والأزرار",
                        items = listOf(
                            "1. الشاشة الرئيسية للمطعم: بانر المطعم + شريط بحث فوري [ابحث عن وجبتك] + شريط العروض والخصومات + فلاتر الأقسام (برجر، وجبات، بيتزا، مشروبات، إضافات) + بطاقات المنتجات.",
                            "2. نافذة تخصيص المنتج: راديو اختيار الحجم [عادي / دبل +8 ر.س] + مربعات اختيار الإضافات [جبن +3، صوص +2، بطاطس +5] + أزرار الكمية [-] [+] + زر [إضافة إلى السلة مع السعر اللحظي].",
                            "3. شاشة السلة والدفع: أزرار تعديل الكمية والحذف + اختيار حي العميل وحساب المسافة بالكيلومتر + حقل كود الخصم [تطبيق FIRST20] + أزرار اختيار الدفع (مدى، Apple Pay، كاش) + زر [تأكيد وإرسال الطلب].",
                            "4. شاشة تتبع الطلب والخريطة الحية (مثل هنجرستيشن): شريط حالات الطلب (تم الاستلام ← يحضر ← جاهز ← في الطريق ← تم التسليم) + خريطة تفاعلية حية تعرض مسار المندوب والوقت المتبقي + بطاقة المندوب وزر [اتصال بالمندوب]."
                        ),
                        actionLabel = "تجربة شاشة العميل الآن",
                        onAction = { onJumpToPortal(AppPortal.CUSTOMER) }
                    )
                }
                item {
                    SrsSpecCard(
                        title = "ثانياً: تطبيق مندوب التوصيل (Driver App) - الشاشات والأزرار",
                        items = listOf(
                            "1. لوحة المندوب الرئيسية: مفتاح [متصل / غير متصل] + 4 بطاقات إحصائية (طلبات اليوم، مكتملة، قيد التوصيل، الأرباح).",
                            "2. قائمة الطلبات المتاحة والجارية: تعرض رقم الطلب، مسار (المطعم ← حي العميل)، المسافة، قيمة الطلب، عمولة المندوب، طريقة الدفع، وملاحظات العميل.",
                            "3. أزرار تحول حالة الطلب للمندوب بالترتيب: زر [قبول الطلب] ← زر [استلام الطلب من المطعم] ← زر [في الطريق للعميل (بدء بث GPS)] ← زر [تم التسليم وإغلاق الطلب].",
                            "4. شاشة الملاحة والمحفظة: خريطة توجيه تفاعلية مع المسافة والوقت المتوقع + كشف حساب العمولات والمبالغ المستحقة والمدفوعة."
                        ),
                        actionLabel = "تجربة شاشة المندوب الآن",
                        onAction = { onJumpToPortal(AppPortal.DRIVER) }
                    )
                }
                item {
                    SrsSpecCard(
                        title = "ثالثاً: لوحة تحكم إدارة المطعم (Admin Dashboard) - الشاشات والأزرار",
                        items = listOf(
                            "1. لوحة المعلومات ورادار الأسطول الدائم: 12 مؤشر أداء مباشر + خريطة مراقبة دائمة لجميع المندوبين والطلبات الجارية لحظياً.",
                            "2. إدارة الطلبات: فلترة حسب الحالة + أزرار [قبول وبدء التحضير]، [جاهز للتسليم]، [تعيين مندوب محدد]، [تأكيد التسليم]، [إلغاء الطلب].",
                            "3. إدارة المنتجات والأصناف: زر [إضافة منتج]، [تعديل السعر والوصف]، مفتاح [متاح / إيقاف مؤقت]، [حذف]، وإضافة أقسام جديدة.",
                            "4. إدارة المندوبين ومحرك العمولات: ملفات المندوبين الكاملة + زر [صرف وتسديد المستحقات] + لوحة إعداد قواعد العمولة (نسبة، مبلغ ثابت، شرائح مسافة، نظام مختلط).",
                            "5. إدارة الكوبونات والتقارير المالية: إنشاء كوبون جديد + السجل المالي الآلي + رسم بياني للمبيعات الأسبوعية (السبت - الجمعة)."
                        ),
                        actionLabel = "تجربة لوحة الإدارة الآن",
                        onAction = { onJumpToPortal(AppPortal.ADMIN) }
                    )
                }
            }

            SrsSection.LIVE_MAP_TRACKING -> {
                item {
                    SrsSpecCard(
                        title = "آلية تتبع المندوب التفاعلية (للعميل والإدارة مثل هنجرستيشن)",
                        items = listOf(
                            "• متى يبدأ التتبع للعميل؟ فور تحول حالة الطلب إلى ASSIGNED_TO_DRIVER ثم PICKED_UP و ON_THE_WAY، تفتح قناة WebSocket/SSE خاصة بالطلب تبث إحداثيات المندوب (lat, lng, heading, speed) كل 3 ثوانٍ.",
                            "• ماذا يرى العميل على الخريطة؟ دبوس المطعم + دبوس موقع العميل + خط المسار المنحني + أيقونة دراجة/سيارة المندوب تتحرك بنعومة مع حساب الوقت التقريبي للوصول (ETA) والمسافة المتبقية بالكيلومتر.",
                            "• كيف تراقب الإدارة الأسطول بشكل دائم؟ تحتوي لوحة الإدارة على (Live Fleet Radar Map) تعرض جميع المندوبين المتصلين في مدينة الرياض في نفس الوقت مع ألوان مميزة لحالتهم (متاح، يستلم طلب، في الطريق للعميل) مع إمكانية الضغط على أي مندوب لمعرفة الطلب المربوط به وسرعته وموقعه.",
                            "• معادلة حساب الوقت المتبقي (ETA): المسافة المتبقية × معامل الازدحام المروري + وقت التسليم."
                        ),
                        actionLabel = "فتح خريطة التتبع الحية",
                        onAction = { onJumpToPortal(AppPortal.CUSTOMER) }
                    )
                }
            }

            SrsSection.DB_SCHEMA -> {
                item {
                    SrsSpecCard(
                        title = "مخطط قاعدة البيانات العلائقية (Database Schema & Foreign Keys)",
                        items = listOf(
                            "1. جدول Users (id PK, name, phone UNIQUE, password_hash, role ENUM[SUPER_ADMIN, RESTAURANT_ADMIN, STAFF, DRIVER, CUSTOMER], status).",
                            "2. جدول Customers (id PK, user_id FK->Users.id, saved_addresses JSON, loyalty_points INT).",
                            "3. جدول Drivers (id PK, user_id FK->Users.id, vehicle_info, is_online BOOL, current_lat DOUBLE, current_lng DOUBLE, total_orders INT, completed_orders INT, cancelled_orders INT, total_delivery_value DECIMAL, total_commissions DECIMAL, paid_commissions DECIMAL, rating DOUBLE).",
                            "4. جدول Categories (id PK, name, image_url, sort_order INT).",
                            "5. جدول Products (id PK, category_id FK->Categories.id, name, description, base_price DECIMAL, has_sizes BOOL, double_extra DECIMAL, addons_json TEXT, is_available BOOL).",
                            "6. جدول Orders (id PK, order_number UNIQUE, customer_id FK, driver_id FK NULLABLE, status ENUM[NEW..DELIVERED, CANCELLED], subtotal DECIMAL, discount DECIMAL, delivery_fee DECIMAL, total_paid DECIMAL, driver_commission DECIMAL, restaurant_net DECIMAL, distance_km DOUBLE, customer_lat DOUBLE, customer_lng DOUBLE, route_progress FLOAT, payment_method, created_at TIMESTAMP).",
                            "7. جدول OrderItems (id PK, order_id FK->Orders.id, product_id FK->Products.id, quantity INT, unit_price DECIMAL, selected_size, selected_addons_json).",
                            "8. جدول Coupons (id PK, code UNIQUE, discount_type ENUM[PERCENT, FIXED], discount_value DECIMAL, min_order DECIMAL, max_discount DECIMAL, start_date, end_date, is_active BOOL).",
                            "9. جدول DriverTransactions (id PK, driver_id FK->Drivers.id, order_id FK NULLABLE, amount DECIMAL, type ENUM[COMMISSION_EARNED, PAYOUT_SETTLEMENT], created_at).",
                            "10. جدول Payments (id PK, order_id FK->Orders.id, amount DECIMAL, method, transaction_ref, status)."
                        )
                    )
                }
            }

            SrsSection.API_ENDPOINTS -> {
                item {
                    SrsSpecCard(
                        title = "واجهات البرمجة المطلوبة (REST API & WebSocket Endpoints)",
                        items = listOf(
                            "• Auth & Roles: POST /api/v1/auth/login | GET /api/v1/users/me (JWT + RBAC Middleware).",
                            "• Customer Menu & Cart: GET /api/v1/categories | GET /api/v1/products | POST /api/v1/coupons/validate.",
                            "• Orders Lifecycle: POST /api/v1/orders (ينشئ الطلب ويحسب التوصيل والعمولة آلياً) | PATCH /api/v1/orders/{id}/status | POST /api/v1/orders/{id}/assign-driver.",
                            "• Driver Operations: GET /api/v1/driver/available-orders | POST /api/v1/driver/orders/{id}/accept | POST /api/v1/driver/location (يرسل إحداثيات GPS كل 3 ثوانٍ).",
                            "• Real-Time Map WebSockets: ws://api.domain.com/ws/orders/{orderId}/track (للعميل لمتابعة المندوب) | ws://api.domain.com/ws/admin/fleet-radar (للإدارة لمراقبة جميع المندوبين بشكل دائم).",
                            "• Financial & Admin: PUT /api/v1/admin/commission-rules | POST /api/v1/admin/drivers/{id}/payout | GET /api/v1/admin/reports/summary."
                        )
                    )
                }
            }

            SrsSection.ROLES_COMMISSIONS -> {
                item {
                    SrsSpecCard(
                        title = "مصفوفة الصلاحيات (RBAC) ومعادلات الحساب المالي الآلي",
                        items = listOf(
                            "• Super Admin: تحكم كامل في النظام + تعديل معادلات عمولة المندوبين + صرف المستحقات + إدارة المدراء.",
                            "• Restaurant Admin: إدارة المنتجات والأسعار والعروض والكوبونات والتقارير والمندوبين.",
                            "• Staff (كاشير / مطبخ): قبول الطلبات، تحويلها لقيد التحضير ثم جاهز، وتعيين المندوبين (ممنوع من تعديل نسب العمولات).",
                            "• المعادلة المالية لكل طلب:\n" +
                                "  1. المبلغ المدفوع من العميل = (مجموع المنتجات + رسوم التوصيل) - قيمة الخصم.\n" +
                                "  2. عمولة المندوب = تُحسب آلياً حسب القاعدة النشطة (نسبة % أو مبلغ ثابت أو شريحة مسافة أو مختلط).\n" +
                                "  3. صافي إيراد المطعم = المبلغ المدفوع من العميل - عمولة المندوب."
                        ),
                        actionLabel = "تجربة محرك العمولات والصلاحيات",
                        onAction = { onJumpToPortal(AppPortal.ADMIN) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SrsSpecCard(
    title: String,
    items: List<String>,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
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
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider()
            items.forEach { line ->
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                FilledTonalButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(actionLabel)
                }
            }
        }
    }
}

private fun buildFullSrsMarkdown(): String {
    return """
# وثيقة تحليل المتطلبات البرمجية (SRS) - نظام إدارة وتوصيل طلبات المطعم المتكامل
## 1. نظرة عامة
نظام مركزي موحد يربط 3 واجهات (تطبيق العميل، تطبيق المندوب، لوحة تحكم إدارة المطعم) بقاعدة بيانات واحدة ونظام تتبع حي على الخريطة التفاعلية (مثل هنجرستيشن).

## 2. دورة حياة الطلب (Order Statuses)
NEW -> ACCEPTED -> PREPARING -> READY -> ASSIGNED_TO_DRIVER -> PICKED_UP -> ON_THE_WAY -> DELIVERED (أو CANCELLED).

## 3. نظام التتبع التفاعلي المباشر (Live Map Tracking)
- للعميل: يبدأ ظهور المندوب على الخريطة التفاعلية عند استلام الطلب مع تحديث موقع المندوب والوقت المتوقع للوصول (ETA) لحظياً.
- للإدارة: خريطة رادار دائمة (Live Fleet Radar) تعرض مواقع جميع المندوبين والطلبات الجارية في آن واحد.

## 4. محرك العمولات المرن
يدعم 4 أنظمة قابلة للتعديل من لوحة الإدارة بدون برمجة:
1) نسبة مئوية من الطلب
2) مبلغ ثابت لكل طلب
3) شرائح المسافة بالكيلومتر
4) نظام مختلط (عمولة أساسية + مبلغ لكل 5 كم إضافية)
    """.trimIndent()
}
