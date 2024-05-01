package com.example.easystudy.ui.schedule

import com.example.easystudy.ExamAppWidget
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.easystudy.databinding.FragmentScheduleBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.RepeatType
import com.example.easystudy.ui.addEvent.AddEventFragment
import com.example.easystudy.viewmodels.EventViewModel
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

    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var eventViewModel: EventViewModel
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
    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetIds: IntArray

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

        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScheduleBinding.bind(view)
        recyclerView = view.findViewById(R.id.event_recycler_view)
        val selectedDate = getSelectedDate()
        displaySchedule(selectedDate)

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

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedEvent = adapter.getEventAtPosition(position)

                CoroutineScope(Dispatchers.IO).launch {
                    eventViewModel.deleteEvent(deletedEvent)
                }
                updateWidget()

                Snackbar.make(
                    binding.root,
                    "Event deleted",
                    Snackbar.LENGTH_LONG
                ).setAction("Undo") {
                    CoroutineScope(Dispatchers.IO).launch {
                        eventViewModel.insertEvent(deletedEvent)
                        withContext(Dispatchers.Main) {
                            adapter.notifyItemInserted(position)
                        }
                    }

                    ///

                    updateWidget()

                }.show()
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun displaySchedule(date: Calendar) {
        val localDate = LocalDate.of(
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH) + 1,
            date.get(Calendar.DAY_OF_MONTH)
        )

        CoroutineScope(Dispatchers.IO).launch {
            val eventsForDate = getEventsForDate(localDate)

            withContext(Dispatchers.Main) {
                eventsForDate.observe(viewLifecycleOwner) { events ->
                    updateUI(events)
                }
            }

        }
    }

    private fun getEventsForDate(date: LocalDate): LiveData<List<Event>> {
        val allEventsLiveData: LiveData<List<Event>> = eventViewModel.getAllEvents()

        val eventsLiveData = MediatorLiveData<List<Event>>()

        eventsLiveData.addSource(allEventsLiveData) { allEvents ->
            val recurringEvents = checkForRecurringEvents(date, allEvents)
            eventsLiveData.value = recurringEvents
        }
        return eventsLiveData
    }

    private fun checkForRecurringEvents(date: LocalDate, allEvents: List<Event>): List<Event> {
        val events: MutableList<Event> = mutableListOf()

        for (event in allEvents) {
            if (event.repeat != RepeatType.NEVER && (event.date.isBefore(date) || event.date == date)) {
                when (event.repeat) {
                    RepeatType.WEEKLY -> {
                        val weeksBetween = ChronoUnit.WEEKS.between(event.date, date)
                        if (date.dayOfWeek == event.date.dayOfWeek &&  (weeksBetween + 1).toInt() <= event.count.toInt())
                            events.add(event)
                    }
                    RepeatType.BIWEEKLY -> {
                        val weeksBetween = ChronoUnit.WEEKS.between(event.date, date)
                        val weekPairs =  (weeksBetween + 1).toInt().div(2)
                        if ((weeksBetween % 2).toInt() == 0 && date.dayOfWeek == event.date.dayOfWeek && weekPairs < event.count.toInt())
                            events.add(event)
                    }
                    else -> {}
                }
            } else { if (event.repeat == RepeatType.NEVER && event.date == date) {events.add(event)}}
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

        updateWidget()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateWidget() {
        // update widget
        val appWidgetManager = AppWidgetManager.getInstance(requireContext())
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(requireContext(), ExamAppWidget::class.java))

        // Оновлення віджетів
        for (appWidgetId in appWidgetIds) {
            Log.d("AppWidgetId", appWidgetId.toString())
            ExamAppWidget.updateWidget(requireContext(), appWidgetManager, appWidgetId)
        }
    }
}
