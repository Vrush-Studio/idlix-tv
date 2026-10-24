package com.personalbotai.idlix

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import java.io.ByteArrayInputStream

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var cursorLayout: CursorLayout
    private lateinit var modeIndicator: TextView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

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
        
        // Kunci agar layar TIDAK PERNAH masuk screensaver atau sleep selama aplikasi IDLIX terbuka
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        cursorLayout = CursorLayout(this)
        webView = WebView(this).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }
        cursorLayout.webViewRef = webView

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        cursorLayout.addView(webView, params)

        modeIndicator = TextView(this).apply {
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#CC000000"))
            setPadding(20, 10, 20, 10)
            textSize = 13f
            visibility = View.GONE
        }
        val indicatorParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 24, 24, 0)
        }
        cursorLayout.addView(modeIndicator, indicatorParams)

        cursorLayout.onModeToggle = { isCursor ->
            modeIndicator.visibility = View.VISIBLE
            modeIndicator.text = if (isCursor) "Mode: Kursor Virtual" else "Mode: D-Pad Biasa"
            modeIndicator.removeCallbacks(null)
            modeIndicator.postDelayed({ modeIndicator.visibility = View.GONE }, 2000)
        }

        setContentView(cursorLayout)

        val settings: WebSettings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.loadsImagesAutomatically = true
        settings.mediaPlaybackRequiresUserGesture = false
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
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

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }
                customView = view
                customViewCallback = callback
                webView.visibility = View.GONE
                val decor = window.decorView as ViewGroup
                decor.addView(customView, 0, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            override fun onHideCustomView() {
                if (customView == null) return
                val decor = window.decorView as ViewGroup
                decor.removeView(customView)
                customView = null
                webView.visibility = View.VISIBLE
                customViewCallback?.onCustomViewHidden()
            }
        }

        webView.loadUrl("https://z2.idlixku.com/")
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SETTINGS) {
            cursorLayout.toggleMode()
            return typeOfKey() // true
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (customView != null) {
                val client = webView.webChromeClient
                client?.onHideCustomView()
                return true
            }
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun typeOfKey(): Boolean {
        return true
    }
}
