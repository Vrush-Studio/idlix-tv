package com.personalbotai.idlix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.webkit.WebView
import android.widget.FrameLayout

class CursorLayout(context: Context) : FrameLayout(context) {
    private var cursorX = 640f
    private var cursorY = 360f
    private val cursorRadius = 8f
    private var isVisible = false
    var isCursorMode = true
        private set
    private val handler = Handler(Looper.getMainLooper())

    var onModeToggle: ((Boolean) -> Unit)? = null
    var webViewRef: WebView? = null

    private val hideCursorRunnable = Runnable {
        if (isCursorMode) {
            isVisible = false
            invalidate()
        }
    }

    private val paint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
    }

    private fun showAndScheduleHide() {
        if (!isCursorMode) return
        isVisible = true
        invalidate()
        handler.removeCallbacks(hideCursorRunnable)
        handler.postDelayed(hideCursorRunnable, 2500)
    }

    fun toggleMode() {
        isCursorMode = !isCursorMode
        if (isCursorMode) {
            // Mode Kursor: Layout yang pegang fokus input
            isFocusable = true
            isFocusableInTouchMode = true
            requestFocus()
            showAndScheduleHide()
        } else {
            // Mode D-Pad: Sembunyikan kursor dan oper fokus langsung ke WebView
            isVisible = false
            invalidate()
            isFocusable = false
            isFocusableInTouchMode = false
            webViewRef?.let {
                it.isFocusable = true
                it.isFocusableInTouchMode = true
                it.requestFocus()
            }
        }
        onModeToggle?.invoke(isCursorMode)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        // Cek tombol toggle MENU
        if (event.action == KeyEvent.ACTION_DOWN) {
            if (event.keyCode == KeyEvent.KEYCODE_MENU || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                toggleMode()
                return true
            }
        }

        // Jika mode D-Pad aktif, biarkan WebView yang menangani semua tombol arah
        if (!isCursorMode) {
            return super.dispatchKeyEvent(event)
        }

        // Jika mode Kursor aktif
        if (event.action == KeyEvent.ACTION_DOWN) {
            val step = 15f
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    cursorY = (cursorY - step).coerceAtLeast(0f)
                    if (cursorY < 120f) scrollWebViewBy(0, -60)
                    showAndScheduleHide()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    cursorY = (cursorY + step).coerceAtMost(height.toFloat())
                    if (cursorY > height - 120f) scrollWebViewBy(0, 60)
                    showAndScheduleHide()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    cursorX = (cursorX - step).coerceAtLeast(0f)
                    showAndScheduleHide()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    cursorX = (cursorX + step).coerceAtMost(width.toFloat())
                    showAndScheduleHide()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    showAndScheduleHide()
                    simulateClick(cursorX, cursorY)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun scrollWebViewBy(dx: Int, dy: Int) {
        webViewRef?.scrollBy(dx, dy)
    }

    private fun simulateClick(x: Float, y: Float) {
        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0)
        val upEvent = MotionEvent.obtain(downTime, eventTime + 50, MotionEvent.ACTION_UP, x, y, 0)
        dispatchTouchEvent(downEvent)
        dispatchTouchEvent(upEvent)
        downEvent.recycle()
        upEvent.recycle()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isVisible && isCursorMode) {
            canvas.drawCircle(cursorX, cursorY, cursorRadius, paint)
            canvas.drawCircle(cursorX, cursorY, cursorRadius, borderPaint)
        }
    }
}
