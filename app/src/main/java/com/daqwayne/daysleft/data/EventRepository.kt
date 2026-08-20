package com.daqwayne.daysleft.data

import android.content.Context
import com.daqwayne.daysleft.model.CountdownEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate

class EventRepository(
    private val context: Context,
) {
    private val file = File(context.filesDir, "events.json")
    private val gson = Gson()

    fun loadEvents(): List<CountdownEvent> {
        if (!file.exists()) return emptyList()
        val jsonString = file.readText()
        val type = object : TypeToken<List<EventJson>>() {}.type
        val jsonList: List<EventJson> =
            try {
                gson.fromJson(jsonString, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList() // Failsafe if the file is corrupted
            }
        return jsonList.map { it.toEvent() }
    }

    fun saveEvents(events: List<CountdownEvent>) {
        val jsonList = events.map { EventJson.fromEvent(it) }
        file.writeText(gson.toJson(jsonList))
    }
}

// Helper class to handle LocalDate safely
private data class EventJson(
    val id: Long,
    val startDate: String,
    val title: String,
    val targetDate: String,
) {
    fun toEvent() =
        CountdownEvent(
            id = id,
            startDate = LocalDate.parse(startDate),
            title = title,
            targetDate = LocalDate.parse(targetDate),
        )

    companion object {
        fun fromEvent(event: CountdownEvent) =
            EventJson(
                id = event.id,
                startDate = event.startDate.toString(),
                title = event.title,
                targetDate = event.targetDate.toString(),
            )
    }
}
