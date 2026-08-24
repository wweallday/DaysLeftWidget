package com.daqwayne.daysleft.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.runtime.Composable
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Brush
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.lifecycleScope
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.data.WidgetConfigRepository
import com.daqwayne.daysleft.model.DotColor
import com.daqwayne.daysleft.model.DotShape
import com.daqwayne.daysleft.model.WidgetConfig
import com.daqwayne.daysleft.widget.DaysLeftWidget
import kotlinx.coroutines.launch

class WidgetConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        val appWidgetId = intent?.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }

        val events = EventRepository(this).loadEvents()
        val configRepo = WidgetConfigRepository(this)
        val initial = configRepo.getConfig(appWidgetId)

        setContent {
            val context = LocalContext.current
            MaterialTheme(
                colorScheme = if (Build.VERSION.SDK_INT >= 31) dynamicDarkColorScheme(context) else darkColorScheme()
            ) {
                var eventId by remember { mutableStateOf(initial.eventId) }
                var shape by remember { mutableStateOf(initial.shape) }
                var color by remember { mutableStateOf(initial.color) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    Text("Widget settings", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(24.dp))

                    Text("EVENT", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OptionRow("Nearest (auto)", selected = eventId == null) { eventId = null }
                    events.forEach { e ->
                        OptionRow(e.title, selected = eventId == e.id) { eventId = e.id }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("DOT SHAPE", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShapeButton("■", DotShape.SQUARE, shape) { shape = it }
                        ShapeButton("●", DotShape.CIRCLE, shape) { shape = it }
                        ShapeButton("✕", DotShape.X, shape) { shape = it }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("DOT COLOR", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DotColor.entries.forEach { c -> ColorButton(c, color) { color = it } }
                    }

                    Spacer(Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (color == DotColor.MATERIAL_YOU && Build.VERSION.SDK_INT >= 31) 
                                    dynamicDarkColorScheme(LocalContext.current).primary 
                                else 
                                    Color(color.hex), 
                                CircleShape
                            )
                            .clickable {
                                lifecycleScope.launch {
                                    configRepo.saveConfig(
                                        appWidgetId, WidgetConfig(eventId, shape, color)
                                    )
                                    val manager = GlanceAppWidgetManager(this@WidgetConfigActivity)
                                    val glanceId = manager.getGlanceIdBy(appWidgetId)
                                    DaysLeftWidget().update(this@WidgetConfigActivity, glanceId)

                                    setResult(
                                        Activity.RESULT_OK,
                                        Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                    )
                                    finish()
                                }
                            }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Save widget", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = { onClick() }).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (selected) "● " else "○ ", color = if (selected) Color.White else Color.Gray)
        Text(label, color = if (selected) Color.White else Color.Gray, fontSize = 16.sp)
    }
}

@Composable
private fun ShapeButton(glyph: String, s: DotShape, current: DotShape, onPick: (DotShape) -> Unit) {
    Text(
        glyph,
        fontSize = 24.sp,
        color = if (current == s) Color.White else Color(0xFF555555),
        modifier = Modifier.clickable { onPick(s) }.padding(8.dp)
    )
}

@Composable
private fun ColorButton(c: DotColor, current: DotColor, onPick: (DotColor) -> Unit) {
    val isSelected = current == c
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(if (isSelected) Color.White else Color.Transparent, CircleShape)
            .clickable { onPick(c) },
        contentAlignment = Alignment.Center
    ) {
        val innerModifier = if (c == DotColor.MATERIAL_YOU) {
            // Draw the rainbow gradient for Material You
            Modifier.size(28.dp).background(
                brush = Brush.sweepGradient(
                    listOf(
                        Color(0xFFE57373), Color(0xFFFFF176), Color(0xFF81C784),
                        Color(0xFF64B5F6), Color(0xFFBA68C8), Color(0xFFE57373)
                    )
                ),
                shape = CircleShape
            )
        } else {
            // Draw the solid hex color for normal colors
            Modifier.size(28.dp).background(Color(c.hex), CircleShape)
        }
        
        Box(innerModifier)
    }
}
