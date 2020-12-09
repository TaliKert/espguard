package li.kta.espguard.room

import androidx.room.*

@Dao
interface SensorDao {

  @Query("SELECT * FROM sensor")
  fun loadAllSensors(): Array<SensorEntity>

  @Query("SELECT * FROM sensor WHERE id==:id")
  fun findSensorById(id: Int): SensorEntity

  @Query("SELECT * FROM sensor WHERE deviceId = :deviceId")
  fun findSensorByDeviceId(deviceId: String): SensorEntity

  @Delete
  fun deleteSensor(vararg sensors: SensorEntity)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertSensors(vararg sensorEntity: SensorEntity)

  @Update
  fun updateSensor(vararg sensors: SensorEntity)

}