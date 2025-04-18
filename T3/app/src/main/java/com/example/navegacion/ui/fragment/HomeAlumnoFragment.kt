package com.example.navegacion.ui.fragment

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomealumnoBinding
import com.example.navegacion.ui.adapter.CalendarAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import java.text.SimpleDateFormat
import java.util.*
class HomeAlumnoFragment: Fragment() {

        private var _binding: FragmentHomealumnoBinding? = null
        private val binding get() = _binding!!
        private val calendarioViewModel: CalendarioViewModel by activityViewModels()

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            _binding = FragmentHomealumnoBinding.inflate(inflater, container, false)
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
                findNavController().navigate(R.id.calendarioAlumnoFragment) // Navegar al calendario al hacer clic en un día
            }
        }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_alumno, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_calendario -> {
                    findNavController().navigate(R.id.calendarioAlumnoFragment)
                    true
                }
                R.id.action_progreso -> {
                    findNavController().navigate(R.id.action_homeAlumnoFragment_to_progresoFragment)
                    true
                }
                R.id.action_temario -> {
                    findNavController().navigate(R.id.action_homeAlumnoFragment_to_temarioAlumnoFragment)
                    true
                }
                R.id.action_calificaciones -> {
                    findNavController().navigate(R.id.action_homeAlumnoFragment_to_calificacionesAlumnoFragmnet)
                    true
                }
                R.id.action_logout -> {
                    findNavController().navigate(R.id.loginFragment)
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

