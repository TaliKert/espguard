package li.kta.espguard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import li.kta.espguard.room.SensorEntity

class SensorViewModel(application: Application) : AndroidViewModel(application) {

  var sensorArray: Array<SensorEntity> = arrayOf()

  fun refresh(){

  }

}