package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.databinding.FragmentCalendarioalumnoBinding
import com.example.navegacion.ui.adapter.EventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import java.text.SimpleDateFormat
import java.util.*

class CalendarioAlumnoFragment : Fragment() {
    private var _binding: FragmentCalendarioalumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private var selectedDate: Long = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalendarioalumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerViewEventos.layoutManager = LinearLayoutManager(requireContext())
        val adapter = EventosAdapter()
        binding.recyclerViewEventos.adapter = adapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            selectedDate = convertToTimestamp(year, month, dayOfMonth)
            actualizarListaEventos(adapter)
        }

        binding.btnAddEvent.setOnClickListener {
            val nuevoEvento = "Evento en ${convertToDateString(selectedDate)}"
            calendarioViewModel.agregarEvento(selectedDate, nuevoEvento)
            actualizarListaEventos(adapter)
            Toast.makeText(requireContext(), "Evento añadido", Toast.LENGTH_SHORT).show()
        }

        // modificado por Aaron boton para volver atras
        binding.btnVolver.setOnClickListener {
            requireActivity().onBackPressed()
        }

        actualizarListaEventos(adapter)
    }

    private fun actualizarListaEventos(adapter: EventosAdapter) {
        val eventos = calendarioViewModel.obtenerEventos().value?.get(selectedDate) ?: emptyList()
        adapter.updateEventos(eventos)
    }

    private fun convertToTimestamp(year: Int, month: Int, day: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, 0, 0, 0)
        return calendar.timeInMillis
    }

    private fun convertToDateString(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
