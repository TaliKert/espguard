package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_sensor.*
import li.kta.espguard.R
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class SensorAddingActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SensorAddingActivity::class.java.name
        const val RESULT_ADDED_SENSOR = 201

        const val EXTRA_ADDED_SENSOR = "added_sensor"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_sensor)
        setSupportActionBar(findViewById(R.id.toolbar_support_add))

        button_add.setOnClickListener { addSensorToDb() }
    }

    private fun getUserEnteredSensor(): SensorEntity? {
        val someEditTextIsEmpty = listOf(et_sensor_name, et_sensor_id)
            .any { TextUtils.isEmpty(it.text) }

        if (someEditTextIsEmpty) return null

        val sensorName = et_sensor_name.text.toString()
        val sensorId = et_sensor_id.text.toString()

        return SensorEntity(deviceId = sensorId, name = sensorName)
    }

    private fun addSensorToDb() {
        Log.i(TAG, "Attempting to add sensor.")
        val sensor = getUserEnteredSensor()

        if (sensor == null) {
            Toast.makeText(this, "Some fields are empty", Toast.LENGTH_SHORT).show()
            return
        }

        Log.i(TAG, "Adding sensor $sensor")

        LocalSensorDb.getSensorDao(this).insertSensors(sensor)
        Toast.makeText(this, "Added sensor ${sensor.name}", Toast.LENGTH_SHORT).show()
        setResult(RESULT_ADDED_SENSOR, Intent().putExtra(EXTRA_ADDED_SENSOR, sensor))

        finish()
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