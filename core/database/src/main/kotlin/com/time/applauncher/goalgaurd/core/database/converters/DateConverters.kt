package com.time.applauncher.goalgaurd.core.database.converters

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

class DateConverters {
    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun toLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun fromLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun toLocalDateTime(date: LocalDateTime?): String? = date?.toString()

    @TypeConverter
    fun fromInstant(value: String?): Instant? = value?.let { Instant.parse(it) }

    @TypeConverter
    fun toInstant(instant: Instant?): String? = instant?.toString()
}
