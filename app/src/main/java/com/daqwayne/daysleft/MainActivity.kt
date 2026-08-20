package com.daqwayne.daysleft
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.ui.EventListScreen
import com.daqwayne.daysleft.widget.DaysLeftWidget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val repo by lazy { EventRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var events by remember { mutableStateOf(repo.loadEvents()) }

                EventListScreen(
                    events = events,
                    onSaveEvent = { event ->
                        val updated =
                            if (events.any { it.id == event.id }) {
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
                    },
                )
            }
        }
    }
}
