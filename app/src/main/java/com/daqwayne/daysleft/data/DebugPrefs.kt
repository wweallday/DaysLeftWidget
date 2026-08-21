package com.daqwayne.daysleft.data

import android.content.Context

object DebugPrefs {
    private const val PREFS = "daysleft_prefs"
    private const val KEY_DEBUG = "debug_mode"

    fun isDebug(context: Context): Boolean =
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_DEBUG, false)

    fun setDebug(
        context: Context,
        on: Boolean,
    ) {
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DEBUG, on)
            .apply()
    }
}
