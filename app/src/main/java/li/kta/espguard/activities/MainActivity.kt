package li.kta.espguard.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import li.kta.espguard.MqttService
import li.kta.espguard.R
import li.kta.espguard.SensorAdapter
import li.kta.espguard.SensorViewModel
import li.kta.espguard.activities.SettingsActivity.Companion.setTheme
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity


class MainActivity : AppCompatActivity() {
    companion object {
        val TAG: String = MainActivity::class.java.name
    }

    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var model: SensorViewModel
    private lateinit var mqttService: MqttService

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_support_main))

        model = ViewModelProvider(this).get(SensorViewModel::class.java)
        createAdapter()

        mqttService = MqttService(this, model.sensorArray)
        mqttService.initialize()

        button_configure_device.setOnClickListener { openNewSensorView() }
    }

    override fun onResume() {
        super.onResume()

        model.refresh()
        sensorAdapter.data = model.sensorArray
    }

    override fun onDestroy() {
        mqttService.destroy()
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


    fun testDbEvents() {
        val dao = LocalSensorDb.getInstance(this).getSensorDao()
        /*dao.loadSensors().forEach { dao.insertEvents(EventEntity(sensorId = it.id, time = "TIME")) }*/
    }
}