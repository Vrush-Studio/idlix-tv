package com.personalbotai.idlix

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout

class CursorLayout(context: Context) : FrameLayout(context) {
    private var cursorX = 500f
    private var cursorY = 500f
    private val cursorRadius = 12f
    private val paint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint().apply {
        color = Color.WHITE
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private var isVisible = true

    init {
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val step = 35f
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_UP -> {
                    cursorY = (cursorY - step).coerceAtLeast(0f)
                    invalidate()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    cursorY = (cursorY + step).coerceAtMost(height.toFloat())
                    invalidate()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    cursorX = (cursorX - step).coerceAtLeast(0f)
                    invalidate()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    cursorX = (cursorX + step).coerceAtMost(width.toFloat())
                    invalidate()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                    simulateClick(cursorX, cursorY)
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
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
        if (isVisible) {
            canvas.drawCircle(cursorX, cursorY, cursorRadius, paint)
            canvas.drawCircle(cursorX, cursorY, cursorRadius, borderPaint)
        }
    }
}
