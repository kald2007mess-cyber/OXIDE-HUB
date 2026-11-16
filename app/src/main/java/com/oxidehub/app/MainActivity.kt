package com.oxidehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject

private val Space = Color(0xFF071426)
private val Panel = Color(0xFF10243D)
private val Cyan = Color(0xFF42D9F5)
private val Muted = Color(0xFF9BB0C8)

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
    var selected by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun refresh() {
        scope.launch {
            loading = true
            error = null
            try {
                profile = api.initialize()
                val p = profile
                if (p?.optBoolean("success", true) == false) {
                    error = p.optString("error", "تعذر تحميل الحساب")
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "تعذر الاتصال بالخادم"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

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
                            val user = profile?.optJSONObject("user")
                            val points = user?.opt("points")?.toString()
                                ?: profile?.opt("points")?.toString()
                                ?: "—"
                            Text(points, color = Cyan, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold)
                            Text("النقاط تُحمّل من الخادم ولا تُحسب محلياً", color = Muted, fontSize = 11.sp)
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("حالة الحساب", color = Color.White, fontWeight = FontWeight.Bold)
                            val userId = profile?.optJSONObject("user")?.optString("id")
                                ?: profile?.optString("userId", "")
                            Text(if (!userId.isNullOrBlank()) userId else "لم يتم تأكيد هوية الحساب", color = Muted)
                            if (loading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Cyan, strokeWidth = 2.dp)
                            error?.let { Text(it, color = Color(0xFFFF9A9A), fontSize = 12.sp) }
                            Button(onClick = { refresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF087F9B))) {
                                Text("تحديث بيانات الحساب")
                            }
                        }
                    }
                    FeatureCard("ابدأ رحلتك", "تابع حالة حسابك ورصيدك من التطبيق الأصلي.")
                    FeatureCard("تنبيه مهم", "لا تُجرِ أي عملية سحب أو استبدال قبل تفعيل وربط خدمات الحساب على الخادم.")
                }
                else -> {
                    Text(tabs[selected], color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    FeatureCard(
                        "هذه الخدمة قيد الربط",
                        "واجهة التطبيق أصبحت أصلية وليست صفحة موقع داخل WebView. هذه الميزة لن تعمل حتى يتم توصيل واجهتها البرمجية والتحقق من حساب المستخدم."
                    )
                    if (selected == 1) FeatureCard("المهام", "سيتم عرض المهام وإرسال إثباتاتها بعد تفعيل API المهام.")
                    if (selected == 2) FeatureCard("المكافآت", "سيتم جلب الرصيد والطلبات من قاعدة البيانات بعد توصيل API المكافآت.")
                    if (selected == 3) FeatureCard("الإحالات", "سيتم ربط رابط الإحالة وإحصاء الدعوات بالخادم.")
                    if (selected == 4) FeatureCard("الأخبار", "سيتم جلب الأخبار والتعليقات والتفاعلات من الخادم.")
                    if (selected == 5) FeatureCard("الدعم", "سيتم تفعيل المحادثات وإرسال الرسائل بعد توصيل API الدعم.")
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("نسخة Android أصلية • بيانات الحساب من الخادم", color = Muted, fontSize = 10.sp)
        }
    }
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
