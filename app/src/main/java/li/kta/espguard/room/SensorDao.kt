package li.kta.espguard.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorDao {

  @Query("SELECT id FROM sensor")
  fun loadSensorIds(): Array<Int>

  @Query("SELECT * FROM sensor")
  fun loadSensors(): Array<SensorEntity>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertSensors(vararg sensorEntity: SensorEntity)

}