package com.example.easystudy.ui.schedule

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(private val context: Context,
                      private val data: ArrayList<Date>,
                      private val currentDate: Calendar,
                      private val changeMonth: Calendar?): RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
    private var mListener: OnItemClickListener? = null
    private var index = -1
    private var selectCurrentDate = true
    private val currentMonth = currentDate[Calendar.MONTH]
    private val currentYear = currentDate[Calendar.YEAR]
    private val currentDay = currentDate[Calendar.DAY_OF_MONTH]
    private val selectedDay =
        when {
            changeMonth != null -> changeMonth.getActualMinimum(Calendar.DAY_OF_MONTH)
            else -> currentDay
        }
    private val selectedMonth =
        when {
            changeMonth != null -> changeMonth[Calendar.MONTH]
            else -> currentMonth
        }
    private val selectedYear =
        when {
            changeMonth != null -> changeMonth[Calendar.YEAR]
            else -> currentYear
        }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val inflater = LayoutInflater.from(context)
        return ViewHolder(inflater.inflate(R.layout.custom_calendar_day, parent, false), mListener!!)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, positionn: Int) {
        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss", Locale.ENGLISH)
        val cal = Calendar.getInstance()
        cal.time = data[positionn]

        val displayMonth = cal[Calendar.MONTH]
        val displayYear= cal[Calendar.YEAR]
        val displayDay = cal[Calendar.DAY_OF_MONTH]

        try {
            val dayInWeekFormat = SimpleDateFormat("E", Locale("uk", "UA"))
            val dayInWeek = dayInWeekFormat.format(cal.time)
            holder.txtDayInWeek!!.text = dayInWeek
        } catch (ex: ParseException) {
            Log.v("Exception", ex.localizedMessage!!)
        }
        holder.txtDay!!.text = cal[Calendar.DAY_OF_MONTH].toString()

        holder.linearLayout!!.setOnClickListener {
            index = holder.adapterPosition
            selectCurrentDate = false
            holder.listener.onItemClick(positionn)
            notifyDataSetChanged()
        }

        if (index == positionn || (displayDay == selectedDay && displayMonth == selectedMonth && displayYear == selectedYear && selectCurrentDate)) {
            makeItemSelected(holder)
        } else {
            makeItemDefault(holder, cal)
        }
    }

    inner class ViewHolder(itemView: View, val listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        var txtDay = itemView.findViewById<TextView>(R.id.txt_day)
        var txtDayInWeek = itemView.findViewById<TextView>(R.id.txt_date)
        var linearLayout = itemView.findViewById<LinearLayout>(R.id.calendar_linear_layout)
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    private fun makeItemDefault(holder: ViewHolder, cal: Calendar) {
        holder.txtDay!!.setTextColor(Color.BLACK)
        holder.txtDayInWeek!!.setTextColor(Color.BLACK)
        holder.linearLayout!!.setBackgroundColor(Color.WHITE)

        if (isPastDate(cal)) {
            holder.linearLayout!!.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
        }

        holder.linearLayout!!.isEnabled = true
    }

    private fun isPastDate(cal: Calendar): Boolean {
        val currentCalendar = Calendar.getInstance()
        return cal.before(currentCalendar)
    }

    private fun makeItemSelected(holder: ViewHolder) {
        holder.txtDay!!.setTextColor(Color.parseColor("#FFFFFF"))
        holder.txtDayInWeek!!.setTextColor(Color.parseColor("#FFFFFF"))
        holder.linearLayout!!.setBackgroundColor(ContextCompat.getColor(context, R.color.dark_blue))
        holder.linearLayout!!.isEnabled = false
    }
}