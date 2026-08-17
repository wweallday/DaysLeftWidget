package com.daqwayne.daysleft.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun CountdownScreen(event: CountdownEvent) {
    // STATE: "now" is the only piece of state. Change it -> UI redraws.
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    // Tick every second forever
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            now = LocalDateTime.now()
        }
    }

    val remaining = Duration.between(now, event.targetDate.atStartOfDay())

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(event.title, fontSize = 20.sp, color = Color(0xFF888888))
        Text(
            "${event.daysLeft(now.toLocalDate())}",
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text("days left", color = Color(0xFF888888))
        Spacer(Modifier.height(24.dp))
        Text(formatHms(remaining), fontSize = 28.sp, color = Color(0xFFCCCCCC))
    }
}

private fun formatHms(d: Duration): String {
    val s = d.seconds.coerceAtLeast(0)
    return "%02d:%02d:%02d".format(s / 3600, (s % 3600) / 60, s % 60)
}
