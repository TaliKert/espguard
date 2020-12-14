package li.kta.espguard.room

import androidx.room.*

@Dao
interface EventDao {

    @Delete
    fun deleteEvents(vararg events: EventEntity)

    @Query("DELETE FROM event")
    fun nukeTable()

    @Query("SELECT * FROM event WHERE deviceId==:deviceId")
    fun findEventsByDeviceId(deviceId: String): Array<EventEntity>

    @Query("SELECT * FROM event")
    fun loadEvents(): Array<EventEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertEvents(vararg eventEntity: EventEntity)

}