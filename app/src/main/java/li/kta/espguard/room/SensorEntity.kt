package li.kta.espguard.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensor")
data class SensorEntity (
  @PrimaryKey var id: Int,
  var turnedOn : Boolean?
)