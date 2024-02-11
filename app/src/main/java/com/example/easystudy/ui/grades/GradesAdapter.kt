package com.example.easystudy.ui.grades

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.entities.Event

class GradesAdapter (private var eventList: List<Event>, private val listener: GradeClickListener) : RecyclerView.Adapter<GradesAdapter.GradeViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_grade, parent, false)
        return GradeViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val currentItem = eventList[position]
        holder.bind(currentItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateEvents(events: List<Event>) {
        this.eventList = events
        notifyDataSetChanged()
    }

    override fun getItemCount() = eventList.size

    interface GradeClickListener {
        fun onGradeClicked(event: Event)
    }

    inner class GradeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventTitleTextView: TextView = itemView.findViewById(R.id.textView_exam_title)
        private val eventTeacherTextView: TextView = itemView.findViewById(R.id.textView_exam_teacher)
        private val eventGradeTextView: TextView = itemView.findViewById(R.id.textView_exam_grade)

        init {
            eventGradeTextView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val event = eventList[position]
                    listener.onGradeClicked(event)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(event: Event) {
            eventTitleTextView.text = event.title
            eventTeacherTextView.text = event.teacher

            val initialGrade = event.grade ?: 0
            eventGradeTextView.setText(initialGrade.toString())
        }

    }
}