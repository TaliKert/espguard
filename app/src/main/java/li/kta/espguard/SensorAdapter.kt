package li.kta.espguard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sensor_list_item.view.*
import li.kta.espguard.room.SensorEntity

class SensorAdapter(private var listener: SensorAdapterListener)
    : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

    companion object {
        val TAG: String = SensorAdapter::class.java.name
    }


    interface SensorAdapterListener {
        fun onButtonClick(sensor: SensorEntity)
    }

    var data = arrayOf<SensorEntity>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder =
            SensorViewHolder(LayoutInflater.from(parent.context)
                                     .inflate(R.layout.sensor_list_item, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = data[position]

        Log.i(TAG, "Binding sensor $position : $sensor")

        holder.itemView.let { view ->
            view.tv_sensor_name.text = sensor.name.toString()
            view.tv_sensor_id.text = sensor.deviceId.toString()

            sensor.getStatus().let {
                view.status_svg.setImageResource(it.iconResource)
                view.tv_sensor_status.setText(it.textResource)
            }

            view.setOnClickListener { listener.onButtonClick(sensor) }
        }
    }

    fun changeSensorStatusesPending(): Unit = notifyDataSetChanged()
}

