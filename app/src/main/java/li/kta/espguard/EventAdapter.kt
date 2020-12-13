package li.kta.espguard

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_list_item.view.*
import li.kta.espguard.room.EventEntity
import java.time.format.DateTimeFormatter

class EventAdapter : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    companion object {
        val TAG: String = EventAdapter::class.java.name
    }

    var data: Array<EventEntity> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder =
            EventViewHolder(LayoutInflater.from(parent.context)
                                    .inflate(R.layout.event_list_item, parent, false))

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = data[position]

        Log.i(TAG, "Binding event $position : $event")

        holder.itemView.apply {
            event_id.text = event.id.toString()
            event_time.text = event.eventTime?.format(
                DateTimeFormatter.ofPattern("HH:mm 'on' EEEE, MMM dd")
            )
        }
    }
}

