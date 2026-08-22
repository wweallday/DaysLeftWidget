package com.daqwayne.daysleft.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object DotGridRenderer {
    fun render(
        context: Context,
        widthDp: Float,
        heightDp: Float,
        total: Int,
        passed: Int,
        shape: Int,
        accentColor: Int
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val w = (widthDp * density).toInt().coerceAtLeast(10)
        val h = (heightDp * density).toInt().coerceAtLeast(10)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Paint background black so we can see the bitmap bounds during resize
        canvas.drawColor(Color.BLACK)

        val spacing = 2f * density
        val minDot = 3f * density
        val cells = total.coerceAtLeast(1).coerceAtMost(400)

        val colsMax = maxOf(1, floor((w + spacing) / (minDot + spacing)).toInt())
        val cols = maxOf(1, minOf(colsMax, ceil(sqrt(cells.toFloat())).toInt()))
        val rows = maxOf(1, ceil(cells.toFloat() / cols).toInt())

        val dot = min(
            (w - (cols - 1) * spacing) / cols,
            (h - (rows - 1) * spacing) / rows
        )
        if (dot <= 0f) return bitmap

        val gridW = cols * dot + (cols - 1) * spacing
        val gridH = rows * dot + (rows - 1) * spacing
        val ox = (w - gridW) / 2f
        val oy = (h - gridH) / 2f

        val fillAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accentColor }
        val fillDim = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF2E2E2E.toInt() }
        val strokeAccent = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = accentColor; strokeWidth = dot / 6f }
        val strokeDim = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; color = 0xFF2E2E2E.toInt(); strokeWidth = dot / 6f }
        val rect = RectF()

        for (i in 0 until cells) {
            val r = i / cols
            val c = i % cols
            val x = ox + c * (dot + spacing)
            val y = oy + r * (dot + spacing)
            rect.set(x, y, x + dot, y + dot)

            val dayAtCell = (i.toLong() * total) / cells
            val isPassed = dayAtCell < passed
            val fill = if (isPassed) fillDim else fillAccent
            val stroke = if (isPassed) strokeDim else strokeAccent

            when (shape) {
                0 -> canvas.drawRect(rect, fill)
                1 -> canvas.drawOval(rect, fill)
                else -> {
                    canvas.drawLine(x, y, x + dot, y + dot, stroke)
                    canvas.drawLine(x + dot, y, x, y + dot, stroke)
                }
            }
        }
        return bitmap
    }
}
