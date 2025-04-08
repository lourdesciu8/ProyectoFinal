package com.example.navegacion.ui.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.example.navegacion.R

class CalificacionesAlumnoFragmnet : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflamos el layout del fragment
        return inflater.inflate(R.layout.fragment_calificacionesalumno, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Acción de volver al fragment anterior
        view.findViewById<ImageButton>(R.id.btnVolver).setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}
