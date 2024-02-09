package com.example.easystudy.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.entities.Event

class EventAdapter(private val eventList: List<Event>) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentItem = eventList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = eventList.size
    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitleTextView: TextView = itemView.findViewById(R.id.textView_event_title)

        fun bind(event: Event) {
            eventTitleTextView.text = event.title
        }
    }
}
