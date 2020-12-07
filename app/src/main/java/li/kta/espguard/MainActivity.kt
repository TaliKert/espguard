package li.kta.espguard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var sensorAdapter: SensorAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createAdapter()
    }

    private fun createAdapter() {
        sensorAdapter = SensorAdapter(arrayListOf()) // TODO
        sensors_recyclerview.adapter = sensorAdapter
        sensors_recyclerview.layoutManager = LinearLayoutManager(this)
    }


}