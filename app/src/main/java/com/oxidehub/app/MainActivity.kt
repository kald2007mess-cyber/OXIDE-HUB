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
import android.widget.VideoView
import java.io.File
import java.io.FileOutputStream
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text as MaterialText
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
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
private val LocalOxideLanguage = compositionLocalOf { "ar" }

@Composable
private fun Text(text: String, modifier: Modifier = Modifier, color: Color = Color.Unspecified, fontSize: TextUnit = TextUnit.Unspecified, fontWeight: FontWeight? = null, letterSpacing: TextUnit = TextUnit.Unspecified, lineHeight: TextUnit = TextUnit.Unspecified, maxLines: Int = Int.MAX_VALUE) {
    MaterialText(text = translateUiText(text, LocalOxideLanguage.current), modifier = modifier, color = color, fontSize = fontSize, fontWeight = fontWeight, letterSpacing = letterSpacing, lineHeight = lineHeight, maxLines = maxLines)
}

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
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("oxide_native_settings", android.content.Context.MODE_PRIVATE) }
    var language by remember { mutableStateOf(preferences.getString("language", "ar") ?: "ar") }
    val tabs = listOf(oxideText("الرئيسية", language), oxideText("مكافأة", language), "VIP", oxideText("إحالة", language), oxideText("أخبار", language))
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Redeem, Icons.Filled.Star, Icons.Filled.Group, Icons.Filled.Article)
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
    var languageExpanded by remember { mutableStateOf(false) }
    var notificationsOpen by remember { mutableStateOf(false) }
    var notificationData by remember { mutableStateOf<JSONObject?>(null) }
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

    LaunchedEffect(notificationsOpen, reloadKey) {
        if (notificationsOpen) {
            try { notificationData = api.get("/api/notifications") }
            catch (e: Exception) { sectionError = e.localizedMessage ?: "تعذر تحميل الإشعارات" }
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
        CompositionLocalProvider(LocalOxideLanguage provides language) {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box {
                    TextButton(onClick = { notificationsOpen = true }) { Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = Cyan) }
                    val unreadCount = notificationData?.optJSONArray("notifications")?.let { array ->
                        (0 until array.length()).count { index -> array.optJSONObject(index)?.isNull("read_at") == true }
                    } ?: 0
                    if (unreadCount > 0) {
                        Box(Modifier.align(Alignment.TopEnd).size(8.dp).background(Color(0xFFEF4444), RoundedCornerShape(50)))
                    }
                }
                Box {
                    TextButton(onClick = { languageExpanded = true }) { Icon(Icons.Filled.Language, contentDescription = "Language", tint = Cyan) }
                    DropdownMenu(expanded = languageExpanded, onDismissRequest = { languageExpanded = false }) {
                        listOf("ar" to "العربية", "en" to "English", "fr" to "Français", "es" to "Español", "ru" to "Русский", "de" to "Deutsch", "tr" to "Türkçe", "id" to "Bahasa Indonesia").forEach { (code, label) ->
                            DropdownMenuItem(text = { Text(if (language == code) "✓ $label" else label) }, onClick = {
                                language = code
                                preferences.edit().putString("language", code).apply()
                                languageExpanded = false
                            })
                        }
                    }
                }
                Box {
                    TextButton(onClick = { settingsExpanded = true }) { Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Cyan) }
                    DropdownMenu(expanded = settingsExpanded, onDismissRequest = { settingsExpanded = false }) {
                        DropdownMenuItem(text = { Text(oxideText("ربط حساب Google Play", language)) }, onClick = {
                            settingsExpanded = false
                            actionMessage = oxideText("ربط Google Play Games سيتاح بعد دمج الخدمة في تطبيق Android.", language)
                        })
                    }
                }
                TextButton(onClick = { selected = 5 }) { Icon(Icons.Filled.HeadsetMic, contentDescription = "Support", tint = Cyan) }
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
                                Icon(imageVector = icons[index], contentDescription = label, modifier = Modifier.size(23.dp), tint = if (active) Color.White else Color(0xFFD6EAFF))
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (active) Color.White else Color(0xFFC6D5E7), maxLines = 1)
                                if (index == 2) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFFFD36B), modifier = Modifier.size(9.dp)); Text("SOON", fontSize = 7.sp, color = Color(0xFFFFD36B)) }
                            }
                        }
                    }
                }
            }


            if (maintenanceActive && selected in listOf(0, 1, 3)) {
                FeatureCard("الموقع تحت الصيانة", maintenanceMessage)
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
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xB810243D)), shape = RoundedCornerShape(13.dp)) {
                            Row(Modifier.fillMaxWidth().padding(15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("المرحلة $stage/3", color = Color.White, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) { Icon(Icons.Filled.Lock, contentDescription = null, tint = Muted, modifier = Modifier.size(14.dp)); Text("SOON", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("منصات التحميل", color = Cyan, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/download.html"))) } catch (_: Exception) { error = "تعذر فتح صفحة Android" } }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xD9087F9B))) { Icon(Icons.Filled.Android, contentDescription = "Android", modifier = Modifier.size(17.dp)); Spacer(Modifier.size(4.dp)); Text("Android", fontSize = 10.sp, maxLines = 1) }
                                Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/ios.html"))) } catch (_: Exception) { error = "تعذر فتح صفحة iOS" } }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xD915506B))) { Icon(Icons.Filled.PhoneIphone, contentDescription = "iOS", modifier = Modifier.size(17.dp)); Spacer(Modifier.size(4.dp)); Text("iOS", fontSize = 10.sp, maxLines = 1) }
                                Button(onClick = { try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OxideApi.BASE_URL + "/beta.html"))) } catch (_: Exception) { error = "تعذر فتح صفحة Beta" } }, modifier = Modifier.weight(1f), contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))) { Icon(Icons.Filled.Science, contentDescription = "Beta", modifier = Modifier.size(17.dp)); Spacer(Modifier.size(4.dp)); Text("Beta", fontSize = 10.sp, maxLines = 1) }
                            }
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
                    // Home page intentionally ends after the Usage Guide.
                }
                1 -> {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Filled.Redeem, contentDescription = null, tint = Cyan, modifier = Modifier.size(24.dp)); Text("OXIDE ISLAND", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
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
                                Button(onClick = { actionMessage = "🔒 SOON — مشاهدة الإعلانات غير متاحة حاليًا." }, enabled = false, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFF244765))) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp)); Text("مشاهدة الإعلانات — SOON") } }
                                Text("مشاهدة الإعلانات غير متاحة حاليًا، كما في الموقع الأصلي.", color = Muted, fontSize = 11.sp)
                            }
                        }
                        Text("المكافآت المتاحة", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        val offers = remoteData?.optJSONArray("offers")
                        if (offers == null || offers.length() == 0) {
                            if (!sectionLoading) FeatureCard("لا توجد عروض", "تعذر تحميل عروض الاستبدال.")
                        } else {
                            for (rowStart in 0 until offers.length() step 2) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                                    for (i in rowStart until minOf(rowStart + 2, offers.length())) {
                                        val offer = offers.optJSONObject(i) ?: continue
                                        val offerId = offer.optInt("id")
                                        val cost = offer.optInt("points")
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xB810243D)),
                                            shape = RoundedCornerShape(18.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x66168BD0))
                                        ) {
                                            Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                                                RewardImage(offerId)
                                                Text(offer.optString("name"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("$cost نقطة", color = Cyan, fontSize = 12.sp)
                                                Button(
                                                    onClick = {
                                                        selectedOfferId = offerId
                                                        selectedOfferName = offer.optString("name")
                                                        selectedOfferCost = cost
                                                        sectionError = null
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xD9087F9B))
                                                ) { Text("استبدال", fontSize = 12.sp) }
                                            }
                                        }
                                    }
                                    if (rowStart + 1 >= offers.length()) Spacer(Modifier.weight(1f))
                                }
                            }
                            if (selectedOfferId > 0) {
                                Dialog(onDismissRequest = { selectedOfferId = 0 }) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = Color(0xD90B1B30),
                                        shape = RoundedCornerShape(20.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xAA168BD0))
                                    ) {
                                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                            Text("طلب استبدال النقاط", color = Cyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                            Text("$selectedOfferName • $selectedOfferCost نقطة", color = Color.White, fontWeight = FontWeight.SemiBold)
                                            Text("أضف UUID الخاص بك لإكمال طلب الاستبدال.", color = Muted, fontSize = 12.sp)
                                            OutlinedField(value = uuidDraft, onValueChange = { uuidDraft = it }, label = "أدخل UUID الخاص بك")
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    modifier = Modifier.weight(1f),
                                                    onClick = {
                                                        if (uuidDraft.isBlank()) sectionError = "أدخل UUID الخاص بحسابك أولاً."
                                                        else {
                                                            sendPost("/api/rewards", JSONObject().put("offerId", selectedOfferId).put("userUuid", uuidDraft.trim()), "تم تسجيل طلبك وإرساله للإدارة. الحالة: قيد المعالجة.")
                                                            selectedOfferId = 0
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xD9087F9B))
                                                ) { Text("تأكيد الطلب") }
                                                TextButton(onClick = { selectedOfferId = 0 }) { Text("إلغاء", color = Muted) }
                                            }
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Filled.Star, contentDescription = null, tint = Cyan, modifier = Modifier.size(24.dp)); Text("OXIDE VIP", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                    FeatureCard("SOON", "اشتراك VIP سيكون متاحًا قريبًا.")
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
                                }, enabled = inviteLink.isNotBlank(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) { Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) { Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp)); Text("نسخ الرابط") } }
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
            sectionError?.let { FeatureCard(oxideText("تنبيه", language), it) }
            Spacer(Modifier.height(4.dp))
            Text("OXIDE HUB • Android Native", color = Muted, fontSize = 10.sp)
            }
            OxideRainOverlay()
            if (notificationsOpen) {
                Dialog(onDismissRequest = { notificationsOpen = false }) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xF20A1A30),
                        shape = RoundedCornerShape(18.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF168BD0))
                    ) {
                        Column(
                            Modifier.fillMaxWidth().heightIn(max = 620.dp).padding(15.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(oxideText("الإشعارات", language), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                TextButton(onClick = { notificationsOpen = false }) { Icon(Icons.Filled.Close, contentDescription = "Close", tint = Cyan) }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                                Button(
                                    onClick = { sendPost("/api/notifications", JSONObject().put("action", "read_all"), oxideText("تم تحديد الإشعارات كمقروءة", language)) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))
                                ) { Text(oxideText("قراءة الكل", language), fontSize = 11.sp) }
                                Button(
                                    onClick = { sendPost("/api/notifications", JSONObject().put("action", "delete_read"), oxideText("تم حذف الإشعارات المقروءة", language)) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15506B))
                                ) { Text(oxideText("حذف المقروءة", language), fontSize = 11.sp) }
                            }
                            Column(Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val items = notificationData?.optJSONArray("notifications")
                                if (items == null || items.length() == 0) {
                                    Text(oxideText("لا توجد إشعارات حتى الآن.", language), color = Muted, modifier = Modifier.padding(vertical = 22.dp))
                                } else {
                                    for (i in 0 until items.length()) {
                                        val item = items.optJSONObject(i) ?: continue
                                        val id = item.optInt("id")
                                        val reward = item.optInt("reward_points")
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xCC102A46)),
                                            shape = RoundedCornerShape(12.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (item.isNull("read_at")) Color(0xFF168BD0) else Color(0xFF29445E))
                                        ) {
                                            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(item.optString("title"), color = Cyan, fontWeight = FontWeight.Bold)
                                                Text(item.optString("body"), color = Color.White, fontSize = 13.sp)
                                                if (reward > 0) Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) { Icon(Icons.Filled.Redeem, contentDescription = null, tint = Green, modifier = Modifier.size(16.dp)); Text("$reward", color = Green, fontSize = 12.sp) }
                                                Text(if (item.isNull("read_at")) oxideText("جديد", language) else oxideText("مقروء", language), color = Muted, fontSize = 10.sp)
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    if (reward > 0 && item.isNull("claimed_at")) {
                                                        Button(onClick = { sendPost("/api/notifications", JSONObject().put("action", "claim").put("id", id), "تمت إضافة $reward نقطة إلى رصيدك") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) {
                                                            Text(oxideText("استلام المكافأة", language), fontSize = 10.sp)
                                                        }
                                                    }
                                                    if (item.isNull("read_at")) {
                                                        TextButton(onClick = { sendPost("/api/notifications", JSONObject().put("action", "read").put("id", id), oxideText("تم تحديد الإشعار كمقروء", language)) }) {
                                                            Text(oxideText("تحديد كمقروء", language), fontSize = 10.sp)
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
                }
            }
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
            val alpha = 0.16f + (i % 5) * 0.035f
            drawCircle(
                color = Color(0xFFB9DFFF).copy(alpha = alpha),
                radius = radius,
                center = Offset(x, y)
            )
        }

        // Fine rain streaks falling diagonally, matching the website's speed and density.
        val rainCount = (w * h / 3000f).toInt().coerceIn(150, 520)
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
                        Color(0xFFEBF8FF).copy(alpha = 0.24f + (i % 5) * 0.045f)
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
private fun RewardImage(offerId: Int) {
    var bitmap by remember(offerId) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(offerId) {
        val filename = when (offerId) {
            1 -> "oxide_55_coins.png"
            2 -> "oxide_145_coins.png"
            3 -> "oxide_premium.png"
            4 -> "oxide_25_bp_coins.png"
            else -> null
        }
        if (filename != null) {
            bitmap = withContext(Dispatchers.IO) {
                try {
                    URL(OxideApi.BASE_URL + "/" + filename).openStream().use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } catch (_: Exception) { null }
            }
        }
    }
    if (bitmap != null) {
        Image(bitmap = bitmap!!, contentDescription = null, modifier = Modifier.fillMaxWidth().height(125.dp), contentScale = ContentScale.Fit)
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
    val postId = post.optInt("id")
    val context = LocalContext.current
    var mediaBitmap by remember(postId) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var videoPath by remember(postId) { mutableStateOf<String?>(null) }
    var videoError by remember(postId) { mutableStateOf(false) }

    LaunchedEffect(postId, post.optString("media_type")) {
        onView()
        if (post.optString("media_type") == "video") {
            videoPath = withContext(Dispatchers.IO) {
                try {
                    val cached = File(context.cacheDir, "oxide_news_" + postId + ".mp4")
                    if (!cached.exists() || cached.length() == 0L) {
                        val connection = URL(OxideApi.BASE_URL + "/api/news/media?id=" + postId).openConnection().apply {
                            connectTimeout = 20000
                            readTimeout = 60000
                            useCaches = false
                        }
                        connection.getInputStream().use { input ->
                            FileOutputStream(cached).use { output -> input.copyTo(output) }
                        }
                    }
                    if (cached.exists() && cached.length() > 0L) cached.absolutePath else null
                } catch (_: Exception) {
                    videoError = true
                    null
                }
            }
        } else {
            mediaBitmap = withContext(Dispatchers.IO) {
                try {
                    URL(OxideApi.BASE_URL + "/api/news/media?id=" + postId).openStream().use {
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
                val readyPath = videoPath
                if (readyPath != null) {
                    AndroidView(
                        modifier = Modifier.fillMaxWidth().height(210.dp),
                        factory = { viewContext ->
                            VideoView(viewContext).apply {
                                setVideoPath(readyPath)
                                setOnPreparedListener { player ->
                                    player.isLooping = true
                                    player.setVolume(0f, 0f)
                                    start()
                                }
                                setOnErrorListener { _, _, _ ->
                                    videoError = true
                                    true
                                }
                            }
                        }
                    )
                } else if (videoError) {
                    Text("تعذر تشغيل الفيديو. تحقق من اتصال الإنترنت أو صيغة الملف.", color = Muted, fontSize = 12.sp)
                } else {
                    Box(Modifier.fillMaxWidth().height(210.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Cyan, strokeWidth = 2.dp)
                    }
                }
            } else if (mediaBitmap != null) {
                Image(bitmap = mediaBitmap!!, contentDescription = post.optString("title"), modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
            }
            Text("المشاهدات: " + post.optInt("views") + " • التعليقات: " + post.optInt("comments_count"), color = Muted, fontSize = 11.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) { Icon(Icons.Filled.Favorite, contentDescription = "Hearts", tint = Cyan, modifier = Modifier.size(15.dp)); Text(post.optInt("heart_count").toString(), color = Cyan); Icon(Icons.Filled.SentimentDissatisfied, contentDescription = "Angry", tint = Cyan, modifier = Modifier.size(15.dp)); Text(post.optInt("angry_count").toString(), color = Cyan); Icon(Icons.Filled.Favorite, contentDescription = "Sad", tint = Cyan, modifier = Modifier.size(15.dp)); Text(post.optInt("sad_count").toString(), color = Cyan); Icon(Icons.Filled.ThumbUp, contentDescription = "Likes", tint = Cyan, modifier = Modifier.size(15.dp)); Text(post.optInt("like_count").toString(), color = Cyan); Icon(Icons.Filled.ThumbDown, contentDescription = "Dislikes", tint = Cyan, modifier = Modifier.size(15.dp)); Text(post.optInt("dislike_count").toString(), color = Cyan) }
            Text("تفاعلك: " + post.optString("my_reaction", "لم تتفاعل بعد"), color = Muted, fontSize = 11.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    "❤️" to Icons.Filled.Favorite,
                    "😡" to Icons.Filled.SentimentDissatisfied,
                    "🥺" to Icons.Filled.Favorite,
                    "👍" to Icons.Filled.ThumbUp,
                    "👎" to Icons.Filled.ThumbDown
                ).forEach { (reaction, vector) ->
                    TextButton(onClick = { onReaction(reaction) }) {
                        Icon(vector, contentDescription = reaction, tint = Cyan, modifier = Modifier.size(19.dp))
                    }
                }
            }
            TextButton(onClick = onLoadComments) { Text("عرض التعليقات") }
            if (comments != null) {
                if (comments.length() == 0) Text("لا توجد تعليقات بعد", color = Muted, fontSize = 12.sp)
                for (i in 0 until comments.length()) {
                    val item = comments.optJSONObject(i) ?: continue
                    Text(item.optString("user_id") + " : " + item.optString("comment"), color = Color.White, fontSize = 12.sp)
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

 
private fun translateUiText(text: String, language: String): String {
    if (language == "ar") return text
    val old = oxideText(text, language)
    if (old != text) return old
    val index = when (language) { "en" -> 0; "fr" -> 1; "es" -> 2; "ru" -> 3; "de" -> 4; "tr" -> 5; "id" -> 6; else -> return text }
    val words = mapOf(
        "طريقة الاستخدام" to listOf("How to use","Comment utiliser","Cómo usar","Как использовать","Anleitung","Nasıl kullanılır","Cara menggunakan"),
        "منصات التحميل" to listOf("Download platforms","Plateformes de téléchargement","Plataformas de descarga","Платформы загрузки","Download-Plattformen","İndirme platformları","Platform unduhan"),
        "رصيد النقاط" to listOf("Points balance","Solde de points","Saldo de puntos","Баланс очков","Punktestand","Puan bakiyesi","Saldo poin"),
        "المكافآت المتاحة" to listOf("Available rewards","Récompenses disponibles","Recompensas disponibles","Доступные награды","Verfügbare Belohnungen","Mevcut ödüller","Hadiah tersedia"),
        "طلب استبدال النقاط" to listOf("Points redemption request","Demande d’échange de points","Solicitud de canje de puntos","Запрос на обмен очков","Punkte-Einlöseanfrage","Puan bozdurma talebi","Permintaan penukaran poin"),
        "أدخل UUID الخاص بك" to listOf("Enter your UUID","Saisissez votre UUID","Introduce tu UUID","Введите UUID","UUID eingeben","UUID'nizi girin","Masukkan UUID Anda"),
        "تأكيد الطلب" to listOf("Confirm request","Confirmer","Confirmar solicitud","Подтвердить запрос","Anfrage bestätigen","Talebi onayla","Konfirmasi permintaan"),
        "إلغاء" to listOf("Cancel","Annuler","Cancelar","Отмена","Abbrechen","İptal","Batal"),
        "المهمات والعروض" to listOf("Tasks and offers","Missions et offres","Tareas y ofertas","Задания и предложения","Aufgaben und Angebote","Görevler ve teklifler","Tugas dan penawaran"),
        "الإحالات والترتيب" to listOf("Referrals and rankings","Parrainages et classements","Referidos y clasificaciones","Рефералы и рейтинги","Empfehlungen und Ranglisten","Davetler ve sıralamalar","Referral dan peringkat"),
        "ادعُ أصدقاءك واربح" to listOf("Invite friends and earn","Invitez vos amis et gagnez","Invita amigos y gana","Приглашайте друзей и зарабатывайте","Lade Freunde ein und verdiene","Arkadaşlarını davet et, kazan","Undang teman dan dapatkan hadiah"),
        "رابط دعوتك" to listOf("Your invite link","Votre lien d’invitation","Tu enlace de invitación","Ваша ссылка-приглашение","Dein Einladungslink","Davet bağlantınız","Tautan undangan Anda"),
        "إضافة إحالة" to listOf("Add referral code","Ajouter un code de parrainage","Añadir código de referido","Добавить реферальный код","Empfehlungscode hinzufügen","Davet kodu ekle","Tambah kode referral"),
        "لا توجد أخبار" to listOf("No news","Aucune actualité","No hay noticias","Новостей нет","Keine Neuigkeiten","Haber yok","Belum ada berita"),
        "الإشعارات" to listOf("Notifications","Notifications","Notificaciones","Уведомления","Benachrichtigungen","Bildirimler","Notifikasi"),
        "قراءة الكل" to listOf("Mark all read","Tout marquer comme lu","Marcar todo como leído","Отметить всё прочитанным","Alle als gelesen markieren","Tümünü okundu işaretle","Tandai semua dibaca"),
        "حذف المقروءة" to listOf("Delete read","Supprimer les lues","Eliminar leídas","Удалить прочитанные","Gelesene löschen","Okunanları sil","Hapus yang dibaca"),
        "استلام المكافأة" to listOf("Claim reward","Réclamer la récompense","Reclamar recompensa","Получить награду","Belohnung abholen","Ödülü al","Klaim hadiah"),
        "تحديد كمقروء" to listOf("Mark as read","Marquer comme lu","Marcar como leído","Отметить прочитанным","Als gelesen markieren","Okundu olarak işaretle","Tandai sudah dibaca"),
        "الدعم والمحادثات" to listOf("Support and messages","Assistance et messages","Soporte y mensajes","Поддержка и сообщения","Support und Nachrichten","Destek ve mesajlar","Dukungan dan pesan"),
        "إرسال الرسالة" to listOf("Send message","Envoyer","Enviar mensaje","Отправить сообщение","Nachricht senden","Mesaj gönder","Kirim pesan"),
        "عرض التعليقات" to listOf("View comments","Voir les commentaires","Ver comentarios","Показать комментарии","Kommentare ansehen","Yorumları göster","Lihat komentar"),
        "لا توجد تعليقات بعد" to listOf("No comments yet","Pas encore de commentaires","Aún no hay comentarios","Комментариев пока нет","Noch keine Kommentare","Henüz yorum yok","Belum ada komentar"),
        "اكتب تعليقاً" to listOf("Write a comment","Écrire un commentaire","Escribe un comentario","Написать комментарий","Kommentar schreiben","Yorum yazın","Tulis komentar"),
        "إرسال التعليق" to listOf("Post comment","Envoyer le commentaire","Enviar comentario","Отправить комментарий","Kommentar senden","Yorumu gönder","Kirim komentar"),
        "لا توجد طلبات سابقة" to listOf("No previous requests","Aucune demande précédente","No hay solicitudes anteriores","Предыдущих запросов нет","Keine früheren Anfragen","Önceki talep yok","Belum ada permintaan"),
        "ستظهر طلباتك هنا بعد تقديم أول طلب استبدال." to listOf("Your requests will appear here after your first redemption.","Vos demandes apparaîtront ici après votre première demande.","Tus solicitudes aparecerán aquí después de tu primer canje.","Ваши запросы появятся здесь после первого обмена.","Deine Anfragen erscheinen nach deiner ersten Einlösung hier.","İlk bozdurma talebinizden sonra burada görünür.","Permintaan Anda akan muncul setelah penukaran pertama."),
        "المهمات المكتملة" to listOf("Completed tasks","Missions terminées","Tareas completadas","Выполненные задания","Abgeschlossene Aufgaben","Tamamlanan görevler","Tugas selesai"),
        "المهمات المتاحة" to listOf("Available tasks","Missions disponibles","Tareas disponibles","Доступные задания","Verfügbare Aufgaben","Mevcut görevler","Tugas tersedia"),
        "لا توجد مهمات متاحة" to listOf("No tasks available","Aucune mission disponible","No hay tareas disponibles","Нет доступных заданий","Keine Aufgaben verfügbar","Mevcut görev yok","Tidak ada tugas tersedia"),
        "ستظهر المهمات هنا عند توفرها." to listOf("Tasks will appear here when available.","Les missions apparaîtront ici lorsqu’elles seront disponibles.","Las tareas aparecerán aquí cuando estén disponibles.","Задания появятся здесь, когда будут доступны.","Aufgaben erscheinen hier, sobald sie verfügbar sind.","Görevler kullanılabilir olduğunda burada görünür.","Tugas akan muncul di sini saat tersedia."),
        "اشتراك VIP سيكون متاحًا قريبًا." to listOf("VIP access will be available soon.","L’accès VIP sera bientôt disponible.","El acceso VIP estará disponible pronto.","Доступ VIP скоро появится.","VIP-Zugang ist bald verfügbar.","VIP erişimi yakında kullanıma açılacak.","Akses VIP segera tersedia."),
        "جارٍ تحميل الرابط..." to listOf("Loading link...","Chargement du lien...","Cargando enlace...","Загрузка ссылки...","Link wird geladen...","Bağlantı yükleniyor...","Memuat tautan..."),
        "تم نسخ رابط الدعوة" to listOf("Invite link copied","Lien copié","Enlace copiado","Ссылка скопирована","Einladungslink kopiert","Davet bağlantısı kopyalandı","Tautan undangan disalin"),
        "مشاركة رابط الدعوة" to listOf("Share invite link","Partager le lien","Compartir enlace","Поделиться ссылкой","Einladungslink teilen","Davet bağlantısını paylaş","Bagikan tautan undangan"),
        "تعذرت مشاركة الرابط" to listOf("Could not share the link","Impossible de partager le lien","No se pudo compartir el enlace","Не удалось поделиться ссылкой","Link konnte nicht geteilt werden","Bağlantı paylaşılamadı","Gagal membagikan tautan"),
        "أدخل رمز الإحالة أولاً" to listOf("Enter a referral code first","Saisissez dabord un code","Introduce primero un código","Сначала введите код","Gib zuerst einen Empfehlungscode ein","Önce davet kodunu girin","Masukkan kode referral terlebih dahulu"),
        "تم تسجيل الإحالة بنجاح" to listOf("Referral registered successfully","Parrainage enregistré","Referido registrado correctamente","Реферал успешно зарегистрирован","Empfehlung erfolgreich registriert","Davet başarıyla kaydedildi","Referral berhasil didaftarkan"),
        "تأكيد رمز الإحالة" to listOf("Confirm referral code","Confirmer le code","Confirmar código","Подтвердить код","Empfehlungscode bestätigen","Davet kodunu onayla","Konfirmasi kode referral"),
        "أفضل أصحاب النقاط" to listOf("Top points holders","Meilleurs détenteurs de points","Usuarios con más puntos","Лидеры по очкам","Top-Punktesammler","En çok puan kazananlar","Pemilik poin terbanyak"),
        "أفضل المحيلين" to listOf("Top referrers","Meilleurs parrains","Mejores referentes","Лучшие рефералы","Top-Empfehler","En iyi davet edenler","Perujuk teratas"),
        "لا توجد إشعارات" to listOf("No notifications","Aucune notification","No hay notificaciones","Нет уведомлений","Keine Benachrichtigungen","Bildirim yok","Tidak ada notifikasi"),
        "اكتب رسالة أولاً" to listOf("Write a message first","Écrivez un message dabord","Escribe un mensaje primero","Сначала напишите сообщение","Schreibe zuerst eine Nachricht","Önce bir mesaj yazın","Tulis pesan terlebih dahulu"),
        "تم إرسال رسالتك للدعم" to listOf("Your message was sent to support","Votre message a été envoyé au support","Tu mensaje se envió al soporte","Сообщение отправлено в поддержку","Deine Nachricht wurde an den Support gesendet","Mesajınız desteğe gönderildi","Pesan Anda telah dikirim ke dukungan"),
        "مكتملة" to listOf("Completed","Terminée","Completada","Выполнено","Abgeschlossen","Tamamlandı","Selesai"),
        "قيد المراجعة" to listOf("Under review","En cours de vérification","En revisión","На проверке","Wird geprüft","İncelemede","Sedang ditinjau"),
        "مرفوضة — يمكنك إعادة الإرسال" to listOf("Rejected — you can resubmit","Refusée — vous pouvez renvoyer","Rechazada — puedes reenviar","Отклонено — можно отправить снова","Abgelehnt — erneut senden möglich","Reddedildi — yeniden gönderebilirsiniz","Ditolak — Anda dapat mengirim ulang"),
        "لم تُرسل بعد" to listOf("Not submitted yet","Pas encore envoyé","Aún no enviado","Еще не отправлено","Noch nicht gesendet","Henüz gönderilmedi","Belum dikirim"),
        "فتح رابط المهمة" to listOf("Open task link","Ouvrir le lien de la mission","Abrir enlace de tarea","Открыть ссылку задания","Aufgabenlink öffnen","Görev bağlantısını aç","Buka tautan tugas"),
        "إعادة إرسال الإثبات" to listOf("Resubmit proof","Renvoyer la preuve","Volver a enviar prueba","Отправить подтверждение снова","Nachweis erneut senden","Kanıtı yeniden gönder","Kirim ulang bukti"),
        "اختيار صورة الإثبات" to listOf("Choose proof image","Choisir une image preuve","Elegir imagen de prueba","Выбрать изображение подтверждения","Nachweisbild auswählen","Kanıt görseli seç","Pilih gambar bukti"),
        "تعذر تشغيل الفيديو. تحقق من اتصال الإنترنت أو صيغة الملف." to listOf("Could not play video. Check your connection or file format.","Lecture vidéo impossible. Vérifiez la connexion ou le format.","No se pudo reproducir el video. Comprueba la conexión o el formato.","Не удалось воспроизвести видео. Проверьте подключение или формат файла.","Video konnte nicht abgespielt werden. Prüfe Verbindung oder Dateiformat.","Video oynatılamadı. Bağlantınızı veya dosya biçimini kontrol edin.","Video tidak dapat diputar. Periksa koneksi atau format file."),
        "لم تتفاعل بعد" to listOf("No reaction yet","Aucune réaction","Aún no has reaccionado","Вы еще не отреагировали","Noch keine Reaktion","Henüz tepki vermediniz","Belum ada reaksi")
    )
    return words[text]?.getOrNull(index) ?: text
}
private fun oxideText(arabic: String, language: String): String {
    if (language == "ar") return arabic
    val translations = mapOf(
        "الرئيسية" to listOf("Home", "Accueil", "Inicio", "Главная", "Startseite", "Ana Sayfa", "Beranda"),
        "مكافأة" to listOf("Rewards", "Récompenses", "Recompensas", "Награды", "Belohnungen", "Ödüller", "Hadiah"),
        "إحالة" to listOf("Referral", "Parrainage", "Referidos", "Рефералы", "Empfehlung", "Davet", "Referral"),
        "أخبار" to listOf("News", "Actualités", "Noticias", "Новости", "Neuigkeiten", "Haberler", "Berita"),
        "الإشعارات" to listOf("Notifications", "Notifications", "Notificaciones", "Уведомления", "Benachrichtigungen", "Bildirimler", "Notifikasi"),
        "قراءة الكل" to listOf("Mark all read", "Tout lire", "Marcar todo leído", "Прочитать все", "Alle als gelesen", "Tümünü oku", "Tandai semua dibaca"),
        "حذف المقروءة" to listOf("Delete read", "Supprimer lus", "Eliminar leídas", "Удалить прочитанные", "Gelesene löschen", "Okunanları sil", "Hapus yang dibaca"),
        "لا توجد إشعارات حتى الآن." to listOf("No notifications yet.", "Aucune notification.", "Aún no hay notificaciones.", "Уведомлений пока нет.", "Noch keine Benachrichtigungen.", "Henüz bildirim yok.", "Belum ada notifikasi."),
        "جديد" to listOf("New", "Nouveau", "Nuevo", "Новое", "Neu", "Yeni", "Baru"),
        "مقروء" to listOf("Read", "Lu", "Leído", "Прочитано", "Gelesen", "Okundu", "Dibaca"),
        "استلام المكافأة" to listOf("Claim reward", "Réclamer", "Reclamar premio", "Получить награду", "Belohnung abholen", "Ödülü al", "Klaim hadiah"),
        "تحديد كمقروء" to listOf("Mark as read", "Marquer comme lu", "Marcar como leído", "Отметить прочитанным", "Als gelesen markieren", "Okundu işaretle", "Tandai dibaca"),
        "تنبيه" to listOf("Notice", "Avis", "Aviso", "Уведомление", "Hinweis", "Uyarı", "Info"),
        "ربط حساب Google Play" to listOf("Connect Google Play", "Associer Google Play", "Vincular Google Play", "Подключить Google Play", "Google Play verbinden", "Google Play'e bağlan", "Hubungkan Google Play"),
        "ربط Google Play Games سيتاح بعد دمج الخدمة في تطبيق Android." to listOf("Google Play Games linking will be available after Android integration.", "La liaison Google Play Games sera disponible après l'intégration Android.", "La vinculación estará disponible tras la integración Android.", "Подключение станет доступно после интеграции Android.", "Die Verknüpfung folgt nach der Android-Integration.", "Android entegrasyonundan sonra kullanılabilir.", "Tersedia setelah integrasi Android.")
    )
    val index = when (language) { "en" -> 0; "fr" -> 1; "es" -> 2; "ru" -> 3; "de" -> 4; "tr" -> 5; "id" -> 6; else -> return arabic }
    return translations[arabic]?.get(index) ?: arabic
}
