package com.daqwayne.daysleft.widget

import android.appwidget.AppWidgetManager
import com.daqwayne.daysleft.data.DebugPrefs
import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
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
import androidx.compose.ui.graphics.Color
import com.daqwayne.daysleft.model.DotShape
import com.daqwayne.daysleft.model.WidgetConfig
import com.daqwayne.daysleft.model.daysLeft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

class DaysLeftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysLeftWidget()

    // Every resize event → force a recompose with the fresh size
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        CoroutineScope(Dispatchers.IO).launch {
            glanceAppWidget.update(context, glanceId)
        }
    }
}

class DaysLeftWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = EventRepository(context).loadEvents()
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val config = WidgetConfigRepository(context).getConfig(widgetId)
        val event = events.find { it.id == config.eventId }
            ?: events.minByOrNull { it.targetDate }

        val opts = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
        val spaceW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 160).toFloat()
        val spaceH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 200).toFloat()
        val debug = DebugPrefs.isDebug(context)

        provideContent {
            WidgetContent(event, config, widgetId, spaceW, spaceH,debug)
        }
    }
}

@Composable
private fun WidgetContent(
    event: CountdownEvent?,
    config: WidgetConfig,
    widgetId: Int,
    spaceW: Float,
    spaceH: Float,
    debug: Boolean,
) {
    val accentColor = Color(config.color.hex)
    val dimColor = Color(0xFF2E2E2E)
    val textColor = Color.White
    val subTextColor = Color(0xFF888888)
    val bgColor = Color.Black

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
                DayGrid(event, accentColor, dimColor, config.shape, spaceW, spaceH, debug)
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
                if (debug) {
                    Text(
                        "#$widgetId · ${spaceW.toInt()}x${spaceH.toInt()}",
                        style = TextStyle(color = ColorProvider(Color(0xFF666666)), fontSize = 9.sp),
                    )
                }
                
            }
        }
    }
}

@Composable
private fun DayGrid(
    event: CountdownEvent,
    accent: Color,
    dim: Color,
    shape: DotShape,
    spaceW: Float,
    spaceH: Float,
    debug: Boolean,
) {
    val glyph = when (shape) {
        DotShape.SQUARE -> "■"
        DotShape.CIRCLE -> "●"
        DotShape.X -> "✕"
    }

    val total = ChronoUnit.DAYS.between(event.startDate, event.targetDate).toInt().coerceAtLeast(1)
    val passed = ChronoUnit.DAYS.between(event.startDate, LocalDate.now()).toInt().coerceIn(0, total)

    val fontScale = androidx.glance.LocalContext.current.resources.configuration.fontScale

    val spacing = 2f
    val availW = spaceW - 24f
    val availH = spaceH - 90f

    val minDot = 4f
    val fudge = 1.35f
    val cells0 = total.coerceAtLeast(1).coerceAtMost(150)

    val colsMax = maxOf(1, floor((availW + spacing) / (minDot + spacing)).toInt())
    val cols = maxOf(1, minOf(colsMax, ceil(sqrt(cells0.toFloat())).toInt()))
    val dotW = (availW - (cols - 1) * spacing) / cols
    val rowsMax = maxOf(1, floor((availH + spacing) / (dotW * fudge + spacing)).toInt())
    val cells = minOf(cells0, cols * rowsMax)
    val rows = ceil(cells.toFloat() / cols).toInt()
    val dot = maxOf(minDot, dotW)

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
                                fontSize = ((dot * 0.75f) / fontScale).sp,
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
        if (debug) {
            Text(
                "${cols}x${rows} · d:${dot.toInt()} · t:${total} · p:${passed}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF666666)),
                    fontSize = 9.sp,
                ),
            )
        }
    }
}
