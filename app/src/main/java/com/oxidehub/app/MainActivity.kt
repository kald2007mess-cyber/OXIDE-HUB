package com.oxidehub.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

private const val OXIDE_SITE_URL = "https://kald2007mess-cyber.github.io/"

class MainActivity : ComponentActivity() {
    private var oxideWebView: WebView? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(3, 8, 22)
        window.navigationBarColor = android.graphics.Color.rgb(3, 8, 22)

        setContent {
            var canGoBack by remember { mutableStateOf(false) }

            BackHandler(enabled = canGoBack) {
                oxideWebView?.goBack()
                canGoBack = oxideWebView?.canGoBack() == true
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        setBackgroundColor(android.graphics.Color.rgb(3, 8, 22))
                        oxideWebView = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.javaScriptCanOpenWindowsAutomatically = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                                canGoBack = view.canGoBack()
                            }
                        }
                        webChromeClient = WebChromeClient()
                        loadUrl(OXIDE_SITE_URL)
                    }
                },
                update = { view ->
                    oxideWebView = view
                    canGoBack = view.canGoBack()
                }
            )
        }
    }

    override fun onDestroy() {
        oxideWebView?.apply {
            stopLoading()
            destroy()
        }
        oxideWebView = null
        super.onDestroy()
    }
}
