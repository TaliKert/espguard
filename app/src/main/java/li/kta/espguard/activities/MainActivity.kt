package li.kta.espguard.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_main.*
import li.kta.espguard.R
import li.kta.espguard.SensorAdapter
import li.kta.espguard.SensorViewModel
import li.kta.espguard.room.LocalSensorDb
import li.kta.espguard.room.SensorEntity

class MainActivity : AppCompatActivity() {

    private lateinit var sensorAdapter: SensorAdapter
    private lateinit var model: SensorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = ViewModelProvider(this).get(SensorViewModel::class.java)

        testDb()

        createAdapter()
    }

    private fun createAdapter() {
        sensorAdapter = SensorAdapter()
        sensors_recyclerview.adapter = sensorAdapter
        sensors_recyclerview.layoutManager = LinearLayoutManager(this)
    }

    
    fun testDb() {
        // instance of db
        val db = Room.databaseBuilder(
            applicationContext, LocalSensorDb::class.java, "mySensors")
            .fallbackToDestructiveMigration() // each time schema changes, data is lost!
            .allowMainThreadQueries() // if possible, use background thread instead
            .build()

        // insert 1 sensor
        val sensor = SensorEntity(0, true)

        db.getSensorDao().insertSensors(sensor)
        db.getSensorDao().loadSensorIds().forEach {
            Log.i("RoomTest", "Sensor ${it}")
        }
    }
    


}