package com.example.easystudy.ui.grades

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.easystudy.R
import com.example.easystudy.databinding.FragmentGradesBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.viewmodels.EventViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

class GradesFragment : Fragment() {

    private var _binding: FragmentGradesBinding? = null

    private val binding get() = _binding!!

    private lateinit var eventViewModel: EventViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GradesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGradesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.gradesRecyclerView)

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.grades_recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = GradesAdapter(emptyList(), object : GradesAdapter.GradeClickListener {
            override fun onGradeClicked(event: Event) {
                showGradeInputDialog(event)
            }
        })
        recyclerView.adapter = adapter

        displayGrades()

    }

    private fun showGradeInputDialog(event: Event) {
        if (event.date.isBefore(LocalDate.now()) ||
            (event.date == LocalDate.now() && event.endTime.isBefore(LocalTime.now()))) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_grade_input, null)
            val editTextGrade = dialogView.findViewById<EditText>(R.id.editText_grade)
            val btnSave = dialogView.findViewById<Button>(R.id.button_save)
            val btnCancel = dialogView.findViewById<Button>(R.id.button_cancel)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .show()

            btnSave.setOnClickListener {
                val inputGrade = editTextGrade.text.toString().toIntOrNull()
                if (inputGrade != null && inputGrade in 0..100) {
                    eventViewModel.updateGrade(event.id, inputGrade)
                    dialog.dismiss()
                } else {
                    editTextGrade.error = "Оцінка має бути в межах від 0 до 100"
                }
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }
        } else {
            Toast.makeText(requireContext(), "Цей екзамен ще не відбувся", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayGrades() {
        CoroutineScope(Dispatchers.IO).launch {
            val eventsByType = eventViewModel.getEventsByType(EventType.EXAM)

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