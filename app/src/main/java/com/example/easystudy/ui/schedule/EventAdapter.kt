package com.example.easystudy.ui.schedule

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.entities.toUkrainianString
class EventAdapter(private val eventList: List<Event>,
                   private val onEventClickListener: OnEventClickListener) : RecyclerView.Adapter<EventAdapter.EventViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentItem = eventList[position]
        holder.bind(currentItem)
    }

    override fun getItemCount() = eventList.size

    interface OnEventClickListener {
        fun onEditButtonClick()
        fun onItemClick()
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitleTextView: TextView = itemView.findViewById(R.id.textView_event_title)
        private val eventTypeTextView: TextView = itemView.findViewById(R.id.textView_type)
        private val eventTimeTextView: TextView = itemView.findViewById(R.id.textView_time)
        private val eventLocationTextView: TextView = itemView.findViewById(R.id.textView_location)
        private val background: View = itemView.findViewById(R.id.event_linear_layout)
        private val ellipse: View = itemView.findViewById(R.id.imageView_ellipse)
        val imageViewEdit: ImageView = itemView.findViewById(R.id.imageView_edit)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val event = eventList[position]
                    onEventClickListener.onItemClick()
                }
            }
            imageViewEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val event = eventList[position]
                    onEventClickListener.onEditButtonClick()
                }
            }
        }

        fun bind(event: Event) {
            val formattedTime = event.startTime.toString() + "-" + event.endTime.toString()


            val textColor = when (event.type) {
                EventType.LECTURE -> R.color.dark_blue
                EventType.PRACTICE -> R.color.dark_green
                EventType.SEMINAR -> R.color.dark_purple
                EventType.EXAM -> R.color.dark_red
            }

            val backgroundDrawable = when (event.type) {
                EventType.LECTURE -> R.drawable.rounded_corners_blue
                EventType.PRACTICE -> R.drawable.rounded_corners_green
                EventType.SEMINAR -> R.drawable.rounded_corners_purple
                EventType.EXAM -> R.drawable.rounded_corners_red
            }

            val ellipseImage = when (event.type) {
                EventType.LECTURE -> R.drawable.ic_ellipse_blue
                EventType.PRACTICE -> R.drawable.ic_ellipse_green
                EventType.SEMINAR -> R.drawable.ic_ellipse_purple
                EventType.EXAM -> R.drawable.ic_ellipse_red
            }

            eventTitleTextView.setTextColor(textColor)
            eventTypeTextView.setTextColor(textColor)
            eventTimeTextView.setTextColor(textColor)
            eventLocationTextView.setTextColor(textColor)
            background.setBackgroundResource(backgroundDrawable)
            ellipse.setBackgroundResource(ellipseImage)


            eventTitleTextView.text = event.title
            eventTypeTextView.text = event.type.toUkrainianString()
            eventTimeTextView.text = formattedTime
            eventLocationTextView.text = event.location

        }
    }
}
