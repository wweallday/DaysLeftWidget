package com.daqwayne.daysleft.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.model.daysLeft
import java.time.LocalDate

@Composable
fun EventListScreen(
    events: List<CountdownEvent>,
    onAddEvent: (CountdownEvent) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF222222),
                contentColor = Color.White
            ) {
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
                    EventCard(event)
                }
            }
        }
    }

    if (showDialog) {
        AddEventDialog(
            onDismiss = { showDialog = false },
            onConfirm = { newEvent ->
                onAddEvent(newEvent)
                showDialog = false
            }
        )
    }
}

@Composable
private fun EventCard(event: CountdownEvent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF111111), MaterialTheme.shapes.medium)
            .padding(24.dp)
    ) {
        Text(event.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                "${event.daysLeft()}",
                color = Color(0xFF4CAF50), // Nice OLED green
                fontSize = 48.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("days left", color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (CountdownEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var daysString by remember { mutableStateOf("30") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF222222),
        title = { Text("New Countdown", color = Color.White) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                    ),
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = daysString,
                    onValueChange = { daysString = it },
                    label = { Text("Days from today") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val days = daysString.toIntOrNull() ?: 30
                onConfirm(CountdownEvent(title.ifEmpty { "Untitled" }, LocalDate.now().plusDays(days.toLong())))
            }) {
                Text("Save", color = Color(0xFF4CAF50))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}
