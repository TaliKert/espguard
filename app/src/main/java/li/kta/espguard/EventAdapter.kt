package li.kta.espguard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_list_item.view.*
import li.kta.espguard.room.EventEntity

class EventAdapter() :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    companion object {
        val TAG: String = EventAdapter::class.java.name
    }

    var data = arrayOf<EventEntity>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = data[position]

        Log.i(TAG, "Binding event $position : $event")

        holder.itemView.apply {
            event_id.text = event.id.toString()
            event_time.text = event.event_time.toString()
        }
    }

}

