package li.kta.espguard.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_sensor_details.*
import li.kta.espguard.R
import li.kta.espguard.EventAdapter
import li.kta.espguard.EventViewModel
import li.kta.espguard.FirebaseService
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class SensorDetailsActivity : AppCompatActivity() {
    companion object {
        val TAG: String = SensorDetailsActivity::class.java.name
        const val EXTRA_SENSOR_ID = "sensorId"
    }

    private lateinit var model: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    lateinit var firebaseEventReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)
        setSupportActionBar(findViewById(R.id.toolbar_support_configure))

        val id = intent.getIntExtra(EXTRA_SENSOR_ID, -1)
        val sensor = LocalSensorDb.getInstance(this).getSensorDao().findSensorById(id)
        model = ViewModelProvider(this).get(EventViewModel::class.java)
        model.deviceId = sensor.deviceId.toString()

        createAdapter()
        setupFirebaseEventReceiver()

        button_configure_device.setOnClickListener { openSensorConfiguration(id) }
        button_delete_device.setOnClickListener { deleteSensor(id) }
    }

    private fun deleteSensor(id: Int) {

        val sensor = LocalSensorDb.getInstance(applicationContext).getSensorDao().findSensorById(id)

        removeEventsFromDatabase(sensor)
        removeSensorFromDatabase(sensor)

        Toast.makeText(this, "Deleted device", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun removeEventsFromDatabase(sensor: SensorEntity) {
        sensor.deviceId?.let {
            val events =
                LocalSensorDb.getInstance(applicationContext).getEventDao().findEventsByDeviceId(it)
            LocalSensorDb.getInstance(applicationContext).getEventDao().deleteEvents(*events)
        }
    }

    private fun removeSensorFromDatabase(sensor: SensorEntity) {
        LocalSensorDb.getInstance(applicationContext).getSensorDao().deleteSensor(sensor)
    }

    override fun onResume() {
        super.onResume()

        refreshData()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(firebaseEventReceiver)
    }

    private fun setupFirebaseEventReceiver() {
        firebaseEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Received broadcast. Refreshing events list...")
                refreshData()
            }
        }
        registerReceiver(
            firebaseEventReceiver,
            IntentFilter(FirebaseService.STATUS_RESPONSE_ACTION)
        )
    }

    fun refreshData() {
        model.refresh()
        eventAdapter.data = model.eventsArray
    }

    private fun createAdapter() {
        eventAdapter = EventAdapter()
        events_recyclerview.adapter = eventAdapter
        events_recyclerview.layoutManager = LinearLayoutManager(this)
        eventAdapter.data = model.eventsArray
    }

    private fun openSensorConfiguration(sensorId: Int) {
        getSensorEntity(sensorId)?.let { openSensorConfiguration(it) }
    }

    private fun getSensorEntity(sensorId: Int): SensorEntity? {
        return LocalSensorDb.getInstance(applicationContext).getSensorDao().findSensorById(sensorId)
    }


    private fun openSensorConfiguration(sensor: SensorEntity) {
        Log.i(TAG, "Opening configurations view for sensor $sensor")
        startActivity(
            Intent(this, ConfigureSensorActivity::class.java)
                .apply { putExtra(ConfigureSensorActivity.EXTRA_SENSOR_ID, sensor.id) })
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