package li.kta.espguard.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "event")
data class EventEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var deviceId: String?,
        val event_time: Date?
)
