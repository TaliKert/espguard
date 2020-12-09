package li.kta.espguard.room

import androidx.room.*

@Dao
interface EventDao {

  @Delete
  fun deleteEvents(vararg events: EventEntity)

  @Query("SELECT * FROM event WHERE id==:id")
  fun findEventsByDeviceId(id: Int): Array<EventEntity>

  @Insert(onConflict = OnConflictStrategy.ABORT)
  fun insertEvents(vararg eventEntity: EventEntity)

}