package com.oxidehub.app

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

private val Space = Color(0xFF071426)
private val Panel = Color(0xFF10243D)
private val Cyan = Color(0xFF42D9F5)
private val Muted = Color(0xFF9BB0C8)
private val Green = Color(0xFF65D6A5)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 20, 38)
        window.navigationBarColor = android.graphics.Color.rgb(7, 20, 38)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                primary = Cyan,
                background = Space,
                surface = Panel,
                onBackground = Color.White,
                onSurface = Color.White
            )) {
                OxideNativeApp(OxideApi(applicationContext))
            }
        }
    }
}

@Composable
private fun OxideNativeApp(api: OxideApi) {
    val backgroundTransition = rememberInfiniteTransition(label = "oxide-background-drift")
    val backgroundDrift by backgroundTransition.animateFloat(
        initialValue = -0.018f,
        targetValue = 0.018f,
        animationSpec = infiniteRepeatable(animation = tween(35000, easing = LinearEasing), repeatMode = androidx.compose.animation.core.RepeatMode.Reverse),
        label = "oxide-background-offset"
    )
    val tabs = listOf("الرئيسية", "مكافأة", "VIP", "إحالة", "أخبار")
    val icons = listOf("⌂", "🎁", "♛", "♧", "▤")
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selected by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var remoteData by remember { mutableStateOf<JSONObject?>(null) }
    var sectionLoading by remember { mutableStateOf(false) }
    var sectionError by remember { mutableStateOf<String?>(null) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }
    var supportDraft by remember { mutableStateOf("") }
    var uuidDraft by remember { mutableStateOf("") }
    var selectedOfferId by remember { mutableStateOf(0) }
    var selectedOfferName by remember { mutableStateOf("") }
    var selectedOfferCost by remember { mutableStateOf(0) }
    var linkCode by remember { mutableStateOf("") }
    var commentDraft by remember { mutableStateOf("") }
    var referralDraft by remember { mutableStateOf("") }
    var orderData by remember { mutableStateOf<JSONObject?>(null) }
    var islandData by remember { mutableStateOf<JSONObject?>(null) }
    var progressData by remember { mutableStateOf<JSONObject?>(null) }
    var settingsExpanded by remember { mutableStateOf(false) }
    var islandTab by remember { mutableStateOf(0) }
    var islandRemainingSeconds by remember { mutableStateOf(0) }
    var commentsData by remember { mutableStateOf<JSONObject?>(null) }
    var commentsPostId by remember { mutableStateOf(0) }
    var selectedMissionId by remember { mutableStateOf(0) }
    var proofUploading by remember { mutableStateOf(false) }
    var maintenanceActive by remember { mutableStateOf(false) }
    var maintenanceMessage by remember { mutableStateOf("نعتذر عن الإزعاج، الموقع تحت الصيانة حالياً. سنعود قريباً.") }
    var backgroundImage by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    fun refreshProfile() {
        scope.launch {
            loading = true
            error = null
            try {
                profile = api.initialize()
                if (profile?.optBoolean("success", true) == false) {
                    error = profile?.optString("error", "تعذر تحميل الحساب")
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "تعذر الاتصال بالخادم"
            } finally {
                loading = false
            }
        }
    }

    val proofPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null && selectedMissionId > 0) {
            scope.launch {
                proofUploading = true
                sectionError = null
                actionMessage = null
                try {
                    val result = api.submitMissionProof(selectedMissionId, uri)
                    if (result.optBoolean("success", false)) {
                        actionMessage = "تم إرسال إثبات المهمة للمراجعة"
                        reloadKey++
                    } else {
                        sectionError = result.optString("error", "تعذر إرسال الإثبات")
                    }
                } catch (e: Exception) {
                    sectionError = e.localizedMessage ?: "تعذر رفع الصورة"
                } finally {
                    proofUploading = false
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshProfile()
        try {
            val maintenance = api.get("/api/maintenance")
            maintenanceActive = maintenance.optBoolean("maintenance", false)
            maintenanceMessage = maintenance.optString("message", maintenanceMessage)
        } catch (_: Exception) { }
        backgroundImage = withContext(Dispatchers.IO) {
            try {
                URL("https://kald2007mess-cyber-github-io.pages.dev/background.png").openStream().use {
                    BitmapFactory.decodeStream(it)?.asImageBitmap()
                }
            } catch (_: Exception) { null }
        }
    }

    LaunchedEffect(selected, reloadKey) {
        if (selected == 0) {
            try {
                progressData = api.get("/api/progress")
                profile = api.initialize()
            } catch (e: Exception) { error = e.localizedMessage ?: "تعذر تحميل التقدم" }
        }
        val endpoint = when (selected) {
            1 -> "/api/rewards"
            3 -> "/api/referral"
            4 -> "/api/news"
            5 -> "/api/support/messages?after=0"
            6 -> "/api/notifications"
            else -> null
        }
        if (endpoint != null) {
            sectionLoading = true
            sectionError = null
            actionMessage = null
            try {
                remoteData = api.get(endpoint)
                if (selected == 1) {
                    orderData = api.get("/api/reward-orders")
                    islandData = api.get("/api/reward-ptc")
                    islandRemainingSeconds = islandData?.optInt("remainingSeconds", 0) ?: 0
                    val missionData = api.get("/api/missions/status")
                    remoteData?.put("nativeMissions", missionData.optJSONArray("missions") ?: JSONArray())
                    remoteData?.put("completedMissions", missionData.optInt("completedMissions", 0))
                }
                if (remoteData?.optBoolean("success", true) == false) {
                    sectionError = remoteData?.optString("error", "تعذر تحميل البيانات")
                }
            } catch (e: Exception) {
                sectionError = e.localizedMessage ?: "تعذر الاتصال بالخادم"
            } finally {
                sectionLoading = false
            }
        }
    }

    LaunchedEffect(selected) {
        if (selected == 1) {
            while (true) {
                delay(1000)
                if (islandRemainingSeconds > 0) islandRemainingSeconds--
                else {
                    try {
                        islandData = api.get("/api/reward-ptc")
                        islandRemainingSeconds = islandData?.optInt("remainingSeconds", 0) ?: 0
                    } catch (_: Exception) { }
                }
            }
        }
    }

    LaunchedEffect(selected) {
        if (selected == 5) {
            while (true) {
                delay(4000)
                try { remoteData = api.get("/api/support/messages?after=0") } catch (_: Exception) { }
            }
        }
    }

    fun linkNativeAccount() {
        scope.launch {
            loading = true
            error = null
            actionMessage = null
            try {
                val result = api.linkNativeAccount(linkCode.trim())
                if (result.optBoolean("success", false) && result.optBoolean("linked", false)) {
                    actionMessage = "تم ربط التطبيق بحساب الموقع: " + result.optString("displayId")
                    linkCode = ""
                    profile = api.initialize()
                    reloadKey++
                } else {
                    error = result.optString("error", "تعذر ربط الحساب")
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "تعذر ربط الحساب"
            } finally {
                loading = false
            }
        }
    }

    fun sendPost(path: String, body: JSONObject, successText: String) {
        scope.launch {
            actionMessage = null
            sectionError = null
            sectionLoading = true
            try {
                val result = api.post(path, body)
                if (result.optBoolean("success", false)) {
                    actionMessage = successText
                    reloadKey++
                    refreshProfile()
                } else {
                    sectionError = result.optString("error", "تعذر تنفيذ العملية")
                }
            } catch (e: Exception) {
                sectionError = e.localizedMessage ?: "تعذر الاتصال بالخادم"
            } finally {
                sectionLoading = false
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF030816),

    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (backgroundImage != null) {
                Image(
                    bitmap = backgroundImage!!,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        translationX = size.width * backgroundDrift
                        translationY = size.height * backgroundDrift
                    },
                    contentScale = ContentScale.Crop
                )
            } else {
                androidx.compose.foundation.layout.Spacer(
                    Modifier.fillMaxSize().background(Color(0xFF030816))
                )
            }
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xAD030816), Color(0xE0030816)))
                )
            )
            OxideRainOverlay()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { selected = 6 }) { Text("🔔", color = Cyan, fontSize = 17.sp) }
                Box {
                    TextButton(onClick = { settingsExpanded = true }) { Text("⚙️", color = Cyan, fontSize = 17.sp) }
                    DropdownMenu(expanded = settingsExpanded, onDismissRequest = { settingsExpanded = false }) {
                        DropdownMenuItem(text = { Text("ربط حساب Google Play") }, onClick = {
                            settingsExpanded = false
                            actionMessage = "ربط Google Play Games سيتاح بعد دمج الخدمة في تطبيق Android."
                        })
                    }
                }
                TextButton(onClick = { selected = 5 }) { Text("🎧", color = Cyan, fontSize = 17.sp) }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("OXIDE HUB", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.Black)
                    Text("SURVIVE • EXPLORE • DOMINATE", color = Cyan, fontSize = 8.sp, letterSpacing = 1.sp)
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xCC0C1930),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF176CAA))
            ) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = {
                        val id = profile?.optString("displayId", profile?.optString("userId", "")) ?: ""
                        if (id.isNotBlank()) {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("OXIDE ID", id))
                            actionMessage = "تم نسخ رقم المستخدم"
                        }
                    }) { Text("نسخ", color = Cyan, fontSize = 11.sp) }
                    Text(profile?.optString("displayId", profile?.optString("userId", "Loading...")) ?: "Loading...", color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("رقم المستخدم الخاص بك", color = Muted, fontSize = 10.sp)
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF176CAA), RoundedCornerShape(13.dp)),
                color = Color(0xEB050F20),
                shape = RoundedCornerShape(13.dp),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, label ->
                        val active = selected == index
                        Surface(
                            modifier = Modifier.weight(1f).height(68.dp),
                            color = if (active) Color(0xFF075A9D) else Color.Transparent,
                            shape = RoundedCornerShape(9.dp),
                            border = if (active) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00AAFF)) else null
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().clickable { if (!(maintenanceActive && index in listOf(0, 1, 3))) selected = index }.padding(horizontal = 2.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(icons[index], fontSize = 21.sp, color = if (active) Color.White else Color(0xFFD6EAFF))
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else Color(0xFFC6D5E7), maxLines = 1)
                                if (index == 2) Text("🔒 SOON", fontSize = 7.sp, color = Color(0xFFFFD36B))
                            }
                        }
                    }
                }
            }


            if (maintenanceActive && selected in listOf(0, 1, 3)) {
                FeatureCard("🛠️ الموقع تحت الصيانة", maintenanceMessage)
            } else when (selected) {
                0 -> {
                    val progress = (progressData?.optInt("progress", 0) ?: 0).coerceIn(0, 3)
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xD90C1930)), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("DOWNLOAD ACCESS", color = Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("التقدم:", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("$progress/3", color = Cyan, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { progress / 3f }, modifier = Modifier.fillMaxWidth(),
                                color = Cyan, trackColor = Color(0xFF244765)
                            )
                        }
                    }
                    Text("UNLOCK YOUR DOWNLOAD", color = Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    for (stage in 1..3) {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(13.dp)) {
                            Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("المرحلة $stage/3", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("🔒 SOON", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("منصات التحميل", color = Cyan, fontWeight = FontWeight.Bold)
                            Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/download.html"))) } catch (_: Exception) { error = "تعذر فتح صفحة Android" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Text("🤖 تحميل ANDROID") }
                            Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/ios.html"))) } catch (_: Exception) { error = "تعذر فتح صفحة iOS" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text(" تحميل iOS") }
                            Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/beta.html"))) } catch (_: Exception) { error = "تعذر فتح صفحة Beta" } }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text("🧪 BETA") }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("طريقة الاستخدام", color = Cyan, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Text("أربع خطوات بسيطة للوصول إلى التحميل", color = Muted, fontSize = 11.sp)
                            listOf(
                                "افتح OXIDE HUB وادخل إلى الصفحة الرئيسية.",
                                "شاهد الإعلانات الثلاثة وأكمل كل إعلان حتى تأكيده.",
                                "اختر جهازك: Android أو iPhone أو Beta.",
                                "حمّل التطبيق وانتظر اكتمال التنزيل ثم ثبّته."
                            ).forEachIndexed { index, step ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Surface(color = Color(0xFF075A9D), shape = RoundedCornerShape(50)) {
                                        Box(Modifier.size(27.dp), contentAlignment = Alignment.Center) { Text((index + 1).toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                                    }
                                    Text(step, color = Color.White, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    Text("مرحباً بك في OXIDE HUB", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("رصيد نقاطك", color = Muted, fontSize = 13.sp)
                            val points = profile?.opt("points")?.toString() ?: "—"
                            Text(points, color = Cyan, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                            Text("الرصيد يُقرأ مباشرة من قاعدة بيانات الموقع", color = Muted, fontSize = 11.sp)
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("حالة الحساب", color = Color.White, fontWeight = FontWeight.Bold)
                            Text(profile?.optString("displayId", "لم يتم تأكيد الحساب") ?: "جارٍ تحميل الحساب", color = Muted)
                            if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cyan, strokeWidth = 2.dp)
                            error?.let { Text(it, color = Color(0xFFFF9A9A), fontSize = 12.sp) }
                            Button(onClick = { refreshProfile() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) {
                                Text("تحديث بيانات الحساب")
                            }
                        }
                    }
                    FeatureCard("ابدأ رحلتك", "اختر أحد الأقسام من الشريط السفلي للوصول إلى خدمات OXIDE HUB.")
                    Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/download.html"))) } catch (_: Exception) { error = "تعذر فتح مركز التنزيلات" } }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text("فتح مركز التنزيلات الرسمي") }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(20.dp)) {
                        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("ربط حساب الموقع", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("افتح الرابط في المتصفح الذي تستخدم فيه حساب OXIDE HUB، انسخ رمز الربط ثم الصقه هنا. الرمز صالح لمدة 5 دقائق ويُستخدم مرة واحدة.", color = Muted, fontSize = 12.sp)
                            OutlinedField(value = linkCode, onValueChange = { linkCode = it }, label = "رمز الربط المكوّن من 32 حرفاً")
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/api/native-link")))
                                    } catch (_: Exception) {
                                        error = "تعذر فتح المتصفح"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))
                            ) { Text("فتح صفحة إنشاء الرمز") }
                            Button(
                                onClick = { if (linkCode.isBlank()) error = "أدخل رمز الربط أولاً" else linkNativeAccount() },
                                enabled = !loading && linkCode.trim().length == 32,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))
                            ) { Text("ربط الحساب الآن") }
                            error?.let { Text(it, color = Color(0xFFFF9A9A), fontSize = 12.sp) }
                        }
                    }
                }
                1 -> {
                    Text("🎁 OXIDE ISLAND", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xE6084170)), shape = RoundedCornerShape(15.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("رصيد النقاط", color = Muted, fontSize = 13.sp)
                            Text((remoteData?.optInt("points", 0) ?: profile?.optInt("points", 0) ?: 0).toString(), color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                            Text("نقاطك المتاحة في OXIDE HUB", color = Muted, fontSize = 11.sp)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        listOf("🎁 اجمع واستبدل", "📋 المهمات والعروض").forEachIndexed { index, label ->
                            Surface(
                                modifier = Modifier.weight(1f).height(82.dp).clickable { islandTab = index },
                                color = if (islandTab == index) Color(0xFF075A9D) else Color(0xF00F2643),
                                shape = RoundedCornerShape(11.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (islandTab == index) Color(0xFF00AAFF) else Color(0xFF176CAA))
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    if (islandTab == 0) {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("تقدم إعلانات الجزيرة", color = Cyan, fontWeight = FontWeight.Bold)
                                val completedAds = islandData?.optInt("completedAds", 0) ?: 0
                                val requiredAds = islandData?.optInt("requiredAds", 10) ?: 10
                                Text("$completedAds / $requiredAds", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                androidx.compose.material3.LinearProgressIndicator(
                                    progress = { if (requiredAds > 0) (completedAds.toFloat() / requiredAds).coerceIn(0f, 1f) else 0f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Cyan,
                                    trackColor = Color(0xFF244765)
                                )
                                Text("المكافأة: ${islandData?.optInt("pointsPerAd", 5) ?: 5} نقاط لكل إعلان", color = Muted, fontSize = 12.sp)
                                val hours = islandRemainingSeconds / 3600
                                val minutes = (islandRemainingSeconds % 3600) / 60
                                val seconds = islandRemainingSeconds % 60
                                Text("الوقت المتبقي للدورة: %02d:%02d:%02d".format(hours, minutes, seconds), color = Muted, fontSize = 12.sp)
                                Button(onClick = { actionMessage = "🔒 SOON — مشاهدة الإعلانات غير متاحة حاليًا." }, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF244765))) { Text("🔒 مشاهدة الإعلانات — SOON") }
                                Text("مشاهدة الإعلانات غير متاحة حاليًا، كما في الموقع الأصلي.", color = Muted, fontSize = 11.sp)
                            }
                        }
                        Text("المكافآت المتاحة", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        val offers = remoteData?.optJSONArray("offers")
                        if (offers == null || offers.length() == 0) {
                            if (!sectionLoading) FeatureCard("لا توجد عروض", "تعذر تحميل عروض الاستبدال.")
                        } else {
                            for (i in 0 until offers.length()) {
                                val offer = offers.optJSONObject(i) ?: continue
                                val offerId = offer.optInt("id")
                                val cost = offer.optInt("points")
                                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(offer.optString("name"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Text("$cost نقطة", color = Cyan)
                                        Button(onClick = {
                                            selectedOfferId = offerId
                                            selectedOfferName = offer.optString("name")
                                            selectedOfferCost = cost
                                            sectionError = null
                                        }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Text("استبدال") }
                                    }
                                }
                            }
                            if (selectedOfferId > 0) {
                                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0B1B30)), shape = RoundedCornerShape(12.dp)) {
                                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("تأكيد استبدال $selectedOfferName مقابل $selectedOfferCost نقطة", color = Cyan, fontWeight = FontWeight.Bold)
                                        OutlinedField(value = uuidDraft, onValueChange = { uuidDraft = it }, label = "OX-XXXXXXXX أو UUID كامل")
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(onClick = {
                                                if (uuidDraft.isBlank()) sectionError = "أدخل UUID الخاص بحسابك أولاً."
                                                else {
                                                    sendPost("/api/rewards", JSONObject().put("offerId", selectedOfferId).put("userUuid", uuidDraft.trim()), "تم تسجيل طلبك وإرساله للإدارة. الحالة: قيد المعالجة.")
                                                    selectedOfferId = 0
                                                }
                                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Text("تأكيد الطلب") }
                                            TextButton(onClick = { selectedOfferId = 0 }) { Text("إلغاء", color = Muted) }
                                        }
                                    }
                                }
                            }
                        }
                        Text("طلبات الاستبدال السابقة", color = Cyan, fontWeight = FontWeight.Bold)
                        val orders = orderData?.optJSONArray("orders")
                        if (orders == null || orders.length() == 0) FeatureCard("لا توجد طلبات سابقة", "ستظهر طلباتك هنا بعد تقديم أول طلب استبدال.")
                        else for (i in 0 until orders.length()) {
                            val order = orders.optJSONObject(i) ?: continue
                            val status = when (order.optString("status")) {
                                "pending" -> "قيد المراجعة"
                                "approved" -> "تمت الموافقة"
                                "rejected" -> "مرفوض"
                                "failed" -> "فشل الطلب"
                                else -> order.optString("status")
                            }
                            FeatureCard("#${order.optInt("id")} • ${order.optString("offer_name")}", "${order.optInt("points_cost")} نقطة — $status")
                        }
                    } else {
                        Text("المهمات والعروض", color = Cyan, fontSize = 19.sp, fontWeight = FontWeight.Bold)
                        val missions = remoteData?.optJSONArray("nativeMissions")
                        val missionTotal = missions?.length() ?: 0
                        var missionCompleted = 0
                        if (missions != null) for (index in 0 until missions.length()) {
                            if (missions.optJSONObject(index)?.optString("status") == "approved") missionCompleted++
                        }
                        val missionPercent = if (missionTotal > 0) missionCompleted.toFloat() / missionTotal.toFloat() else 0f
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                            Column(Modifier.fillMaxWidth().padding(15.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mission Journey", color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("$missionCompleted / $missionTotal", color = Cyan, fontWeight = FontWeight.Bold)
                                }
                                androidx.compose.material3.LinearProgressIndicator(progress = { missionPercent }, modifier = Modifier.fillMaxWidth(), color = Cyan, trackColor = Color(0xFF244765))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("${(missionPercent * 100).toInt()}% مكتمل", color = Muted, fontSize = 11.sp)
                                    Text("${(missionTotal - missionCompleted).coerceAtLeast(0)} مهمات متبقية", color = Muted, fontSize = 11.sp)
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("الرصيد: ${remoteData?.optInt("points", profile?.optInt("points", 0) ?: 0) ?: 0} نقطة", color = Cyan, fontSize = 11.sp)
                                    Text("المهمات المكتملة", color = Muted, fontSize = 11.sp)
                                }
                            }
                        }
                        Text("المهمات المتاحة", color = Cyan, fontWeight = FontWeight.Bold)
                        if (missions == null || missions.length() == 0) FeatureCard("لا توجد مهمات متاحة", "ستظهر المهمات هنا عند توفرها.")
                        else for (i in 0 until missions.length()) {
                            val item = missions.optJSONObject(i) ?: continue
                            MissionCard(item = item, onOpen = { url ->
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                            }, onPickProof = {
                                selectedMissionId = item.optInt("id")
                                proofPicker.launch("image/*")
                            })
                        }
                        if (proofUploading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cyan, strokeWidth = 2.dp)
                    }
                }
                2 -> {
                    Text("👑 OXIDE VIP", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    FeatureCard("🔒 SOON", "اشتراك VIP سيكون متاحًا قريبًا.")
                }
                3 -> {
                    Text("الإحالات والترتيب", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    val user = remoteData?.optJSONObject("user")
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("ادعُ أصدقاءك واربح", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("صديقك يحصل على 30 نقطة وأنت تحصل على 50 نقطة عند إدخال رمز دعوتك.", color = Muted, fontSize = 12.sp)
                            val code = remoteData?.optString("referralCode", "") ?: ""
                            val inviteLink = if (code.isNotBlank()) OxideApi.BASE_URL + "/?ref=" + code else ""
                            Text("رابط دعوتك", color = Muted, fontSize = 11.sp)
                            Text(if (inviteLink.isBlank()) "جارٍ تحميل الرابط..." else inviteLink, color = Color.White, fontSize = 11.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Button(onClick = {
                                    if (inviteLink.isNotBlank()) {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("OXIDE referral", inviteLink))
                                        actionMessage = "تم نسخ رابط الدعوة"
                                    }
                                }, enabled = inviteLink.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Text("📋 نسخ الرابط") }
                                Button(onClick = {
                                    if (inviteLink.isNotBlank()) {
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "OXIDE HUB")
                                            putExtra(Intent.EXTRA_TEXT, "انضم إلى OXIDE HUB عبر رابط الدعوة الخاص بي واربح! $inviteLink")
                                        }
                                        try { context.startActivity(Intent.createChooser(share, "مشاركة رابط الدعوة")) } catch (_: Exception) { sectionError = "تعذرت مشاركة الرابط" }
                                    }
                                }, enabled = inviteLink.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text("↗ مشاركة") }
                            }
                            Text("الأصدقاء المدعوون: ${user?.optInt("referrals", 0) ?: 0}", color = Color.White)
                            Text("النقاط المكتسبة من الإحالات: ${user?.optInt("earned", 0) ?: 0}", color = Muted)
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("إضافة إحالة", color = Cyan, fontWeight = FontWeight.Bold)
                            Text("أدخل رمز الشخص الذي دعاك. لا يمكن إضافة إحالة بعد تسجيلها مرة واحدة.", color = Muted, fontSize = 12.sp)
                            OutlinedField(value = referralDraft, onValueChange = { referralDraft = it }, label = "رمز الإحالة OX-XXXXXXXX")
                            Button(onClick = {
                                if (referralDraft.isBlank()) sectionError = "أدخل رمز الإحالة أولاً"
                                else sendPost("/api/referral", JSONObject().put("referrerId", referralDraft.trim()), "تم تسجيل الإحالة بنجاح")
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Text("تأكيد رمز الإحالة") }
                        }
                    }
                    RankingList("أفضل أصحاب النقاط", remoteData?.optJSONArray("pointsRanking"), "points", "نقطة")
                    RankingList("أفضل المحيلين", remoteData?.optJSONArray("referralRanking"), "referrals", "إحالة")
                }
                4 -> {
                    Text("الأخبار", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    val posts = remoteData?.optJSONArray("posts")
                    if (posts == null || posts.length() == 0) {
                        if (!sectionLoading) FeatureCard("لا توجد أخبار", "ستظهر منشورات الإدارة هنا عند توفرها.")
                    } else {
                        for (i in 0 until posts.length()) {
                            val post = posts.optJSONObject(i) ?: continue
                            NewsCard(
                                post = post,
                                comment = commentDraft,
                                comments = if (commentsPostId == post.optInt("id")) commentsData?.optJSONArray("comments") else null,
                                onView = {
                                    scope.launch { try { api.post("/api/news", JSONObject().put("action", "view").put("postId", post.optInt("id"))) } catch (_: Exception) {} }
                                },
                                onLoadComments = {
                                    scope.launch {
                                        try {
                                            commentsData = api.get("/api/news?postId=${post.optInt("id")}")
                                            commentsPostId = post.optInt("id")
                                        } catch (e: Exception) {
                                            sectionError = e.localizedMessage ?: "تعذر تحميل التعليقات"
                                        }
                                    }
                                },
                                onCommentChange = { commentDraft = it },
                                onReaction = { reaction ->
                                    sendPost("/api/news", JSONObject().put("action", "reaction").put("postId", post.optInt("id")).put("reaction", reaction), "تم تسجيل تفاعلك")
                                },
                                onComment = {
                                    sendPost("/api/news", JSONObject().put("action", "comment").put("postId", post.optInt("id")).put("comment", commentDraft.trim()), "تم إرسال تعليقك")
                                    commentDraft = ""
                                }
                            )
                        }
                    }
                }
                6 -> {
                    TextButton(onClick = { selected = 0 }) { Text("⬅ إغلاق الإشعارات", color = Cyan) }
                    Text("الإشعارات", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    val notifications = remoteData?.optJSONArray("notifications")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { sendPost("/api/notifications", JSONObject().put("action", "read_all"), "تم تحديد الإشعارات كمقروءة") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text("قراءة الكل") }
                        Button(onClick = { sendPost("/api/notifications", JSONObject().put("action", "delete_read"), "تم حذف الإشعارات المقروءة") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text("حذف المقروءة") }
                    }
                    if (notifications == null || notifications.length() == 0) {
                        FeatureCard("لا توجد إشعارات", "ستظهر هنا تنبيهات حسابك ومكافآتك الجديدة.")
                    } else {
                        for (i in 0 until notifications.length()) {
                            val item = notifications.optJSONObject(i) ?: continue
                            val id = item.optInt("id")
                            val reward = item.optInt("reward_points")
                            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                    Text(item.optString("title"), color = Cyan, fontWeight = FontWeight.Bold)
                                    Text(item.optString("body"), color = Color.White, fontSize = 13.sp)
                                    if (reward > 0) Text("المكافأة: $reward نقطة", color = Green)
                                    Text(if (item.isNull("read_at")) "جديد" else "مقروء", color = Muted, fontSize = 11.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (reward > 0 && item.isNull("claimed_at")) {
                                            Button(onClick = { sendPost("/api/notifications", JSONObject().put("action", "claim").put("id", id), "تمت إضافة $reward نقطة إلى رصيدك") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Text("استلام المكافأة") }
                                        }
                                        if (item.isNull("read_at")) TextButton(onClick = { sendPost("/api/notifications", JSONObject().put("action", "read").put("id", id), "تم تحديد الإشعار كمقروء") }) { Text("تحديد كمقروء") }
                                    }
                                }
                            }
                        }
                    }
                }
                5 -> {
                    TextButton(onClick = { selected = 0 }) { Text("⬅ العودة للرئيسية", color = Cyan) }
                    Text("الدعم والمحادثات", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    val messages = remoteData?.optJSONArray("messages")
                    if (messages == null || messages.length() == 0) {
                        FeatureCard("المحادثة", "لا توجد رسائل سابقة. يمكنك إرسال رسالة للدعم أدناه.")
                    } else {
                        for (i in 0 until messages.length()) {
                            val item = messages.optJSONObject(i) ?: continue
                            val mine = item.optString("sender") == "user"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = if (mine) Color(0xFF123B50) else Panel),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(if (mine) "أنت" else "الدعم", color = Cyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(if (item.optString("message_type", "text") == "text") item.optString("message_text") else "تم استلام وسائط", color = Color.White)
                                }
                            }
                        }
                    }
                    OutlinedField(value = supportDraft, onValueChange = { supportDraft = it }, label = "اكتب رسالتك للدعم")
                    Button(
                        onClick = {
                            if (supportDraft.isBlank()) sectionError = "اكتب رسالة أولاً"
                            else {
                                val message = supportDraft.trim()
                                sendPost("/api/support/send", JSONObject().put("message", message), "تم إرسال رسالتك للدعم")
                                supportDraft = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))
                    ) { Text("إرسال الرسالة") }
                }
            }

            if (sectionLoading && selected != 0) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cyan, strokeWidth = 2.dp)
            actionMessage?.let { Text(it, color = Green, fontWeight = FontWeight.Bold) }
            sectionError?.let { FeatureCard("تنبيه", it) }
            Spacer(Modifier.height(4.dp))
            Text("OXIDE HUB • Android Native", color = Muted, fontSize = 10.sp)
            }
        }
    }
}


@Composable
private fun OxideRainOverlay() {
    // A continuous 60-second animation clock keeps each drop moving smoothly.
    val transition = rememberInfiniteTransition(label = "oxide-real-rain")
    val seconds by transition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing)
        ),
        label = "oxide-rain-clock"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = seconds

        // Subtle star layer from the website's independently animated .stars background.
        val starCount = (w * h / 18000f).toInt().coerceIn(35, 120)
        for (i in 0 until starCount) {
            val x = (i * 83.17f + 19f) % w
            val y = (i * 137.31f + t * 0.55f) % h
            val radius = 0.45f + (i % 4) * 0.22f
            val alpha = 0.10f + (i % 5) * 0.025f
            drawCircle(
                color = Color(0xFFB9DFFF).copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }

        // Fine rain streaks falling diagonally, matching the website's speed and density.
        val rainCount = (w * h / 3000f).toInt().coerceIn(100, 440)
        for (i in 0 until rainCount) {
            val seed = i.toFloat()
            val xStart = ((seed * 47.7f) % (w + 35f))
            val speed = 420f + (i % 17) * 26.8f
            val drift = -28f + (i % 13) * 3.8f
            val y = ((seed * 91.3f + t * speed) % (h + 70f)) - 35f
            val length = 9f + (i % 17) * 0.95f
            val x = xStart + drift * t * 0.035f
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCCEBFF).copy(alpha = 0f),
                        Color(0xFFC2E7FF).copy(alpha = 0.10f + (i % 5) * 0.025f),
                        Color(0xFFEBF8FF).copy(alpha = 0.16f + (i % 5) * 0.04f)
                    ),
                    startY = y - length,
                    endY = y
                ),
                start = Offset(x, y - length),
                end = Offset(x + drift * 0.035f, y),
                strokeWidth = if (i % 3 == 0) 1.2f else 0.75f,
                cap = StrokeCap.Round
            )
        }

        // Larger water beads slowly slide down the glass with faint wet trails.
        val glassCount = (w / 7f).toInt().coerceIn(24, 85)
        for (i in 0 until glassCount) {
            val seed = i.toFloat()
            val radius = 1.1f + (i % 8) * 0.47f
            val vy = 7f + (i % 10) * 2f
            val trailLength = 12f + (i % 13) * 2.8f
            val alpha = 0.18f + (i % 7) * 0.05f
            val phase = i * 0.71f
            val baseX = (seed * 109f + 23f) % w
            val x = baseX + kotlin.math.sin(t * 0.7f + phase) * (i % 3) * 1.3f
            val y = ((seed * 173f + t * vy) % (h + 40f)) - 20f

            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFC3E5FF).copy(alpha = 0f),
                        Color(0xFFC6E5FF).copy(alpha = alpha * 0.23f)
                    ),
                    startY = y - trailLength,
                    endY = y
                ),
                start = Offset(x, y - trailLength),
                end = Offset(x, y),
                strokeWidth = (radius * 0.55f).coerceAtLeast(0.7f),
                cap = StrokeCap.Round
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = alpha * 0.9f),
                        Color(0xFFCEEBFF).copy(alpha = alpha * 0.42f),
                        Color(0xFF60A4DC).copy(alpha = alpha * 0.19f),
                        Color(0xFFE1F5FF).copy(alpha = 0f)
                    ),
                    center = Offset(x - radius * 0.28f, y - radius * 0.35f),
                    radius = radius * 1.35f
                ),
                radius = radius * 1.18f,
                center = Offset(x, y)
            )
            drawCircle(
                color = Color(0xFFE7F7FF).copy(alpha = alpha * 0.65f),
                radius = radius * 0.48f,
                center = Offset(x - radius * 0.12f, y - radius * 0.12f),
                style = Stroke(width = 0.65f)
            )
            drawCircle(
                color = Color.White.copy(alpha = alpha * 0.8f),
                radius = (radius * 0.15f).coerceAtLeast(0.45f),
                center = Offset(x - radius * 0.25f, y - radius * 0.42f)
            )
        }
    }
}

@Composable
private fun MissionCard(item: JSONObject, onOpen: (String) -> Unit, onPickProof: () -> Unit) {
    val status = item.optString("status", "not_submitted")
    val statusText = when (status) {
        "approved" -> "مكتملة"
        "pending" -> "قيد المراجعة"
        "rejected" -> "مرفوضة — يمكنك إعادة الإرسال"
        else -> "لم تُرسل بعد"
    }
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(item.optString("title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(item.optString("platform"), color = Cyan, fontSize = 12.sp)
            Text(item.optString("description"), color = Muted, fontSize = 12.sp)
            Text("المكافأة: ${item.optInt("rewardPoints")} نقطة", color = Cyan)
            Text("الحالة: $statusText", color = if (status == "approved") Green else Muted)
            val target = item.optString("targetUrl")
            if (target.startsWith("https://") || target.startsWith("http://")) {
                TextButton(onClick = { onOpen(target) }) { Text("فتح رابط المهمة") }
            }
            if (status != "approved" && status != "pending") {
                Button(onClick = onPickProof, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) {
                    Text(if (status == "rejected") "إعادة إرسال الإثبات" else "اختيار صورة الإثبات")
                }
            }
        }
    }
}

@Composable
private fun NewsCard(
    post: JSONObject,
    comment: String,
    comments: JSONArray?,
    onView: () -> Unit,
    onLoadComments: () -> Unit,
    onCommentChange: (String) -> Unit,
    onReaction: (String) -> Unit,
    onComment: () -> Unit
) {
    var mediaBitmap by remember(post.optInt("id")) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    val context = LocalContext.current
    LaunchedEffect(post.optInt("id")) {
        onView()
        if (post.optString("media_type") != "video") {
            mediaBitmap = withContext(Dispatchers.IO) {
                try {
                    URL(OxideApi.BASE_URL + "/api/news/media?id=" + post.optInt("id")).openStream().use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } catch (_: Exception) { null }
            }
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(post.optString("title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            if (post.optString("media_type") == "video") {
                Button(onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/api/news/media?id=" + post.optInt("id")))) } catch (_: Exception) {}
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Text("▶ تشغيل الفيديو") }
            } else if (mediaBitmap != null) {
                Image(bitmap = mediaBitmap!!, contentDescription = post.optString("title"), modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
            }
            Text("المشاهدات: ${post.optInt("views")} • التعليقات: ${post.optInt("comments_count")}", color = Muted, fontSize = 11.sp)
            Text("❤️ ${post.optInt("heart_count")}   👍 ${post.optInt("like_count")}   😡 ${post.optInt("angry_count")}   🥺 ${post.optInt("sad_count")}   👎 ${post.optInt("dislike_count")}", color = Cyan)
            Text("تفاعلك: ${post.optString("my_reaction", "لم تتفاعل بعد")}", color = Muted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("❤️", "😡", "🥺", "👍", "👎").forEach { emoji ->
                    TextButton(onClick = { onReaction(emoji) }) { Text(emoji) }
                }
            }
            TextButton(onClick = onLoadComments) { Text("عرض التعليقات") }
            if (comments != null) {
                if (comments.length() == 0) Text("لا توجد تعليقات بعد", color = Muted, fontSize = 12.sp)
                for (i in 0 until comments.length()) {
                    val item = comments.optJSONObject(i) ?: continue
                    Text("${item.optString("user_id")} : ${item.optString("comment")}", color = Color.White, fontSize = 12.sp)
                }
            }
            OutlinedField(value = comment, onValueChange = onCommentChange, label = "اكتب تعليقاً")
            Button(onClick = onComment, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) {
                Text("إرسال التعليق")
            }
        }
    }
}

@Composable
private fun RankingList(title: String, rows: JSONArray?, valueKey: String, suffix: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = Cyan, fontWeight = FontWeight.Bold)
            if (rows == null || rows.length() == 0) {
                Text("لا توجد بيانات حالياً", color = Muted, fontSize = 12.sp)
            } else {
                for (i in 0 until rows.length()) {
                    val row = rows.optJSONObject(i) ?: continue
                    Text("${row.optInt("rank")}. ${row.optString("userId")} — ${row.optInt(valueKey)} $suffix", color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun OutlinedField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Muted) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Cyan,
            unfocusedBorderColor = Muted,
            cursorColor = Cyan
        )
    )
}

@Composable
private fun FeatureCard(title: String, description: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text(title, color = Cyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(description, color = Muted, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}
