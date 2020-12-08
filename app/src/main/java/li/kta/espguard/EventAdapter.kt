package li.kta.espguard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_list_item.view.*
import li.kta.espguard.room.EventEntity


class EventAdapter(private val sensorId: Int, private val applicationContext: Context)
    : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    companion object {
        val TAG: String = EventAdapter::class.java.name

        val mockEvents: ArrayList<EventEntity> =
                listOf(0 to "TIME0", 0 to "TIME01", 1 to "TIME10", 1 to "TIME11", 1 to "TIME12")
                        .mapTo(arrayListOf()) { EventEntity(sensorId = it.first, time = it.second) }
    }

    /*private var events: ArrayList<EventEntity> = arrayListOf()*/
    private var events: ArrayList<EventEntity> = mockEvents  // TODO: REPLACE

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder =
            EventViewHolder(LayoutInflater.from(parent.context)
                                    .inflate(R.layout.event_list_item, parent, false))

    override fun getItemCount(): Int = events.size

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]

        Log.i(TAG, "Binding event $position : $event")

        holder.itemView.apply {
            event_id.text = event.id.toString()
            event_time.text = event.time.toString()
        }
    }

    // TODO: maybe find where we need to do more event updates?
    fun updateEvents() = updateEvents(getSensorEvents(sensorId))

    private fun getSensorEvents(sensorId: Int): Array<EventEntity> =
            /*LocalSensorDb.getInstance(applicationContext).getSensorDao().loadEvents(sensorId)*/
            mockEvents.toTypedArray()

    fun updateEvents(events: Array<EventEntity>) {
        this.events.clear()
        this.events.addAll(events)
        notifyDataSetChanged()
    }
}

