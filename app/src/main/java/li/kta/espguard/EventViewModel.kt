package li.kta.espguard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import li.kta.espguard.room.EventEntity
import li.kta.espguard.room.LocalSensorDb

class EventViewModel(application: Application) : AndroidViewModel(application) {
    private var sensorDb: LocalSensorDb = LocalSensorDb.getInstance(application)

    val events: ArrayList<EventEntity> = arrayListOf()

    fun refresh() {
        /*events = sensorDb.getSensorDao().loadSensors()*/
    }
}