package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_configure_sensor.*
import li.kta.espguard.MqttService
import li.kta.espguard.R
import li.kta.espguard.room.LocalSensorDb

class ConfigureSensorActivity : AppCompatActivity() {

  companion object {
    val TAG = ConfigureSensorActivity::class.java.name
    const val EXTRA_SENSOR_ID = "sensorId"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_configure_sensor)
    setSupportActionBar(findViewById(R.id.toolbar_support_configure))

    val id = intent.getIntExtra(SensorDetailsActivity.EXTRA_SENSOR_ID, -1)


    button_health_check.setOnClickListener {
      var sensor = LocalSensorDb.getInstance(this).getSensorDao().findSensorById(id)
      MqttService.getInstance()?.healthCheck(sensor)
    }
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.menu_toolbar, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.open_settings) {
      startActivity(Intent(this, SettingsActivity::class.java))
      return true
    }
    return super.onOptionsItemSelected(item)
  }

}