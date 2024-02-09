package com.example.easystudy.ui.addEvent

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.example.easystudy.databinding.FragmentAddEventBinding
import com.example.easystudy.databinding.FragmentExamsBinding
import com.example.easystudy.ui.schedule.ScheduleFragment


class AddEventFragment : Fragment() {

    private var _binding: FragmentAddEventBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
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

        setSaveButtonClickListener()
        setCancelButtonClickListener()

        return root
    }

    private fun setSaveButtonClickListener() {
        binding.buttonSaveEvent.setOnClickListener {
            val fragment = ScheduleFragment()
            val fragmentManager = requireActivity().supportFragmentManager

            fragmentManager.beginTransaction()
                .replace(com.example.easystudy.R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setCancelButtonClickListener() {
        binding.buttonCancel.setOnClickListener {
            val fragment = ScheduleFragment()
            val fragmentManager = requireActivity().supportFragmentManager

            fragmentManager.beginTransaction()
                .replace(com.example.easystudy.R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

}