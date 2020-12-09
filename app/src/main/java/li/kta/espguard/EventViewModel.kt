package li.kta.espguard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private var localDb: LocalSensorDb = LocalSensorDb.getInstance(application)
    var eventsArray: Array<EventEntity> = arrayOf()
    var deviceId: String = ""

    init {
        refresh()
    }

    fun refresh() {
        eventsArray = localDb.getEventDao().findEventsByDeviceId(deviceId)
    }
}