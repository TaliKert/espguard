package li.kta.espguard.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_sensor_details.*
import li.kta.espguard.R
import li.kta.espguard.EventAdapter
import li.kta.espguard.EventViewModel
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class SensorDetailsActivity : AppCompatActivity() {
    companion object {
        val TAG: String = SensorDetailsActivity::class.java.name
        const val EXTRA_SENSOR_ID = "sensorId"
    }

    private var model: EventViewModel? = null
    private var eventAdapter: EventAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_details)
        setSupportActionBar(findViewById(R.id.toolbar_support_configure))

        model = ViewModelProvider(this).get(EventViewModel::class.java)

        val id = intent.getIntExtra(EXTRA_SENSOR_ID, -1)

        createAdapter(id)

        button_add_sensor.setOnClickListener { openSensorConfiguration(id) }
    }

    override fun onResume() {
        super.onResume()

        model?.refresh()
        eventAdapter?.updateEvents()
    }

    private fun createAdapter(sensorId: Int) {
        eventAdapter = EventAdapter(sensorId, applicationContext)

        eventAdapter?.let {
            events_recyclerview.adapter = it
            events_recyclerview.layoutManager = LinearLayoutManager(this)
            it.updateEvents()
            it.notifyDataSetChanged()
        }
    }

    private fun openSensorConfiguration(sensorId: Int) {
        getSensorEntity(sensorId)?.let { openSensorConfiguration(it) }
    }

    /* TODO: TEMPORARY METHOD, HAS TO BE REPLACED WITH DAO METHOD */
    private fun getSensorEntity(sensorId: Int): SensorEntity? =
            LocalSensorDb.getInstance(applicationContext).getSensorDao().loadSensors()
                    .find { it.id == sensorId }

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