
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
import com.google.firebase.auth.FirebaseAuth

class HomeAlumnoFragment : Fragment() {

    private var _binding: FragmentHomealumnoBinding? = null
    private val binding get() = _binding!!
    private val calendarioViewModel: CalendarioViewModel by activityViewModels()
    private lateinit var resumenAdapter: ResumenEventosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomealumnoBinding.inflate(inflater, container, false)
        return binding.root
    }

   /* override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.menuButton.setOnClickListener {
            showPopupMenu(it)
        }

        setupRecyclerView()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        calendarioViewModel.cargarEventosAlumno(uid)

        calendarioViewModel.eventosAlumno.observe(viewLifecycleOwner) { lista ->
            val eventosOrdenados = lista
                .sortedBy { it.fecha }
                .take(5)
            resumenAdapter.updateEventos(eventosOrdenados)
        }
    }*/
   override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
       super.onViewCreated(view, savedInstanceState)

       // Botón para ver notificaciones pendientes
       binding.btnNotificaciones.setOnClickListener {
           findNavController().navigate(R.id.action_homeAlumnoFragment_to_notificacionesFragment)
       }

       // Menú contextual (popup)
       binding.menuButton.setOnClickListener {
           showPopupMenu(it)
       }

       // Configurar RecyclerView
       setupRecyclerView()

       // Cargar eventos del alumno
       val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
       calendarioViewModel.cargarEventosAlumno(uid)

       // Observar eventos y mostrarlos
       calendarioViewModel.eventosAlumno.observe(viewLifecycleOwner) { lista ->
           val eventosOrdenados = lista
               .sortedBy { it.fecha }
               .take(5)
           resumenAdapter.updateEventos(eventosOrdenados)
       }
   }


    private fun setupRecyclerView() {
        resumenAdapter = ResumenEventosAdapter(mutableListOf()) { evento ->
            val bundle = Bundle().apply {
                putLong("fechaSeleccionada", evento.fecha ?: 0L)
            }
            findNavController().navigate(R.id.calendarioAlumnoFragment, bundle)
        }

        binding.recyclerResumenEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resumenAdapter
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu_alumno, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_foro -> {
                    findNavController().navigate(R.id.action_homeAlumno_to_hilosFragment)
                    true
                }
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
