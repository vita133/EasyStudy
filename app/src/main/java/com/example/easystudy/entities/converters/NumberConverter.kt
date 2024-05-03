package com.example.easystudy.entities.converters
import androidx.room.TypeConverter

class NumberConverter {
    @TypeConverter
    fun fromNumber(value: Number?): Long? {
        return value?.toLong()
    }

    @TypeConverter
    fun toNumber(value: Long?): Number? {
        return value?.let { it }
    }
}