package com.example.easystudy.ui.eventInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.easystudy.databinding.FragmentEventInfoBinding
import com.example.easystudy.entities.Event
import com.example.easystudy.entities.EventType
import com.example.easystudy.entities.RepeatType
import com.example.easystudy.viewmodels.EventViewModel
import java.time.LocalDate

class EventInfoFragment : Fragment() {
    private var _binding: FragmentEventInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val arguments = arguments
        var eventId: Long = 0
        if (arguments != null) {
            eventId = arguments.getLong("event")
        }
        val eventViewModel = ViewModelProvider(this).get(EventViewModel::class.java)

        val eventLiveData = eventViewModel.getEventById(eventId)
        eventLiveData.observe(viewLifecycleOwner) { event ->
            if (event == null) {
                return@observe
            }
            binding.textViewName.text = event.title
            val time = event.startTime.toString() + " - " + event.endTime.toString()
            binding.textViewTime.text = time
            binding.textViewDate.text = event.date.toString()
            binding.textViewLocation.text = event.location
            binding.textViewProfessor.text = event.teacher
            binding.textViewRepeat.text = event.repeat.toString()
            val progress = calculateProgress(event)
            val progressString = if (progress == 100) {
                if(event.type == EventType.EXAM){
                    "Ви вже склали іспит!"
                } else
                "Ви вже пройшли всі заняття!"
            } else {
                "Ви вже пройшли " + progress.toString() + "% занять"
            }
            binding.textViewProgress.text = progressString
            binding.textViewProgressPercent.text = progress.toString() + "%"
            binding.progressBar.progress = progress
        }
        return root
    }

    fun calculateProgress(event: Event): Int {
        val currentDate = LocalDate.now()
        val startDate = event.date
        val totalLessons = event.count.toInt()
        val repeatType = event.repeat

        val passedLessons = if (currentDate.isAfter(startDate)) {
            when (repeatType) {
                RepeatType.WEEKLY -> {
                    val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, currentDate)
                    (weeksBetween + 1).toInt()
                }
                RepeatType.BIWEEKLY -> {
                    val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, currentDate)
                    ((weeksBetween + 1) / 2).toInt()
                }
                RepeatType.NEVER -> {
                    1
                }
            }
        } else {
            0
        }
        val progress = (passedLessons.toDouble() / totalLessons.toDouble() * 100).toInt()

        return progress
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}