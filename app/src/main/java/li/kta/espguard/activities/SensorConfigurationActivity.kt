package li.kta.espguard.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_configure_sensor.*
import li.kta.espguard.R
import li.kta.espguard.helpers.ToastHelper.toast
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity
import li.kta.espguard.services.MqttService

class SensorConfigurationActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SensorConfigurationActivity::class.java.name
        const val EXTRA_SENSOR_ID = "sensorId"

        const val RESULT_DELETE_SENSOR = 201
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configure_sensor)
        setSupportActionBar(findViewById(R.id.toolbar_support_configure))

        findSensorEntity(getExtraSensorId())?.let { sensor -> setupButtons(sensor) }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
            if (item.itemId == R.id.open_settings) {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            } else super.onOptionsItemSelected(item)


    private fun setupButtons(sensor: SensorEntity) {
        switch_sensor_on.isChecked = sensor.turnedOn
        switch_sensor_on.setOnCheckedChangeListener { _, _ -> toggleOnSwitch(sensor) }

        button_sensor_name_save.setOnClickListener { saveNewName(sensor) }

        button_delete_events.setOnClickListener { deleteEvents(sensor) }
        button_delete_device.setOnClickListener { deleteSensor(sensor) }
    }

    private fun toggleOnSwitch(sensor: SensorEntity) {
        sensor.turnedOn = !sensor.turnedOn
        LocalSensorDb.getSensorDao(applicationContext)
                .updateSensorTurnedOn(sensor.turnedOn, sensor.id)
        MqttService.getInstance()?.turnOnOff(sensor)
    }

    private fun saveNewName(sensor: SensorEntity) {
        val newName = et_sensor_name.text.toString()

        if (newName.isEmpty()) {
            toast(this, R.string.toast_sensor_rename_fail)
            return
        }

        Log.i(TAG, "Changing name of $sensor to $newName")
        LocalSensorDb.getSensorDao(applicationContext).updateSensorName(newName, sensor.id)

        toast(this, R.string.toast_rename_to, newName)
    }

    private fun deleteEvents(sensor: SensorEntity) {
        Log.i(TAG, "Deleting events of $sensor")
        removeEventsFromDatabase(sensor)

        toast(this, R.string.toast_delete_sensor_events, sensor.name ?: "")
    }

    private fun removeEventsFromDatabase(sensor: SensorEntity) {
        sensor.deviceId?.let {
            LocalSensorDb.getEventDao(applicationContext)
                    .let { dao -> dao.deleteEvents(*dao.findEventsByDeviceId(it)) }
        }
    }

    private fun deleteSensor(sensor: SensorEntity) {
        Log.i(TAG, "Deleting sensor $sensor")

        removeEventsFromDatabase(sensor)
        removeSensorFromDatabase(sensor)

        toast(this, R.string.toast_device_deleted)

        setResult(RESULT_DELETE_SENSOR)
        finish()
    }

    private fun getExtraSensorId(): Int = intent.getIntExtra(EXTRA_SENSOR_ID, -1)

    private fun findSensorEntity(id: Int): SensorEntity? =
            LocalSensorDb.getSensorDao(applicationContext).findSensorById(id)

    private fun removeSensorFromDatabase(sensor: SensorEntity): Unit =
            LocalSensorDb.getSensorDao(applicationContext).deleteSensor(sensor)

}