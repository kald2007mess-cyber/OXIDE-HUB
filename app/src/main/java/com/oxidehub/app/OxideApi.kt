package com.oxidehub.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Native Android client for the OXIDE HUB production API.
 * The server remains the authority for identity, balances and rewards.
 */
class OxideApi(context: Context) {
    private val preferences = context.getSharedPreferences("oxide_session", Context.MODE_PRIVATE)

    suspend fun initialize(): JSONObject = request("/api/me", "GET", null)
    suspend fun get(path: String): JSONObject = request(path, "GET", null)
    suspend fun post(path: String, body: JSONObject): JSONObject = request(path, "POST", body.toString())

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
