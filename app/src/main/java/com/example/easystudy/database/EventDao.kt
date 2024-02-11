package com.example.easystudy.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import java.time.LocalDate

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE type = :eventType")
    fun getEventsByType(eventType: EventType): LiveData<List<Event>>

    @Query("SELECT * FROM events")
    fun getAllEvents(): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE date = :date")
    fun getEventsByDate(date: LocalDate): LiveData<List<Event>>

    @Query("SELECT * FROM events WHERE id = :eventId")
    fun getEventById(eventId: Long): LiveData<Event>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<Event>)

    @Delete
    suspend fun deleteEvent(event: Event)

    @Query("DELETE FROM events WHERE id = :eventId")
    suspend fun deleteEventById(eventId: Long)

    @Query("UPDATE events SET grade = :newGrade WHERE id = :eventId")
    suspend fun updateGrade(eventId: Long, newGrade: Int)
}
