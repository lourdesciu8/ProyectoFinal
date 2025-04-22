/*
package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomealumnoBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import java.util.*

class HomeAlumnoFragment : Fragment() {

    private var _binding: FragmentHomealumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomealumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        binding.recyclerResumenEventos.layoutManager = LinearLayoutManager(requireContext())

        calendarioViewModel.obtenerEventos().observe(viewLifecycleOwner) { mapa ->
            val todosLosEventos = mapa.flatMap { (fecha, lista) ->
                lista.map { it.copy(timestamp = fecha) }
            }.sortedBy { it.timestamp }

            val eventosLimitados = todosLosEventos.take(5)

            val adapter = ResumenEventosAdapter(eventosLimitados) { evento ->
                val bundle = Bundle().apply {
                    putLong("fechaSeleccionada", evento.timestamp ?: 0L)
                }
                findNavController().navigate(R.id.calendarioAlumnoFragment, bundle)
            }

            binding.recyclerResumenEventos.adapter = adapter
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
*/
package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomealumnoBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel
import java.util.*

class HomeAlumnoFragment : Fragment() {

    private var _binding: FragmentHomealumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomealumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cargar eventos desde Firebase al entrar a Home
        calendarioViewModel.cargarEventosDesdeFirebase()

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        binding.recyclerResumenEventos.layoutManager = LinearLayoutManager(requireContext())

        calendarioViewModel.obtenerEventos().observe(viewLifecycleOwner) { mapa ->
            val todosLosEventos = mapa.flatMap { (fecha, lista) ->
                lista.map { it.copy(timestamp = fecha) }
            }.sortedBy { it.timestamp }

            val eventosLimitados = todosLosEventos.take(5)

            val adapter = ResumenEventosAdapter(eventosLimitados) { evento ->
                val bundle = Bundle().apply {
                    putLong("fechaSeleccionada", evento.timestamp ?: 0L)
                }
                findNavController().navigate(R.id.calendarioAlumnoFragment, bundle)
            }

            binding.recyclerResumenEventos.adapter = adapter
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



