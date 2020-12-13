package li.kta.espguard.room

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import li.kta.espguard.R
import java.time.ZonedDateTime

@Parcelize
@Entity(tableName = "sensor")
data class SensorEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int = 0,
        var deviceId: String?,
        var name: String?,
        var turnedOn: Boolean = false,
        var lastHealthCheck: ZonedDateTime? = null,
        var successfulHealthCheck: ZonedDateTime? = null
) : Parcelable {

  enum class Status(val iconResource: Int, val textResource: Int) {
        FAILED(R.drawable.ic_failed_24, R.string.sensor_status_failed),
        PENDING(R.drawable.ic_pending_24, R.string.sensor_status_pending),
        HEALTHY(R.drawable.ic_healthy_24, R.string.sensor_status_healthy),
        SWITCHED_OFF(R.drawable.ic_switched_off_24, R.string.sensor_status_off)
    }

    fun getStatus(): Status {
        val last = lastHealthCheck
        val prevSuccess = successfulHealthCheck

        if (last == null || prevSuccess == null
                || prevSuccess.plusSeconds(14).isBefore(ZonedDateTime.now()))
            return Status.FAILED

        if (!last.isBefore(prevSuccess)) return Status.PENDING

        return if (turnedOn) Status.HEALTHY else Status.SWITCHED_OFF
    }
}