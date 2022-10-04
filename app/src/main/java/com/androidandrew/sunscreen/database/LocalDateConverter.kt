package com.androidandrew.sunscreen.database

import androidx.room.TypeConverter
import java.time.LocalDate

object LocalDateConverter {

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString)
    }

    @TypeConverter
    fun toString(date: LocalDate): String {
        return date.toString()
    }
}