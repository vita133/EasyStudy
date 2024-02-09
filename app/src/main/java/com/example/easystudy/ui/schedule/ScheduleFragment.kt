package com.example.easystudy.ui.schedule

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.database.EventDao
import com.example.easystudy.database.EventDatabase
import com.example.easystudy.databinding.FragmentScheduleBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.RepeatType
import com.example.easystudy.ui.addEvent.AddEventFragment
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale


class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventDao: EventDao


    private val lastDayInCalendar = Calendar.getInstance(Locale("uk", "UA"))
    private val sdf = SimpleDateFormat("MMMM yyyy", Locale("uk", "UA"))
    private val cal = Calendar.getInstance(Locale("uk", "UA"))

    private val currentDate = Calendar.getInstance(Locale("uk", "UA"))
    private val currentDay = currentDate[Calendar.DAY_OF_MONTH]
    private val currentMonth = currentDate[Calendar.MONTH]
    private val currentYear = currentDate[Calendar.YEAR]

    private var selectedDay: Int = currentDay
    private var selectedMonth: Int = currentMonth
    private var selectedYear: Int = currentYear

    private val dates = ArrayList<Date>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scheduleViewModel =
            ViewModelProvider(this).get(ScheduleViewModel::class.java)

        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.calendarRecyclerView)

        lastDayInCalendar.add(Calendar.MONTH, 6)
        setUpCalendar()
        setFloatingButtonClickListener()

        return root
    }

    private fun setFloatingButtonClickListener() {
        binding.floatingActionButton2.setOnClickListener {
            val fragment = AddEventFragment()
            val fragmentManager = requireActivity().supportFragmentManager

            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setUpCalendar(changeMonth: Calendar? = null) {
        displayDate(selectedDay, selectedMonth, selectedYear)

        val monthCalendar = cal.clone() as Calendar
        val maxDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        /**
         *
         * If changeMonth is not null, then I will take the day, month, and year from it,
         * otherwise set the selected date as the current date.
         */
        selectedDay =
            when {
                changeMonth != null -> changeMonth.getActualMinimum(Calendar.DAY_OF_MONTH)
                else -> currentDay
            }
        selectedMonth =
            when {
                changeMonth != null -> changeMonth[Calendar.MONTH]
                else -> currentMonth
            }
        selectedYear =
            when {
                changeMonth != null -> changeMonth[Calendar.YEAR]
                else -> currentYear
            }

        var currentPosition = 0
        dates.clear()
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)

        /**
         * Fill dates with days and set currentPosition.
         * currentPosition is the position of first selected day.
         */
        while (dates.size < maxDaysInMonth) {
            // get position of selected day
            if (monthCalendar[Calendar.DAY_OF_MONTH] == selectedDay)
                currentPosition = dates.size
            dates.add(monthCalendar.time)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Assigning calendar view.
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.calendarRecyclerView!!.layoutManager = layoutManager
        val calendarAdapter = CalendarAdapter(requireContext(), dates, currentDate, changeMonth)
        binding.calendarRecyclerView!!.adapter = calendarAdapter

        /**
         * If you start the application, it centers the current day, but only if the current day
         * is not one of the first (1, 2, 3) or one of the last (29, 30, 31).
         */
        when {
            currentPosition > 2 -> binding.calendarRecyclerView!!.scrollToPosition(currentPosition - 3)
            maxDaysInMonth - currentPosition < 2 -> binding.calendarRecyclerView!!.scrollToPosition(currentPosition)
            else -> binding.calendarRecyclerView!!.scrollToPosition(currentPosition)
        }


        /**
         * After calling up the OnClickListener, the text of the current month and year is changed.
         * Then change the selected day.
         */
        calendarAdapter.setOnItemClickListener(object : CalendarAdapter.OnItemClickListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemClick(position: Int) {
                val clickCalendar = Calendar.getInstance()
                clickCalendar.time = dates[position]
                selectedDay = clickCalendar[Calendar.DAY_OF_MONTH]
                selectedMonth = clickCalendar[Calendar.MONTH]
                selectedYear = clickCalendar[Calendar.YEAR]

                displayDate(selectedDay, selectedMonth, selectedYear)
                displaySchedule(getSelectedDate())
            }
        })
    }

    private fun displayDate(day: Int, month: Int, year: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }

        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("uk", "UA"))
        val formattedDate = dateFormat.format(calendar.time)
        binding.textViewDateTodayMain.text = formattedDate

        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale("uk", "UA"))
        val formattedDayOfWeek = dayOfWeekFormat.format(calendar.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        binding.textViewToday.text = formattedDayOfWeek

    }

    private fun getSelectedDate(): Calendar {
        val selectedDate = Calendar.getInstance(Locale("uk", "UA"))
        selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay)
        selectedDate.set(Calendar.MONTH, selectedMonth)
        selectedDate.set(Calendar.YEAR, selectedYear)
        return selectedDate
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScheduleBinding.bind(view)

        recyclerView = view.findViewById(R.id.event_recycler_view)
        val selectedDate = getSelectedDate()
        displaySchedule(selectedDate)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun displaySchedule(date: Calendar) {
        val localDate = LocalDate.of(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )
        val database = EventDatabase.getDatabase(requireContext())

        eventDao = database.eventDao()

//        val eventsForDate = getEventsForDate(localDate)
//        updateUI(eventsForDate)
        val eventListLiveData = eventDao.getEventsByDate(localDate)

        eventListLiveData.observe(viewLifecycleOwner) { events ->
            updateUI(events)
        }
    }


//    private fun checkForRecurringEvents(date: LocalDate): List<Event> {
//        val events: MutableList<Event> = mutableListOf()
//
//        val allEvents = eventDao.getAllEvents()
//
//        for (event in allEvents) {
//            if (event.repeat != RepeatType.NEVER && (event.date.isBefore(date) || event.date == date)) {
//                when (event.repeat) {
//                    RepeatType.WEEKLY -> {
//                        if (date.dayOfWeek == event.date.dayOfWeek)
//                            events.add(event)
//                    }
//                    RepeatType.BIWEEKLY -> {
//                        val weeksBetween = ChronoUnit.WEEKS.between(event.date, date)
//                        if (weeksBetween % 2 == 0 && date.dayOfWeek == event.date.dayOfWeek)
//                            events.add(event)
//                    }
//                }
//            }
//        }
//
//        return events
//    }

    private fun updateUI(events: List<Event>) {
        val adapter = EventAdapter(events)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
