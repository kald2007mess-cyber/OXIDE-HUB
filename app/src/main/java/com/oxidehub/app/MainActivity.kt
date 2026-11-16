package com.oxidehub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private const val API_BASE_URL = "https://oxide-hub-api.kald2007mess.workers.dev"

private val Night = Color(0xFF070914)
private val Panel = Color(0xFF15152A)
private val Violet = Color(0xFFB44CFF)
private val Cyan = Color(0xFF4BCBFF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(7, 9, 20)
        window.navigationBarColor = android.graphics.Color.rgb(7, 9, 20)

        setContent {
            var connectionState by remember { mutableStateOf("جارٍ الاتصال بالخادم...") }
            var connected by remember { mutableStateOf(false) }
            var checking by remember { mutableStateOf(false) }

            fun checkApi() {
                if (checking) return
                checking = true
                connectionState = "جارٍ فحص الاتصال..."
                Thread {
                    val result = try {
                        val connection = (URL("$API_BASE_URL/health").openConnection() as HttpURLConnection).apply {
                            requestMethod = "GET"
                            connectTimeout = 10000
                            readTimeout = 10000
                        }
                        try {
                            val code = connection.responseCode
                            val body = (if (code in 200..299) connection.inputStream else connection.errorStream)
                                ?.bufferedReader()?.use { it.readText() }.orEmpty()
                            val json = JSONObject(body)
                            code in 200..299 &&
                                json.optBoolean("success") &&
                                json.optString("service") == "OXIDE HUB API" &&
                                json.optString("database") == "configured"
                        } finally {
                            connection.disconnect()
                        }
                    } catch (_: Exception) {
                        false
                    }
                    runOnUiThread {
                        connected = result
                        checking = false
                        connectionState = if (result) {
                            "متصل بالخادم وقاعدة البيانات مهيأة"
                        } else {
                            "تعذر الاتصال. تحقق من الإنترنت وحاول مجددًا"
                        }
                    }
                }.start()
            }

            LaunchedEffect(Unit) { checkApi() }

            OxideApp(
                connectionState = connectionState,
                connected = connected,
                checking = checking,
                onRetry = { checkApi() }
            )
        }
    }
}

@Composable
private fun OxideApp(
    connectionState: String,
    connected: Boolean,
    checking: Boolean,
    onRetry: () -> Unit
) {
    var selected by remember { mutableStateOf("الرئيسية") }
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Night,
            surface = Panel,
            primary = Violet,
            secondary = Cyan,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF11102A), Night, Color(0xFF080D19))
                    )
                )
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(34.dp))
            Text("🔥", fontSize = 66.sp)
            Text(
                "OXIDE HUB",
                color = Color.White,
                fontSize = 27.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp
            )
            Text(
                "YOUR SPACE. YOUR REWARDS.",
                color = Violet,
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(28.dp))

            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("مرحبًا بك في OXIDE HUB", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("حالة الاتصال", color = Cyan, fontWeight = FontWeight.Bold)
                    Text(
                        connectionState,
                        color = if (connected) Color(0xFF73E6A2) else Color(0xFFFFC56B),
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onRetry,
                        enabled = !checking,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (checking) "جارٍ الفحص..." else "إعادة فحص الاتصال")
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            listOf("المهام", "الإحالات", "السحب", "الملف الشخصي").forEach { item ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    onClick = { selected = item },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selected == item) Color(0xFF302044) else Panel
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text(
                        item,
                        Modifier.fillMaxWidth().padding(15.dp),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            Text(
                "OXIDE HUB • ANDROID",
                color = Color(0xFF77778F),
                fontSize = 10.sp,
                letterSpacing = 2.sp
            )
        }
    }
}
