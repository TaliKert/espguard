package li.kta.espguard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.sensor_list_item.view.*
import li.kta.espguard.room.SensorEntity

class SensorAdapter() : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

  var data = arrayOf<SensorEntity>()

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
      tv_name.text = sensor.id.toString()
    }

    }
}

