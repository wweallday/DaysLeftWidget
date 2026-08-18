package com.daqwayne.daysleft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.daqwayne.daysleft.data.EventRepository
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.ui.EventListScreen

class MainActivity : ComponentActivity() {
    // The Repository handles our JSON file
    private val repo by lazy { EventRepository(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Load events from JSON file when app starts
            var events by remember { mutableStateOf(repo.loadEvents()) }

            EventListScreen(
                events = events,
                onAddEvent = { newEvent ->
                    // Add to list in memory
                    val updatedList = events + newEvent
                    events = updatedList
                    // Save the new list to JSON file on disk
                    repo.saveEvents(updatedList)
                },
            )
        }
    }
}
