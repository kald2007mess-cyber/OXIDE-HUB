package com.oxidehub.app

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Native Android client for the OXIDE HUB production API.
 * The server remains the authority for identity, balances and rewards.
 */
class OxideApi(context: Context) {
    private val preferences = context.getSharedPreferences("oxide_session", Context.MODE_PRIVATE)

    suspend fun initialize(): JSONObject = request("/api/me", "GET", null)
    suspend fun get(path: String): JSONObject = request(path, "GET", null)
    suspend fun post(path: String, body: JSONObject): JSONObject = request(path, "POST", body.toString())
    suspend fun linkNativeAccount(code: String): JSONObject =
        request("/api/native-link", "POST", JSONObject().put("code", code).toString())

    suspend fun submitMissionProof(missionId: Int, uri: Uri): JSONObject =
        withContext(Dispatchers.IO) {
            require(missionId > 0) { "Invalid mission" }
            val mime = contextResolver().getType(uri) ?: "image/jpeg"
            require(mime.startsWith("image/")) { "اختر صورة فقط" }

            val boundary = "OxideBoundary" + UUID.randomUUID().toString().replace("-", "")
            val connection = (URL(BASE_URL + "/api/missions/submit").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 20000
                readTimeout = 30000
                doOutput = true
                useCaches = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Cache-Control", "no-store")
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                preferences.getString("visitor_cookie", null)?.let {
                    setRequestProperty("Cookie", it)
                }
            }

            try {
                connection.outputStream.use { output ->
                    val separator = "--$boundary\r\n"
                    output.write((separator + "Content-Disposition: form-data; name=\"missionId\"\r\n\r\n$missionId\r\n").toByteArray(Charsets.UTF_8))
                    output.write((separator + "Content-Disposition: form-data; name=\"proof\"; filename=\"mission-proof\"\r\nContent-Type: $mime\r\n\r\n").toByteArray(Charsets.UTF_8))
                    val input = contextResolver().openInputStream(uri) ?: throw IllegalStateException("تعذر قراءة الصورة")
                    input.use { source ->
                        val buffer = ByteArray(8192)
                        var total = 0
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            total += count
                            require(total <= 8 * 1024 * 1024) { "حجم الصورة يجب ألا يتجاوز 8 MB" }
                            output.write(buffer, 0, count)
                        }
                    }
                    output.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8))
                }

                connection.getHeaderField("Set-Cookie")?.let { header ->
                    val cookie = header.substringBefore(";").trim()
                    if (cookie.startsWith("oxide_visitor=")) {
                        preferences.edit().putString("visitor_cookie", cookie).apply()
                    }
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val json = try { JSONObject(responseText) } catch (_: Exception) {
                    JSONObject().put("success", false).put("error", "INVALID_SERVER_RESPONSE")
                }
                if (code !in 200..299 && !json.has("error")) json.put("error", "HTTP_$code")
                json
            } finally {
                connection.disconnect()
            }
        }

    private fun contextResolver() = appContext.contentResolver

    private suspend fun request(path: String, method: String, body: String?): JSONObject =
        withContext(Dispatchers.IO) {
            require(path.startsWith("/") && !path.startsWith("//")) { "Invalid API path" }
            val connection = (URL(BASE_URL + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15000
                readTimeout = 20000
                useCaches = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Cache-Control", "no-store")
                preferences.getString("visitor_cookie", null)?.let {
                    setRequestProperty("Cookie", it)
                }
                if (body != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                }
            }
            try {
                if (body != null) {
                    connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                }
                connection.getHeaderField("Set-Cookie")?.let { header ->
                    val cookie = header.substringBefore(";").trim()
                    if (cookie.startsWith("oxide_visitor=")) {
                        preferences.edit().putString("visitor_cookie", cookie).apply()
                    }
                }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val responseText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val json = try {
                    JSONObject(responseText)
                } catch (_: Exception) {
                    JSONObject().put("success", false).put("error", "INVALID_SERVER_RESPONSE")
                }
                if (code !in 200..299 && !json.has("error")) json.put("error", "HTTP_$code")
                json
            } finally {
                connection.disconnect()
            }
        }

    companion object {
        const val BASE_URL = "https://kald2007mess-cyber-github-io.pages.dev"
    }
}
