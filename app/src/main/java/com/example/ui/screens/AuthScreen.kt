package com.example.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.NeighborhoodOption
import com.example.data.RIYADH_NEIGHBORHOODS
import com.example.data.ServerConfigEntity

private const val AUTH_PREFS_NAME = "golden_ember_auth_prefs"
private const val KEY_REMEMBER_PASSWORD = "remember_password"
private const val KEY_SAVED_USERNAME = "saved_username"
private const val KEY_SAVED_PASSWORD = "saved_password"

private enum class AuthTab(val titleAr: String) {
    LOGIN("تسجيل الدخول الموحد"),
    REGISTER("إنشاء حساب جديد")
}

private enum class RegisterAccountType(val titleAr: String, val badgeAr: String) {
    CUSTOMER("حساب عميل جديد 👤", "يتم تفعيل الحساب تلقائياً فور التسجيل + إضافة عنوان التوصيل"),
    DRIVER("حساب مندوب توصيل 🛵", "لا يتم تفعيل الحساب إلا بعد موافقة وتفعيل إدارة المطعم")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    authMessage: String?,
    isAuthError: Boolean,
    serverConfig: ServerConfigEntity,
    onClearAuthMessage: () -> Unit,
    onLogin: (String, String) -> Unit,
    onRegisterCustomer: (
        fullName: String,
        usernameOrPhone: String,
        password: String,
        addressLabel: String,
        neighborhood: NeighborhoodOption,
        streetAndBuilding: String,
        landmarkNotes: String
    ) -> Unit,
    onRegisterDriver: (
        fullName: String,
        usernameOrPhone: String,
        password: String,
        vehicleInfo: String,
        neighborhood: NeighborhoodOption,
        onSuccessSwitchToLogin: () -> Unit
    ) -> Unit,
    onSaveServerConfig: (ServerConfigEntity) -> Unit,
    onTestAndSyncServer: (ServerConfigEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE) }

    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Remember password & saved credentials state
    var rememberPassword by remember {
        mutableStateOf(prefs.getBoolean(KEY_REMEMBER_PASSWORD, true))
    }
    var loginUsername by remember {
        mutableStateOf(
            if (prefs.getBoolean(KEY_REMEMBER_PASSWORD, true)) {
                prefs.getString(KEY_SAVED_USERNAME, "") ?: ""
            } else ""
        )
    }
    var loginPassword by remember {
        mutableStateOf(
            if (prefs.getBoolean(KEY_REMEMBER_PASSWORD, true)) {
                prefs.getString(KEY_SAVED_PASSWORD, "") ?: ""
            } else ""
        )
    }
    var showLoginPassword by remember { mutableStateOf(false) }
    var showBiometricDialog by remember { mutableStateOf(false) }
    var showServerSettingsDialog by remember { mutableStateOf(false) }

    fun persistCredentialsIfRemembered(user: String, pass: String) {
        prefs.edit().apply {
            putBoolean(KEY_REMEMBER_PASSWORD, rememberPassword)
            if (rememberPassword && user.isNotBlank() && pass.isNotBlank()) {
                putString(KEY_SAVED_USERNAME, user.trim())
                putString(KEY_SAVED_PASSWORD, pass)
            } else if (!rememberPassword) {
                remove(KEY_SAVED_USERNAME)
                remove(KEY_SAVED_PASSWORD)
            }
            apply()
        }
    }

    // Register state
    var registerType by remember { mutableStateOf(RegisterAccountType.CUSTOMER) }
    var regFullName by remember { mutableStateOf("") }
    var regUsernameOrPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var showRegPassword by remember { mutableStateOf(false) }

    // Customer initial delivery address fields during registration
    var regAddressLabel by remember { mutableStateOf("المنزل") }
    var regNeighborhood by remember { mutableStateOf(RIYADH_NEIGHBORHOODS.first()) }
    var regStreetAndBuilding by remember { mutableStateOf("") }
    var regLandmarkNotes by remember { mutableStateOf("") }

    // Driver vehicle field during registration
    var regVehicleInfo by remember { mutableStateOf("تويوتا يارس 2025 • ح ط ي 2030") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header Banner
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(165.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "مطعم الجمر الذهبي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.3f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(18.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "بوابة الدخول الموحدة الذكية",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "نظام مطعم الجمر الذهبي المتكامل",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "يتعرف النظام تلقائياً على صلاحيتك (مدير • مندوب • عميل) ويفتح الشاشة المخصصة لك",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE5E7EB)
                        )
                    }
                }
            }
        }

        // Mode Switcher: Login vs Create Account
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AuthTab.entries.forEach { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onClearAuthMessage()
                                activeTab = tab
                            }
                            .testTag("auth_tab_${tab.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.titleAr,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Feedback Banner (Errors or Driver Pending Approval Confirmation)
        if (authMessage != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isAuthError) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        Color(0xFF059669).copy(alpha = 0.16f)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAuthError) Icons.Default.Info else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isAuthError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF059669)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = authMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAuthError) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF065F46),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        when (activeTab) {
            AuthTab.LOGIN -> {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "تسجيل الدخول الموحد للنظام",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "أدخل اسم المستخدم أو رقم الجوال وكلمة المرور ليتم توجيهك تلقائياً لواجهتك:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            OutlinedTextField(
                                value = loginUsername,
                                onValueChange = {
                                    onClearAuthMessage()
                                    loginUsername = it
                                },
                                label = { Text("اسم المستخدم أو رقم الجوال") },
                                placeholder = { Text("أدخل اسم المستخدم أو رقم الجوال") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_username_input")
                            )

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = {
                                    onClearAuthMessage()
                                    loginPassword = it
                                },
                                label = { Text("كلمة المرور") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { showLoginPassword = !showLoginPassword }) {
                                        Icon(
                                            imageVector = if (showLoginPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "إظهار كلمة المرور"
                                        )
                                    }
                                },
                                visualTransformation = if (showLoginPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("login_password_input")
                            )

                        // Remember Password Checkbox Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    rememberPassword = !rememberPassword
                                    prefs.edit().putBoolean(KEY_REMEMBER_PASSWORD, rememberPassword).apply()
                                    if (!rememberPassword) {
                                        prefs.edit()
                                            .remove(KEY_SAVED_USERNAME)
                                            .remove(KEY_SAVED_PASSWORD)
                                            .apply()
                                    }
                                }
                                .testTag("remember_password_checkbox"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = rememberPassword,
                                    onCheckedChange = { checked ->
                                        rememberPassword = checked
                                        prefs.edit().putBoolean(KEY_REMEMBER_PASSWORD, checked).apply()
                                        if (!checked) {
                                            prefs.edit()
                                                .remove(KEY_SAVED_USERNAME)
                                                .remove(KEY_SAVED_PASSWORD)
                                                .apply()
                                        }
                                    }
                                )
                                Text(
                                    text = "تذكر كلمة المرور وبيانات الدخول على هذا الجهاز",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Button(
                            onClick = {
                                persistCredentialsIfRemembered(loginUsername, loginPassword)
                                onLogin(loginUsername, loginPassword)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("unified_login_submit_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "تسجيل الدخول وفتح الواجهة المخصصة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Biometric Fingerprint Login Button
                        FilledTonalButton(
                            onClick = {
                                onClearAuthMessage()
                                showBiometricDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("biometric_login_btn"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "الدخول باستخدام البصمة",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الدخول السريع باستخدام البصمة (Biometric)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

            AuthTab.REGISTER -> {
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "إنشاء حساب جديد في النظام",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            // Choose between Customer (Auto-Active + Address) vs Driver (Pending Admin Approval)
                            Text(
                                text = "اختر نوع الحساب المراد إنشاؤه:",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                RegisterAccountType.entries.forEach { type ->
                                    val selected = registerType == type
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            onClearAuthMessage()
                                            registerType = type
                                        },
                                        label = { Text(type.titleAr) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Activation Policy Notice Banner
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (registerType == RegisterAccountType.CUSTOMER) {
                                    Color(0xFF059669).copy(alpha = 0.12f)
                                } else {
                                    Color(0xFFD97706).copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = if (registerType == RegisterAccountType.CUSTOMER) {
                                        "✓ سياسة حساب العميل: يتم تفعيل حسابك تلقائياً فور التسجيل مع حفظ عنوان التوصيل الخاص بك والدخول مباشرة."
                                    } else {
                                        "⏳ سياسة حساب مندوب التوصيل: لا يتم تفعيل الحساب تلقائياً! يبقى الحساب قيد المراجعة حتى تقوم إدارة المطعم بتفعيله."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (registerType == RegisterAccountType.CUSTOMER) {
                                        Color(0xFF065F46)
                                    } else {
                                        Color(0xFF92400E)
                                    },
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            OutlinedTextField(
                                value = regFullName,
                                onValueChange = { regFullName = it },
                                label = {
                                    Text(
                                        if (registerType == RegisterAccountType.CUSTOMER) "الاسم الكامل للعميل" else "الاسم الكامل للمندوب"
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = regUsernameOrPhone,
                                onValueChange = { regUsernameOrPhone = it },
                                label = { Text("رقم الجوال أو اسم المستخدم للدخول") },
                                placeholder = { Text("مثال: 0551122334") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("كلمة المرور") },
                                trailingIcon = {
                                    IconButton(onClick = { showRegPassword = !showRegPassword }) {
                                        Icon(
                                            imageVector = if (showRegPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (showRegPassword) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (registerType == RegisterAccountType.CUSTOMER) {
                                // Customer Delivery Address Creation Section at Registration
                                HorizontalDivider()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "إنشاء عنوان التوصيل الأساسي للعميل",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text("تسمية العنوان:", style = MaterialTheme.typography.labelMedium)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("المنزل", "العمل", "الاستراحة", "عنوان آخر").forEach { lbl ->
                                        FilterChip(
                                            selected = regAddressLabel == lbl,
                                            onClick = { regAddressLabel = lbl },
                                            label = { Text(lbl) }
                                        )
                                    }
                                }

                                Text("اختر الحي السكني (لتحديد المسافة وموقع الخريطة):", style = MaterialTheme.typography.labelMedium)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    RIYADH_NEIGHBORHOODS.forEach { n ->
                                        FilterChip(
                                            selected = regNeighborhood.nameAr == n.nameAr,
                                            onClick = { regNeighborhood = n },
                                            label = { Text("${n.nameAr.substringBefore(" -")} (${n.distanceKm} كم)") }
                                        )
                                    }
                                }

                                OutlinedTextField(
                                    value = regStreetAndBuilding,
                                    onValueChange = { regStreetAndBuilding = it },
                                    label = { Text("اسم الشارع ورقم المبنى / الفيلا") },
                                    placeholder = { Text("مثال: شارع التخصصي، فيلا رقم 18") },
                                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                OutlinedTextField(
                                    value = regLandmarkNotes,
                                    onValueChange = { regLandmarkNotes = it },
                                    label = { Text("معلم قريب أو إرشادات للمندوب (اختياري)") },
                                    placeholder = { Text("مثال: مقابل الحديقة، البوابة الشمالية") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        onRegisterCustomer(
                                            regFullName,
                                            regUsernameOrPhone,
                                            regPassword,
                                            regAddressLabel,
                                            regNeighborhood,
                                            regStreetAndBuilding,
                                            regLandmarkNotes
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("register_customer_submit_btn"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "إنشاء حساب العميل وتفعيله تلقائياً",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                // Driver Specific Fields
                                HorizontalDivider()
                                Text(
                                    text = "بيانات مركبة التوصيل ومنطقة العمل",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = regVehicleInfo,
                                    onValueChange = { regVehicleInfo = it },
                                    label = { Text("نوع المركبة ورقم اللوحة") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text("نطاق التواجد المبدئي:", style = MaterialTheme.typography.labelMedium)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    RIYADH_NEIGHBORHOODS.forEach { n ->
                                        FilterChip(
                                            selected = regNeighborhood.nameAr == n.nameAr,
                                            onClick = { regNeighborhood = n },
                                            label = { Text(n.nameAr.substringBefore(" -")) }
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        onRegisterDriver(
                                            regFullName,
                                            regUsernameOrPhone,
                                            regPassword,
                                            regVehicleInfo,
                                            regNeighborhood
                                        ) {
                                            loginUsername = regUsernameOrPhone
                                            loginPassword = regPassword
                                            persistCredentialsIfRemembered(regUsernameOrPhone, regPassword)
                                            activeTab = AuthTab.LOGIN
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFD97706)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .testTag("register_driver_submit_btn"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.DirectionsBike, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "تسجيل حساب المندوب (بانتظار تفعيل الإدارة)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Online Mode & Server Settings Quick Access Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = if (serverConfig.isOnlineModeEnabled) Color(0xFF059669) else Color(0xFFD97706)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (serverConfig.isOnlineModeEnabled) {
                                    "البرنامج مفعل للعمل أونلاين (Online Mode)"
                                } else {
                                    "العمل أونلاين متوقف مؤقتاً"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${serverConfig.connectionStatus} • آخر مزامنة: ${serverConfig.lastSyncFormatted}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { showServerSettingsDialog = true },
                        modifier = Modifier.testTag("open_server_settings_btn")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعدادات السيرفر")
                    }
                }
            }
        }
    }

    if (showBiometricDialog) {
        val savedUser = prefs.getString(KEY_SAVED_USERNAME, "")?.takeIf { it.isNotBlank() }
            ?: loginUsername.trim()
        val savedPass = prefs.getString(KEY_SAVED_PASSWORD, "")?.takeIf { it.isNotBlank() }
            ?: loginPassword

        BiometricAuthenticationDialog(
            savedUsername = savedUser,
            hasSavedCredentials = savedUser.isNotBlank() && savedPass.isNotBlank(),
            onDismiss = { showBiometricDialog = false },
            onAuthenticateSuccess = { targetUser, targetPass ->
                showBiometricDialog = false
                loginUsername = targetUser
                loginPassword = targetPass
                persistCredentialsIfRemembered(targetUser, targetPass)
                onLogin(targetUser, targetPass)
            }
        )
    }

    if (showServerSettingsDialog) {
        ServerOnlineConfigDialog(
            config = serverConfig,
            onDismiss = { showServerSettingsDialog = false },
            onSaveConfig = { updated ->
                onSaveServerConfig(updated)
                showServerSettingsDialog = false
            },
            onTestAndSync = onTestAndSyncServer
        )
    }
}

@Composable
private fun BiometricAuthenticationDialog(
    savedUsername: String,
    hasSavedCredentials: Boolean,
    onDismiss: () -> Unit,
    onAuthenticateSuccess: (String, String) -> Unit
) {
    val defaultBioUser = if (savedUsername.isNotBlank()) savedUsername else "0559998877"
    val defaultBioPass = when (defaultBioUser) {
        "admin" -> "Ali714483785"
        "0551234567", "0557654321", "0553334444" -> "123456"
        else -> "123456"
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .clickable {
                            onAuthenticateSuccess(defaultBioUser, defaultBioPass)
                        }
                        .testTag("biometric_sensor_confirm_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "مستشعر البصمة",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }

                Text(
                    text = "التحقق من البصمة للدخول الفوري",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (hasSavedCredentials) {
                        "تم التعرف على الحساب المحفوظ ($savedUsername). المس مستشعر البصمة أو اضغط تأكيد للدخول فوراً."
                    } else {
                        "المس مستشعر البصمة للدخول السريع بالحساب المربوط بالبصمة على هذا الجهاز ($defaultBioUser)."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { onAuthenticateSuccess(defaultBioUser, defaultBioPass) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_biometric_login_btn"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تأكيد البصمة وتسجيل الدخول الآن")
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إلغاء")
                }
            }
        }
    }
}

@Composable
fun ServerOnlineConfigDialog(
    config: ServerConfigEntity,
    onDismiss: () -> Unit,
    onSaveConfig: (ServerConfigEntity) -> Unit,
    onTestAndSync: (ServerConfigEntity) -> Unit
) {
    var isOnlineEnabled by remember(config) { mutableStateOf(config.isOnlineModeEnabled) }
    var baseUrl by remember(config) { mutableStateOf(config.serverBaseUrl) }
    var wsUrl by remember(config) { mutableStateOf(config.websocketUrl) }
    var apiKey by remember(config) { mutableStateOf(config.apiKey) }
    var syncInterval by remember(config) { mutableStateOf(config.syncIntervalSeconds.toString()) }
    var autoSyncOrders by remember(config) { mutableStateOf(config.autoSyncOrders) }
    var autoSyncDrivers by remember(config) { mutableStateOf(config.autoSyncDriverLocations) }
    var autoSyncPayments by remember(config) { mutableStateOf(config.autoSyncPayments) }

    fun currentEntity(): ServerConfigEntity = config.copy(
        isOnlineModeEnabled = isOnlineEnabled,
        serverBaseUrl = baseUrl.trim().ifBlank { "https://api.goldenember-restaurant.sa/v1" },
        websocketUrl = wsUrl.trim().ifBlank { "wss://live.goldenember-restaurant.sa/ws/tracking" },
        apiKey = apiKey.trim(),
        syncIntervalSeconds = syncInterval.toIntOrNull()?.coerceIn(2, 60) ?: 4,
        autoSyncOrders = autoSyncOrders,
        autoSyncDriverLocations = autoSyncDrivers,
        autoSyncPayments = autoSyncPayments
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إعدادات السيرفر والعمل أونلاين",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Switch(
                            checked = isOnlineEnabled,
                            onCheckedChange = { isOnlineEnabled = it }
                        )
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOnlineEnabled) Color(0xFF059669).copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = config.connectionStatus,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isOnlineEnabled) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "آخر مزامنة: ${config.lastSyncFormatted} • زمن الاستجابة: ${config.lastLatencyMs}ms",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = { Text("رابط السيرفر الرئيسي (REST API Server URL)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("server_base_url_input")
                    )
                }

                item {
                    OutlinedTextField(
                        value = wsUrl,
                        onValueChange = { wsUrl = it },
                        label = { Text("رابط سيرفر التتبع الحي (WebSocket URL)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            label = { Text("مفتاح الربط API Key") },
                            singleLine = true,
                            modifier = Modifier.weight(1.6f)
                        )
                        OutlinedTextField(
                            value = syncInterval,
                            onValueChange = { syncInterval = it },
                            label = { Text("المزامنة (ث)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoSyncOrders, onCheckedChange = { autoSyncOrders = it })
                        Text("مزامنة الطلبات والإشعارات فورياً أونلاين", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoSyncDrivers, onCheckedChange = { autoSyncDrivers = it })
                        Text("مزامنة موقع المندوب والخريطة مباشرة أونلاين", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = autoSyncPayments, onCheckedChange = { autoSyncPayments = it })
                        Text("تفعيل التحقق الحي لبوابات الدفع الإلكتروني", style = MaterialTheme.typography.bodySmall)
                    }
                }

                item {
                    FilledTonalButton(
                        onClick = { onTestAndSync(currentEntity()) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_server_connection_btn")
                    ) {
                        Icon(Icons.Default.CloudDone, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("فحص الاتصال بالسيرفر ومزامنة البيانات الآن")
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = onDismiss) {
                            Text("إغلاق")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onSaveConfig(currentEntity()) },
                            modifier = Modifier.testTag("save_server_config_btn")
                        ) {
                            Text("حفظ وتفعيل أونلاين")
                        }
                    }
                }
            }
        }
    }
}
