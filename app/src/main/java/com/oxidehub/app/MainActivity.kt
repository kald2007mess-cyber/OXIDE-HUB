package com.oxidehub.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

private val Night = Color(0xFF070914)
private val Panel = Color(0xFF111F35)
private val Violet = Color(0xFFB44CFF)
private val Cyan = Color(0xFF4BCBFF)
private val Green = Color(0xFF35EF91)
private val Muted = Color(0xFFB8C9DD)

private enum class OxideTab { HOME, REWARDS, REFERRAL, NEWS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 9, 20)
        window.navigationBarColor = android.graphics.Color.rgb(7, 9, 20)
        setContent { OxideHubApp(OxideApi(applicationContext)) }
    }
}

@Composable
private fun OxideHubApp(api: OxideApi) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var tab by rememberSaveable { mutableStateOf(OxideTab.HOME) }
    var displayId by remember { mutableStateOf("جارٍ التحميل...") }
    var rawUserId by remember { mutableStateOf("") }
    var points by remember { mutableIntStateOf(0) }
    var progress by remember { mutableIntStateOf(0) }
    var offers by remember { mutableStateOf(listOf<Pair<Int, Pair<String, Int>>>()) }
    var referralCode by remember { mutableStateOf("") }
    var referralCount by remember { mutableIntStateOf(0) }
    var referralEarned by remember { mutableIntStateOf(0) }
    var referralInput by rememberSaveable { mutableStateOf("") }
    var news by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var status by remember { mutableStateOf("جارٍ الاتصال بالخادم...") }
    var loading by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var playInfoOpen by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<Pair<Int, Pair<String, Int>>?>(null) }
    var actionMessage by remember { mutableStateOf("") }

    suspend fun loadCore() {
        loading = true
        status = "جارٍ تحميل بيانات الحساب..."
        try {
            val me = api.get("/api/me")
            if (!me.optBoolean("success")) throw IllegalStateException(me.optString("error", "تعذر تحميل الحساب"))
            rawUserId = me.optString("userId")
            displayId = me.optString("displayId", "غير متاح")
            points = me.optInt("points", 0)

            val p = api.get("/api/progress")
            progress = p.optInt("progress", 0).coerceIn(0, 3)

            val rewards = api.get("/api/rewards")
            if (rewards.optBoolean("success")) {
                points = rewards.optInt("points", points)
                val array = rewards.optJSONArray("offers") ?: JSONArray()
                val parsed = mutableListOf<Pair<Int, Pair<String, Int>>>()
                for (i in 0 until array.length()) {
                    val item = array.optJSONObject(i) ?: continue
                    parsed.add(item.optInt("id") to (item.optString("name") to item.optInt("points")))
                }
                offers = parsed
            }
            status = "تم الاتصال وحفظ بيانات الحساب"
        } catch (error: Exception) {
            status = error.message ?: "تعذر الاتصال بالخادم"
        } finally {
            loading = false
        }
    }

    suspend fun loadReferral() {
        try {
            val data = api.get("/api/referral")
            if (data.optBoolean("success")) {
                referralCode = data.optString("referralCode")
                val user = data.optJSONObject("user")
                referralCount = user?.optInt("referrals") ?: 0
                referralEarned = user?.optInt("earned") ?: 0
                points = user?.optInt("points") ?: points
            }
        } catch (_: Exception) { }
    }

    suspend fun loadNews() {
        try {
            val data = api.get("/api/news")
            if (data.optBoolean("success")) {
                val array = data.optJSONArray("posts") ?: JSONArray()
                val parsed = mutableListOf<Pair<String, String>>()
                for (i in 0 until array.length()) {
                    val post = array.optJSONObject(i) ?: continue
                    parsed.add(post.optString("title", "OXIDE HUB") to post.optString("media_type", ""))
                }
                news = parsed
            }
        } catch (_: Exception) { }
    }

    LaunchedEffect(Unit) { loadCore() }
    LaunchedEffect(tab) {
        if (tab == OxideTab.REFERRAL) loadReferral()
        if (tab == OxideTab.NEWS) loadNews()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Night, surface = Panel, primary = Violet,
            secondary = Cyan, onBackground = Color.White, onSurface = Color.White
        )
    ) {
        Scaffold(
            containerColor = Night,
            topBar = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("OXIDE HUB", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("SURVIVE • EXPLORE • DOMINATE", color = Violet, fontSize = 9.sp, letterSpacing = 1.sp)
                    }
                    Box {
                        IconButton(onClick = { settingsOpen = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "الإعدادات", tint = Cyan)
                        }
                        DropdownMenu(expanded = settingsOpen, onDismissRequest = { settingsOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("ربط حساب Google Play") },
                                onClick = { settingsOpen = false; playInfoOpen = true }
                            )
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF0B1729)) {
                    NavigationBarItem(tab == OxideTab.HOME, { tab = OxideTab.HOME }, { Icon(Icons.Default.Home, null) }, label = { Text("الرئيسية") })
                    NavigationBarItem(tab == OxideTab.REWARDS, { tab = OxideTab.REWARDS }, { Icon(Icons.Default.CardGiftcard, null) }, label = { Text("المكافآت") })
                    NavigationBarItem(tab == OxideTab.REFERRAL, { tab = OxideTab.REFERRAL }, { Icon(Icons.Default.Group, null) }, label = { Text("الإحالات") })
                    NavigationBarItem(tab == OxideTab.NEWS, { tab = OxideTab.NEWS }, { Icon(Icons.Default.Article, null) }, label = { Text("الأخبار") })
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (tab) {
                    OxideTab.HOME -> {
                        InfoCard {
                            Text("رقم المستخدم الخاص بك", color = Cyan, fontSize = 12.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(displayId, color = Green, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                TextButton(onClick = {
                                    if (rawUserId.isNotBlank()) {
                                        clipboard.setText(AnnotatedString(displayId))
                                        Toast.makeText(context, "تم نسخ رقم المستخدم", Toast.LENGTH_SHORT).show()
                                    }
                                }) { Text("نسخ") }
                            }
                        }
                        InfoCard {
                            Text("رصيدك الحالي", color = Muted)
                            Text(points.toString(), color = Green, fontSize = 34.sp, fontWeight = FontWeight.Black)
                            Text("OXIDE POINTS", color = Cyan, fontSize = 10.sp, letterSpacing = 2.sp)
                        }
                        InfoCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("تقدم فتح التحميل", color = Muted, modifier = Modifier.weight(1f))
                                Text("$progress/3", color = Green, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { progress / 3f },
                                modifier = Modifier.fillMaxWidth(),
                                color = Green,
                                trackColor = Color(0xFF26384D)
                            )
                            Text("مشاهدة الإعلانات متوقفة حاليًا — SOON", color = Color(0xFFFFC95C), fontSize = 12.sp)
                        }
                        Text(status, color = Muted, fontSize = 12.sp)
                        Button(onClick = { scope.launch { loadCore() } }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                            Text(if (loading) "جارٍ التحديث..." else "تحديث بيانات الحساب")
                        }
                    }
                    OxideTab.REWARDS -> {
                        InfoCard {
                            Text("رصيدك الحالي", color = Muted)
                            Text(points.toString(), color = Green, fontSize = 32.sp, fontWeight = FontWeight.Black)
                            Text("OXIDE POINTS", color = Cyan, fontSize = 10.sp)
                        }
                        if (actionMessage.isNotBlank()) Text(actionMessage, color = Muted, fontSize = 12.sp)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(offers) { offer ->
                                InfoCard {
                                    Text(offer.second.first, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("\${offer.second.second} POINTS", color = Cyan)
                                    Button(
                                        onClick = { selectedOffer = offer; actionMessage = "" },
                                        enabled = points >= offer.second.second,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text(if (points >= offer.second.second) "استبدال" else "نقاط غير كافية") }
                                }
                            }
                        }
                    }
                    OxideTab.REFERRAL -> {
                        InfoCard {
                            Text("ادعُ أصدقاءك واربح", color = Cyan, fontWeight = FontWeight.Bold)
                            Text("صديقك يحصل على 30 نقطة وأنت تحصل على 50 نقطة.", color = Muted, fontSize = 13.sp)
                            Text("رمز دعوتك: $referralCode", color = Green, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Button(onClick = {
                                val link = "https://oxide-hub-pages.pages.dev/?ref=$referralCode"
                                clipboard.setText(AnnotatedString(link))
                                Toast.makeText(context, "تم نسخ رابط الدعوة", Toast.LENGTH_SHORT).show()
                            }, modifier = Modifier.fillMaxWidth()) { Text("نسخ رابط الدعوة") }
                        }
                        InfoCard {
                            Text("الإحالات: $referralCount", color = Color.White)
                            Text("النقاط المكتسبة من الإحالات: $referralEarned", color = Green)
                        }
                        InfoCard {
                            OutlinedTextField(
                                value = referralInput,
                                onValueChange = { referralInput = it },
                                label = { Text("أدخل رمز دعوة") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Button(onClick = {
                                scope.launch {
                                    try {
                                        val result = api.post("/api/referral", JSONObject().put("code", referralInput.trim()))
                                        if (result.optBoolean("success")) {
                                            referralInput = ""
                                            actionMessage = "تم تسجيل رمز الدعوة بنجاح."
                                            loadCore()
                                            loadReferral()
                                        } else actionMessage = result.optString("error", "تعذر تسجيل الرمز")
                                    } catch (e: Exception) { actionMessage = e.message ?: "تعذر الاتصال" }
                                }
                            }, modifier = Modifier.fillMaxWidth()) { Text("تأكيد رمز الدعوة") }
                            if (actionMessage.isNotBlank()) Text(actionMessage, color = Muted, fontSize = 12.sp)
                        }
                    }
                    OxideTab.NEWS -> {
                        if (news.isEmpty()) {
                            Text("لا توجد أخبار منشورة حاليًا.", color = Muted)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(news) { post ->
                                    InfoCard {
                                        Text(post.first, color = Color.White, fontWeight = FontWeight.Bold)
                                        if (post.second.isNotBlank()) Text("نوع المنشور: \${post.second}", color = Muted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                        OutlinedButton(onClick = { scope.launch { loadNews() } }, modifier = Modifier.fillMaxWidth()) { Text("تحديث الأخبار") }
                    }
                }
            }
        }
    }

    if (playInfoOpen) {
        AlertDialog(
            onDismissRequest = { playInfoOpen = false },
            title = { Text("Google Play Games") },
            text = { Text("سيتم تفعيل ربط الحساب بعد إعداد خدمة Google Play Games في Play Console وربطها بالخادم. لن نعرض نجاحًا وهميًا قبل اكتمال الربط.") },
            confirmButton = { TextButton(onClick = { playInfoOpen = false }) { Text("حسنًا") } }
        )
    }

    selectedOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { selectedOffer = null },
            title = { Text("تأكيد الاستبدال") },
            text = { Text("سيتم إرسال طلب \${offer.second.first} مقابل \${offer.second.second} نقطة باستخدام حسابك الحالي ($displayId).") },
            confirmButton = {
                TextButton(onClick = {
                    val current = offer
                    selectedOffer = null
                    scope.launch {
                        try {
                            val result = api.post("/api/rewards", JSONObject().put("offerId", current.first).put("userUuid", rawUserId))
                            if (result.optBoolean("success")) {
                                points = result.optInt("points", points)
                                actionMessage = "تم إرسال طلب الاستبدال للإدارة."
                            } else actionMessage = result.optString("error", "تعذر إرسال الطلب")
                        } catch (e: Exception) { actionMessage = e.message ?: "تعذر الاتصال" }
                    }
                }) { Text("تأكيد") }
            },
            dismissButton = { TextButton(onClick = { selectedOffer = null }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun InfoCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Panel)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}
