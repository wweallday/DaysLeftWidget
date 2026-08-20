package com.daqwayne.daysleft.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.daqwayne.daysleft.MainActivity
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.model.daysLeft
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class DaysLeftWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysLeftWidget()
}

class DaysLeftWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val events = EventRepository(context).loadEvents()
        val event = events.minByOrNull { it.targetDate }

        provideContent {
            WidgetContent(event)
        }
    }
}

@Composable
private fun WidgetContent(event: CountdownEvent?) {
    val accentColor = androidx.compose.ui.graphics.Color(0xFF4CAF50)
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
                DayGrid(event, accentColor, dimColor)
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
private fun DayGrid(event: CountdownEvent, accent: androidx.compose.ui.graphics.Color, dim: androidx.compose.ui.graphics.Color) {
    val total = ChronoUnit.DAYS.between(event.startDate, event.targetDate).toInt().coerceAtLeast(1)
    val passed = ChronoUnit.DAYS.between(event.startDate, LocalDate.now()).toInt().coerceIn(0, total)
    val cells = total.coerceAtMost(35)
    val cols = 7

    Column {
        val rows = (cells + cols - 1) / cols
        for (r in 0 until rows) {
            Row {
                for (c in 0 until cols) {
                    val i = r * cols + c
                    if (i < cells) {
                        val dayAtCell = (i.toLong() * total) / cells
                        Box(
                            modifier = GlanceModifier
                                .size(8.dp)
                                .background(if (dayAtCell < passed) dim else accent)
                        ) { }
                        if (c < cols - 1) Spacer(GlanceModifier.width(4.dp))
                    } else {
                        Box(
                            modifier = GlanceModifier.size(8.dp)
                        ) { }
                        if (c < cols - 1) Spacer(GlanceModifier.width(4.dp))
                    }
                }
            }
            if (r < rows - 1) Spacer(GlanceModifier.height(4.dp))
        }
    }
}
