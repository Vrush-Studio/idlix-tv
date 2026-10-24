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

    // Daftar domain iklan, tracker, dan pop-up agresif umum
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
        webView = WebView(this)
        setContentView(webView)

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
                    // Blokir request iklan dengan mengembalikan response kosong
                    return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                }
                return super.shouldInterceptRequest(view, request)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: ""
                // Cegah link pop-up membuka browser luar atau tab iklan liar
                if (isAd(url)) {
                    return true
                }
                if (!url.contains("idlix") && !url.contains("google") && !url.contains("tmdb")) {
                    // Cegah redirect keluar ke domain iklan baru
                    return true
                }
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
