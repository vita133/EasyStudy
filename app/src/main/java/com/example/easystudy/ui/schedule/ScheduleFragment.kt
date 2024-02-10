package com.example.easystudy.ui.schedule

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.database.EventDao
import com.example.easystudy.database.EventDatabase
import com.example.easystudy.databinding.FragmentScheduleBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.entities.RepeatType
import com.example.easystudy.ui.addEvent.AddEventFragment
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private lateinit var adapter: EventAdapter


    private val lastDayInCalendar = Calendar.getInstance(Locale("uk", "UA"))
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

        while (dates.size < maxDaysInMonth) {
            if (monthCalendar[Calendar.DAY_OF_MONTH] == selectedDay)
                currentPosition = dates.size
            dates.add(monthCalendar.time)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.calendarRecyclerView.layoutManager = layoutManager
        val calendarAdapter = CalendarAdapter(requireContext(), dates, currentDate, changeMonth)
        binding.calendarRecyclerView.adapter = calendarAdapter

        when {
            currentPosition > 2 -> binding.calendarRecyclerView.scrollToPosition(currentPosition - 3)
            maxDaysInMonth - currentPosition < 2 -> binding.calendarRecyclerView.scrollToPosition(currentPosition)
            else -> binding.calendarRecyclerView.scrollToPosition(currentPosition)
        }

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

        val database = EventDatabase.getDatabase(requireContext())
        eventDao = database.eventDao()

        adapter = EventAdapter(emptyList(), object : EventAdapter.OnEventClickListener {
            override fun onEditButtonClick(event: Event) {
                val action = ScheduleFragmentDirections.actionNavigationScheduleToNavigationAddEvent(event.id)
                Navigation.findNavController(requireView()).navigate(action)
            }

            override fun onItemClick(event: Event) {
                val action = ScheduleFragmentDirections.actionNavigationScheduleToNavigationEventInfo(event.id)
                Navigation.findNavController(requireView()).navigate(action)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        setupSwipeToDelete()


        CoroutineScope(Dispatchers.IO).launch {

            withContext(Dispatchers.Main) {
                val eventsByType = eventDao.getEventsByType(EventType.LECTURE)
                eventsByType.observe(viewLifecycleOwner) { events ->
                    Log.d("EventDao", "Events by type LECTURE: $events")
                }

                val allEvents = eventDao.getAllEvents()
                allEvents.observe(viewLifecycleOwner) { events ->
                    Log.d("EventDao", "All events: $events")
                }

                val currentDate = LocalDate.now()
                val eventsByDate = eventDao.getEventsByDate(currentDate)
                eventsByDate.observe(viewLifecycleOwner) { events ->
                    Log.d("EventDao", "Events for date $currentDate: $events")
                }

                val eventId = 1L
                val eventById = eventDao.getEventById(eventId)
                eventById.observe(viewLifecycleOwner) { event ->
                    Log.d("EventDao", "Event with ID $eventId: $event")
                }
            }
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedEvent = adapter.getEventAtPosition(position)


                CoroutineScope(Dispatchers.IO).launch {
                    eventDao.deleteEvent(deletedEvent)
                }

                Snackbar.make(
                    binding.root,
                    "Event deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    CoroutineScope(Dispatchers.IO).launch {
                        eventDao.insertEvent(deletedEvent)
                        withContext(Dispatchers.Main) {
                            adapter.notifyItemInserted(position)
                        }
                    }
                }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
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

        CoroutineScope(Dispatchers.IO).launch {
            val eventsForDate = getEventsForDate(localDate)

            withContext(Dispatchers.Main) {
                eventsForDate.observe(viewLifecycleOwner) { events ->
                    updateUI(events)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getEventsForDate(date: LocalDate): LiveData<List<Event>> {
        val allEventsLiveData: LiveData<List<Event>> = eventDao.getAllEvents()

        val eventsLiveData = MediatorLiveData<List<Event>>()

        eventsLiveData.addSource(allEventsLiveData) { allEvents ->
            val recurringEvents = checkForRecurringEvents(date, allEvents)
            val eventsForDate = mutableListOf<Event>()

            for (event in allEvents) {
                if (event.date == date || recurringEvents.any { it.date == event.date }) {
                    eventsForDate.add(event)
                }
            }
            eventsLiveData.value = eventsForDate
        }
        return eventsLiveData
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkForRecurringEvents(date: LocalDate, allEvents: List<Event>): List<Event> {
        val events: MutableList<Event> = mutableListOf()

        for (event in allEvents) {
            if (event.repeat != RepeatType.NEVER && (event.date.isBefore(date) )) { //|| event.date == date)) {
                when (event.repeat) {
                    RepeatType.WEEKLY -> {
                        if (date.dayOfWeek == event.date.dayOfWeek)
                            events.add(event)
                    }
                    RepeatType.BIWEEKLY -> {
                        val weeksBetween = ChronoUnit.WEEKS.between(event.date, date)
                        if ((weeksBetween % 2).toInt() == 0 && date.dayOfWeek == event.date.dayOfWeek)
                            events.add(event)
                    }
                    else -> {}
                }
            }
        }
        return events
    }

    private fun updateUI(events: List<Event>) {
        if (events.isEmpty()) {
            binding.imageViewNoEvents.visibility = View.VISIBLE
            binding.textViewNoEvents.visibility = View.VISIBLE
        } else {
            binding.imageViewNoEvents.visibility = View.GONE
            binding.textViewNoEvents.visibility = View.GONE
        }
        adapter.updateEvents(events)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
