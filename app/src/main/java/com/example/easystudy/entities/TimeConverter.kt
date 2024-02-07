package com.example.easystudy.entities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalTime

object TimeConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    @JvmStatic
    fun toTime(value: String?): LocalTime? {
        return value?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    @JvmStatic
    fun fromTime(time: LocalTime?): String? {
        return time?.toString()
    }
}
