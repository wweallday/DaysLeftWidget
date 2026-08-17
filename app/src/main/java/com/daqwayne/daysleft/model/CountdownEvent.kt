package com.daqwayne.daysleft.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CountdownEvent(
    val title: String,
    val targetDate: LocalDate,
)

fun CountdownEvent.daysLeft(today: LocalDate = LocalDate.now()): Long = ChronoUnit.DAYS.between(today, targetDate)
