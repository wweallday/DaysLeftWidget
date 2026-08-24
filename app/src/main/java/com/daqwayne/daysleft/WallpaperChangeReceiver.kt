package com.daqwayne.daysleft

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.daqwayne.daysleft.widget.DaysLeftWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WallpaperChangeReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == Intent.ACTION_WALLPAPER_CHANGED) {
            // Wallpaper changed! Force all widgets to re-render with the new palette
            CoroutineScope(Dispatchers.IO).launch {
                DaysLeftWidget().updateAll(context)
            }
        }
    }
}
