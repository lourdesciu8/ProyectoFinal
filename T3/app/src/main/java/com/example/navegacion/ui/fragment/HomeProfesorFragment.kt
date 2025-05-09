package com.example.navegacion.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.navegacion.R
import com.example.navegacion.databinding.FragmentHomeprofesorBinding
import com.example.navegacion.ui.adapter.ResumenEventosAdapter

class HomeProfesorFragment : Fragment() {

    private var _binding: FragmentHomeprofesorBinding? = null
    private val binding get() = _binding!!


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

