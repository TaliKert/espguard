package li.kta.espguard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class SensorViewModel(application: Application) : AndroidViewModel(application) {

  var localDb: LocalSensorDb = LocalSensorDb.getInstance(application)
  var sensorArray: Array<SensorEntity> = arrayOf()

  init {
    refresh()
  }

  fun refresh(){
    sensorArray = localDb.getSensorDao().loadAllSensors()
  }

}