package com.daqwayne.daysleft.widget

import androidx.glance.layout.width
import android.appwidget.AppWidgetManager
import com.daqwayne.daysleft.data.DebugPrefs
import android.content.Context
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.ui.text.buildAnnotatedString
import androidx.glance.layout.width
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.toArgb
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
import androidx.glance.LocalSize
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
import android.widget.RemoteViews
import com.daqwayne.daysleft.R
import androidx.glance.LocalContext
import androidx.glance.appwidget.AndroidRemoteViews
import com.daqwayne.daysleft.MainActivity
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.data.WidgetConfigRepository
import com.daqwayne.daysleft.model.CountdownEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import com.daqwayne.daysleft.model.DotShape
import com.daqwayne.daysleft.model.WidgetConfig
import com.daqwayne.daysleft.model.daysLeft
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

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
        
        // Cancel previous pending update
        updateJob?.cancel()
        
        // Wait 400ms for the resize drag to finish
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
    val bgColor = Color.Black
    val textColor = Color.White
    val subTextColor = Color(0xFF888888)
    val (accentArgb, dimArgb) = accentAndDim(config.color.hex)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(bgColor)
            .clickable(actionStartActivity(MainActivity::class.java))
            .padding(12.dp),
    ) {
        if (event == null) {
            Text("No countdowns", style = TextStyle(color = ColorProvider(subTextColor)))
        } else {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
            ) {
                val total = ChronoUnit.DAYS.between(event.startDate, event.targetDate).toInt().coerceAtLeast(1)
                val passed = ChronoUnit.DAYS.between(event.startDate, LocalDate.now()).toInt().coerceIn(0, total)

                // Calculate available space for the grid
                // 24dp accounts for the 12dp outer padding on left/right.
                // 64dp reserves space for the bottom text row + spacers so the grid doesn't overlap.
                val gridWidthDp = (spaceW - 24f).coerceAtLeast(10f)
                val gridHeightDp = (spaceH - 64f).coerceAtLeast(20f)

                val bitmap = DotGridRenderer.render(
                    context = LocalContext.current,
                    widthDp = gridWidthDp,
                    heightDp = gridHeightDp,
                    total = total,
                    passed = passed,
                    shape = config.shape.ordinal,
                    accentColor = accentArgb,
                    debug = debug,
                    leftPadding = config.leftPadding,
                    rightPadding = config.rightPadding,
                    topPadding = config.topPadding,
                    bottomPadding = config.bottomPadding,
                    dotSpacing = config.dotSpacing
                )

                val rv = RemoteViews(LocalContext.current.packageName, R.layout.widget_grid).apply {
                    setImageViewBitmap(R.id.grid_image, bitmap)
                }

                // The Grid takes up all remaining vertical space above the text
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
                // BOTTOM ROW: Title on Left, Days on Right
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    // Title takes up available space
                    Text(
                        text = event.title,
                        modifier = GlanceModifier.defaultWeight(),
                        style = TextStyle(
                            color = ColorProvider(subTextColor), 
                            fontSize = 13.sp
                        ),
                        maxLines = 1,
                    )
                    
                    Spacer(GlanceModifier.width(8.dp))
                    
                    // NUMBER (Bold)
                    Text(
                        text = "${event.daysLeft()}",
                        style = TextStyle(
                            color = ColorProvider(textColor), 
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold  // Only this part is bold
                        ),
                        maxLines = 1,
                    )
                    
                    // "days left" (Normal weight)
                    Text(
                        text = " days left",
                        style = TextStyle(
                            color = ColorProvider(subTextColor), 
                            fontSize = 14.sp
                            // No fontWeight = normal by default
                        ),
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
private fun DayGrid(
    event: CountdownEvent,
    accent: Color,
    dim: Color,
    shape: DotShape,
    spaceW: Float,
    spaceH: Float,
    debug: Boolean,
    config: WidgetConfig
) {
    val glyph = when (shape) {
        DotShape.SQUARE -> "■"
        DotShape.CIRCLE -> "●"
        DotShape.X -> "✕"
    }

    val total = ChronoUnit.DAYS.between(event.startDate, event.targetDate).toInt().coerceAtLeast(1)
    val passed = ChronoUnit.DAYS.between(event.startDate, LocalDate.now()).toInt().coerceIn(0, total)

    val fontScale = androidx.glance.LocalContext.current.resources.configuration.fontScale

    val spacing = config.dotSpacing
    val availW = spaceW - config.leftPadding - config.rightPadding
    val availH = spaceH - config.topPadding - config.bottomPadding
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

@Composable
private fun accentAndDim(hex: Long): Pair<Int, Int> {
    val ctx = LocalContext.current
    return when {
        hex >= 0 -> hex.toInt() to 0xFF2E2E2E.toInt()                     // preset
        Build.VERSION.SDK_INT >= 31 -> {
            val s = dynamicDarkColorScheme(ctx)
            s.primary.toArgb() to s.surfaceVariant.toArgb()               // Material You
        }
        else -> 0xFF4CAF50.toInt() to 0xFF2E2E2E.toInt()                  // pre-S fallback
    }
}
