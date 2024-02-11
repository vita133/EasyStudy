package com.example.easystudy.ui.exams

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.databinding.FragmentExamsBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.ui.schedule.EventAdapter
import com.example.easystudy.viewmodels.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExamsFragment : Fragment() {

    private var _binding: FragmentExamsBinding? = null

    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventAdapter
    private lateinit var eventsViewModel: EventViewModel



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val examsViewModel =
            ViewModelProvider(this).get(ExamsViewModel::class.java)

        _binding = FragmentExamsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        examsViewModel.text.observe(viewLifecycleOwner) {
        }

        eventsViewModel = ViewModelProvider(this).get(EventViewModel::class.java)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.examsRecyclerView)


        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.exams_recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = EventAdapter(emptyList(), object : EventAdapter.OnEventClickListener {
            override fun onEditButtonClick(event: Event) {
                val action = ExamsFragmentDirections.actionNavigationExamsToNavigationAddEvent(event.id)
                Navigation.findNavController(requireView()).navigate(action)
            }

            override fun onItemClick(event: Event) {
                val action = ExamsFragmentDirections.actionNavigationExamsToNavigationEventInfo(event.id)
                Navigation.findNavController(requireView()).navigate(action)
            }
        })
        recyclerView.adapter = adapter

        displaySchedule()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun displaySchedule() {
        CoroutineScope(Dispatchers.IO).launch {
            val eventsByType = eventsViewModel.getEventsByType(EventType.EXAM)

            withContext(Dispatchers.Main) {
                eventsByType.observe(viewLifecycleOwner) { events ->
                    adapter.updateEvents(events)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}