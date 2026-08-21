package com.daqwayne.daysleft.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.daqwayne.daysleft.MainActivity
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.data.WidgetConfigRepository
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.model.DotShape
import com.daqwayne.daysleft.model.WidgetConfig
import com.daqwayne.daysleft.model.daysLeft
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.sqrt

class DaysLeftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysLeftWidget()
}

class DaysLeftWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = EventRepository(context).loadEvents()
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val config = WidgetConfigRepository(context).getConfig(widgetId)
        val event = events.find { it.id == config.eventId }
            ?: events.minByOrNull { it.targetDate }

        provideContent {
            WidgetContent(event, config)
        }
    }
}

@Composable
private fun WidgetContent(event: CountdownEvent?, config: WidgetConfig) {
    val accentColor = androidx.compose.ui.graphics.Color(config.color.hex)
    val dimColor = androidx.compose.ui.graphics.Color(0xFF2E2E2E)
    val textColor = androidx.compose.ui.graphics.Color.White
    val subTextColor = androidx.compose.ui.graphics.Color(0xFF888888)
    val bgColor = androidx.compose.ui.graphics.Color.Black

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(MainActivity::class.java))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (event == null) {
            Text("No countdowns", style = TextStyle(color = ColorProvider(subTextColor)))
        } else {
            Column(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            ) {
                DayGrid(event, accentColor, dimColor, config.shape)
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    "${event.daysLeft()} days",
                    style = TextStyle(
                        color = ColorProvider(textColor),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    event.title,
                    style = TextStyle(color = ColorProvider(subTextColor), fontSize = 12.sp),
                )
            }
        }
    }
}

@Composable
private fun DayGrid(
    event: CountdownEvent,
    accent: androidx.compose.ui.graphics.Color,
    dim: androidx.compose.ui.graphics.Color,
    shape: DotShape
) {
    val glyph = when (shape) {
        DotShape.SQUARE -> "■"
        DotShape.CIRCLE -> "●"
        DotShape.X -> "✕"
    }

    val total = ChronoUnit.DAYS.between(event.startDate, event.targetDate).toInt().coerceAtLeast(1)
    val passed = ChronoUnit.DAYS.between(event.startDate, LocalDate.now()).toInt().coerceIn(0, total)
    val cells = total.coerceAtMost(400)

    val size = LocalSize.current
    val spacing = 2f
    val availW = size.width.value - 24f
    val availH = size.height.value - 70f

    var bestCols = 1
    var bestDot = 0f
    val maxCols = minOf(cells, 40)
    for (c in 1..maxCols) {
        val r = ceil(cells.toFloat() / c).toInt()
        val dw = (availW - (c - 1) * spacing) / c 
        val dh = (availH - (r - 1) * spacing) / r
        val d = minOf(dw, dh)
        if (d > bestDot) {
            bestDot = d
            bestCols = c
        }
    }
    val cols = bestCols
    val rows = ceil(cells.toFloat() / cols).toInt()
    val dot = maxOf(2f, bestDot)

    Column {
        for (r in 0 until rows) {
            Row {
                for (c in 0 until cols) {
                    val i = r * cols + c
                    if (i < cells) {
                        val dayAtCell = (i.toLong() * total) / cells
                        Text(
                            glyph,
                            modifier = GlanceModifier
                                .padding(end = spacing.dp, bottom = spacing.dp)
                                .size(dot.dp),
                            style = TextStyle(
                                color = ColorProvider(if (dayAtCell < passed) dim else accent),
                                fontSize = dot.sp,
                            ),
                        )
                    } else {
                        Box(
                            GlanceModifier
                                .padding(end = spacing.dp, bottom = spacing.dp)
                                .size(dot.dp)
                        ) { }
                    }
                }
            }
        }
    }
}
