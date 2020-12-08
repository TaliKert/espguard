package li.kta.espguard.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SensorDao {

  @Query("SELECT * FROM sensor")
  fun loadSensors(): Array<SensorEntity>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertSensors(vararg sensorEntity: SensorEntity)


    /*@Query("SELECT * FROM event WHERE sensorId==:sensorId")
    fun loadEvents(sensorId: Int): Array<EventEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertEvents(vararg eventEntity: EventEntity)*/
}