package li.kta.espguard

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.event_list_item.view.*
import li.kta.espguard.room.EventEntity
import java.util.*


class EventAdapter(private val sensorId: Int, private val applicationContext: Context)
    : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    companion object {
        val TAG: String = EventAdapter::class.java.name

        val mockEvents: ArrayList<EventEntity> = listOf("ID", "ID", "ID")
                .map { it to Calendar.getInstance().time }
                .mapTo(arrayListOf()) { EventEntity(deviceId = it.first, event_time = it.second) }
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
            event_time.text = event.event_time.toString()
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

