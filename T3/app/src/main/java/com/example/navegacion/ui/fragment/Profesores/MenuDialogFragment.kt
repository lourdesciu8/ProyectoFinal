package com.example.navegacion.ui.fragment.Profesores

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.navegacion.databinding.DialogIconoMenuBinding

class MenuDialogFragment : DialogFragment() {
//usamos dialog para asi no tener que cargar un fragment
    // entero y poder poner este encima de nuestro fragment
    private var binding: DialogIconoMenuBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogIconoMenuBinding.inflate(LayoutInflater.from(context))
        val view = binding?.root

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}