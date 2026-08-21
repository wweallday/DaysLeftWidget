package com.daqwayne.daysleft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.DisplayMode
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.model.daysLeft
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun EventListScreen(
    events: List<CountdownEvent>,
    onSaveEvent: (CountdownEvent) -> Unit,
    onDeleteEvent: (CountdownEvent) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<CountdownEvent?>(null) }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Text("+", fontSize = 24.sp)
            }
        }
    ) { padding ->
        if (events.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No events yet.\nTap + to add one.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(events) { event ->
                    EventCard(
                        event = event,
                        onClick = { editingEvent = event },
                        onDelete = { onDeleteEvent(event) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        EventFormDialog(
            initial = null,
            onDismiss = { showAddDialog = false },
            onSave = { event ->
                onSaveEvent(event)
                showAddDialog = false
            }
        )
    }

    editingEvent?.let { event ->
        EventFormDialog(
            initial = event,
            onDismiss = { editingEvent = null },
            onSave = { updated ->
                onSaveEvent(updated)
                editingEvent = null
            }
        )
    }
}

@Composable
private fun EventCard(
    event: CountdownEvent,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111), MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(event.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "${event.daysLeft()} days left",
                color = Color(0xFF4CAF50),
                fontSize = 16.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                event.targetDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onDelete) {
            Text("✕", color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventFormDialog(
    initial: CountdownEvent?,
    onDismiss: () -> Unit,
    onSave: (CountdownEvent) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }

    val initialStartMillis = (initial?.startDate ?: LocalDate.now())
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialStartMillis,
        initialDisplayMode = DisplayMode.Input   // compact text style, not a giant calendar
    )

    val initialTargetMillis = (initial?.targetDate ?: LocalDate.now().plusDays(30))
        .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val targetDatePickerState = rememberDatePickerState(initialSelectedDateMillis = initialTargetMillis)

    var showStartPicker by remember { mutableStateOf(false) }

    val startMillis = startDatePickerState.selectedDateMillis ?: initialStartMillis
    val startDate = Instant.ofEpochMilli(startMillis)
        .atZone(ZoneId.systemDefault()).toLocalDate()
    val startText = if (startDate == LocalDate.now()) "Today"
        else startDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val targetDate = Instant.ofEpochMilli(
                    targetDatePickerState.selectedDateMillis ?: initialTargetMillis
                ).atZone(ZoneId.systemDefault()).toLocalDate()

                onSave(
                    CountdownEvent(
                        id = initial?.id ?: System.currentTimeMillis(),
                        startDate = startDate,
                        title = title.ifEmpty { "Untitled" },
                        targetDate = targetDate
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Compact start row — tap to reveal the picker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Starts:", color = Color.Gray)
                TextButton(onClick = { showStartPicker = !showStartPicker }) {
                    Text("$startText ${if (showStartPicker) "▴" else "▾"}", color = Color.White)
                }
            }
            AnimatedVisibility(visible = showStartPicker) {
                DatePicker(state = startDatePickerState)
            }

            Spacer(Modifier.height(8.dp))
            Text("Target Date", color = Color.White, fontWeight = FontWeight.Bold)
            DatePicker(state = targetDatePickerState)
        }
    }
}
