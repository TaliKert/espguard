package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_sensor.*
import li.kta.espguard.R
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class AddSensorActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_sensor)
    setSupportActionBar(findViewById(R.id.toolbar_support_add))

    button_add.setOnClickListener {
      addSensorToDb()
    }
  }

  private fun getUserEnteredSensor(): SensorEntity? {
    val allEditTextsHaveContent = listOf(et_sensor_name, et_sensor_id)
      .all { !TextUtils.isEmpty(it.text) }

    if (!allEditTextsHaveContent){
      return null
    }

    val sensorName = et_sensor_name.text.toString()
    val sensorId = et_sensor_id.text.toString()

    return SensorEntity(0, sensorId, sensorName, true)
  }

  private fun addSensorToDb() {
    val sensor = getUserEnteredSensor()
    if (sensor != null) {
      LocalSensorDb.getInstance(this).getSensorDao().insertSensors(sensor)
      Toast.makeText(this, "Added sensor ${sensor.name}", Toast.LENGTH_SHORT).show()
      finish()
    } else {
      Toast.makeText(this, "Some fields are empty", Toast.LENGTH_SHORT).show()
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