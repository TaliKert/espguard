package li.kta.espguard.room

import android.content.Context
import androidx.room.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Database(entities = [SensorEntity::class, EventEntity::class], version = 5, exportSchema = false)
@TypeConverters(ZDTConverter::class)
abstract class LocalSensorDb: RoomDatabase() {

  companion object {
    private lateinit var dbInstance: LocalSensorDb

        @Synchronized
        fun getInstance(context: Context): LocalSensorDb {
            if (this::dbInstance.isInitialized) return dbInstance

            dbInstance = Room.databaseBuilder(context, LocalSensorDb::class.java, "myDatabase")
                    .fallbackToDestructiveMigration() // each time schema changes, data is lost!
                    .allowMainThreadQueries() // if possible, use background thread instead
                    .build()

            return dbInstance
        }
    }

  abstract fun getSensorDao(): SensorDao

  abstract fun getEventDao(): EventDao

}

object ZDTConverter {
  private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  @TypeConverter
  @JvmStatic
  fun toOffsetDateTime(value: String?): ZonedDateTime? {
    return value?.let {
      return formatter.parse(value, ZonedDateTime::from)
    }
  }

  @TypeConverter
  @JvmStatic
  fun fromOffsetDateTime(date: ZonedDateTime?): String? {
    return date?.format(formatter)
  }
}