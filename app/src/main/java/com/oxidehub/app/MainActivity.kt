package com.oxidehub.app

import android.content.Intent
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
    val tabs = listOf("الرئيسية", "المهام", "المكافآت", "الإحالات", "الأخبار", "الدعم")
    val icons = listOf("⌂", "✓", "★", "⇧", "▤", "✉")
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
    var linkCode by remember { mutableStateOf("") }
    var commentDraft by remember { mutableStateOf("") }
    var selectedMissionId by remember { mutableStateOf(0) }
    var proofUploading by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) { refreshProfile() }

    LaunchedEffect(selected, reloadKey) {
        val endpoint = when (selected) {
            1 -> "/api/missions/status"
            2 -> "/api/rewards"
            3 -> "/api/referral"
            4 -> "/api/news"
            5 -> "/api/support/messages?after=0"
            else -> null
        }
        if (endpoint != null) {
            sectionLoading = true
            sectionError = null
            actionMessage = null
            try {
                remoteData = api.get(endpoint)
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
        containerColor = Space,
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0A1A2E)) {
                tabs.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Text(icons[index], fontSize = 20.sp) },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFF0B2039), Space, Color(0xFF030B15))))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("OXIDE HUB", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("YOUR SPACE. YOUR REWARDS.", color = Cyan, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                Surface(color = Color(0xFF15324B), shape = RoundedCornerShape(50)) {
                    Text("● ONLINE", color = Cyan, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 11.sp)
                }
            }

            when (selected) {
                0 -> {
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
                    Text("المهام", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    val missions = remoteData?.optJSONArray("missions")
                    if (missions == null || missions.length() == 0) {
                        if (!sectionLoading) FeatureCard("لا توجد مهام متاحة", "لم يُرجع الخادم مهام مفعّلة لهذا الحساب حالياً.")
                    } else {
                        Text("المهام المكتملة: ${remoteData?.optInt("completedMissions", 0) ?: 0}", color = Green)
                        for (i in 0 until missions.length()) {
                            val item = missions.optJSONObject(i) ?: continue
                            MissionCard(
                                item = item,
                                onOpen = { url ->
                                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (_: Exception) {}
                                },
                                onPickProof = {
                                    selectedMissionId = item.optInt("id")
                                    proofPicker.launch("image/*")
                                }
                            )
                        }
                    }
                    if (proofUploading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cyan, strokeWidth = 2.dp)
                }
                2 -> {
                    Text("المكافآت", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("رصيدك: ${remoteData?.optInt("points", 0) ?: profile?.optInt("points", 0) ?: 0} نقطة", color = Cyan, fontWeight = FontWeight.Bold)
                    val offers = remoteData?.optJSONArray("offers")
                    if (offers == null || offers.length() == 0) {
                        if (!sectionLoading) FeatureCard("لا توجد عروض", "تعذر تحميل عروض الاستبدال.")
                    } else {
                        OutlinedField(value = uuidDraft, onValueChange = { uuidDraft = it }, label = "أدخل UUID حساب اللعبة")
                        for (i in 0 until offers.length()) {
                            val offer = offers.optJSONObject(i) ?: continue
                            val offerId = offer.optInt("id")
                            val cost = offer.optInt("points")
                            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(offer.optString("name"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Text("$cost نقطة", color = Cyan)
                                    Button(
                                        onClick = {
                                            if (uuidDraft.isBlank()) sectionError = "أدخل UUID أولاً"
                                            else sendPost("/api/rewards", JSONObject().put("offerId", offerId).put("userUuid", uuidDraft.trim()), "تم إرسال طلب الاستبدال للمراجعة")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))
                                    ) { Text("طلب الاستبدال") }
                                }
                            }
                        }
                    }
                    FeatureCard("تنبيه", "يتم خصم النقاط وإرسال الطلب إلى الإدارة فقط عند نجاح الخادم في إنشاء الطلب.")
                }
                3 -> {
                    Text("الإحالات والترتيب", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    val user = remoteData?.optJSONObject("user")
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("رمز الإحالة: ${remoteData?.optString("referralCode", "—") ?: "—"}", color = Cyan, fontWeight = FontWeight.Bold)
                            Text("الأصدقاء المدعوون: ${user?.optInt("referrals", 0) ?: 0}", color = Color.White)
                            Text("النقاط المكتسبة من الإحالات: ${user?.optInt("earned", 0) ?: 0}", color = Muted)
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
                5 -> {
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
                                    Text(item.optString("message_text"), color = Color.White)
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
    onCommentChange: (String) -> Unit,
    onReaction: (String) -> Unit,
    onComment: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(post.optString("title"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("المشاهدات: ${post.optInt("views")} • التعليقات: ${post.optInt("comments_count")}", color = Muted, fontSize = 11.sp)
            Text("❤️ ${post.optInt("heart_count")}   👍 ${post.optInt("like_count")}   😡 ${post.optInt("angry_count")}", color = Cyan)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("❤️", "😡", "🥺", "👍", "👎").forEach { emoji ->
                    TextButton(onClick = { onReaction(emoji) }) { Text(emoji) }
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
