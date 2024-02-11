package com.example.easystudy.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val type: EventType,
    val teacher: String,
    val repeat: RepeatType,
    val location: String,
    val count: Number,
    var grade: Number? = null
)