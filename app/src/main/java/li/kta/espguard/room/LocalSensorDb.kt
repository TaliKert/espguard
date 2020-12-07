package li.kta.espguard.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SensorEntity::class], version = 1)
abstract class LocalSensorDb: RoomDatabase() {

  abstract fun getSensorDao(): SensorDao

}