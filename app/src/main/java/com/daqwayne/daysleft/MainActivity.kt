package com.daqwayne.daysleft

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.daqwayne.daysleft.model.CountdownEvent
import com.daqwayne.daysleft.ui.CountdownScreen
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CountdownScreen(
                event =
                    CountdownEvent(
                        title = "Sample countdown",
                        targetDate = LocalDate.now().plusDays(41),
                    ),
            )
        }
    }
}
