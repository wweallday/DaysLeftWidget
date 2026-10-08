package com.daqwayne.daysleft.ui

import com.daqwayne.daysleft.data.DebugPrefs
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.compose.runtime.Composable
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.OutlinedTextField // <-- ADDED MISSING IMPORT
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

        // --- FIX 1: setContent MUST be inside onCreate! ---
        setContent {
            val context = LocalContext.current
            
            // Consolidate into a SINGLE state object so config.copy() works
            var config by remember { mutableStateOf(initial) }

            MaterialTheme(
                colorScheme = if (Build.VERSION.SDK_INT >= 31) dynamicDarkColorScheme(context) else darkColorScheme()
            ) {
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
                    OptionRow("Nearest (auto)", selected = config.eventId == null) { config = config.copy(eventId = null) }
                    events.forEach { e ->
                        OptionRow(e.title, selected = config.eventId == e.id) { config = config.copy(eventId = e.id) }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("DOT SHAPE", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShapeButton("■", DotShape.SQUARE, config.shape) { config = config.copy(shape = it) }
                        ShapeButton("●", DotShape.CIRCLE, config.shape) { config = config.copy(shape = it) }
                        ShapeButton("✕", DotShape.X, config.shape) { config = config.copy(shape = it) }
                    }

                    Spacer(Modifier.height(24.dp))
                    Text("DOT COLOR", color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DotColor.entries.forEach { c -> ColorButton(c, config.color) { config = config.copy(color = it) } }
                    }

                    // --- DEBUG BLOCK ---
                    if (DebugPrefs.isDebug(context)) {
                        Text(
                            text = "🛠️ Debug Layout Settings",
                            fontWeight = FontWeight.Bold,
                            color = Color.Red,
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = config.leftPadding.toString(),
                            onValueChange = { config = config.copy(leftPadding = it.toFloatOrNull() ?: 12f) },
                            label = { Text("Left Padding (dp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                        
                        OutlinedTextField(
                            value = config.rightPadding.toString(),
                            onValueChange = { config = config.copy(rightPadding = it.toFloatOrNull() ?: 12f) },
                            label = { Text("Right Padding (dp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = config.topPadding.toString(),
                            onValueChange = { config = config.copy(topPadding = it.toFloatOrNull() ?: 12f) },
                            label = { Text("Top Padding (dp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = config.bottomPadding.toString(),
                            onValueChange = { config = config.copy(bottomPadding = it.toFloatOrNull() ?: 64f) },
                            label = { Text("Bottom Padding (dp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = config.dotSpacing.toString(),
                            onValueChange = { config = config.copy(dotSpacing = it.toFloatOrNull() ?: 4f) },
                            label = { Text("Dot Spacing (dp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }
                    // --- END DEBUG BLOCK ---

                    Spacer(Modifier.height(32.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (config.color == DotColor.MATERIAL_YOU && Build.VERSION.SDK_INT >= 31) 
                                    dynamicDarkColorScheme(LocalContext.current).primary 
                                else 
                                    Color(config.color.hex), 
                                CircleShape
                            )
                            .clickable {
                                lifecycleScope.launch {
                                    // Save the single config object
                                    configRepo.saveConfig(appWidgetId, config)
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
    } // <-- onCreate closes here!
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
