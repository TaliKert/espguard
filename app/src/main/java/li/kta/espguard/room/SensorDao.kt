package li.kta.espguard.room

import androidx.room.*
import java.time.ZonedDateTime

@Dao
interface SensorDao {

  @Query("SELECT * FROM sensor")
  fun loadAllSensors(): Array<SensorEntity>

  @Query("SELECT * FROM sensor WHERE id = :id LIMIT 1")
  fun findSensorById(id: Int): SensorEntity

  @Query("SELECT * FROM sensor WHERE deviceId = :deviceId LIMIT 1")
  fun findSensorByDeviceId(deviceId: String): SensorEntity?

  @Delete
  fun deleteSensor(vararg sensors: SensorEntity)

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertSensors(vararg sensorEntity: SensorEntity)

  @Update
  fun updateSensor(vararg sensors: SensorEntity)

  @Query("UPDATE sensor SET name = :name WHERE id = :id")
  fun updateSensorName(name: String, id: Int)

  @Query("UPDATE sensor SET turnedOn = :turnedOn WHERE id = :id")
  fun updateSensorTurnedOn(turnedOn: Boolean, id: Int)

  @Query("UPDATE sensor SET lastHealthCheck = :lastHealthCheck WHERE id = :id")
  fun updateSensorLastHealthCheck(lastHealthCheck: ZonedDateTime, id: Int)
}