package com.example.easystudy.ui.addEvent

import android.R
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.easystudy.database.EventDao
import com.example.easystudy.database.EventDatabase
import com.example.easystudy.databinding.FragmentAddEventBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.entities.RepeatType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null

    private val binding get() = _binding!!

    private lateinit var eventDao: EventDao

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEventBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val spinner: Spinner = binding.spinnerTypeSubject
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            com.example.easystudy.R.array.subject_types, R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        setupDateInput()
        setupTimeInput()
        setSaveButtonClickListener()
        setCancelButtonClickListener()

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setSaveButtonClickListener() {
        binding.buttonSaveEvent.setOnClickListener {
            val title = binding.editTextNameSubject.text.toString()
            val dateText =binding.editTextDate.text.toString()
            val startTimeText = binding.editTextTimeStart.text.toString()
            val endTimeText = binding.editTextTimeEnd.text.toString()
            val typeText = binding.spinnerTypeSubject.selectedItem.toString()
            val teacher =  binding.editTextProfessor.text.toString()
            val repeatText = binding.spinnerRepeat.selectedItem.toString()
            val location =  binding.editTextPlace.text.toString()
            val count =  binding.editTextLessons.text.toString()

            if (title.isBlank() || dateText.isBlank() || startTimeText.isBlank() ||
                endTimeText.isBlank() || typeText.isBlank() || teacher.isBlank() ||
                location.isBlank() || count.isBlank()) {
                Toast.makeText(requireContext(), "Поля не можуть бути пустими", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (startTimeText.isNotEmpty() && endTimeText.isNotEmpty()) {
                val startTimeParts = startTimeText.split(":")
                val endTimeParts = endTimeText.split(":")
                val startHour = startTimeParts[0].toInt()
                val endHour = endTimeParts[0].toInt()
                val startMinute = startTimeParts[1].toInt()
                val endMinute = endTimeParts[1].toInt()

                if (startHour > endHour || (startHour == endHour && startMinute >= endMinute)) {
                    binding.editTextTimeEnd.error = "Час кінця має бути пізніше за час старту"
                    return@setOnClickListener
                } else {
                    binding.editTextTimeEnd.error = null
                }
            }

            val date =  LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            val startTime: LocalTime = LocalTime.parse(startTimeText, DateTimeFormatter.ofPattern("HH:mm"))
            val endTime: LocalTime = LocalTime.parse(endTimeText, DateTimeFormatter.ofPattern("HH:mm"))
            val type = when (typeText) {
                "Лекція" -> EventType.LECTURE
                "Екзамен" -> EventType.EXAM
                "Семінар" -> EventType.SEMINAR
                "Практична" -> EventType.PRACTICE
                else -> {EventType.LECTURE}
            }
            val repeat =  when (repeatText) {
                "Ніколи" -> RepeatType.NEVER
                "Щотижня" -> RepeatType.WEEKLY
                "Що 2 тижні" -> RepeatType.BIWEEKLY
                else -> {RepeatType.NEVER}
            }

            val event = Event(
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                type = type,
                teacher = teacher,
                repeat = repeat,
                location = location,
                count = count.toInt()
            )

            val database = EventDatabase.getDatabase(requireContext())
            eventDao = database.eventDao()

            CoroutineScope(Dispatchers.IO).launch {
                eventDao.insertEvent(event)
            }
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setupDateInput() {
        binding.editTextDate.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            @SuppressLint("SetTextI18n")
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input.length == 2 || input.length == 5) {
                    if (input[input.length - 1] != '.') {
                        binding.editTextDate.setText(input.substring(0, input.length) + ".")
                        binding.editTextDate.setSelection(binding.editTextDate.text.length)
                    }
                }

                val parts = input.split(".")
                if (parts.size == 3) {
                    val day = parts[0].toIntOrNull()
                    val month = parts[1].toIntOrNull()
                    val year = parts[2].toIntOrNull()
                    if (day == null || month == null || year == null ||
                        day !in 1..31 || month !in 1..12 || year !in 1900..2100 || parts[2].length > 4
                    ) {
                        binding.editTextDate.error = "Неправильний формат дати"
                        binding.buttonSaveEvent.isEnabled = false
                    } else {
                        binding.editTextDate.error = null
                        binding.buttonSaveEvent.isEnabled = true
                    }
                }
            }
        })
    }

    private fun setupTimeInput() {
        binding.editTextTimeStart.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                if (input.length == 2) {
                    if (input[input.length - 1] != ':') {
                        val editedInput = input.substring(0, 2) + ":"
                        binding.editTextTimeStart.setText(editedInput)
                        binding.editTextTimeStart.setSelection(binding.editTextTimeStart.text.length)
                    }
                }

                val parts = input.split(":")
                if (parts.size == 2) {
                    val hours = parts[0].toIntOrNull()
                    val minutes = parts[1].toIntOrNull()
                    if (hours == null || minutes == null ||
                        hours !in 0..23 || minutes !in 0..59
                    ) {
                        binding.editTextTimeStart.error = "Неправильний формат часу"
                        binding.buttonSaveEvent.isEnabled = false
                    } else {
                        binding.editTextTimeStart.error = null
                        binding.buttonSaveEvent.isEnabled = true
                    }
                }
            }
        })

        binding.editTextTimeEnd.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()

                if (input.length == 2) {
                    if (input[input.length - 1] != ':') {
                        val editedInput = input.substring(0, 2) + ":"
                        binding.editTextTimeEnd.setText(editedInput)
                        binding.editTextTimeEnd.setSelection(binding.editTextTimeEnd.text.length)
                    }
                }

                val parts = input.split(":")
                if (parts.size == 2) {
                    val hours = parts[0].toIntOrNull()
                    val minutes = parts[1].toIntOrNull()
                    if (hours == null || minutes == null ||
                        hours !in 0..23 || minutes !in 0..59
                    ) {
                        binding.editTextTimeEnd.error = "Неправильний формат часу"
                        binding.buttonSaveEvent.isEnabled = false
                    } else {
                        binding.editTextTimeEnd.error = null
                        binding.buttonSaveEvent.isEnabled = true
                    }
                }
            }
        })
    }

    private fun setCancelButtonClickListener() {
        binding.buttonCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}