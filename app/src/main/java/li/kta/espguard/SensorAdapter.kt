package li.kta.espguard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class SensorAdapter(var dataset: List<Sensor>) : RecyclerView.Adapter<SensorAdapter.SensorViewHolder>() {

  inner class SensorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false)
    return SensorViewHolder(view)
  }

  override fun getItemCount(): Int {
    TODO("Not yet implemented")
  }

  override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
    TODO("Not yet implemented")
  }

}
