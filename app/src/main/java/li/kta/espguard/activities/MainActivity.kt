package li.kta.espguard.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*
import li.kta.espguard.*
import li.kta.espguard.MqttService.Companion.STATUS_RESPONSE_ACTION
import li.kta.espguard.activities.SettingsActivity.Companion.setTheme
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

/** TODO
 *    - Node configuration communication
 *      * Time of day
 *      * cooldown period (how long to wait until next alert)
 *    - More settings
 *    - Health check at adding new device
 *    - Toolbar color with theme change
 *    - Use resource files: text values in strings.xml
 *    - Make use of string formatting at least once
 *    - colors.xml and styles.xml
 *    - Dimensions in dimens.xml
 *    - The app uses threading to delegate work off UI thread: result of background work should still be notified in the UI
 */
class MainActivity : AppCompatActivity() {
    companion object {
        val TAG: String = MainActivity::class.java.name
    }

    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var model: SensorViewModel
    lateinit var healthCheckReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_support_main))

        model = ViewModelProvider(this).get(SensorViewModel::class.java)
        createAdapter()

        MqttService.initializeMqttService(this, model.sensorArray)

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
    }


    override fun onResume() {
        super.onResume()

        refreshData()
    }

    fun refreshData() {
        model.refresh()
        sensorAdapter.data = model.sensorArray
    }

    override fun onDestroy() {
        MqttService.destroyMqttService()
        unregisterReceiver(healthCheckReceiver)
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

    private fun openNewSensorView() {
        startActivity(Intent(this, AddSensorActivity::class.java))
    }

    private fun openSensorConfiguration(sensor: SensorEntity) {
        Log.i(TAG, "Opening configurations view for sensor $sensor")
        val intent = Intent(this, ConfigureSensorActivity::class.java)
        intent.putExtra(ConfigureSensorActivity.EXTRA_SENSOR_ID, sensor.id)
        startActivity(intent)
    }

    private fun openSensorDetailsView(sensor: SensorEntity) {
        Log.i(TAG, "Opening details view for sensor $sensor")
        startActivity(
            Intent(this, SensorDetailsActivity::class.java)
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
        sensorAdapter.data = model.sensorArray
    }

}