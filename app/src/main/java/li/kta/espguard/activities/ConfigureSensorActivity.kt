package li.kta.espguard.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import li.kta.espguard.R

class ConfigureSensorActivity : AppCompatActivity() {

  companion object {
    val TAG = ConfigureSensorActivity::class.java.name
    const val EXTRA_SENSOR_ID = "sensorId"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_configure_sensor)
  }

}