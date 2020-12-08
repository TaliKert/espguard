package li.kta.espguard.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SensorEntity::class], version = 3)
abstract class LocalSensorDb: RoomDatabase() {

  companion object {
    private lateinit var dbInstance: LocalSensorDb

    @Synchronized
    fun getInstance(context: Context): LocalSensorDb {
      if(!this::dbInstance.isInitialized) {
        dbInstance = Room.databaseBuilder(
          context, LocalSensorDb::class.java, "mySensors")
          .fallbackToDestructiveMigration() // each time schema changes, data is lost!
          .allowMainThreadQueries() // if possible, use background thread instead
          .build()
      }
      return dbInstance

    }
  }

  abstract fun getSensorDao(): SensorDao

}