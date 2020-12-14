package li.kta.espguard.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import li.kta.espguard.room.EventDao
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb

class EventViewModel(application: Application) : AndroidViewModel(application) {

    private var eventDao: EventDao = LocalSensorDb.getEventDao(application)
    var eventsArray: Array<EventEntity> = arrayOf()
    var deviceId: String = ""

    init {
        refresh()
    }


    fun refresh() {
        eventsArray = eventDao.findEventsByDeviceId(deviceId)
    }

}