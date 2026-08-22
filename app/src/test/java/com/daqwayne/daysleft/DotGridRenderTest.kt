package com.daqwayne.daysleft

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.daqwayne.daysleft.widget.DotGridView
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DotGridRenderTest {
    private fun render(
        shape: Int,
        w: Int,
        h: Int,
        total: Int,
        passed: Int,
        accent: Int,
        name: String,
    ) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val view =
            DotGridView(context).apply {
                setTotal(total)
                setPassed(passed)
                setShape(shape)
                setAccent(accent)
            }

        view.measure(
            View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY),
        )
        view.layout(0, 0, w, h)

        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(0xFF000000.toInt()) // OLED black base
        view.draw(canvas)

        val out = File("build/render_$name.png")
        out.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        println("RENDERED: ${out.absolutePath}")
    }

    @Test
    fun renderAll() {
        // sizes copied straight from your widget ruler (minus padding)
        render(0, 350, 480, 392, 233, 0xFF4CAF50.toInt(), "square_big") // year, ■
        render(1, 350, 480, 392, 233, 0xFF2196F3.toInt(), "circle_big") // year, ●
        render(2, 350, 480, 392, 233, 0xFFFF9800.toInt(), "x_big") // year, ✕
        render(2, 150, 110, 70, 31, 0xFFFFEB3B.toInt(), "x_small") // 2x2, ✕
    }
}
