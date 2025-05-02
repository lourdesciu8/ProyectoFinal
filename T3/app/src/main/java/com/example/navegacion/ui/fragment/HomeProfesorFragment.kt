
/*package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomeprofesorBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel

class HomeProfesorFragment : Fragment() {

    private var _binding: FragmentHomeprofesorBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

    private lateinit var resumenAdapter: ResumenEventosAdapter

    private val moduloActual = "NombreDelModulo" // ⚡ TODO: pon aquí el módulo correcto del profesor

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        setupRecyclerView()

        // 🛠️ CORREGIDO: Ahora carga eventos de módulo (no de alumno)
        calendarioViewModel.cargarEventosModuloDesdeFirebase(moduloActual)

        observarEventosDesdeViewModel()
    }

    private fun setupRecyclerView() {
        resumenAdapter = ResumenEventosAdapter(mutableListOf()) { evento ->
            // Al pulsar un evento -> navegar al calendario
            findNavController().navigate(R.id.calendarioProfesorFragment)
        }

        binding.recyclerResumenEventosProfesor.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resumenAdapter
        }
    }

    private fun observarEventosDesdeViewModel() {
        calendarioViewModel.obtenerEventosModulo().observe(viewLifecycleOwner) { mapa ->
            val todosEventos = mapa.values.flatten()
                .sortedBy { it.timestamp } // Ordenar por fecha
                .take(10) // Mostrar solo los 10 más próximos
            resumenAdapter.updateEventos(todosEventos)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_profesor, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_calendario_profesor -> {
                    findNavController().navigate(R.id.calendarioProfesorFragment)
                    true
                }
                R.id.action_calificaciones_profesor -> {
                    findNavController().navigate(R.id.calificacionesProfesorFragment)
                    true
                }
                R.id.action_temario_profesor -> {
                    findNavController().navigate(R.id.temarioProfesorFragment)
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomeprofesorBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter
import com.example.navegacion.ui.viewmodel.CalendarioViewModel

class HomeProfesorFragment : Fragment() {

    private var _binding: FragmentHomeprofesorBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()

    private lateinit var resumenAdapter: ResumenEventosAdapter
    private var moduloActual: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeprofesorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        setupRecyclerView()

        // 🛠️ AHORA DETECTAMOS AUTOMÁTICAMENTE el módulo del profesor
        calendarioViewModel.obtenerModuloDelProfesor { modulo ->
            if (modulo != null) {
                moduloActual = modulo
                calendarioViewModel.cargarEventosModuloDesdeFirebase(modulo)
                observarEventosDesdeViewModel()
            } else {
                // Aquí podrías mostrar un error (no se encontró módulo asociado)
            }
        }
    }

    private fun setupRecyclerView() {
        resumenAdapter = ResumenEventosAdapter(mutableListOf()) { evento ->
            // Al pulsar un evento -> navegar al calendario
            findNavController().navigate(R.id.calendarioProfesorFragment)
        }

        binding.recyclerResumenEventosProfesor.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resumenAdapter
        }
    }

    private fun observarEventosDesdeViewModel() {
        calendarioViewModel.obtenerEventosModulo().observe(viewLifecycleOwner) { mapa ->
            val todosEventos = mapa.values.flatten()
                .sortedBy { it.timestamp } // Ordenar por fecha
                .take(10) // Mostrar solo los 10 más próximos
            resumenAdapter.updateEventos(todosEventos)
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_profesor, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_foro->{
                    findNavController().navigate(R.id.action_homeProfesor_to_hilosFragment)
                    true
                }
                R.id.action_calendario_profesor -> {
                    findNavController().navigate(R.id.calendarioProfesorFragment)
                    true
                }
                R.id.action_calificaciones_profesor -> {
                    findNavController().navigate(R.id.calificacionesProfesorFragment)
                    true
                }
                R.id.action_temario_profesor -> {
                    findNavController().navigate(R.id.temarioProfesorFragment)
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

