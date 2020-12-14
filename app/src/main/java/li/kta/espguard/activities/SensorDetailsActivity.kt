package li.kta.espguard.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_sensor_details.*
import li.kta.espguard.FirebaseService
import li.kta.espguard.R
import li.kta.espguard.adapters.EventAdapter
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity
import li.kta.espguard.viewModels.EventViewModel

class SensorDetailsActivity : AppCompatActivity() {

    companion object {
        val TAG: String = SensorDetailsActivity::class.java.name
        const val EXTRA_SENSOR_ID = "sensorId"
        const val REQUEST_CODE_CONFIGURATIONS = 100
    }

    private var id: Int = -1
    private var model: EventViewModel? = null
    private var eventAdapter: EventAdapter? = null
    private var firebaseEventReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)
        setSupportActionBar(findViewById(R.id.toolbar_support_configure))

        Log.i(TAG, "Created the activity.")

        id = intent.getIntExtra(EXTRA_SENSOR_ID, -1)

        setupDetails()

        setupFirebaseEventReceiver()

        button_configure_device.setOnClickListener { openSensorConfiguration(id) }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CODE_CONFIGURATIONS) return

        when (resultCode) {
            SensorConfigurationActivity.RESULT_DELETE_SENSOR -> finish()
            else -> refreshDetails()  // maybe rename, maybe deleted events
        }
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

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(firebaseEventReceiver)
    }


    private fun setupDetails() {
        getSensorEntity(id)?.let { setupDetails(it) }
    }

    private fun setupDetails(sensor: SensorEntity) {
        Log.i(TAG, "Setting up details for $sensor")

        model = ViewModelProvider(this).get(EventViewModel::class.java)
        model?.deviceId = sensor.deviceId.toString()

        createAdapter()
        setupTextViews(sensor)
    }

    private fun refreshDetails() {
        refreshData()
        getSensorEntity(id)?.let { setupTextViews(it) }
    }

    private fun setupTextViews(sensor: SensorEntity) {
        tv_sensor_name.text = resources.getString(R.string.sensor_name_template, sensor.name)
        tv_sensor_id.text = resources.getString(R.string.sensor_id_template, sensor.deviceId)
        tv_sensor_status.text = resources.getString(
                R.string.sensor_status_template,
                resources.getString(sensor.getStatus().textResource))
    }

    private fun setupFirebaseEventReceiver() {
        firebaseEventReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                Log.i(TAG, "Received broadcast. Refreshing events list...")
                refreshData()
            }
        }

        registerReceiver(firebaseEventReceiver, IntentFilter(FirebaseService.STATUS_RESPONSE_ACTION))
    }

    fun refreshData() {
        model?.refresh()
        model?.let { eventAdapter?.data = it.eventsArray }
    }

    private fun createAdapter() {
        eventAdapter = EventAdapter()
        events_recyclerview.adapter = eventAdapter
        events_recyclerview.layoutManager = LinearLayoutManager(this)
        refreshData()
    }

    private fun openSensorConfiguration(sensorId: Int) {
        getSensorEntity(sensorId)?.let { openSensorConfiguration(it) }
    }


    private fun openSensorConfiguration(sensor: SensorEntity) {
        Log.i(TAG, "Opening configurations view for sensor $sensor")
        startActivityForResult(Intent(this, SensorConfigurationActivity::class.java)
                                       .apply { putExtra(EXTRA_SENSOR_ID, sensor.id) },
                               REQUEST_CODE_CONFIGURATIONS)
    }

    private fun getSensorEntity(sensorId: Int): SensorEntity? =
            LocalSensorDb.getSensorDao(applicationContext).findSensorById(sensorId)

}