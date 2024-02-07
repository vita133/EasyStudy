package com.example.easystudy.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate


object DateConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    @JvmStatic
    fun toDate(timestamp: Long?): LocalDate? {
        return timestamp?.let { LocalDate.ofEpochDay(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    @JvmStatic
    fun toTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}
