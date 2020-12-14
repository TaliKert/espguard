package li.kta.espguard.room

import androidx.room.TypeConverter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object ZDTConverter {

    private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    @TypeConverter
    @JvmStatic
    fun toOffsetDateTime(value: String?): ZonedDateTime? =
            value?.let { formatter.parse(value, ZonedDateTime::from) }

    @TypeConverter
    @JvmStatic
    fun fromOffsetDateTime(date: ZonedDateTime?): String? = date?.format(formatter)

}