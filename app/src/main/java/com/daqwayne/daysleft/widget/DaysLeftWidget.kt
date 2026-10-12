package com.daqwayne.daysleft.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.RemoteViews
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AndroidRemoteViews
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
import androidx.glance.layout.*
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.daqwayne.daysleft.MainActivity
import com.daqwayne.daysleft.R
import com.daqwayne.daysleft.data.DebugPrefs
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.data.WidgetConfigRepository
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.model.WidgetConfig
import com.daqwayne.daysleft.model.daysLeft
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DaysLeftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysLeftWidget()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var updateJob: Job? = null

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        
        updateJob?.cancel()
        updateJob = scope.launch {
            delay(400)
            glanceAppWidget.update(context, glanceId)
        }
    }
}

class DaysLeftWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = EventRepository(context).loadEvents()
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val config = WidgetConfigRepository(context).getConfig(widgetId)
        val event = events.find { it.id == config.eventId } ?: events.minByOrNull { it.targetDate }

        val opts = AppWidgetManager.getInstance(context).getAppWidgetOptions(widgetId)
        val spaceW = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 160).toFloat()
        val spaceH = opts.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 200).toFloat()
        val debug = DebugPrefs.isDebug(context)

        provideContent {
            WidgetContent(event, config, widgetId, spaceW, spaceH, debug)
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
    val bgColor = Color.Black
    val textColor = Color.White
    val subTextColor = Color(0xFF888888)
    val (accentArgb, _) = accentAndDim(config.color.hex)

    // UNIFIED LAYOUT: Outer Box handles padding based on config
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(MainActivity::class.java))
            .padding(
                start = config.leftPadding.dp,
                end = config.rightPadding.dp,
                top = config.topPadding.dp
            ),
    ) {
        if (event == null) {
            Text("No countdowns", style = TextStyle(color = ColorProvider(subTextColor)))
        } else {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                val total = ChronoUnit.DAYS.between(event.startDate, event.targetDate).toInt().coerceAtLeast(1)
                val passed = ChronoUnit.DAYS.between(event.startDate, LocalDate.now()).toInt().coerceIn(0, total)

                // Calculate available space dynamically using config defaults (12, 12, 12, 64)
                val gridWidthDp = (spaceW - config.leftPadding - config.rightPadding).coerceAtLeast(10f)
                val gridHeightDp = (spaceH - config.topPadding - config.bottomPadding).coerceAtLeast(20f)

                val bitmap = DotGridRenderer.render(
                    context = LocalContext.current,
                    widthDp = gridWidthDp,
                    heightDp = gridHeightDp,
                    total = total,
                    passed = passed,
                    shape = config.shape.ordinal,
                    accentColor = accentArgb,
                    debug = debug,
                    dotSpacing = config.dotSpacing // Pass the adjustable spacing here
                )

                val rv = RemoteViews(LocalContext.current.packageName, R.layout.widget_grid).apply {
                    setImageViewBitmap(R.id.grid_image, bitmap)
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .defaultWeight()
                ) {
                    AndroidRemoteViews(
                        remoteViews = rv,
                        modifier = GlanceModifier.fillMaxSize()
                    )
                }

                Spacer(GlanceModifier.height(8.dp))

                // BOTTOM ROW: Title on Left, Days on Right
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = event.title,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(color = ColorProvider(subTextColor), fontSize = 13.sp),
                        maxLines = 1,
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        text = "${event.daysLeft()}",
                        style = TextStyle(color = ColorProvider(textColor), fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                    Text(
                        text = " days left",
                        style = TextStyle(color = ColorProvider(subTextColor), fontSize = 14.sp),
                        maxLines = 1,
                    )
                }

                if (debug) {
                    Spacer(GlanceModifier.height(4.dp))
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
private fun accentAndDim(hex: Long): Pair<Int, Int> {
    val ctx = LocalContext.current
    return when {
        hex >= 0 -> hex.toInt() to 0xFF2E2E2E.toInt()
        Build.VERSION.SDK_INT >= 31 -> {
            val s = dynamicDarkColorScheme(ctx)
            s.primary.toArgb() to s.surfaceVariant.toArgb()
        }
        else -> 0xFF4CAF50.toInt() to 0xFF2E2E2E.toInt()
    }
}
