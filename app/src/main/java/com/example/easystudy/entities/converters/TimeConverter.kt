package com.example.easystudy.entities.converters

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalTime

object TimeConverter {
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
