package com.personalbotai.idlix

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
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
        
        cursorLayout = CursorLayout(this)
        webView = WebView(this)

        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        cursorLayout.addView(webView, params)

        // Indikator mode kecil di pojok kanan atas
        modeIndicator = TextView(this).apply {
            text = "Mode: Kursor (Tekan Menu untuk D-Pad)"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#99000000"))
            setPadding(16, 8, 16, 8)
            textSize = 12f
            visibility = View.GONE
        }
        val indicatorParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            setMargins(0, 20, 20, 0)
        }
        cursorLayout.addView(modeIndicator, indicatorParams)

        cursorLayout.onModeToggle = {
            modeIndicator.visibility = View.VISIBLE
            modeIndicator.text = "Mode Ditukar (Tekan Menu)"
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

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // Inject script injection untuk tombol Speed / Fullscreen helper di video HTML5
                injectVideoHelperScript()
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
                cursorLayout.visibility = View.GONE
                val decor = window.decorView as ViewGroup
                decor.addView(customView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
            }

            override fun onHideCustomView() {
                if (customView == null) return
                val decor = window.decorView as ViewGroup
                decor.removeView(customView)
                customView = null
                cursorLayout.visibility = View.VISIBLE
                customViewCallback?.onCustomViewHidden()
            }
        }

        webView.loadUrl("https://z2.idlixku.com/")
    }

    private fun injectVideoHelperScript() {
        val js = """
            (function() {
                if (window.hasSpeedHelper) return;
                window.hasSpeedHelper = true;
                
                // Buat floating menu kecepatan video
                const div = document.createElement('div');
                div.style.position = 'fixed';
                div.style.bottom = '20px';
                div.style.right = '20px';
                div.style.zIndex = '999999';
                div.style.background = 'rgba(0,0,0,0.8)';
                div.style.padding = '8px 12px';
                div.style.borderRadius = '8px';
                div.style.color = '#fff';
                div.style.fontFamily = 'sans-serif';
                div.style.fontSize = '14px';
                div.style.display = 'flex';
                div.style.gap = '8px';
                div.style.alignItems = 'center';
                div.innerHTML = '<span>Speed:</span>' +
                    '<button onclick="setSpeed(1)" style="background:#333;color:#fff;border:none;padding:4px 8px;border-radius:4px;">1x</button>' +
                    '<button onclick="setSpeed(1.25)" style="background:#333;color:#fff;border:none;padding:4px 8px;border-radius:4px;">1.25x</button>' +
                    '<button onclick="setSpeed(1.5)" style="background:#333;color:#fff;border:none;padding:4px 8px;border-radius:4px;">1.5x</button>' +
                    '<button onclick="setSpeed(2)" style="background:#333;color:#fff;border:none;padding:4px 8px;border-radius:4px;">2x</button>';
                document.body.appendChild(div);

                window.setSpeed = function(s) {
                    const vids = document.querySelectorAll('video');
                    vids.forEach(v => v.playbackRate = s);
                };
            })();
        """
        webView.evaluateJavascript(js, null)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
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
}
