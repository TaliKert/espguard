package li.kta.espguard.room

import androidx.room.*

@Dao
interface SensorDao {

  @Query("SELECT * FROM sensor")
  fun loadAllSensors(): Array<SensorEntity>

  @Query("SELECT * FROM sensor WHERE id==:id")
  fun findSensorById(id: Int): SensorEntity

  @Delete
  fun deleteSensor(vararg sensors: SensorEntity)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertSensors(vararg sensorEntity: SensorEntity)

}