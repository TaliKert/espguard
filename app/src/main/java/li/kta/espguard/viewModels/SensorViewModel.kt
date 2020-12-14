package li.kta.espguard.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorDao
import li.kta.espguard.room.SensorEntity

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    private var sensorDao: SensorDao = LocalSensorDb.getSensorDao(application)
    var sensorArray: Array<SensorEntity> = arrayOf()

    init {
        refresh()
    }


    fun refresh() {
        sensorArray = sensorDao.loadAllSensors()
    }

}