package com.example.easystudy.ui.eventInfo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.easystudy.databinding.FragmentEventInfoBinding

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

//        val event = arguments?.getSerializable(EVENT_KEY) as? Event
//
//        event?.let {
//            binding.textViewName.text = it.title // Припустимо, що title - це назва події
//        }

        return root
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