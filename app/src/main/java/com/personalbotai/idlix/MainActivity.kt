package com.personalbotai.idlix

import android.app.Activity
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.view.KeyEvent
import java.io.ByteArrayInputStream

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var cursorLayout: CursorLayout

    private val adHosts = arrayOf(
        "adservice", "doubleclick", "googlesyndication", "googleadservices",
        "adnxs", "ads", "popads", "popcash", "adsterra", "exoclick",
        "propellerads", "monetag", "yandex", "histats", "analytics",
        "trafficjunky", "adtrue", "bidgear", "clicksor", "juicyads",
        "onclicktop", "creativecdn", "adcolony", "applovin", "vungle"
    )

    private fun isAd(url: String): Boolean {
        val lowerUrl = url.lowercase()
        for (host in adHosts) {
            if (lowerUrl.contains(host)) return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        cursorLayout = CursorLayout(this)
        webView = WebView(this)
        cursorLayout.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        setContentView(cursorLayout)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                val url = request?.url?.toString() ?: ""
                if (isAd(url)) {
                    return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: ""
                if (isAd(url)) return true
                if (!url.contains("idlix") && !url.contains("google") && !url.contains("tmdb")) return true
                return false
            }
        }
        webView.loadUrl("https://z2.idlixku.com/")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
