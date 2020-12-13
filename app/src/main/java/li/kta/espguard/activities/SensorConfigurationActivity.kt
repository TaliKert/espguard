package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_configure_sensor.*
import li.kta.espguard.MqttService
import li.kta.espguard.R
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class SensorConfigurationActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SensorConfigurationActivity::class.java.name
        const val EXTRA_SENSOR_ID = "sensorId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_sensor)
        setSupportActionBar(findViewById(R.id.toolbar_support_configure))

        val id = intent.getIntExtra(SensorDetailsActivity.EXTRA_SENSOR_ID, -1)
        val sensor = LocalSensorDb.getSensorDao(this).findSensorById(id)

        button_health_check.setOnClickListener {
            MqttService.getInstance()?.healthCheck(sensor)
        }

        switch_sensor_on.isChecked = sensor.turnedOn

        switch_sensor_on.setOnCheckedChangeListener { _, isSwitchedOn ->
            sensor.turnedOn = isSwitchedOn
            LocalSensorDb.getSensorDao(this).updateSensor(sensor)
            MqttService.getInstance()?.turnOnOff(sensor)
        }

        button_delete_device.setOnClickListener { deleteSensor(id) }
    }

    private fun deleteSensor(id: Int) {
        val sensor = LocalSensorDb.getSensorDao(applicationContext).findSensorById(id)

        Log.i(TAG, "Deleting sensor $sensor")

        removeEventsFromDatabase(sensor)
        removeSensorFromDatabase(sensor)

        Toast.makeText(this, "Deleted device", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun removeEventsFromDatabase(sensor: SensorEntity) {
        sensor.deviceId?.let {
            LocalSensorDb.getEventDao(applicationContext).deleteEvents(
                    *LocalSensorDb.getEventDao(applicationContext).findEventsByDeviceId(it))
        }
    }

    private fun removeSensorFromDatabase(sensor: SensorEntity) {
        LocalSensorDb.getSensorDao(applicationContext).deleteSensor(sensor)
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