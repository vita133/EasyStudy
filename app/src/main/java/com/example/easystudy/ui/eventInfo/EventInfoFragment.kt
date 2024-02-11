package com.example.easystudy.ui.eventInfo

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateProgress(event: Event): Int {
        val currentDate = LocalDate.now()
        val startDate = event.date
        val totalLessons = event.count.toInt()
        val repeatType = event.repeat
        Log.e("totalLessons", totalLessons.toString())
        Log.e ("startDate", startDate.toString())
        Log.e("currentDate", currentDate.toString())

        val passedLessons = if (currentDate.isAfter(startDate)) {
            when (repeatType) {
                RepeatType.WEEKLY -> {
                    val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, currentDate)
                    (weeksBetween + 1).toInt()
                    Log.e("weeksBetween", weeksBetween.toString())
                }
                RepeatType.BIWEEKLY -> {
                    val weeksBetween = java.time.temporal.ChronoUnit.WEEKS.between(startDate, currentDate)
                    ((weeksBetween + 1) / 2).toInt()
                    Log.e("weeksBetween", weeksBetween.toString())
                }
                RepeatType.NEVER -> {
                    1
                }
            }
        } else {
            0
        }
        Log.e("passedLessons", passedLessons.toString())
        val progress = (passedLessons.toDouble() / totalLessons.toDouble() * 100).toInt()

        Log.e("progress", progress.toString())
        return progress
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    companion object {
//        private const val EVENT_KEY = "event"
//
//        fun newInstance(event: Event): EventInfoFragment {
//            val fragment = EventInfoFragment()
//            val args = Bundle().apply {
//                putSerializable(EVENT_KEY, event as Serializable)
//            }
//            fragment.arguments = args
//            return fragment
//        }
//    }
}