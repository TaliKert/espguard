package li.kta.espguard.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor")
data class SensorEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    var deviceId: String?,
    var name: String?,
    var turnedOn: Boolean?
)