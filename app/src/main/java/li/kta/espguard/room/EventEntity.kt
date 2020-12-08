package li.kta.espguard.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "event")
data class EventEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var sensorId: Int,
        var time: String?
)
