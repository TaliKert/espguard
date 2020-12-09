package li.kta.espguard.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.ZonedDateTime

@Entity(tableName = "sensor")
data class SensorEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var deviceId: String?,
    var name: String?,
    var turnedOn: Boolean,
    var lastHealthCheck: ZonedDateTime?,
    var successfulHealthCheck: ZonedDateTime?
)