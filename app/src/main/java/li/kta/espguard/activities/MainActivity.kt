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
import li.kta.espguard.R
import li.kta.espguard.SensorAdapter
import li.kta.espguard.SensorViewModel
import li.kta.espguard.activities.SettingsActivity.Companion.setTheme
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity


class MainActivity : AppCompatActivity() {

    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var model: SensorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        setTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_support_main))

        model = ViewModelProvider(this).get(SensorViewModel::class.java)
        //testDb()
        createAdapter()

        button_add_sensor.setOnClickListener{ openNewSensorView()}
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
        val intent = Intent(this, ConfigureSensorActivity::class.java)
        intent.putExtra(ConfigureSensorActivity.EXTRA_SENSOR_ID, sensor.id)
        startActivity(intent)
    }

    private fun createAdapter() {
        sensorAdapter = SensorAdapter(
            object : SensorAdapter.SensorAdapterListener {
                override fun onButtonClick(sensor: SensorEntity) {
                    openSensorConfiguration(sensor)
                }
            }
        )
        sensors_recyclerview.adapter = sensorAdapter
        sensors_recyclerview.layoutManager = LinearLayoutManager(this)
        sensorAdapter.data = model.sensorArray
    }

    
    fun testDb() {
        // instance of db
        val db = LocalSensorDb.getInstance(this)

        // insert 1 sensor
        val sensor = SensorEntity(0, true)

        db.getSensorDao().insertSensors(sensor)
        db.getSensorDao().loadSensorIds().forEach {
            Log.i("RoomTest", "Sensor ${it}")
        }
    }
    


}