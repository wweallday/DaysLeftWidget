package com.daqwayne.daysleft.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daqwayne.daysleft.BuildConfig
import com.daqwayne.daysleft.data.DebugPrefs

@Composable
fun SettingsScreen(onDebugChanged: () -> Unit) {
    val context = LocalContext.current
    var debug by remember { mutableStateOf(DebugPrefs.isDebug(context)) }
    var tapCount by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "daqwayne",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable {
                tapCount++
                if (tapCount >= 3) {
                    tapCount = 0
                    debug = !debug
                    DebugPrefs.setDebug(context, debug)
                    onDebugChanged()   // pokes the widget to refresh
                    Toast.makeText(
                        context,
                        if (debug) "Debug mode ON 🐛" else "Debug mode OFF",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        Text("v${BuildConfig.VERSION_NAME}", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
        Text(
            if (debug) "🐛 debug mode: on" else "debug mode: off",
            color = if (debug) Color(0xFF4CAF50) else Color(0xFF555555),
            fontSize = 12.sp,
        )
    }
}
