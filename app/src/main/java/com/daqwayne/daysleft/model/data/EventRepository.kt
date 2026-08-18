package com.daqwayne.daysleft.data

import android.content.Context
import com.daqwayne.daysleft.model.CountdownEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate

class EventRepository(
    private val context: Context,
) {
    private val file = File(context.filesDir, "events.json")

    fun loadEvents(): List<CountdownEvent> {
        if (!file.exists()) return emptyList()
        val jsonString = file.readText()
        val jsonArray = JSONArray(jsonString)

        return (0 until jsonArray.length()).map { i ->
            val obj = jsonArray.getJSONObject(i)
            CountdownEvent(
                title = obj.getString("title"),
                targetDate = LocalDate.parse(obj.getString("date")),
            )
        }
    }

    fun saveEvents(events: List<CountdownEvent>) {
        val jsonArray = JSONArray()
        events.forEach { event ->
            val obj =
                JSONObject().apply {
                    put("title", event.title)
                    put("date", event.targetDate.toString())
                }
            jsonArray.put(obj)
        }
        file.writeText(jsonArray.toString())
    }
}
