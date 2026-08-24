package com.daqwayne.daysleft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.ui.EventListScreen
import com.daqwayne.daysleft.ui.SettingsScreen
import androidx.glance.appwidget.updateAll
import com.daqwayne.daysleft.widget.DaysLeftWidget
import kotlinx.coroutines.launch
import android.os.Build
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private val repo by lazy { EventRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            MaterialTheme(
                colorScheme = if (Build.VERSION.SDK_INT >= 31) {
                    dynamicDarkColorScheme(context)
                } else {
                    darkColorScheme()
                }
            ) 
          {
                var tab by remember { mutableStateOf(0) }
                var events by remember { mutableStateOf(repo.loadEvents()) }

                Box(Modifier.fillMaxSize()) {
                    when (tab) {
                        0 -> EventListScreen(
                            events = events,
                            onSaveEvent = { event ->
                                val updated = if (events.any { it.id == event.id }) {
                                    events.map { if (it.id == event.id) event else it }
                                } else {
                                    events + event
                                }
                                events = updated
                                repo.saveEvents(updated)
                                lifecycleScope.launch { DaysLeftWidget().updateAll(this@MainActivity) }
                            },
                            onDeleteEvent = { event ->
                                val updated = events.filterNot { it.id == event.id }
                                events = updated
                                repo.saveEvents(updated)
                                lifecycleScope.launch { DaysLeftWidget().updateAll(this@MainActivity) }
                            }
                        )
                        1 -> SettingsScreen(onDebugChanged = {
                            lifecycleScope.launch { DaysLeftWidget().updateAll(this@MainActivity) }
                        })
                    }

                    // FLOATING DOCK — MUST be the LAST child of this Box so it draws ON TOP
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp)
                            .background(Color(0xFF222222), RoundedCornerShape(32.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DockButton("●", selected = tab == 0) { tab = 0 }
                            DockButton("☰", selected = tab == 1) { tab = 1 }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch { DaysLeftWidget().updateAll(this@MainActivity) }
    }
}

@Composable
private fun DockButton(icon: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(if (selected) Color(0xFF333333) else Color.Transparent, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(icon, color = if (selected) Color.White else Color.Gray, fontSize = 20.sp)
    }
}
