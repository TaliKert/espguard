package li.kta.espguard.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SensorEntity::class, EventEntity::class], version = 4)
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