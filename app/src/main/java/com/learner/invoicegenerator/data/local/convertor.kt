package com.learner.invoicegenerator.data.local

import androidx.room.TypeConverter
import java.time.LocalDate


class convertor {

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}