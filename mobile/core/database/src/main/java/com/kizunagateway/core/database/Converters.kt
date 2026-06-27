package com.kizunagateway.core.database

import androidx.room.TypeConverter
import com.kizunagateway.domain.model.OutboundSmsStatus
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    @TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it, formatter) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.format(formatter)
    }

    @TypeConverter
    fun fromOutboundStatus(value: String?): OutboundSmsStatus? {
        return value?.let { OutboundSmsStatus.valueOf(it) }
    }

    @TypeConverter
    fun outboundStatusToString(status: OutboundSmsStatus?): String? {
        return status?.name
    }
}
