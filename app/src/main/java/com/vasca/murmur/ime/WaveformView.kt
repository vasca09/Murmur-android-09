package com.vasca.murmur.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import kotlin.math.max

class WaveformView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xfff59d3d.toInt(); strokeWidth = 5f; strokeCap = Paint.Cap.ROUND }
    @Volatile private var level = 0f
    fun setLevel(value: Float) { level = value.coerceIn(0f, 1f); postInvalidateOnAnimation() }
    override fun onDraw(canvas: Canvas) { super.onDraw(canvas); val bars = 18; val gap = width.toFloat() / bars; for (i in 0 until bars) { val rhythm = if (i % 3 == 0) 1f else .55f; val h = max(5f, height * (0.12f + level * rhythm)); val x = i * gap + gap / 2; canvas.drawLine(x, height / 2f - h / 2, x, height / 2f + h / 2, paint) } }
}
