package com.example.easystudy
import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.easystudy.database.EventDatabase


class App : Application() {
    private lateinit var database: EventDatabase

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            EventDatabase::class.java,
            "event-database"
        ).build()
    }
}