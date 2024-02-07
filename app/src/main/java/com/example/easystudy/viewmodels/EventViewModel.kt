package com.example.easystudy.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.easystudy.database.EventDao
import com.example.easystudy.database.EventDatabase
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private val eventDao: EventDao
    val allEvents: LiveData<List<Event>>

    init {
        val database = EventDatabase.getDatabase(application)
        eventDao = database.eventDao()
        allEvents = eventDao.getAllEvents()
    }

    fun getEventsByType(eventType: EventType): LiveData<List<Event>> {
        return eventDao.getEventsByType(eventType)
    }

    fun insertEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            eventDao.insertEvent(event)
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            eventDao.deleteEvent(event)
        }
    }

    fun getEventById(eventId: Long): LiveData<Event> {
        return eventDao.getEventById(eventId)
    }

    fun getEventsByDate(date: LocalDate): LiveData<List<Event>> {
        return eventDao.getEventsByDate(date)
    }
}
