package com.example.easystudy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.easystudy.entities.DateConverter
import com.example.easystudy.entities.TimeConverter
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.NumberConverter

@Database(entities = [Event::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, TimeConverter::class, NumberConverter::class)
abstract class EventDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao

    companion object {
        @Volatile
        private var INSTANCE: EventDatabase? = null

        fun getDatabase(context: Context): EventDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EventDatabase::class.java,
                    "event_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
