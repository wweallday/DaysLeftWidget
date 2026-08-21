package com.daqwayne.daysleft.data

import android.content.Context
import com.daqwayne.daysleft.model.WidgetConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class WidgetConfigRepository(
    private val context: Context,
) {
    private val file = File(context.filesDir, "widget_config.json")
    private val gson = Gson()

    private fun loadAll(): MutableMap<String, WidgetConfig> {
        if (!file.exists()) return mutableMapOf()
        return try {
            val type = object : TypeToken<MutableMap<String, WidgetConfig>>() {}.type
            gson.fromJson(file.readText(), type) ?: mutableMapOf()
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    fun getConfig(widgetId: Int): WidgetConfig = loadAll()[widgetId.toString()] ?: WidgetConfig()

    fun saveConfig(
        widgetId: Int,
        config: WidgetConfig,
    ) {
        val all = loadAll()
        all[widgetId.toString()] = config
        file.writeText(gson.toJson(all))
    }
}
