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
import kotlinx.android.synthetic.main.activity_main.*
import li.kta.espguard.*
import li.kta.espguard.MqttService.Companion.STATUS_REQUEST_ACTION
import li.kta.espguard.MqttService.Companion.STATUS_RESPONSE_ACTION
import li.kta.espguard.activities.SensorAddingActivity.Companion.EXTRA_ADDED_SENSOR
import li.kta.espguard.activities.SensorAddingActivity.Companion.RESULT_ADDED_SENSOR
import li.kta.espguard.activities.SettingsActivity.Companion.setTheme
import li.kta.espguard.room.SensorEntity

/** TODO
 *    - HEALTH CHECK BUTTON user feedback?
 *    - ON/OFF display for sensors
 *    - DB updating in configurations gets overwritten for some reason
 *    - Health check when adding a new device
 *    - Toolbar color with theme change
 *    - Use resource files: text values in strings.xml
 *    - Use string resources also for all Toasts?
 *    - TEST STUFF (deletions, renames, lateinit nulls, settings, ...)
 *    - colors.xml and styles.xml
 *    - Dimensions in dimens.xml
 *    - The app uses threading to delegate work off UI thread: result of background work should still be notified in the UI
 *    - Actually test deleting data in app settings
 */
class MainActivity : AppCompatActivity() {
    companion object {
        val TAG: String = MainActivity::class.java.name
        const val REQUEST_CODE_ADD_SENSOR = 1302
    }

    private var sensorAdapter: SensorAdapter? = null
    private var model: SensorViewModel? = null
    private var healthCheckReceiver: BroadcastReceiver? = null
    private var healthCheckRequestReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_support_main))

        model = ViewModelProvider(this).get(SensorViewModel::class.java)
        createAdapter()

        model?.let { MqttService.initializeMqttService(this, it.sensorArray) }

        setupHealthCheckReceiver()

        button_add_device.setOnClickListener { openNewSensorView() }

    }

    private fun setupHealthCheckReceiver() {
        healthCheckReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                refreshData()
            }
        }
        registerReceiver(healthCheckReceiver, IntentFilter(STATUS_RESPONSE_ACTION))

        healthCheckRequestReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                sensorAdapter?.changeSensorStatusesPending()
            }
        }
        registerReceiver(healthCheckRequestReceiver, IntentFilter(STATUS_REQUEST_ACTION))
    }


    override fun onResume() {
        super.onResume()
        refreshData()
    }

    fun refreshData() {
        model?.let {
            it.refresh()
            sensorAdapter?.data = it.sensorArray
            MqttService.getInstance()?.sensors = it.sensorArray
        }
    }

    override fun onDestroy() {
        MqttService.destroyMqttService()

        listOfNotNull(healthCheckReceiver, healthCheckRequestReceiver).map(::unregisterReceiver)

        super.onDestroy()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CODE_ADD_SENSOR || resultCode != RESULT_ADDED_SENSOR) return

        refreshData()
        MqttService.getInstance()?.let { mqtt ->
            if (data == null) return

            val sensor: SensorEntity = data.getParcelableExtra(EXTRA_ADDED_SENSOR) ?: return

            mqtt.subscribe(sensor)
            mqtt.healthCheck(sensor)
        }
    }

    private fun openNewSensorView() {
        startActivityForResult(
                Intent(this, SensorAddingActivity::class.java),
                REQUEST_CODE_ADD_SENSOR
        )
    }

    private fun openSensorDetailsView(sensor: SensorEntity) {
        Log.i(TAG, "Opening details view for sensor $sensor")
        startActivity(Intent(this, SensorDetailsActivity::class.java)
                              .apply { putExtra(SensorDetailsActivity.EXTRA_SENSOR_ID, sensor.id) })
    }

    private fun createAdapter() {
        sensorAdapter = SensorAdapter(
                object : SensorAdapter.SensorAdapterListener {
                    override fun onButtonClick(sensor: SensorEntity) {
                        openSensorDetailsView(sensor)
                    }
                }
        )

        sensors_recyclerview.adapter = sensorAdapter
        sensors_recyclerview.layoutManager = LinearLayoutManager(this)
        refreshData()
    }
}