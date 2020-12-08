package li.kta.espguard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sensor_list_item.view.*

import li.kta.espguard.room.SensorEntity

class SensorAdapter(private var listener: SensorAdapterListener) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

  interface SensorAdapterListener {
    fun onButtonClick(sensor: SensorEntity)
  }

  var data = arrayOf<SensorEntity>()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  inner class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
    return SensorViewHolder(view)
  }

  override fun getItemCount(): Int {
    return data.size
  }

  override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
    val sensor = data[position]

    holder.itemView.apply {
      tv_sensor_name.text = sensor.name.toString()
      tv_sensor_id.text = sensor.deviceId.toString()

      button_configure.setOnClickListener{ listener.onButtonClick(sensor) }
    }

  }

}

