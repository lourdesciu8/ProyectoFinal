package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomealumnosBinding
import com.example.navegacion.ui.adapter.CalendarAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import java.text.SimpleDateFormat
import java.util.*

class HomeAlumnosFragment : Fragment() {
    private var _binding: FragmentHomealumnosBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomealumnosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        setupCalendar()
    }

    private fun setupCalendar() {
        val calendarRecyclerView = binding.calendarRecyclerView
        val daysList = mutableListOf<Pair<String, Boolean>>() // Lista con los días y si tienen eventos

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("d", Locale.getDefault())

        for (i in 0..6) {
            val day = Calendar.getInstance()
            day.add(Calendar.DAY_OF_MONTH, i)
            val timestamp = day.timeInMillis
            val tieneEventos = calendarioViewModel.obtenerEventos().value?.containsKey(timestamp) ?: false
            daysList.add(Pair(dateFormat.format(day.time), tieneEventos))
        }

        calendarRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        calendarRecyclerView.adapter = CalendarAdapter(daysList) { selectedDay ->
            findNavController().navigate(R.id.calendarioFragment) // Navegar al calendario al hacer clic en un día
        }
    }


    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_calendario -> {
                    findNavController().navigate(R.id)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
